# HDFSMetadataLog

`HDFSMetadataLog` is an extension of the [MetadataLog](MetadataLog.md) abstraction for [metadata storage](#extensions) to store [batch files](#batch-files) in a [metadata log directory](#metadataPath) on Hadoop DFS (for fault-tolerance and reliability).

## Extensions

* [CommitLog](CommitLog.md)
* [CompactibleFileStreamLog](datasources/file/CompactibleFileStreamLog.md)
* [KafkaSourceInitialOffsetWriter](kafka/KafkaSourceInitialOffsetWriter.md)
* [OffsetSeqLog](OffsetSeqLog.md)

## Creating Instance

`HDFSMetadataLog` takes the following to be created:

* <span id="sparkSession"> `SparkSession`
* <span id="path"> Path of the [metadata log directory](#metadataPath)

While being created, `HDFSMetadataLog` makes sure that the [path](#path) exists (and creates it if not).

## <span id="metadataPath"> Metadata Log Directory

`HDFSMetadataLog` uses the given [path](#path) as the **metadata log directory** with metadata logs ([one per batch](#batchIdToPath)).

The path is immediately converted to a Hadoop [Path]({{ hadoop.api }}/org/apache/hadoop/fs/Path.html) for file management.

## <span id="fileManager"> CheckpointFileManager

`HDFSMetadataLog` [creates a CheckpointFileManager](CheckpointFileManager.md#create) (with the [metadata log directory](#metadataPath)) when [created](#creating-instance).

## <span id="formats"> Implicit Json4s Formats

`HDFSMetadataLog` uses [Json4s](http://json4s.org/) with the [Jackson](https://github.com/FasterXML/jackson-databind) binding for metadata [serialization](#serialize) and [deserialization](#deserialize) (to and from JSON format).

## <span id="getLatest"> Latest Committed Batch Id with Metadata (If Available)

```scala
getLatest(): Option[(Long, T)]
```

`getLatest` is a part of [MetadataLog](MetadataLog.md#getLatest) abstraction.

`getLatest` requests the internal <<fileManager, FileManager>> for the files in <<metadataPath, metadata log directory>> that match <<batchFilesFilter, batch file filter>>.

`getLatest` takes the batch ids (the batch files correspond to) and sorts the ids in reverse order.

`getLatest` gives the first batch id with the metadata which <<get, could be found in the metadata storage>>.

!!! note
    It is possible that the batch id could be in the metadata storage, but not available for retrieval.

## <span id="get"><span id="get-batchId"> Retrieving Metadata of Streaming Batch (if Available)

```scala
get(
  batchId: Long): Option[T]
```

`get` is part of the [MetadataLog](MetadataLog.md#get) abstraction.

`get`...FIXME

### <span id="deserialize"> Deserializing Metadata

```scala
deserialize(
  in: InputStream): T
```

`deserialize` deserializes a metadata (of type `T`) from a given `InputStream`.

`deserialize` is used to [retrieve metadata of a batch](#get).

## <span id="get-range"> Retrieving Metadata of Streaming Batches (if Available)

```scala
get(
  startId: Option[Long],
  endId: Option[Long]): Array[(Long, T)]
```

`get` is part of the [MetadataLog](MetadataLog.md#get) abstraction.

`get`...FIXME

## <span id="add"> Persisting Metadata of Streaming Micro-Batch

```scala
add(
  batchId: Long,
  metadata: T): Boolean
```

`add` is part of the [MetadataLog](MetadataLog.md#add) abstraction.

`add` return `true` when the metadata of the streaming batch was not available and persisted successfully. Otherwise, `add` returns `false`.

Internally, `add` <<get, looks up metadata of the given streaming batch>> (`batchId`) and returns `false` when found.

Otherwise, when not found, `add` <<batchIdToPath, creates a metadata log file>> for the given `batchId` and <<writeBatchToFile, writes metadata to the file>>. `add` returns `true` if successful.

### <span id="writeBatchToFile"> Writing Batch Metadata to File (Metadata Log)

```scala
writeBatchToFile(
  metadata: T,
  path: Path): Unit
```

`writeBatchToFile` requests the <<fileManager, CheckpointFileManager>> to [createAtomic](CheckpointFileManager.md#createAtomic) (for the specified `path` and the `overwriteIfPossible` flag disabled).

`writeBatchToFile` then <<serialize, serializes the metadata>> (to the `CancellableFSDataOutputStream` output stream) and closes the stream.

In case of an exception, `writeBatchToFile` simply requests the `CancellableFSDataOutputStream` output stream to `cancel` (so that the output file is not generated) and re-throws the exception.

### <span id="serialize"> Serializing Metadata

```scala
serialize(
  metadata: T,
  out: OutputStream): Unit
```

`serialize` simply writes out the log data in a serialized format (using [Json4s (with Jackson binding)](#formats) library).

## <span id="purge"> Purging Expired Metadata

```scala
purge(
  thresholdBatchId: Long): Unit
```

`purge` is part of the [MetadataLog](MetadataLog.md#purge) abstraction.

`purge`...FIXME

## <span id="isBatchFile"><span id="batchFilesFilter"> Batch Files

`HDFSMetadataLog` considers a file a **batch file** when the name is simply a `long` number.

`HDFSMetadataLog` uses a Hadoop [PathFilter]({{ hadoop.api }}/org/apache/hadoop/fs/PathFilter.html) to list only batch files.

## <span id="verifyBatchIds"> Verifying Batch Ids

```scala
verifyBatchIds(
  batchIds: Seq[Long],
  startId: Option[Long],
  endId: Option[Long]): Unit
```

`verifyBatchIds`...FIXME

`verifyBatchIds` is used when:

* `FileStreamSourceLog` is requested to [get](datasources/file/FileStreamSourceLog.md#get)
* `HDFSMetadataLog` is requested to [get](#get-range)

## <span id="batchIdToPath"> Path of Metadata File by Batch Id

```scala
batchIdToPath(
  batchId: Long): Path
```

`batchIdToPath` simply creates a Hadoop [Path]({{ hadoop.api }}/org/apache/hadoop/fs/Path.html) for the file by the given `batchId` under the [metadata log directory](#metadataPath).

## <span id="pathToBatchId"> Batch Id by Path of Metadata File

```scala
pathToBatchId(
  path: Path): Long
```

`pathToBatchId`...FIXME
