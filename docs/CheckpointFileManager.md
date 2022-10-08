# CheckpointFileManager

`CheckpointFileManager` is an [abstraction](#contract) of [checkpoint managers](#implementations) that manage checkpoint files (metadata of streaming batches) on Hadoop DFS-compatible file systems.

`CheckpointFileManager` is [created](#create) based on [spark.sql.streaming.checkpointFileManagerClass](configuration-properties.md#spark.sql.streaming.checkpointFileManagerClass) configuration property if defined before reverting to the available [checkpoint managers](#implementations).

## Contract (Subset)

### <span id="createAtomic"> createAtomic

```scala
createAtomic(
  path: Path,
  overwriteIfPossible: Boolean): CancellableFSDataOutputStream
```

Used when:

* `HDFSMetadataLog` is requested to [addNewBatchByStream](HDFSMetadataLog.md#addNewBatchByStream)
* `StreamMetadata` is requested to [write a metadata to a file](StreamMetadata.md#write)
* `HDFSBackedStateStoreProvider` is requested for the [deltaFileStream](stateful-stream-processing/HDFSBackedStateStoreProvider.md#deltaFileStream) and [writeSnapshotFile](stateful-stream-processing/HDFSBackedStateStoreProvider.md#writeSnapshotFile)
* `RocksDBFileManager` is requested for the [zipToDfsFile](stateful-stream-processing/RocksDBFileManager.md#zipToDfsFile)
* `StateSchemaCompatibilityChecker` is requested for the [createSchemaFile](stateful-stream-processing/StateSchemaCompatibilityChecker.md#createSchemaFile)

### <span id="createCheckpointDirectory"> createCheckpointDirectory

```scala
createCheckpointDirectory(): Path
```

Creates the checkpoint path

Used when:

* `ResolveWriteToStream` is requested to `resolveCheckpointLocation`

## Implementations

* [FileContextBasedCheckpointFileManager](FileContextBasedCheckpointFileManager.md)
* [FileSystemBasedCheckpointFileManager](FileSystemBasedCheckpointFileManager.md)

## <span id="create"> Creating CheckpointFileManager

```scala
create(
  path: Path,
  hadoopConf: Configuration): CheckpointFileManager
```

`create` uses [spark.sql.streaming.checkpointFileManagerClass](configuration-properties.md#spark.sql.streaming.checkpointFileManagerClass) as the name of the [implementation](#implementations) to instantiate.

If undefined, `create` creates a [FileContextBasedCheckpointFileManager](FileContextBasedCheckpointFileManager.md) first, and, in case of an `UnsupportedFileSystemException`, falls back to a [FileSystemBasedCheckpointFileManager](FileSystemBasedCheckpointFileManager.md).

`create` prints out the following WARN message to the logs if `UnsupportedFileSystemException` happens:

```text
Could not use FileContext API for managing Structured Streaming checkpoint files at [path].
Using FileSystem API instead for managing log files.
If the implementation of FileSystem.rename() is not atomic, then the correctness and fault-tolerance of your Structured Streaming is not guaranteed.
```

---

`create` is used when:

* `HDFSMetadataLog` is requested for the [fileManager](HDFSMetadataLog.md#fileManager)
* `StreamExecution` is requested for the [fileManager](StreamExecution.md#fileManager)
* `ResolveWriteToStream` is requested to `resolveCheckpointLocation`
* `StreamMetadata` is requested to [read the metadata from a file](StreamMetadata.md#read) and [write a metadata to a file](StreamMetadata.md#write)
* `HDFSBackedStateStoreProvider` is requested for the [fm](stateful-stream-processing/HDFSBackedStateStoreProvider.md#fm)
* `RocksDBFileManager` is requested for the [fm](stateful-stream-processing/RocksDBFileManager.md#fm)
* `StateSchemaCompatibilityChecker` is requested for the [fm](stateful-stream-processing/StateSchemaCompatibilityChecker.md#fm)
