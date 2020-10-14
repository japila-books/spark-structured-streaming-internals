# CompactibleFileStreamLog

`CompactibleFileStreamLog` is the <<contract, extension>> of the <<spark-sql-streaming-HDFSMetadataLog.md#, HDFSMetadataLog contract>> for <<implementations, compactible metadata logs>> that <<compactLogs, compactLogs>> every <<compactInterval, compact interval>>.

[[minBatchesToRetain]][[spark.sql.streaming.minBatchesToRetain]]
`CompactibleFileStreamLog` uses <<spark-sql-streaming-properties.md#spark.sql.streaming.minBatchesToRetain, spark.sql.streaming.minBatchesToRetain>> configuration property (default: `100`) for <<deleteExpiredLog, deleteExpiredLog>>.

[[COMPACT_FILE_SUFFIX]]
`CompactibleFileStreamLog` uses *.compact* suffix for <<batchIdToPath, batchIdToPath>>, <<getBatchIdFromFileName, getBatchIdFromFileName>>, and the <<compactInterval, compactInterval>>.

[[contract]]
.CompactibleFileStreamLog Contract (Abstract Methods Only)
[cols="30m,70",options="header",width="100%"]
|===
| Method
| Description

| compactLogs
a| [[compactLogs]]

[source, scala]
----
compactLogs(logs: Seq[T]): Seq[T]
----

Used when `CompactibleFileStreamLog` is requested to <<compact, compact>> and <<allFiles, allFiles>>

| defaultCompactInterval
a| [[defaultCompactInterval]]

[source, scala]
----
defaultCompactInterval: Int
----

Default <<compactInterval, compaction interval>>

Used exclusively when `CompactibleFileStreamLog` is requested for the <<compactInterval, compactInterval>>

| fileCleanupDelayMs
a| [[fileCleanupDelayMs]]

[source, scala]
----
fileCleanupDelayMs: Long
----

Used exclusively when `CompactibleFileStreamLog` is requested to <<deleteExpiredLog, deleteExpiredLog>>

| isDeletingExpiredLog
a| [[isDeletingExpiredLog]]

[source, scala]
----
isDeletingExpiredLog: Boolean
----

Used exclusively when `CompactibleFileStreamLog` is requested to <<add, store (add) metadata of a streaming batch>>

|===

[[implementations]]
.CompactibleFileStreamLogs
[cols="30,70",options="header",width="100%"]
|===
| CompactibleFileStreamLog
| Description

| [FileStreamSinkLog](datasources/file/FileStreamSinkLog.md)
| [[FileStreamSinkLog]]

| [FileStreamSourceLog](datasources/file/FileStreamSourceLog.md)
| [[FileStreamSourceLog]] `CompactibleFileStreamLog` (of `FileEntry` metadata) of [FileStreamSource](datasources/file/FileStreamSource.md)

|===

=== [[creating-instance]] Creating CompactibleFileStreamLog Instance

`CompactibleFileStreamLog` takes the following to be created:

* [[metadataLogVersion]] Metadata version
* [[sparkSession]] `SparkSession`
* [[path]] Path of the metadata log directory

NOTE: `CompactibleFileStreamLog` is a Scala abstract class and cannot be <<creating-instance, created>> directly. It is created indirectly for the <<implementations, concrete CompactibleFileStreamLogs>>.

=== [[batchIdToPath]] `batchIdToPath` Method

[source, scala]
----
batchIdToPath(batchId: Long): Path
----

NOTE: `batchIdToPath` is part of the <<spark-sql-streaming-HDFSMetadataLog.md#batchIdToPath, HDFSMetadataLog Contract>> to...FIXME.

`batchIdToPath`...FIXME

=== [[pathToBatchId]] `pathToBatchId` Method

[source, scala]
----
pathToBatchId(path: Path): Long
----

NOTE: `pathToBatchId` is part of the <<spark-sql-streaming-HDFSMetadataLog.md#pathToBatchId, HDFSMetadataLog Contract>> to...FIXME.

`pathToBatchId`...FIXME

=== [[isBatchFile]] `isBatchFile` Method

[source, scala]
----
isBatchFile(path: Path): Boolean
----

NOTE: `isBatchFile` is part of the <<spark-sql-streaming-HDFSMetadataLog.md#isBatchFile, HDFSMetadataLog Contract>> to...FIXME.

`isBatchFile`...FIXME

=== [[serialize]] Serializing Metadata (Writing Metadata in Serialized Format) -- `serialize` Method

[source, scala]
----
serialize(
  logData: Array[T],
  out: OutputStream): Unit
----

NOTE: `serialize` is part of the <<spark-sql-streaming-HDFSMetadataLog.md#serialize, HDFSMetadataLog Contract>> to serialize metadata (write metadata in serialized format).

