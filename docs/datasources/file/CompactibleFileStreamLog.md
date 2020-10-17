# CompactibleFileStreamLog

`CompactibleFileStreamLog` is an [extension](#contract) of the [HDFSMetadataLog](../../HDFSMetadataLog.md) abstraction for [metadata logs](#implementations) that can [compactLogs](#compactLogs) every [compact interval](#compactInterval).

## Creating Instance

`CompactibleFileStreamLog` takes the following to be created:

* <span id="metadataLogVersion"> Version of the Metadata Log
* <span id="sparkSession"> `SparkSession`
* <span id="path"> Path of the Metadata Log

??? note "Abstract Class"
    `CompactibleFileStreamLog` is an abstract class and cannot be created directly. It is created indirectly for the [concrete CompactibleFileStreamLogs](#implementations).

## Contract

### <span id="compactLogs"> Compacting Logs

```scala
compactLogs(
  logs: Seq[T]): Seq[T]
```

Used to [store metadata](#add) and for [all files (except deleted)](#allFiles)

### <span id="defaultCompactInterval"> Compact Interval

```scala
defaultCompactInterval: Int
```

Used for [compactInterval](#compactInterval)

### <span id="fileCleanupDelayMs"> File Cleanup Delay

```scala
fileCleanupDelayMs: Long
```

Used for [delete expired log entries](#deleteExpiredLog)

### <span id="isDeletingExpiredLog"> isDeletingExpiredLog

```scala
isDeletingExpiredLog: Boolean
```

Used to [store metadata](#add)

## Implementations

* [FileStreamSinkLog](FileStreamSinkLog.md)
* [FileStreamSourceLog](FileStreamSourceLog.md)

## <span id="minBatchesToRetain"> spark.sql.streaming.fileSink.log.cleanupDelay

`CompactibleFileStreamLog` uses [spark.sql.streaming.fileSink.log.cleanupDelay](../../spark-sql-streaming-properties.md#spark.sql.streaming.fileSink.log.cleanupDelay) configuration property to [delete expired log entries](#deleteExpiredLog).

## <span id="COMPACT_FILE_SUFFIX"> compact File Suffix

`CompactibleFileStreamLog` uses **.compact** file suffix for [batchIdToPath](#batchIdToPath), [getBatchIdFromFileName](#getBatchIdFromFileName), and the [compactInterval](#compactInterval).

## <span id="add"> Storing Metadata

```scala
add(
  batchId: Long,
  logs: Array[T]): Boolean
```

`add`...FIXME

`add` is part of the [MetadataLog](../../MetadataLog.md#add) abstraction.

### <span id="compact"> Compacting

```scala
compact(
  batchId: Long,
  logs: Array[T]): Boolean
```

`compact`...FIXME

### <span id="deleteExpiredLog"> Deleting Expired Log Entries

```scala
deleteExpiredLog(
  currentBatchId: Long): Unit
```

`deleteExpiredLog`...FIXME

`deleteExpiredLog` does nothing and simply returns when the current batch ID incremented (`currentBatchId + 1`) is below the [compact interval](#compactInterval) plus the [minBatchesToRetain](#minBatchesToRetain).

## <span id="allFiles"> All Files (Except Deleted)

```scala
allFiles(): Array[T]
```

`allFiles`...FIXME

`allFiles` is used when:

* `FileStreamSource` is [created](FileStreamSource.md)
* `MetadataLogFileIndex` is [created](MetadataLogFileIndex.md)

## <span id="batchIdToPath"> Converting Batch Id to Hadoop Path

```scala
batchIdToPath(
  batchId: Long): Path
```

`batchIdToPath`...FIXME

`batchIdToPath` is part of the [HDFSMetadataLog](../../HDFSMetadataLog.md#batchIdToPath) abstraction.

## <span id="pathToBatchId"> Converting Hadoop Path to Batch Id

```scala
pathToBatchId(
  path: Path): Long
```

`pathToBatchId`...FIXME

`pathToBatchId` is part of the [HDFSMetadataLog](../../HDFSMetadataLog.md#pathToBatchId) abstraction.

## <span id="isBatchFile"> isBatchFile

```scala
isBatchFile(
  path: Path): Boolean
```

`isBatchFile` is `true` when successful to [get the batchId](#getBatchIdFromFileName) for the given path. Otherwise is `false`.

`isBatchFile` is part of the [HDFSMetadataLog](../../HDFSMetadataLog.md#isBatchFile) abstraction.

## <span id="serialize"> Serializing Metadata (Writing Metadata in Serialized Format)

```scala
serialize(
  logData: Array[T],
  out: OutputStream): Unit
```

`serialize` writes the version header (`v` and the <<metadataLogVersion, metadataLogVersion>>) out to the given output stream (in `UTF_8`).

`serialize` then writes the log data (serialized using [Json4s (with Jackson binding)](../../HDFSMetadataLog.md#formats) library). Entries are separated by new lines.

`serialize` is part of the [HDFSMetadataLog](../../HDFSMetadataLog.md#serialize) abstraction.

## <span id="deserialize"> Deserializing Metadata

```scala
deserialize(
  in: InputStream): Array[T]
```

`deserialize`...FIXME

`deserialize` is part of the [HDFSMetadataLog](../../HDFSMetadataLog.md#deserialize) abstraction.

## <span id="getBatchIdFromFileName"> getBatchIdFromFileName Utility

```scala
getBatchIdFromFileName(
  fileName: String): Long
```

`getBatchIdFromFileName` simply removes the [.compact](#COMPACT_FILE_SUFFIX) suffix from the given `fileName` and converts the remaining part to a number.

`getBatchIdFromFileName` is used for [pathToBatchId](#pathToBatchId), [isBatchFile](#isBatchFile), and [delete expired log entries](#deleteExpiredLog).
