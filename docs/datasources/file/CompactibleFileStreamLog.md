# CompactibleFileStreamLog

`CompactibleFileStreamLog` is an [extension](#contract) of the [HDFSMetadataLog](../../HDFSMetadataLog.md) abstraction for [metadata logs](#implementations) that can [compact logs](#compactLogs) at [regular intervals](#compactInterval).

## Creating Instance

`CompactibleFileStreamLog` takes the following to be created:

* <span id="metadataLogVersion"> Version of the Metadata Log
* <span id="sparkSession"> `SparkSession`
* <span id="path"> Path of the Metadata Log

??? note "Abstract Class"
    `CompactibleFileStreamLog` is an abstract class and cannot be created directly. It is created indirectly for the [concrete CompactibleFileStreamLogs](#implementations).

## Contract

### <span id="compactLogs"> Filtering Out Obsolete Logs

```scala
compactLogs(
  logs: Seq[T]): Seq[T]
```

Used when [storing metadata](#add) and for [all files (except deleted)](#allFiles)

!!! important
    `compactLogs` does nothing important in the available [implementations](#implementations). Consider this method a noop.

### <span id="defaultCompactInterval"> Default Compact Interval

```scala
defaultCompactInterval: Int
```

Used for the [compact interval](#compactInterval)

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

## <span id="compact"> Compaction

```scala
compact(
  batchId: Long,
  logs: Array[T]): Boolean
```

`compact` [finds valid metadata files for compaction](#getValidBatchesBeforeCompactionBatch) (for the given compaction `batchId` and [compact interval](#compactInterval)) and makes sure that [they are all available](../../HDFSMetadataLog.md#get). `compact` tracks elapsed time (`loadElapsedMs`).

`compact` [filters out obsolete logs](#compactLogs) among the valid metadata files and the input `logs` (which actually does nothing important given the note in [compactLogs](#compactLogs)).

`compact` [stores the metadata](../../HDFSMetadataLog.md#add) (the filtered metadata files and the input `logs`) for the input `batchId`. `compact` tracks elapsed time (`writeElapsedMs`).

`compact` prints out the following DEBUG message (only when the total elapsed time of `loadElapsedMs` and `writeElapsedMs` are below the unconfigurable `2000` ms):

```text
Compacting took [elapsedMs] ms (load: [loadElapsedMs] ms, write: [writeElapsedMs] ms) for compact batch [batchId]
```

In case the total epased time is above the unconfigurable `2000` ms, `compact` prints out the following WARN messages:

```text
Compacting took [elapsedMs] ms (load: [loadElapsedMs] ms, write: [writeElapsedMs] ms) for compact batch [batchId]
Loaded [allLogs] entries (estimated [allLogs] bytes in memory), and wrote [compactedLogs] entries for compact batch [batchId]
```

`compact` throws an `IllegalStateException` when one of the metadata files to compact is not valid (not accessible on a file system or of incorrect format):

```text
[batchIdToPath] doesn't exist when compacting batch [batchId] (compactInterval: [compactInterval])
```

`compact` is used while [storing metadata for streaming batch](#add).

## <span id="minBatchesToRetain"> spark.sql.streaming.fileSink.log.cleanupDelay

`CompactibleFileStreamLog` uses [spark.sql.streaming.fileSink.log.cleanupDelay](../../spark-sql-streaming-properties.md#spark.sql.streaming.fileSink.log.cleanupDelay) configuration property to [delete expired log entries](#deleteExpiredLog).

## <span id="COMPACT_FILE_SUFFIX"> compact File Suffix

`CompactibleFileStreamLog` uses **.compact** file suffix for [batchIdToPath](#batchIdToPath), [getBatchIdFromFileName](#getBatchIdFromFileName), and the [compactInterval](#compactInterval).

## <span id="add"> Storing Metadata for Streaming Batch

```scala
add(
  batchId: Long,
  logs: Array[T]): Boolean
```

`add` checks whether the given `batchId` is [compaction batch](#isCompactionBatch) or not (alongside [compact interval](#compactInterval)).

`add`...FIXME

`add` is part of the [MetadataLog](../../MetadataLog.md#add) abstraction.

### <span id="deleteExpiredLog"> Deleting Expired Log Entries

```scala
deleteExpiredLog(
  currentBatchId: Long): Unit
```

`deleteExpiredLog`...FIXME

`deleteExpiredLog` does nothing and simply returns when the current batch ID incremented (`currentBatchId + 1`) is below the [compact interval](#compactInterval) plus the [minBatchesToRetain](#minBatchesToRetain).

## <span id="compactInterval"> Compact Interval

```scala
compactInterval: Int
```

`compactInterval` is the number of metadata log files between compactions.

??? note "Lazy Value"
    `compactInterval` is a Scala **lazy value** which means that the code to initialize it is executed once only (when accessed for the first time) and cached afterwards.

`compactInterval` finds compacted IDs and determines the compact interval.

`compactInterval` requests the [CheckpointFileManager](../../HDFSMetadataLog.md#fileManager) for the [files](../../CheckpointFileManager.md#list) in the [metadataPath](../../HDFSMetadataLog.md#metadataPath) that are [batch](#isBatchFile) (and possibly [compacted](#COMPACT_FILE_SUFFIX)). `compactInterval` takes the [compacted files](#COMPACT_FILE_SUFFIX) only (if available), [converts them to batch IDs](#pathToBatchId) and sorts in descending order.

`compactInterval` starts with the [default compact interval](#defaultCompactInterval).

* If there are two compacted IDs, their difference is the compact interval
* If there is one compacted ID only, `compactInterval` "derives" the compact interval (FIXME)

`compactInterval` asserts that the compact interval is a positive value or throws an `AssertionError`.

`compactInterval` prints out the following INFO message to the logs (with the [defaultCompactInterval](#defaultCompactInterval)):

```text
Set the compact interval to [interval] [defaultCompactInterval: [defaultCompactInterval]]
```

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

## Utilities

### <span id="getBatchIdFromFileName"> getBatchIdFromFileName

```scala
getBatchIdFromFileName(
  fileName: String): Long
```

`getBatchIdFromFileName` simply removes the [.compact](#COMPACT_FILE_SUFFIX) suffix from the given `fileName` and converts the remaining part to a number.

`getBatchIdFromFileName` is used for [pathToBatchId](#pathToBatchId), [isBatchFile](#isBatchFile), and [delete expired log entries](#deleteExpiredLog).

### <span id="getValidBatchesBeforeCompactionBatch"> getValidBatchesBeforeCompactionBatch

```scala
getValidBatchesBeforeCompactionBatch(
  compactionBatchId: Long,
  compactInterval: Int): Seq[Long]
```

`getValidBatchesBeforeCompactionBatch`...FIXME

`getBatchIdFromFileName` is used for [compaction](#compact).
