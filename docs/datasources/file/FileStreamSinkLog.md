# FileStreamSinkLog

`FileStreamSinkLog` is a [CompactibleFileStreamLog](CompactibleFileStreamLog.md) (of [SinkFileStatus](SinkFileStatus.md)es) for [FileStreamSink](FileStreamSink.md#fileLog) and [MetadataLogFileIndex](MetadataLogFileIndex.md#metadataLog).

`FileStreamSinkLog` concatenates metadata logs to a single compact file after defined compact interval.

## Creating Instance

`FileStreamSinkLog` (like the parent [CompactibleFileStreamLog](CompactibleFileStreamLog.md)) takes the following to be created:

* <span id="metadataLogVersion"> [Version](#Version) of the Metadata Log
* <span id="sparkSession"> `SparkSession`
* <span id="path"> Path of the Metadata Log

## Configuration Properties

### <span id="fileCleanupDelayMs"><span id="fileSinkLogCleanupDelay"> spark.sql.streaming.fileSink.log.cleanupDelay

`FileStreamSinkLog` uses [spark.sql.streaming.fileSink.log.cleanupDelay](../../configuration-properties.md#spark.sql.streaming.fileSink.log.cleanupDelay) configuration property for [fileCleanupDelayMs](CompactibleFileStreamLog.md#fileCleanupDelayMs).

### <span id="defaultCompactInterval"><span id="fileSinkLogDeletion"> spark.sql.streaming.fileSink.log.compactInterval

`FileStreamSinkLog` uses [spark.sql.streaming.fileSink.log.compactInterval](../../configuration-properties.md#spark.sql.streaming.fileSink.log.compactInterval) configuration property for [defaultCompactInterval](CompactibleFileStreamLog.md#defaultCompactInterval).

### <span id="isDeletingExpiredLog"><span id="fileSinkLogDeletion"> spark.sql.streaming.fileSink.log.deletion

`FileStreamSinkLog` uses [spark.sql.streaming.fileSink.log.deletion](../../configuration-properties.md#spark.sql.streaming.fileSink.log.deletion) configuration property for [isDeletingExpiredLog](CompactibleFileStreamLog.md#isDeletingExpiredLog).

## <span id="compactLogs"> Compacting Logs

```scala
compactLogs(
  logs: Seq[SinkFileStatus]): Seq[SinkFileStatus]
```

`compactLogs` finds [delete](#DELETE_ACTION) actions in the given collection of [SinkFileStatus](SinkFileStatus.md)es.

If there are no deletes, `compactLogs` gives the `SinkFileStatus`es back (unmodified).

Otherwise, `compactLogs` removes the deleted paths from the `SinkFileStatus`es.

`compactLogs` is part of the [CompactibleFileStreamLog](CompactibleFileStreamLog.md#compactLogs) abstraction.

## <span id="VERSION"> Version

`FileStreamSinkLog` uses **1** for the version.

## Actions

### <span id="ADD_ACTION"> Add

`FileStreamSinkLog` uses **add** action to create new [metadata logs](SinkFileStatus.md).

### <span id="DELETE_ACTION"> Delete

`FileStreamSinkLog` uses **delete** action to mark [status files](SinkFileStatus.md) to be excluded from [compaction](#compactLogs).

!!! important
    Delete action is not used in Spark Structured Streaming and [will be removed in 3.1.0](https://issues.apache.org/jira/browse/SPARK-32648).
