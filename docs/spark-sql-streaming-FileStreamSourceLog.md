== [[FileStreamSourceLog]] FileStreamSourceLog

`FileStreamSourceLog` is a concrete <<spark-sql-streaming-CompactibleFileStreamLog.md#, CompactibleFileStreamLog>> (of `FileEntry` metadata) of <<spark-sql-streaming-FileStreamSource.md#, FileStreamSource>>.

`FileStreamSourceLog` uses a fixed-size <<fileEntryCache, cache>> of metadata of compaction batches.

[[defaultCompactInterval]]
`FileStreamSourceLog` uses <<spark-sql-streaming-SQLConf.md#fileSourceLogCompactInterval, spark.sql.streaming.fileSource.log.compactInterval>> configuration property (default: `10`) for the <<spark-sql-streaming-CompactibleFileStreamLog.md#defaultCompactInterval, default compaction interval>>.

[[fileCleanupDelayMs]]
`FileStreamSourceLog` uses <<spark-sql-streaming-SQLConf.md#fileSourceLogCleanupDelay, spark.sql.streaming.fileSource.log.cleanupDelay>> configuration property (default: `10` minutes) for the <<spark-sql-streaming-CompactibleFileStreamLog.md#fileCleanupDelayMs, fileCleanupDelayMs>>.

[[isDeletingExpiredLog]]
`FileStreamSourceLog` uses <<spark-sql-streaming-SQLConf.md#fileSourceLogDeletion, spark.sql.streaming.fileSource.log.deletion>> configuration property (default: `true`) for the <<spark-sql-streaming-CompactibleFileStreamLog.md#isDeletingExpiredLog, isDeletingExpiredLog>>.

=== [[creating-instance]] Creating FileStreamSourceLog Instance

`FileStreamSourceLog` (like the parent <<spark-sql-streaming-CompactibleFileStreamLog.md#, CompactibleFileStreamLog>>) takes the following to be created:

* [[metadataLogVersion]] Metadata version
* [[sparkSession]] `SparkSession`
* [[path]] Path of the metadata log directory

=== [[add]] Storing (Adding) Metadata of Streaming Batch -- `add` Method

[source, scala]
----
add(
  batchId: Long,
  logs: Array[FileEntry]): Boolean
----

NOTE: `add` is part of the <<spark-sql-streaming-MetadataLog.md#add, MetadataLog Contract>> to store (_add_) metadata of a streaming batch.

`add` requests the parent `CompactibleFileStreamLog` to <<spark-sql-streaming-CompactibleFileStreamLog.md#add, store metadata>> (possibly <<spark-sql-streaming-CompactibleFileStreamLog.md#compact, compacting logs>> if the batch is <<spark-sql-streaming-CompactibleFileStreamLog.md#isCompactionBatch, compaction>>).

If so (and this is a compation batch), `add` adds the batch and the logs to <<fileEntryCache, fileEntryCache>> internal registry (and possibly removing the eldest entry if the size is above the <<cacheSize, cacheSize>>).

=== [[get]][[get-range]] `get` Method

[source, scala]
----
get(
  startId: Option[Long],
  endId: Option[Long]): Array[(Long, Array[FileEntry])]
----

NOTE: `get` is part of the <<spark-sql-streaming-MetadataLog.md#get, MetadataLog Contract>> to...FIXME.

`get`...FIXME

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| cacheSize
a| [[cacheSize]] Size of the <<fileEntryCache, fileEntryCache>> that is exactly the <<spark-sql-streaming-CompactibleFileStreamLog.md#compactInterval, compact interval>>

Used when the <<fileEntryCache, fileEntryCache>> is requested to add a new entry in <<add, add>> and <<get, get>> a compaction batch

| fileEntryCache
a| [[fileEntryCache]] Metadata of a streaming batch (`FileEntry`) per batch ID (`LinkedHashMap[Long, Array[FileEntry]]`) of size configured using the <<cacheSize, cacheSize>>

* New entry added for a <<spark-sql-streaming-CompactibleFileStreamLog.md#isCompactionBatch, compaction batch>> when <<add, storing (adding) metadata of a streaming batch>>

Used when <<get, get>> (for a <<spark-sql-streaming-CompactibleFileStreamLog.md#isCompactionBatch, compaction batch>>)

|===
