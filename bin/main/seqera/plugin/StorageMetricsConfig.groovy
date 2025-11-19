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
import nextflow.Session

/**
 * Configuration for storage metrics plugin
 *
 * @author Florian Wuennemann
 */
@CompileStatic
class StorageMetricsConfig {

    /**
     * Path to the output file for storage metrics
     */
    final String fileName

    /**
     * Whether to overwrite existing file
     */
    final boolean overwrite

    StorageMetricsConfig(Map config, Session session) {
        // Use custom filename if provided, otherwise generate unique default
        this.fileName = config.file ?: generateDefaultFileName(session)
        this.overwrite = config.overwrite != false
    }

    static StorageMetricsConfig fromSession(Session session) {
        final config = session.config.navigate('storagemetrics') as Map ?: [:]
        return new StorageMetricsConfig(config, session)
    }

    /**
     * Generate a default filename that includes both run name and unique ID
     * This ensures each execution (including resumes) gets its own metrics file
     */
    private static String generateDefaultFileName(Session session) {
        final runName = session.runName ?: 'unknown'
        final uniqueId = session.uniqueId?.toString()?.take(8) ?: 'unknown'
        return "storage-metrics.${runName}.${uniqueId}.txt"
    }
}
