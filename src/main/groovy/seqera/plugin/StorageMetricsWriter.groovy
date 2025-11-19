/*
 * Copyright 2021, Seqera Labs
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
import nextflow.processor.TaskHandler
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * Writer for storage metrics output file
 *
 * @author Florian Wuennemann
 */
@Slf4j
@CompileStatic
class StorageMetricsWriter {

    private static final List<String> FIELDS = [
        'task_id',
        'hash',
        'process',
        'tag',
        'status',
        'workdir',
        'size_bytes',
        'size_human'
    ]

    private final Path outputFile
    private final PrintWriter writer
    private boolean headerWritten = false

    StorageMetricsWriter(Path outputFile) {
        this.outputFile = outputFile

        // Create parent directories if needed
        final parent = outputFile.parent
        if (parent && !Files.exists(parent)) {
            Files.createDirectories(parent)
        }

        // Open file for writing
        this.writer = new PrintWriter(
            Files.newBufferedWriter(
                outputFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        )

        log.debug "Storage metrics will be written to: $outputFile"
    }

    synchronized void writeHeader() {
        if (!headerWritten) {
            writer.println(FIELDS.join('\t'))
            headerWritten = true
        }
    }

    synchronized void writeRecord(TaskHandler handler, long sizeBytes, String sizeHuman) {
        if (!headerWritten) {
            writeHeader()
        }

        final task = handler.task
        final record = [
            task.id?.toString() ?: 'N/A',
            task.hashLog ?: 'N/A',
            task.processor?.name ?: 'N/A',
            task.config?.tag ?: '-',
            handler.status?.toString() ?: 'N/A',
            task.workDir?.toString() ?: 'N/A',
            sizeBytes.toString(),
            sizeHuman
        ]

        writer.println(record.join('\t'))
        writer.flush()
    }

    void close() {
        writer?.close()
        log.info "Storage metrics written to: $outputFile"
    }
}
