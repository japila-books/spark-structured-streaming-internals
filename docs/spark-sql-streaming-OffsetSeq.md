== [[OffsetSeq]] OffsetSeq

`OffsetSeq` is the metadata managed by <<spark-sql-streaming-OffsetSeqLog.md#, Hadoop DFS-based metadata storage>>.

`OffsetSeq` is <<creating-instance, created>> (possibly using the <<fill, fill>> factory methods) when:

* `OffsetSeqLog` is requested to <<spark-sql-streaming-OffsetSeqLog.md#deserialize, deserialize metadata>> (retrieve metadata from a persistent storage)

* `StreamProgress` is requested to <<spark-sql-streaming-StreamProgress.md#toOffsetSeq, convert itself to OffsetSeq>> (most importantly when `MicroBatchExecution` stream execution engine is requested to <<spark-sql-streaming-MicroBatchExecution.md#constructNextBatch, construct the next streaming micro-batch>> to <<spark-sql-streaming-MicroBatchExecution.md#constructNextBatch-walCommit, commit available offsets for a batch to the write-ahead log>>)

* `ContinuousExecution` stream execution engine is requested to <<spark-sql-streaming-ContinuousExecution.md#getStartOffsets, get start offsets>> and <<spark-sql-streaming-ContinuousExecution.md#addOffset, addOffset>>

=== [[creating-instance]] Creating OffsetSeq Instance

`OffsetSeq` takes the following when created:

* [[offsets]] Collection of optional <<spark-sql-streaming-Offset.md#, Offsets>> (with `None` for <<toStreamProgress, streaming sources with no new data available>>)
* [[metadata]] Optional <<spark-sql-streaming-OffsetSeqMetadata.md#, OffsetSeqMetadata>> (default: `None`)

=== [[toStreamProgress]] Converting to StreamProgress -- `toStreamProgress` Method

[source, scala]
----
toStreamProgress(
  sources: Seq[BaseStreamingSource]): StreamProgress
----

`toStreamProgress` creates a new <<spark-sql-streaming-StreamProgress.md#, StreamProgress>> and adds the <<spark-sql-streaming-Source.md#, streaming sources>> for which there are new <<offsets, offsets>> available.

NOTE: <<offsets, Offsets>> is a collection with _holes_ (empty elements) for streaming sources with no new data available.

`toStreamProgress` throws an `AssertionError` if the number of the input `sources` does not match the <<offsets, offsets>>:

```
There are [[offsets.size]] sources in the checkpoint offsets and now there are [[sources.size]] sources requested by the query. Cannot continue.
```

[NOTE]
====
`toStreamProgress` is used when:

* `MicroBatchExecution` is requested to <<spark-sql-streaming-MicroBatchExecution.md#populateStartOffsets, populate start offsets from offsets and commits checkpoints>> and <<spark-sql-streaming-MicroBatchExecution.md#constructNextBatch, construct (or skip) the next streaming micro-batch>>

* `ContinuousExecution` is requested for <<spark-sql-streaming-ContinuousExecution.md#getStartOffsets, start offsets>>
====

=== [[toString]] Textual Representation -- `toString` Method

[source, scala]
----
toString: String
----

NOTE: `toString` is part of the ++https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#toString--++[java.lang.Object] contract for the string representation of the object.

`toString` simply converts the <<offsets, Offsets>> to JSON (if an offset is available) or `-` (a dash if an offset is not available for a streaming source at that position).

=== [[fill]] Creating OffsetSeq Instance -- `fill` Factory Methods

[source, scala]
----
fill(
  offsets: Offset*): OffsetSeq // <1>
fill(
  metadata: Option[String],
  offsets: Offset*): OffsetSeq
----
<1> Uses no metadata (`None`)

`fill` simply creates an <<creating-instance, OffsetSeq>> for the given variable sequence of <<spark-sql-streaming-Offset.md#, Offsets>> and the optional <<spark-sql-streaming-OffsetSeqMetadata.md#, OffsetSeqMetadata>> (in JSON format).

[NOTE]
====
`fill` is used when:

* `OffsetSeqLog` is requested to <<spark-sql-streaming-OffsetSeqLog.md#deserialize, deserialize metadata>>

* `ContinuousExecution` stream execution engine is requested to <<spark-sql-streaming-ContinuousExecution.md#getStartOffsets, get start offsets>> and <<spark-sql-streaming-ContinuousExecution.md#addOffset, addOffset>>
====
