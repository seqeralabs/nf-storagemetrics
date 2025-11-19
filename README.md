# nf-storagemetrics

A Nextflow plugin that tracks storage metrics for pipeline executions, providing detailed per-task work directory sizes and total storage consumption.

## Features

- **Per-task storage tracking**: Captures work directory size for each task
- **Total storage summary**: Aggregates total work directory usage across the entire pipeline
- **Human-readable output**: Displays sizes in appropriate units (KB, MB, GB, etc.)
- **TSV metrics file**: Generates a tab-separated file similar to trace files
- **Configurable**: Customize output file name and plugin behavior

## Quick Start

### Installation

Add the plugin to your `nextflow.config`:

```groovy
plugins {
    id 'nf-storagemetrics@0.1.0'
}
```

Or specify it on the command line:

```bash
nextflow run <pipeline> -plugins nf-storagemetrics@0.1.0
```

### Basic Usage

Once enabled, the plugin automatically tracks storage metrics during pipeline execution:

```bash
nextflow run hello -plugins nf-storagemetrics@0.1.0
```

The plugin will:
1. Track work directory size for each completed task
2. Write metrics to `storage-metrics.txt` in the launch directory
3. Display a summary in the log at pipeline completion:

```
Storage Metrics Summary:
  Total tasks:     42
  Total size:      1.5 GB
  Average/task:    35.7 MB
  Metrics file:    storage-metrics.txt
```

## Configuration

Configure the plugin behavior in your `nextflow.config`:

```groovy
storagemetrics {
    file = 'custom-metrics.tsv'         // Custom output file name
    overwrite = true                    // Overwrite existing file (default: true)
}
```

### Configuration Options

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `file` | string | `storage-metrics.<runName>.<uniqueId>.txt` | Name of the output metrics file. If not specified, includes run name and unique ID to prevent overwrites |
| `overwrite` | boolean | `true` | Whether to overwrite existing metrics file |

**Note**: The plugin is automatically enabled when loaded. To disable, simply don't include it in your plugins list.

### Default Filename Behavior

By default, the plugin generates unique filenames using the pattern:
```
storage-metrics.<runName>.<uniqueId>.txt
```

For example: `storage-metrics.amazing_tesla.abcd1234.txt`

This ensures:
- Multiple pipeline runs don't overwrite each other
- Resumed runs (`-resume`) get separate metrics files
- Each execution is tracked independently

### Example Configuration

```groovy
// Custom metrics file location
storagemetrics {
    file = 'results/storage-report.tsv'
}
```

```groovy
// Prevent overwriting (useful for debugging)
storagemetrics {
    overwrite = false
}
```

## Output Format

The plugin generates a tab-separated file with the following columns:

| Column | Description |
|--------|-------------|
| `task_id` | Unique task identifier |
| `hash` | Task hash (same as in .command.log) |
| `process` | Process name |
| `tag` | Task tag (if specified) |
| `status` | Task completion status |
| `workdir` | Path to task work directory |
| `size_bytes` | Directory size in bytes |
| `size_human` | Human-readable size (e.g., "1.5 GB") |

### Example Output

```tsv
task_id	hash	process	tag	status	workdir	size_bytes	size_human
1	7f/a3b2c1	FASTQC	sample1	COMPLETED	/work/7f/a3b2c1...	52428800	50 MB
2	8a/d4e5f6	ALIGN	sample1	COMPLETED	/work/8a/d4e5f6...	209715200	200 MB
3	9b/c7d8e9	CALL_VARIANTS	sample1	COMPLETED	/work/9b/c7d8e9...	104857600	100 MB
```

## Architecture

### Components

- **StorageMetricsConfig** (`StorageMetricsConfig.groovy:27`): Configuration management
- **StorageMetricsWriter** (`StorageMetricsWriter.groovy:30`): Handles metrics file I/O
- **StoragemetricsObserver** (`StoragemetricsObserver.groovy:37`): Implements TraceObserver for lifecycle hooks
- **StoragemetricsFactory** (`StoragemetricsFactory.groovy:29`): Factory for observer instantiation

### How It Works

1. **Initialization**: When a workflow starts, the plugin reads configuration and initializes the metrics writer
2. **Task Monitoring**: For each completed task, the plugin:
   - Walks the task's work directory file tree
   - Calculates total size by summing all regular files
   - Writes a record to the metrics file
   - Updates running totals
3. **Completion**: When the workflow completes, the plugin:
   - Closes the metrics file
   - Logs a summary with total and average sizes

### Error Handling

- Missing work directories are logged at debug level and skipped
- File access errors during size calculation are handled gracefully
- Individual task failures don't break the entire metrics collection

## Development

### Building

Build the plugin:
```bash
make assemble
# or: ./gradlew assemble
```

### Testing

Run tests:
```bash
make test
# or: ./gradlew test
```

### Local Installation

Install to your local Nextflow installation:
```bash
make install
# or: ./gradlew install
```

Test with a pipeline:
```bash
nextflow run hello -plugins nf-storagemetrics@0.1.0
```

### Project Structure

```
nf-storagemetrics/
├── src/main/groovy/seqera/plugin/
│   ├── StorageMetricsConfig.groovy    # Configuration
│   ├── StorageMetricsWriter.groovy    # File I/O
│   ├── StoragemetricsObserver.groovy  # Main observer
│   ├── StoragemetricsFactory.groovy   # Factory
│   ├── StoragemetricsPlugin.groovy    # Plugin entry point
│   └── StoragemetricsExtension.groovy # Extension functions
├── src/test/groovy/seqera/plugin/
│   └── StoragemetricsObserverTest.groovy
├── build.gradle                        # Build configuration
└── README.md
```

## Requirements

- Nextflow `>=24.10.0`
- Java 11 or later

## Use Cases

- **Resource optimization**: Identify tasks with unexpectedly large work directories
- **Cost management**: Track storage consumption for cloud pipelines
- **Debugging**: Understand where disk space is being used
- **Capacity planning**: Historical data for infrastructure sizing
- **Cleanup prioritization**: Target large work directories for cleanup

## Publishing

To publish the plugin to the Nextflow Plugin Registry:

1. Create `$HOME/.gradle/gradle.properties` with your API key:
   ```
   npr.apiKey=<your-token>
   ```

2. Release the plugin:
   ```bash
   make release
   # or: ./gradlew releasePlugin
   ```

> [!NOTE]
> The Nextflow Plugin Registry is currently preview technology. Contact info@nextflow.io for access.

## License

Apache License 2.0

## Contributing

Contributions welcome! Please open an issue or pull request on GitHub.

## Support

For issues or questions:
- GitHub Issues: [github.com/FloWuenne/nf-storagemetrics](https://github.com/FloWuenne/nf-storagemetrics)
- Nextflow Community: [community.nextflow.io](https://community.nextflow.io)
