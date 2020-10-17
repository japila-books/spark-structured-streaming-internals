# FileStreamSinkLog

`FileStreamSinkLog` is a [CompactibleFileStreamLog](CompactibleFileStreamLog.md) (of [SinkFileStatus](SinkFileStatus.md)es) for [FileStreamSink](FileStreamSink.md#fileLog) and [MetadataLogFileIndex](MetadataLogFileIndex.md#metadataLog).

## Creating Instance

`FileStreamSinkLog` (like the parent [CompactibleFileStreamLog](CompactibleFileStreamLog.md)) takes the following to be created:

* <span id="metadataLogVersion"> [Version](#Version) of the Metadata Log
* <span id="sparkSession"> `SparkSession`
* <span id="path"> Path of the Metadata Log

## Configuration Properties

### <span id="fileCleanupDelayMs"><span id="fileSinkLogCleanupDelay"> spark.sql.streaming.fileSink.log.cleanupDelay

`FileStreamSinkLog` uses [spark.sql.streaming.fileSink.log.cleanupDelay](../../spark-sql-streaming-properties.md#spark.sql.streaming.fileSink.log.cleanupDelay) configuration property for [fileCleanupDelayMs](CompactibleFileStreamLog.md#fileCleanupDelayMs).

### <span id="defaultCompactInterval"><span id="fileSinkLogDeletion"> spark.sql.streaming.fileSink.log.compactInterval

`FileStreamSinkLog` uses [spark.sql.streaming.fileSink.log.compactInterval](../../spark-sql-streaming-properties.md#spark.sql.streaming.fileSink.log.compactInterval) configuration property for [defaultCompactInterval](CompactibleFileStreamLog.md#defaultCompactInterval).

### <span id="isDeletingExpiredLog"><span id="fileSinkLogDeletion"> spark.sql.streaming.fileSink.log.deletion

`FileStreamSinkLog` uses [spark.sql.streaming.fileSink.log.deletion](../../spark-sql-streaming-properties.md#spark.sql.streaming.fileSink.log.deletion) configuration property for [isDeletingExpiredLog](CompactibleFileStreamLog.md#isDeletingExpiredLog).

## <span id="compactLogs"> Compacting Logs

```scala
compactLogs(
  logs: Seq[SinkFileStatus]): Seq[SinkFileStatus]
```

`compactLogs`...FIXME

`compactLogs` is part of the [CompactibleFileStreamLog](CompactibleFileStreamLog.md#compactLogs) abstraction.

## <span id="VERSION"> Version

`FileStreamSinkLog` uses **1** for the version.

## <span id="ADD_ACTION"> Add Action

`FileStreamSinkLog` uses **add** action to create new [metadata logs](SinkFileStatus.md).

## <span id="DELETE_ACTION"> Delete Action

`FileStreamSinkLog` uses **delete** action to mark [metadata logs](SinkFileStatus.md) that should be excluded from [compaction](#compactLogs).
