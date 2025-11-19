# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

You are a world class Nextflow plugin developer. You understand the Nextflow codebase and ecosystem, know which plugins exist and where Nextflow can be augmented with additional functionality with plugins. You prioritize safety and good coding practices and will suggest best implementation to the user.

This is **nf-storagemetrics**, a Nextflow plugin that provides storage metrics tracking capabilities through the Nextflow plugin system. The plugin is built using the Nextflow Plugin SDK and follows Nextflow's extension point architecture.

## Build Commands

```bash
# Build the plugin
make assemble
# or: ./gradlew assemble

# Run tests
make test
# or: ./gradlew test

# Install plugin to local Nextflow installation
make install
# or: ./gradlew install

# Clean build artifacts
make clean
# or: ./gradlew clean

# Publish plugin to registry
make release
# or: ./gradlew releasePlugin
```

## Testing the Plugin

After building and installing locally, test with:
```bash
nextflow run hello -plugins nf-storagemetrics@0.1.0
```

## Plugin Architecture

This plugin implements the **Trace Observer** extension point pattern, allowing it to hook into Nextflow's execution lifecycle.

### Core Components

1. **StoragemetricsPlugin** (`src/main/groovy/seqera/plugin/StoragemetricsPlugin.groovy:27`)
   - Entry point extending `BasePlugin`
   - Registered via `build.gradle:11` with `className = 'seqera.plugin.StoragemetricsPlugin'`

2. **StoragemetricsFactory** (`src/main/groovy/seqera/plugin/StoragemetricsFactory.groovy:29`)
   - Implements `TraceObserverFactory`
   - Creates observer instances for pipeline execution tracking
   - Declared as extension point in `build.gradle:14`

3. **StoragemetricsObserver** (`src/main/groovy/seqera/plugin/StoragemetricsObserver.groovy:30`)
   - Implements `TraceObserver` interface
   - Receives lifecycle events: `onFlowCreate()`, `onFlowComplete()`, etc.
   - Currently implements basic hello/goodbye messages

4. **StoragemetricsExtension** (`src/main/groovy/seqera/plugin/StoragemetricsExtension.groovy:29`)
   - Extends `PluginExtensionPoint`
   - Provides custom functions callable from Nextflow scripts
   - Example: `@Function void sayHello(String target)` can be imported in pipelines
   - Declared as extension point in `build.gradle:13`

### Plugin Configuration

The `build.gradle` file defines:
- Plugin version: `0.1.0`
- Target Nextflow version: `24.10.0`
- Provider: `Seqera`
- Two extension points: `StoragemetricsExtension` and `StoragemetricsFactory`

### Extension Point Pattern

Nextflow plugins use a dual extension point system:
1. **TraceObserverFactory**: Creates observers that react to pipeline execution events
2. **PluginExtensionPoint**: Provides functions/operators that can be imported in Nextflow DSL scripts

Both must be declared in `build.gradle` under `nextflowPlugin.extensionPoints`.

## Testing Framework

Uses **Spock** framework with Groovy for testing:
- Test location: `src/test/groovy/seqera/plugin/`
- Example: `StoragemetricsObserverTest.groovy` tests factory instantiation
- Run individual test class: `./gradlew test --tests StoragemetricsObserverTest`