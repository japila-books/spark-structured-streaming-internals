# HDFSMetadataLog

`HDFSMetadataLog` is a [metadata storage](MetadataLog.md) (of type `T`) that uses Hadoop DFS for fault-tolerance and reliability.

[[metadataPath]]
`HDFSMetadataLog` uses the given <<path, path>> as the *metadata directory* with metadata logs. The path is immediately converted to a Hadoop https://hadoop.apache.org/docs/r2.7.3/api/org/apache/hadoop/fs/Path.html[Path] for file management.

[[formats]]
`HDFSMetadataLog` uses http://json4s.org/[Json4s] with the https://github.com/FasterXML/jackson-databind[Jackson] binding for metadata <<serialize, serialization>> and <<deserialize, deserialization>> (to and from JSON format).

`HDFSMetadataLog` is further customized by the <<extensions, extensions>>.

[[extensions]]
.HDFSMetadataLogs (Direct Extensions Only)
[cols="30,70",options="header",width="100%"]
|===
| HDFSMetadataLog
| Description

| _Anonymous_
| [[KafkaSource]] `HDFSMetadataLog` of [KafkaSourceOffsets](datasources/kafka/KafkaSourceOffset.md) for [KafkaSource](datasources/kafka/KafkaSource.md)

| _Anonymous_
| [[RateStreamMicroBatchReader]] `HDFSMetadataLog` of <<spark-sql-streaming-Offset.md#LongOffset, LongOffsets>> for <<spark-sql-streaming-RateStreamMicroBatchReader.md#, RateStreamMicroBatchReader>>

