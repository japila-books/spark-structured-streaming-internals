# CheckpointFileManager

`CheckpointFileManager` is the <<contract, abstraction>> of <<implementations, checkpoint managers>> that manage checkpoint files (metadata of streaming batches) on Hadoop DFS-compatible file systems.

`CheckpointFileManager` is <<create, created>> per <<SQLConf.md#STREAMING_CHECKPOINT_FILE_MANAGER_CLASS, spark.sql.streaming.checkpointFileManagerClass>> configuration property if defined before reverting to the available <<implementations, checkpoint managers>>.

`CheckpointFileManager` is used exclusively by [HDFSMetadataLog](HDFSMetadataLog.md), [StreamMetadata](StreamMetadata.md) and [HDFSBackedStateStoreProvider](HDFSBackedStateStoreProvider.md).

[[contract]]
.CheckpointFileManager Contract
[cols="30m,70",options="header",width="100%"]
|===
| Method
| Description

| createAtomic
a| [[createAtomic]]

[source, scala]
----
createAtomic(
  path: Path,
  overwriteIfPossible: Boolean): CancellableFSDataOutputStream
----

Used when:

* `HDFSMetadataLog` is requested to [store metadata for a batch](HDFSMetadataLog.md#add) (that [writeBatchToFile](HDFSMetadataLog.md#writeBatchToFile))

* `StreamMetadata` helper object is requested to [persist metadata](StreamMetadata.md#write)

* `HDFSBackedStateStore` is requested for the [deltaFileStream](HDFSBackedStateStore.md#deltaFileStream)

* `HDFSBackedStateStoreProvider` is requested to [writeSnapshotFile](HDFSBackedStateStoreProvider.md#writeSnapshotFile)

| delete
a| [[delete]]

[source, scala]
----
delete(path: Path): Unit
----

Deletes the given path recursively (if exists)

Used when:

* `RenameBasedFSDataOutputStream` is requested to `cancel`

* `CompactibleFileStreamLog` is requested to [store metadata for a batch](datasources/file/CompactibleFileStreamLog.md#add) (that [deleteExpiredLog](datasources/file/CompactibleFileStreamLog.md#deleteExpiredLog))

* `HDFSMetadataLog` is requested to [remove expired metadata](HDFSMetadataLog.md#purge) and [purgeAfter](HDFSMetadataLog.md#purgeAfter)

* `HDFSBackedStateStoreProvider` is requested to [do maintenance](HDFSBackedStateStoreProvider.md#doMaintenance) (that [cleans up](HDFSBackedStateStoreProvider.md#cleanup))

| exists
a| [[exists]]

[source, scala]
----
exists(path: Path): Boolean
----

Used when `HDFSMetadataLog` is [created](HDFSMetadataLog.md) (to create the [metadata directory](HDFSMetadataLog.md#metadataPath)) and requested for [metadata of a batch](HDFSMetadataLog.md#get)

| isLocal
a| [[isLocal]]

[source, scala]
----
isLocal: Boolean
----

Does not seem to be used.

| list
a| [[list]]

[source, scala]
----
list(
  path: Path): Array[FileStatus] // <1>
list(
  path: Path,
  filter: PathFilter): Array[FileStatus]
----
<1> Uses `PathFilter` that accepts all files in the path

Lists all files in the given path

Used when:

* `HDFSBackedStateStoreProvider` is requested for [all delta and snapshot files](HDFSBackedStateStoreProvider.md#fetchFiles)

* `CompactibleFileStreamLog` is requested for the [compact interval](datasources/file/CompactibleFileStreamLog.md#compactInterval) and to [deleteExpiredLog](datasources/file/CompactibleFileStreamLog.md#deleteExpiredLog)

* `HDFSMetadataLog` is requested for [metadata of one or more batches](HDFSMetadataLog.md#get-range), the [latest committed batch](HDFSMetadataLog.md#getLatest), [ordered batch metadata files](HDFSMetadataLog.md#getOrderedBatchFiles), to [remove expired metadata](HDFSMetadataLog.md#purge) and [purgeAfter](HDFSMetadataLog.md#purgeAfter)

| mkdirs
a| [[mkdirs]]

[source, scala]
----
mkdirs(path: Path): Unit
----

Used when:

* `HDFSMetadataLog` is [created](HDFSMetadataLog.md)

* `HDFSBackedStateStoreProvider` is requested to [initialize](HDFSBackedStateStoreProvider.md#init)

| open
a| [[open]]

[source, scala]
----
open(path: Path): FSDataInputStream
----

Opens a file (by the given path) for reading

Used when:

* `HDFSMetadataLog` is requested for [metadata of a batch](HDFSMetadataLog.md#get)

* `HDFSBackedStateStoreProvider` is requested to [retrieve the state store for a specified version](HDFSBackedStateStoreProvider.md#getStore) (that [updateFromDeltaFile](HDFSBackedStateStoreProvider.md#updateFromDeltaFile)), and [readSnapshotFile](HDFSBackedStateStoreProvider.md#readSnapshotFile)

|===

[[implementations]]
.CheckpointFileManagers
[cols="30,70",options="header",width="100%"]
|===
| CheckpointFileManager
| Description

| [FileContextBasedCheckpointFileManager](FileContextBasedCheckpointFileManager.md)
| [[FileContextBasedCheckpointFileManager]] Default `CheckpointFileManager` that uses Hadoop's https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileContext.html[FileContext] API for managing checkpoint files (unless <<create, spark.sql.streaming.checkpointFileManagerClass configuration property is used>>)

| [FileSystemBasedCheckpointFileManager](FileSystemBasedCheckpointFileManager.md)
| [[FileSystemBasedCheckpointFileManager]] Basic `CheckpointFileManager` that uses Hadoop's https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html[FileSystem] API for managing checkpoint files (that <<create, assumes>> that the implementation of `FileSystem.rename()` is atomic or the correctness and fault-tolerance of Structured Streaming is not guaranteed)

|===

=== [[create]] Creating CheckpointFileManager Instance -- `create` Object Method

[source, scala]
----
create(
  path: Path,
  hadoopConf: Configuration): CheckpointFileManager
----

`create` finds [spark.sql.streaming.checkpointFileManagerClass](SQLConf.md#STREAMING_CHECKPOINT_FILE_MANAGER_CLASS) configuration property in the `hadoopConf` configuration.

If found, `create` simply instantiates whatever `CheckpointFileManager` implementation is defined.

If not found, `create` creates a [FileContextBasedCheckpointFileManager](FileContextBasedCheckpointFileManager.md).

In case of `UnsupportedFileSystemException`, `create` prints out the following WARN message to the logs and creates (_falls back on_) a [FileSystemBasedCheckpointFileManager](FileSystemBasedCheckpointFileManager.md).

```text
Could not use FileContext API for managing Structured Streaming checkpoint files at [path]. Using FileSystem API instead for managing log files. If the implementation of FileSystem.rename() is not atomic, then the correctness and fault-tolerance of your Structured Streaming is not guaranteed.
```

`create` is used when:

* [HDFSMetadataLog](HDFSMetadataLog.md) is created

* `StreamMetadata` utility is used to [write metadata to a file](StreamMetadata.md#write) (when [StreamExecution](StreamExecution.md) is created)

* `HDFSBackedStateStoreProvider` is requested for a [CheckpointFileManager](HDFSBackedStateStoreProvider.md#fm)
