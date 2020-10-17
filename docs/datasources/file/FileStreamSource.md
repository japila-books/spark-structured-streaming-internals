# FileStreamSource

`FileStreamSource` is a [streaming source](../../Source.md) that reads files (in a given [file format](#fileFormatClassName)) from a [directory](#path).

`FileStreamSource` is a [SupportsAdmissionControl](../../SupportsAdmissionControl.md).

`FileStreamSource` is used by [DataSource.createSource](../../DataSource.md#createSource) for `FileFormat`.

!!! tip
    Learn more in [Demo: Using File Streaming Source](../../demo/using-file-streaming-source.md).

## Creating Instance

`FileStreamSource` takes the following to be created:

* <span id="sparkSession"> `SparkSession`
* <span id="path"> Path
* <span id="fileFormatClassName"> Class Name of `FileFormat`
* <span id="schema"> Schema
* <span id="partitionColumns"> Names of the Partition Columns (if any)
* <span id="metadataPath"> Metadata Path
* <span id="options"> Options (`Map[String, String]`)

`FileStreamSource` is created when `DataSource` is requested to [create a streaming source](../../DataSource.md#createSource) for `FileFormat` data sources.

While being created, `FileStreamSource` prints out the following INFO message to the logs (with the [maxFilesPerBatch](#maxFilesPerBatch) and [maxFileAgeMs](#maxFileAgeMs) options):

```text
maxFilesPerBatch = [maxFilesPerBatch], maxFileAgeMs = [maxFileAgeMs]
```

`FileStreamSource` requests the [FileStreamSourceLog](#metadataLog) for [all files](CompactibleFileStreamLog.md#allFiles) that are added to [seenFiles](#seenFiles) internal registry. `FileStreamSource` requests the [seenFiles](#seenFiles) internal registry to `purge` (remove aged entries).

## <span id="sourceOptions"><span id="FileStreamOptions"> Options

Options are case-insensitive (so `cleanSource` and `CLEANSOURCE` are equivalent).

### <span id="cleanSource"> cleanSource

How to clean up completed files.

Available modes:

* `archive`
* `delete`
* `off`

### <span id="fileNameOnly"> fileNameOnly

Whether to check for new files on on the filename only (`true`) or the full path (`false`)

Default: `false`

When enabled, `FileStreamSource` prints out the following WARN message to the logs:

```text
'fileNameOnly' is enabled. Make sure your file names are unique (e.g. using UUID), otherwise, files with the same name but under different paths will be considered the same and causes data lost.
```

### <span id="latestFirst"> latestFirst

Whether to scan latest files first (`true`) or not (`false`)

Default: `false`

When enabled, `FileStreamSource` prints out the following WARN message to the logs:

```text
'latestFirst' is true. New files will be processed first, which may affect the watermark value. In addition, 'maxFileAge' will be ignored.
```

### <span id="maxFileAgeMs"> maxFileAgeMs

Maximum age of a file that can be found in this directory, before being ignored

Default: `7d`

Uses time suffices: `us`, `ms`, `s`, `m`, `min`, `h`, `d`. No suffix is assumed to be in ms.

### <span id="maxFilesPerTrigger"><span id="maxFilesPerBatch"> maxFilesPerTrigger

Maximum number of files per trigger (batch)

### <span id="sourceArchiveDir"> sourceArchiveDir

Archive directory to move completed files to (for [cleanSource](#cleanSource) set to `archive`)

## <span id="sourceCleaner"> FileStreamSourceCleaner

`FileStreamSource` may create a [FileStreamSourceCleaner](FileStreamSourceCleaner.md) based on [cleanSource](#cleanSource) option.

## <span id="metadataLog"> FileStreamSourceLog

`FileStreamSource` uses [FileStreamSourceLog](FileStreamSourceLog.md) (for the given [metadataPath](#metadataPath)).

## <span id="metadataLogCurrentOffset"><span id="currentLogOffset"> Latest Offset

`FileStreamSource` tracks the latest offset in `metadataLogCurrentOffset` internal registry.

## <span id="seenFiles"> Seen Files Registry

```scala
seenFiles: SeenFilesMap
```

`seenFiles` is...FIXME

`seenFiles` is used for...FIXME

## <span id="commit"> Committing

```scala
commit(
  end: Offset): Unit
```

`commit` is...FIXME

`commit` is part of the [Source](../../Source.md#commit) abstraction.

## <span id="getDefaultReadLimit"> getDefaultReadLimit

```scala
getDefaultReadLimit: ReadLimit
```

`getDefaultReadLimit` is...FIXME

`getDefaultReadLimit` is part of the [SupportsAdmissionControl](../../SupportsAdmissionControl.md#getDefaultReadLimit) abstraction.

## <span id="getOffset"> getOffset

```scala
getOffset: Option[Offset]
```

`getOffset` simply throws an `UnsupportedOperationException`:

```text
latestOffset(Offset, ReadLimit) should be called instead of this method
```

`getOffset` is part of the [Source](../../Source.md#getOffset) abstraction.

## <span id="getBatch"> Generating DataFrame for Streaming Batch

```scala
getBatch(
  start: Option[Offset],
  end: Offset): DataFrame
```

`getBatch`...FIXME

`FileStreamSource.getBatch` asks <<metadataLog, metadataLog>> for the batch.

You should see the following INFO and DEBUG messages in the logs:

```text
Processing ${files.length} files from ${startId + 1}:$endId
Streaming ${files.mkString(", ")}
```

The method to create a result batch is given at instantiation time (as `dataFrameBuilder` constructor parameter).

`getBatch` is part of the [Source](../../Source.md#getBatch) abstraction.

## <span id="fetchMaxOffset"> fetchMaxOffset

```scala
fetchMaxOffset(limit: ReadLimit): FileStreamSourceOffset
```

`fetchMaxOffset`...FIXME

`fetchMaxOffset` is used for [latestOffset](#latestOffset).

## <span id="fetchAllFiles"> fetchAllFiles

```scala
fetchAllFiles(): Seq[(String, Long)]
```

`fetchAllFiles`...FIXME

`fetchAllFiles` is used for [fetchMaxOffset](#fetchMaxOffset).

## <span id="latestOffset"> latestOffset

```scala
latestOffset(
  startOffset: streaming.Offset,
  limit: ReadLimit): streaming.Offset
```

`latestOffset`...FIXME

`latestOffset` is part of the [SparkDataStream](../../SparkDataStream.md#latestOffset) abstraction.

## <span id="stop"> Stopping Streaming Source

```scala
stop(): Unit
```

`stop`...FIXME

`stop` is part of the [SupportsAdmissionControl](../../SupportsAdmissionControl.md#stop) abstraction.

## <span id="allFilesUsingInMemoryFileIndex"> allFilesUsingInMemoryFileIndex

```scala
allFilesUsingInMemoryFileIndex(): Seq[FileStatus]
```

`allFilesUsingInMemoryFileIndex` is...FIXME

`allFilesUsingInMemoryFileIndex` is used for [fetchAllFiles](#fetchAllFiles).

## <span id="allFilesUsingMetadataLogFileIndex"> allFilesUsingMetadataLogFileIndex

```scala
allFilesUsingMetadataLogFileIndex(): Seq[FileStatus]
```

`allFilesUsingMetadataLogFileIndex` is...FIXME

`allFilesUsingMetadataLogFileIndex` is used for [fetchAllFiles](#fetchAllFiles)

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.FileStreamSource` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.execution.streaming.FileStreamSource=ALL
```

Refer to [Logging](../../spark-logging.md).