| <<spark-sql-streaming-CommitLog.md#, CommitLog>>
| [[CommitLog]] [Offset commit log](StreamExecution.md#commitLog) of [streaming query execution engines](StreamExecution.md)

| [CompactibleFileStreamLog](datasources/file/CompactibleFileStreamLog.md)
| [[CompactibleFileStreamLog]] Compactible metadata logs (that compact logs at regular interval)

| [KafkaSourceInitialOffsetWriter](datasources/kafka/KafkaSourceInitialOffsetWriter.md)
| [[KafkaSourceInitialOffsetWriter]] `HDFSMetadataLog` of [KafkaSourceOffsets](datasources/kafka/KafkaSourceOffset.md) for [KafkaSource](datasources/kafka/KafkaSource.md)

| [OffsetSeqLog](spark-sql-streaming-OffsetSeqLog.md)
| [[OffsetSeqLog]] [Write-Ahead Log (WAL)](StreamExecution.md#offsetLog) of [streaming query execution engines](StreamExecution.md)

|===

## Creating Instance

`HDFSMetadataLog` takes the following to be created:

* [[sparkSession]] `SparkSession`
* [[path]] Path of the metadata log directory

While being <<creating-instance, created>> `HDFSMetadataLog` creates the <<path, path>> unless exists already.

## <span id="getLatest"> Latest Committed Batch Id with Metadata (If Available)

```scala
getLatest(): Option[(Long, T)]
```

`getLatest` requests the internal <<fileManager, FileManager>> for the files in <<metadataPath, metadata directory>> that match <<batchFilesFilter, batch file filter>>.

`getLatest` takes the batch ids (the batch files correspond to) and sorts the ids in reverse order.

`getLatest` gives the first batch id with the metadata which <<get, could be found in the metadata storage>>.

NOTE: It is possible that the batch id could be in the metadata storage, but not available for retrieval.

`getLatest` is a part of [MetadataLog](MetadataLog.md#getLatest) abstraction.

=== [[serialize]] Serializing Metadata (Writing Metadata in Serialized Format) -- `serialize` Method

[source, scala]
----
serialize(
  metadata: T,
  out: OutputStream): Unit
----

`serialize` simply writes the log data (serialized using <<formats, Json4s (with Jackson binding)>> library).

NOTE: `serialize` is used exclusively when `HDFSMetadataLog` is requested to <<writeBatchToFile, write metadata of a streaming batch to a file (metadata log)>> (when <<add, storing metadata of a streaming batch>>).

=== [[deserialize]] Deserializing Metadata (Reading Metadata from Serialized Format) -- `deserialize` Method

[source, scala]
----
deserialize(in: InputStream): T
----

`deserialize` deserializes a metadata (of type `T`) from a given `InputStream`.

NOTE: `deserialize` is used exclusively when `HDFSMetadataLog` is requested to <<get, retrieve metadata of a batch>>.

=== [[get]][[get-batchId]] Retrieving Metadata Of Streaming Batch -- `get` Method

[source, scala]
----
get(batchId: Long): Option[T]
----

`get`...FIXME

`get` is part of the [MetadataLog](MetadataLog.md#get) abstraction.

=== [[get-range]] Retrieving Metadata of Range of Batches -- `get` Method

[source, scala]
----
get(
  startId: Option[Long],
  endId: Option[Long]): Array[(Long, T)]
----

`get`...FIXME

`get` is part of the [MetadataLog](MetadataLog.md#get) abstraction.

=== [[add]] Persisting Metadata of Streaming Micro-Batch -- `add` Method

[source, scala]
----
add(
  batchId: Long,
  metadata: T): Boolean
----

`add` return `true` when the metadata of the streaming batch was not available and persisted successfully. Otherwise, `add` returns `false`.

Internally, `add` <<get, looks up metadata of the given streaming batch>> (`batchId`) and returns `false` when found.

Otherwise, when not found, `add` <<batchIdToPath, creates a metadata log file>> for the given `batchId` and <<writeBatchToFile, writes metadata to the file>>. `add` returns `true` if successful.

`add` is part of the [MetadataLog](MetadataLog.md#add) abstraction.

=== [[purge]] Removing Expired Metadata (Purging) -- `purge` Method

[source, scala]
----
purge(thresholdBatchId: Long): Unit
----

`purge`...FIXME

`purge` is part of the [MetadataLog](MetadataLog.md#purge) abstraction.

=== [[batchIdToPath]] Creating Batch Metadata File -- `batchIdToPath` Method

[source, scala]
----
batchIdToPath(batchId: Long): Path
----

`batchIdToPath` simply creates a Hadoop https://hadoop.apache.org/docs/r2.7.3/api/org/apache/hadoop/fs/Path.html[Path] for the file called by the specified `batchId` under the <<metadataPath, metadata directory>>.

`batchIdToPath` is used when:

* `CompactibleFileStreamLog` is requested to [compact](datasources/file/CompactibleFileStreamLog.md#compact) and [allFiles](datasources/file/CompactibleFileStreamLog.md#allFiles)

* `HDFSMetadataLog` is requested to <<add, add>>, <<get, get>>, <<purge, purge>>, and <<purgeAfter, purgeAfter>>

=== [[isBatchFile]] `isBatchFile` Method

[source, scala]
----
isBatchFile(path: Path): Boolean
----

`isBatchFile`...FIXME

NOTE: `isBatchFile` is used exclusively when `HDFSMetadataLog` is requested for the <<batchFilesFilter, PathFilter of batch files>>.

=== [[pathToBatchId]] `pathToBatchId` Method

[source, scala]
----
pathToBatchId(path: Path): Long
----

`pathToBatchId`...FIXME

`pathToBatchId` is used when:

* `CompactibleFileStreamLog` is requested for the [compact interval](datasources/file/CompactibleFileStreamLog.md#compactInterval)

* `HDFSMetadataLog` is requested to <<isBatchFile, isBatchFile>>, <<get-range, get metadata of a range of batches>>, <<getLatest, getLatest>>, <<getOrderedBatchFiles, getOrderedBatchFiles>>, <<purge, purge>>, and <<purgeAfter, purgeAfter>>

=== [[verifyBatchIds]] `verifyBatchIds` Object Method

[source, scala]
----
verifyBatchIds(
  batchIds: Seq[Long],
  startId: Option[Long],
  endId: Option[Long]): Unit
----

`verifyBatchIds`...FIXME

`verifyBatchIds` is used when:

* `FileStreamSourceLog` is requested to [get](datasources/file/FileStreamSourceLog.md#get)
* `HDFSMetadataLog` is requested to [get](#get-range)

=== [[parseVersion]] Retrieving Version (From Text Line) -- `parseVersion` Internal Method

[source, scala]
----
parseVersion(
  text: String,
  maxSupportedVersion: Int): Int
----

`parseVersion`...FIXME

`parseVersion` is used when:

* `KafkaSourceInitialOffsetWriter` is requested to [deserialize metadata](datasources/kafka/KafkaSourceInitialOffsetWriter.md#deserialize)

* `KafkaSource` is requested for the [initial partition offsets](datasources/kafka/KafkaSource.md#initialPartitionOffsets)

* `CommitLog` is requested to <<spark-sql-streaming-CommitLog.md#deserialize, deserialize metadata>>

* `CompactibleFileStreamLog` is requested to [deserialize metadata](datasources/file/CompactibleFileStreamLog.md#deserialize)

* `OffsetSeqLog` is requested to <<spark-sql-streaming-OffsetSeqLog.md#deserialize, deserialize metadata>>

* `RateStreamMicroBatchReader` is requested to <<spark-sql-streaming-RateStreamMicroBatchReader.md#deserialize, deserialize metadata>>

=== [[purgeAfter]] `purgeAfter` Method

[source, scala]
----
purgeAfter(thresholdBatchId: Long): Unit
----

`purgeAfter`...FIXME

NOTE: `purgeAfter` seems to be used exclusively in tests.

=== [[writeBatchToFile]] Writing Batch Metadata to File (Metadata Log) -- `writeBatchToFile` Internal Method

[source, scala]
----
writeBatchToFile(
  metadata: T,
  path: Path): Unit
----

`writeBatchToFile` requests the <<fileManager, CheckpointFileManager>> to [createAtomic](CheckpointFileManager.md#createAtomic) (for the specified `path` and the `overwriteIfPossible` flag disabled).

`writeBatchToFile` then <<serialize, serializes the metadata>> (to the `CancellableFSDataOutputStream` output stream) and closes the stream.

In case of an exception, `writeBatchToFile` simply requests the `CancellableFSDataOutputStream` output stream to `cancel` (so that the output file is not generated) and re-throws the exception.

NOTE: `writeBatchToFile` is used exclusively when `HDFSMetadataLog` is requested to <<add, store (persist) metadata of a streaming batch>>.

=== [[getOrderedBatchFiles]] Retrieving Ordered Batch Metadata Files -- `getOrderedBatchFiles` Method

[source, scala]
----
getOrderedBatchFiles(): Array[FileStatus]
----

`getOrderedBatchFiles`...FIXME

NOTE: `getOrderedBatchFiles` does not seem to be used at all.

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| batchFilesFilter
a| [[batchFilesFilter]] Hadoop's https://hadoop.apache.org/docs/r2.7.3/api/org/apache/hadoop/fs/PathFilter.html[PathFilter] of <<isBatchFile, batch files>> (with names being long numbers)

Used when:

* `CompactibleFileStreamLog` is requested for the [compactInterval](datasources/file/CompactibleFileStreamLog.md#compactInterval)

* `HDFSMetadataLog` is requested to <<get, get batch metadata>>, <<getLatest, getLatest>>, <<getOrderedBatchFiles, getOrderedBatchFiles>>, <<purge, purge>>, and <<purgeAfter, purgeAfter>>

| fileManager
a| [[fileManager]] [CheckpointFileManager](CheckpointFileManager.md)

Used when...FIXME

|===
