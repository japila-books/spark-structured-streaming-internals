# OffsetSeqLog

`OffsetSeqLog` is a [HDFSMetadataLog](HDFSMetadataLog.md) of [OffsetSeq](OffsetSeq.md).

`OffsetSeqLog` is used as the [write-ahead log (WAL) of offsets](StreamExecution.md#offsetLog) of the [streaming query execution engines](StreamExecution.md#implementations).

## Creating Instance

`OffsetSeqLog` takes the following to be created:

* <span id="sparkSession"> `SparkSession` ([Spark SQL]({{ book.spark_sql }}/SparkSession))
* <span id="path"> Metadata log directory

`OffsetSeqLog` is created when:

* `StreamExecution` is [created](StreamExecution.md#offsetLog)

## <span id="get"> get

```scala
get(
  batchId: Long): Option[OffsetSeq]
```

`get` is part of the [HDFSMetadataLog](HDFSMetadataLog.md#get) abstraction.

---

`get` looks up the `batchId` in the [cachedMetadata](#cachedMetadata), if available, or uses the parent `HDFSMetadataLog` to [find it](HDFSMetadataLog.md#get).

## <span id="add"> add

```scala
add(
  batchId: Long,
  metadata: OffsetSeq): Boolean
```

`add` is part of the [HDFSMetadataLog](HDFSMetadataLog.md#add) abstraction.

---

`add` requests the parent `HDFSMetadataLog` to [add](HDFSMetadataLog.md#add) the given `batchId` and `metadata`.

When successful, `add` adds it to the [cachedMetadata](#cachedMetadata) and removes all previous batch metadata but the last two.

<!---
## Review Me

[[OffsetSeq]][[offsets]][[metadata]]
`OffsetSeqLog` uses [OffsetSeq](OffsetSeq.md) for metadata which holds an ordered collection of offsets and optional metadata (as [OffsetSeqMetadata](OffsetSeqMetadata.md) for [event-time watermark](watermark/index.md)).

[[VERSION]]
`OffsetSeqLog` uses `1` for the version when <<serialize, serializing>> and <<deserialize, deserializing>> metadata.

=== [[serialize]] Serializing Metadata (Writing Metadata in Serialized Format) -- `serialize` Method

[source, scala]
----
serialize(
  offsetSeq: OffsetSeq,
  out: OutputStream): Unit
----

`serialize` firstly writes out the <<VERSION, version>> prefixed with `v` on a single line (e.g. `v1`) followed by the [optional metadata](OffsetSeq.md#metadata) in JSON format.

`serialize` then writes out the [offsets](OffsetSeq.md#offsets) in JSON format, one per line.

NOTE: No offsets to write in `offsetSeq` for a streaming source is marked as *-* (a dash) in the log.

```text
$ ls -tr [checkpoint-directory]/offsets
0 1 2 3 4 5 6

$ cat [checkpoint-directory]/offsets/6
v1
{"batchWatermarkMs":0,"batchTimestampMs":1502872590006,"conf":{"spark.sql.shuffle.partitions":"200","spark.sql.streaming.stateStore.providerClass":"org.apache.spark.sql.execution.streaming.state.HDFSBackedStateStoreProvider"}}
51
```

`serialize` is part of [HDFSMetadataLog](HDFSMetadataLog.md#serialize) abstraction.

=== [[deserialize]] Deserializing Metadata (Reading OffsetSeq from Serialized Format) -- `deserialize` Method

[source, scala]
----
deserialize(in: InputStream): OffsetSeq
----

`deserialize` firstly parses the <<VERSION, version>> on the first line.

`deserialize` reads the optional metadata (with an empty line for metadata not available).

`deserialize` creates a [SerializedOffset](Offset.md#SerializedOffset) for every line left.

In the end, `deserialize` creates a [OffsetSeq](OffsetSeq.md#fill) for the optional metadata and the `SerializedOffsets`.

When there are no lines in the `InputStream`, `deserialize` throws an `IllegalStateException`:

```text
Incomplete log file
```

`deserialize` is part of [HDFSMetadataLog](HDFSMetadataLog.md#deserialize) abstraction.
-->
