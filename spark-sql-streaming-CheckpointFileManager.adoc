== [[CheckpointFileManager]] CheckpointFileManager Contract

`CheckpointFileManager` is the <<contract, abstraction>> of <<implementations, checkpoint managers>> that manage checkpoint files (metadata of streaming batches) on Hadoop DFS-compatible file systems.

`CheckpointFileManager` is <<create, created>> per <<spark-sql-streaming-SQLConf.adoc#STREAMING_CHECKPOINT_FILE_MANAGER_CLASS, spark.sql.streaming.checkpointFileManagerClass>> configuration property if defined before reverting to the available <<implementations, checkpoint managers>>.

`CheckpointFileManager` is used exclusively by <<spark-sql-streaming-HDFSMetadataLog.adoc#, HDFSMetadataLog>>, <<spark-sql-streaming-StreamMetadata.adoc#, StreamMetadata>> and <<spark-sql-streaming-HDFSBackedStateStoreProvider.adoc#, HDFSBackedStateStoreProvider>>.

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

* `HDFSMetadataLog` is requested to <<spark-sql-streaming-HDFSMetadataLog.adoc#add, store metadata for a batch>> (that <<spark-sql-streaming-HDFSMetadataLog.adoc#writeBatchToFile, writeBatchToFile>>)

* `StreamMetadata` helper object is requested to <<spark-sql-streaming-StreamMetadata.adoc#write, persist metadata>>

* `HDFSBackedStateStore` is requested for the <<spark-sql-streaming-HDFSBackedStateStore.adoc#deltaFileStream, deltaFileStream>>

* `HDFSBackedStateStoreProvider` is requested to <<spark-sql-streaming-HDFSBackedStateStoreProvider.adoc#writeSnapshotFile, writeSnapshotFile>>

| delete
a| [[delete]]

[source, scala]
----
delete(path: Path): Unit
----

Deletes the given path recursively (if exists)

Used when:

* `RenameBasedFSDataOutputStream` is requested to `cancel`

* `CompactibleFileStreamLog` is requested to <<spark-sql-streaming-CompactibleFileStreamLog.adoc#add, store metadata for a batch>> (that <<spark-sql-streaming-CompactibleFileStreamLog.adoc#deleteExpiredLog, deleteExpiredLog>>)

* `HDFSMetadataLog` is requested to <<spark-sql-streaming-HDFSMetadataLog.adoc#purge, remove expired metadata>> and <<spark-sql-streaming-HDFSMetadataLog.adoc#purgeAfter, purgeAfter>>

* `HDFSBackedStateStoreProvider` is requested to <<spark-sql-streaming-HDFSBackedStateStoreProvider.adoc#doMaintenance, do maintenance>> (that <<spark-sql-streaming-HDFSBackedStateStoreProvider.adoc#cleanup, cleans up>>)

| exists
a| [[exists]]

[source, scala]
----
exists(path: Path): Boolean
----

Used when `HDFSMetadataLog` is <<spark-sql-streaming-HDFSMetadataLog.adoc#, created>> (to create the <<spark-sql-streaming-HDFSMetadataLog.adoc#metadataPath, metadata directory>>) and requested for <<spark-sql-streaming-HDFSMetadataLog.adoc#get, metadata of a batch>>

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

* `HDFSBackedStateStoreProvider` is requested for <<spark-sql-streaming-HDFSBackedStateStoreProvider.adoc#fetchFiles, all delta and snapshot files>>

* `CompactibleFileStreamLog` is requested for the <<spark-sql-streaming-CompactibleFileStreamLog.adoc#compactInterval, compact interval>> and to <<spark-sql-streaming-CompactibleFileStreamLog.adoc#deleteExpiredLog, deleteExpiredLog>>

* `HDFSMetadataLog` is requested for <<spark-sql-streaming-HDFSMetadataLog.adoc#get-range, metadata of one or more batches>>, the <<getLatest, latest committed batch>>, <<spark-sql-streaming-HDFSMetadataLog.adoc#getOrderedBatchFiles, ordered batch metadata files>>, to <<spark-sql-streaming-HDFSMetadataLog.adoc#purge, remove expired metadata>> and <<spark-sql-streaming-HDFSMetadataLog.adoc#purgeAfter, purgeAfter>>

