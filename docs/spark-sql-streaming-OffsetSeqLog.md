== [[OffsetSeqLog]] OffsetSeqLog -- Hadoop DFS-based Metadata Storage of OffsetSeqs

`OffsetSeqLog` is a <<spark-sql-streaming-HDFSMetadataLog.md#, Hadoop DFS-based metadata storage>> for <<OffsetSeq, OffsetSeq>> metadata.

[[OffsetSeq]][[offsets]][[metadata]]
`OffsetSeqLog` uses <<spark-sql-streaming-OffsetSeq.md#, OffsetSeq>> for metadata which holds an ordered collection of offsets and optional metadata (as <<spark-sql-streaming-OffsetSeqMetadata.md#, OffsetSeqMetadata>> for <<spark-sql-streaming-watermark.md#, event-time watermark>>).

`OffsetSeqLog` is <<creating-instance, created>> exclusively for the <<spark-sql-streaming-StreamExecution.md#offsetLog, write-ahead log (WAL) of offsets>> of <<spark-sql-streaming-StreamExecution.md#, stream execution engines>> (i.e. <<spark-sql-streaming-ContinuousExecution.md#, ContinuousExecution>> and <<spark-sql-streaming-MicroBatchExecution.md#, MicroBatchExecution>>).

[[VERSION]]
`OffsetSeqLog` uses `1` for the version when <<serialize, serializing>> and <<deserialize, deserializing>> metadata.

=== [[creating-instance]] Creating OffsetSeqLog Instance

`OffsetSeqLog` (like the parent <<spark-sql-streaming-HDFSMetadataLog.md#creating-instance, HDFSMetadataLog>>) takes the following to be created:

* [[sparkSession]] `SparkSession`
* [[path]] Path of the metadata log directory

=== [[serialize]] Serializing Metadata (Writing Metadata in Serialized Format) -- `serialize` Method

[source, scala]
----
serialize(
  offsetSeq: OffsetSeq,
  out: OutputStream): Unit
----

NOTE: `serialize` is part of <<spark-sql-streaming-HDFSMetadataLog.md#serialize, HDFSMetadataLog Contract>> to serialize metadata (write metadata in serialized format).

`serialize` firstly writes out the <<VERSION, version>> prefixed with `v` on a single line (e.g. `v1`) followed by the <<spark-sql-streaming-OffsetSeq.md#metadata, optional metadata>> in JSON format.

`serialize` then writes out the <<spark-sql-streaming-OffsetSeq.md#offsets, offsets>> in JSON format, one per line.

NOTE: No offsets to write in `offsetSeq` for a streaming source is marked as *-* (a dash) in the log.

```
$ ls -tr [checkpoint-directory]/offsets
0 1 2 3 4 5 6

$ cat [checkpoint-directory]/offsets/6
v1
{"batchWatermarkMs":0,"batchTimestampMs":1502872590006,"conf":{"spark.sql.shuffle.partitions":"200","spark.sql.streaming.stateStore.providerClass":"org.apache.spark.sql.execution.streaming.state.HDFSBackedStateStoreProvider"}}
51
```

=== [[deserialize]] Deserializing Metadata (Reading OffsetSeq from Serialized Format) -- `deserialize` Method

[source, scala]
----
deserialize(in: InputStream): OffsetSeq
----

NOTE: `deserialize` is part of <<spark-sql-streaming-HDFSMetadataLog.md#deserialize, HDFSMetadataLog Contract>> to deserialize metadata (read metadata from serialized format).

`deserialize` firstly parses the <<VERSION, version>> on the first line.

`deserialize` reads the optional metadata (with an empty line for metadata not available).

`deserialize` creates a <<spark-sql-streaming-Offset.md#SerializedOffset, SerializedOffset>> for every line left.

In the end, `deserialize` creates a <<spark-sql-streaming-OffsetSeq.md#fill, OffsetSeq>> for the optional metadata and the `SerializedOffsets`.

When there are no lines in the `InputStream`, `deserialize` throws an `IllegalStateException`:

```
Incomplete log file
```
