# FileStreamSourceLog

`FileStreamSourceLog` is a concrete [CompactibleFileStreamLog](../../CompactibleFileStreamLog.md) (of `FileEntry` metadata) of [FileStreamSource](FileStreamSource.md).

`FileStreamSourceLog` uses a fixed-size <<fileEntryCache, cache>> of metadata of compaction batches.

[[defaultCompactInterval]]
`FileStreamSourceLog` uses <<SQLConf.md#fileSourceLogCompactInterval, spark.sql.streaming.fileSource.log.compactInterval>> configuration property (default: `10`) for the [default compaction interval](../../CompactibleFileStreamLog.md#defaultCompactInterval).

[[fileCleanupDelayMs]]
`FileStreamSourceLog` uses <<SQLConf.md#fileSourceLogCleanupDelay, spark.sql.streaming.fileSource.log.cleanupDelay>> configuration property (default: `10` minutes) for the [fileCleanupDelayMs](../../CompactibleFileStreamLog.md#fileCleanupDelayMs).

[[isDeletingExpiredLog]]
`FileStreamSourceLog` uses <<SQLConf.md#fileSourceLogDeletion, spark.sql.streaming.fileSource.log.deletion>> configuration property (default: `true`) for the [isDeletingExpiredLog](../../CompactibleFileStreamLog.md#isDeletingExpiredLog).

## Creating Instance

`FileStreamSourceLog` (like the parent [CompactibleFileStreamLog](../../CompactibleFileStreamLog.md)) takes the following to be created:

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

`add` requests the parent `CompactibleFileStreamLog` to [store metadata](../../CompactibleFileStreamLog.md#add) (possibly [compacting logs](../../CompactibleFileStreamLog.md#compact) if the batch is [compaction](../../CompactibleFileStreamLog.md#isCompactionBatch)).

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

## Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| cacheSize
a| [[cacheSize]] Size of the <<fileEntryCache, fileEntryCache>> that is exactly the [compact interval](../../CompactibleFileStreamLog.md#compactInterval)

Used when the <<fileEntryCache, fileEntryCache>> is requested to add a new entry in <<add, add>> and <<get, get>> a compaction batch

| fileEntryCache
a| [[fileEntryCache]] Metadata of a streaming batch (`FileEntry`) per batch ID (`LinkedHashMap[Long, Array[FileEntry]]`) of size configured using the <<cacheSize, cacheSize>>

* New entry added for a [compaction batch](../../CompactibleFileStreamLog.md#isCompactionBatch) when <<add, storing (adding) metadata of a streaming batch>>

Used when <<get, get>> (for a [compaction batch](../../CompactibleFileStreamLog.md#isCompactionBatch))

|===