| mkdirs
a| [[mkdirs]]

[source, scala]
----
mkdirs(path: Path): Unit
----

Used when:

* `HDFSMetadataLog` is <<spark-sql-streaming-HDFSMetadataLog.adoc#, created>>

* `HDFSBackedStateStoreProvider` is requested to <<spark-sql-streaming-HDFSBackedStateStoreProvider.adoc#init, initialize>>

| open
a| [[open]]

[source, scala]
----
open(path: Path): FSDataInputStream
----

Opens a file (by the given path) for reading

Used when:

* `HDFSMetadataLog` is requested for <<spark-sql-streaming-HDFSMetadataLog.adoc#get, metadata of a batch>>

* `HDFSBackedStateStoreProvider` is requested to <<spark-sql-streaming-HDFSBackedStateStoreProvider.adoc#getStore, retrieve the state store for a specified version>> (that <<spark-sql-streaming-HDFSBackedStateStoreProvider.adoc#updateFromDeltaFile, updateFromDeltaFile>>), and <<spark-sql-streaming-HDFSBackedStateStoreProvider.adoc#readSnapshotFile, readSnapshotFile>>

|===

[[implementations]]
.CheckpointFileManagers
[cols="30,70",options="header",width="100%"]
|===
| CheckpointFileManager
| Description

| <<spark-sql-streaming-FileContextBasedCheckpointFileManager.adoc#, FileContextBasedCheckpointFileManager>>
| [[FileContextBasedCheckpointFileManager]] Default `CheckpointFileManager` that uses Hadoop's https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileContext.html[FileContext] API for managing checkpoint files (unless <<create, spark.sql.streaming.checkpointFileManagerClass configuration property is used>>)

| <<spark-sql-streaming-FileSystemBasedCheckpointFileManager.adoc#, FileSystemBasedCheckpointFileManager>>
| [[FileSystemBasedCheckpointFileManager]] Basic `CheckpointFileManager` that uses Hadoop's https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html[FileSystem] API for managing checkpoint files (that <<create, assumes>> that the implementation of `FileSystem.rename()` is atomic or the correctness and fault-tolerance of Structured Streaming is not guaranteed)

|===

=== [[create]] Creating CheckpointFileManager Instance -- `create` Object Method

[source, scala]
----
create(
  path: Path,
  hadoopConf: Configuration): CheckpointFileManager
----

`create` finds <<spark-sql-streaming-SQLConf.adoc#STREAMING_CHECKPOINT_FILE_MANAGER_CLASS, spark.sql.streaming.checkpointFileManagerClass>> configuration property in the `hadoopConf` configuration.

If found, `create` simply instantiates whatever `CheckpointFileManager` implementation is defined.

If not found, `create` creates a <<spark-sql-streaming-FileContextBasedCheckpointFileManager.adoc#, FileContextBasedCheckpointFileManager>>.

In case of `UnsupportedFileSystemException`, `create` prints out the following WARN message to the logs and creates (_falls back on_) a <<spark-sql-streaming-FileSystemBasedCheckpointFileManager.adoc#, FileSystemBasedCheckpointFileManager>>.

```
Could not use FileContext API for managing Structured Streaming checkpoint files at [path]. Using FileSystem API instead for managing log files. If the implementation of FileSystem.rename() is not atomic, then the correctness and fault-tolerance of your Structured Streaming is not guaranteed.
```

[NOTE]
====
`create` is used when:

* `HDFSMetadataLog` is <<spark-sql-streaming-HDFSMetadataLog.adoc#, created>>

* `StreamMetadata` helper object is requested to <<spark-sql-streaming-StreamMetadata.adoc#write, write metadata to a file>> (when `StreamExecution` is <<spark-sql-streaming-StreamExecution.adoc#, created>>)

* `HDFSBackedStateStoreProvider` is requested for the <<spark-sql-streaming-HDFSBackedStateStoreProvider.adoc#fm, CheckpointFileManager>>
====
