/*
 * Copyright 2025, Seqera Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package seqera.plugin

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.Session
import nextflow.processor.TaskHandler
import nextflow.trace.TraceObserver
import nextflow.trace.TraceRecord
import nextflow.util.MemoryUnit
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicLong

/**
 * Observer that tracks storage metrics for Nextflow tasks
 *
 * @author Florian Wuennemann
 */
@Slf4j
@CompileStatic
class StoragemetricsObserver implements TraceObserver {

    private final StorageMetricsConfig config
    private StorageMetricsWriter writer
    private final AtomicLong totalBytes = new AtomicLong(0)
    private final AtomicLong taskCount = new AtomicLong(0)
    private Path workflowWorkDir

    StoragemetricsObserver(StorageMetricsConfig config) {
        this.config = config
    }

    @Override
    void onFlowCreate(Session session) {
        // Resolve output file path relative to launch directory
        final outputPath = session.workDir.parent.resolve(config.fileName)
        this.writer = new StorageMetricsWriter(outputPath)
        this.workflowWorkDir = session.workDir

        log.info "Storage metrics tracking enabled - output: ${config.fileName}"
    }

    @Override
    void onProcessComplete(TaskHandler handler, TraceRecord trace) {
        if (!writer) {
            return
        }

        try {
            final workDir = handler.task?.workDir
            if (!workDir || !Files.exists(workDir)) {
                log.debug "Work directory not found for task ${handler.task?.hashLog}"
                return
            }

            final sizeBytes = calculateDirectorySize(workDir)
            final sizeHuman = new MemoryUnit(sizeBytes).toString()

            // Write to metrics file
            writer.writeRecord(handler, sizeBytes, sizeHuman)

            // Update totals
            totalBytes.addAndGet(sizeBytes)
            taskCount.incrementAndGet()

            log.trace "Task ${handler.task.hashLog}: ${sizeHuman}"

        } catch (Exception e) {
            log.warn "Failed to calculate storage metrics for task ${handler.task?.hashLog}: ${e.message}"
        }
    }

    @Override
    void onFlowComplete() {
        if (!writer) {
            return
        }

        try {
            writer.close()

            // Log summary
            final totalSize = new MemoryUnit(totalBytes.get())
            final tasks = taskCount.get()
            final avgSize = tasks > 0 ? new MemoryUnit((long)(totalBytes.get() / tasks)) : new MemoryUnit(0L)

            log.info """
            |
            |Storage Metrics Summary:
            |  Total tasks:     ${tasks}
            |  Total size:      ${totalSize}
            |  Average/task:    ${avgSize}
            |  Metrics file:    ${config.fileName}
            |""".stripMargin()

        } catch (Exception e) {
            log.error "Failed to finalize storage metrics: ${e.message}", e
        }
    }

    /**
     * Calculate the total size of a directory by summing all regular files
     */
    private static long calculateDirectorySize(Path dir) {
        try {
            return Files.walk(dir)
                .filter { Files.isRegularFile(it) }
                .mapToLong { path ->
                    try {
                        return Files.size(path)
                    } catch (IOException e) {
                        log.trace "Unable to get size for file: ${path}"
                        return 0L
                    }
                }
                .sum()
        } catch (IOException e) {
            log.warn "Error walking directory ${dir}: ${e.message}"
            return 0L
        }
    }
}