`serialize` firstly writes the version header (`v` and the <<metadataLogVersion, metadataLogVersion>>) out to the given output stream (in `UTF_8`).

`serialize` then writes the log data (serialized using <<spark-sql-streaming-HDFSMetadataLog.md#formats, Json4s (with Jackson binding)>> library). Entries are separated by new lines.

=== [[deserialize]] Deserializing Metadata -- `deserialize` Method

[source, scala]
----
deserialize(in: InputStream): Array[T]
----

NOTE: `deserialize` is part of the <<spark-sql-streaming-HDFSMetadataLog.md#deserialize, HDFSMetadataLog Contract>> to...FIXME.

`deserialize`...FIXME

=== [[add]] Storing Metadata Of Streaming Batch -- `add` Method

[source, scala]
----
add(
  batchId: Long,
  logs: Array[T]): Boolean
----

NOTE: `add` is part of the <<spark-sql-streaming-HDFSMetadataLog.md#add, HDFSMetadataLog Contract>> to store metadata for a batch.

`add`...FIXME

=== [[allFiles]] `allFiles` Method

[source, scala]
----
allFiles(): Array[T]
----

`allFiles`...FIXME

`allFiles` is used when:

* `FileStreamSource` is [created](datasources/file/FileStreamSource.md)
* `MetadataLogFileIndex` is [created](datasources/file/MetadataLogFileIndex.md)

=== [[compact]] `compact` Internal Method

[source, scala]
----
compact(
  batchId: Long,
  logs: Array[T]): Boolean
----

`compact` <<getValidBatchesBeforeCompactionBatch, getValidBatchesBeforeCompactionBatch>> (with the streaming batch and the <<compactInterval, compact interval>>).

`compact`...FIXME

In the end, `compact` <<compactLogs, compactLogs>> and requests the parent `HDFSMetadataLog` to <<spark-sql-streaming-HDFSMetadataLog.md#add, persist metadata of a streaming batch (to a metadata log file)>>.

NOTE: `compact` is used exclusively when `CompactibleFileStreamLog` is requested to <<add, persist metadata of a streaming batch>>.

=== [[getValidBatchesBeforeCompactionBatch]] `getValidBatchesBeforeCompactionBatch` Object Method

[source, scala]
----
getValidBatchesBeforeCompactionBatch(
  compactionBatchId: Long,
  compactInterval: Int): Seq[Long]
----

`getValidBatchesBeforeCompactionBatch`...FIXME

NOTE: `getValidBatchesBeforeCompactionBatch` is used exclusively when `CompactibleFileStreamLog` is requested to <<compact, compact>>.

=== [[isCompactionBatch]] `isCompactionBatch` Object Method

[source, scala]
----
isCompactionBatch(batchId: Long, compactInterval: Int): Boolean
----

`isCompactionBatch`...FIXME

`isCompactionBatch` is used when:

* `CompactibleFileStreamLog` is requested to <<batchIdToPath, batchIdToPath>>, <<add, store the metadata of a batch>>, <<deleteExpiredLog, deleteExpiredLog>>, and <<getValidBatchesBeforeCompactionBatch, getValidBatchesBeforeCompactionBatch>>

* `FileStreamSourceLog` is requested to [store the metadata of a batch](datasources/file/FileStreamSourceLog.md#add) and [get](datasources/file/FileStreamSourceLog.md#get)

=== [[getBatchIdFromFileName]] `getBatchIdFromFileName` Object Method

[source, scala]
----
getBatchIdFromFileName(fileName: String): Long
----

`getBatchIdFromFileName` simply removes the <<COMPACT_FILE_SUFFIX, .compact>> suffix from the given `fileName` and converts the remaining part to a number.

NOTE: `getBatchIdFromFileName` is used when `CompactibleFileStreamLog` is requested to <<pathToBatchId, pathToBatchId>>, <<isBatchFile, isBatchFile>>, and <<deleteExpiredLog, deleteExpiredLog>>.

=== [[deleteExpiredLog]] `deleteExpiredLog` Internal Method

[source, scala]
----
deleteExpiredLog(
  currentBatchId: Long): Unit
----

`deleteExpiredLog` does nothing and simply returns when the current batch ID incremented (`currentBatchId + 1`) is below the <<compactInterval, compact interval>> plus the <<minBatchesToRetain, minBatchesToRetain>>.

`deleteExpiredLog`...FIXME

NOTE: `deleteExpiredLog` is used exclusively when `CompactibleFileStreamLog` is requested to <<add, store metadata of a streaming batch>>.

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| compactInterval
a| [[compactInterval]] *Compact interval*

|===
