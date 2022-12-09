# OffsetSeq

## Creating Instance

`OffsetSeq` takes the following to be created:

* <span id="offsets"> [Offset](Offset.md)s (`Seq[Option[OffsetV2]]`)
* <span id="metadata"> [OffsetSeqMetadata](OffsetSeqMetadata.md)

`OffsetSeq` is created when:

* `OffsetSeq` is requested to [fill](#fill)
* `StreamProgress` is requested to [toOffsetSeq](StreamProgress.md#toOffsetSeq)

## <span id="fill"> fill

```scala
fill(
  offsets: Offset*): OffsetSeq // (1)!
fill(
  metadata: Option[String],
  offsets: Offset*): OffsetSeq
```

1. Uses no `metadata`

`fill`...FIXME

---

`fill` is used when:

* `OffsetSeqLog` is requested to [deserialize an OffsetSeq](OffsetSeqLog.md#deserialize)
* `ContinuousExecution` is requested to [getStartOffsets](continuous-execution/ContinuousExecution.md#getStartOffsets), [addOffset](continuous-execution/ContinuousExecution.md#addOffset)

<!---
## Review Me

`OffsetSeq` is the metadata managed by [Hadoop DFS-based metadata storage](OffsetSeqLog.md).

`OffsetSeq` is <<creating-instance, created>> (possibly using the <<fill, fill>> factory methods) when:

* `OffsetSeqLog` is requested to [deserialize metadata](OffsetSeqLog.md#deserialize) (retrieve metadata from a persistent storage)

* `StreamProgress` is requested to [convert itself to OffsetSeq](StreamProgress.md#toOffsetSeq) (most importantly when `MicroBatchExecution` stream execution engine is requested to [construct the next streaming micro-batch](micro-batch-execution/MicroBatchExecution.md#constructNextBatch) to [commit available offsets for a batch to the write-ahead log](micro-batch-execution/MicroBatchExecution.md#constructNextBatch-walCommit))

* `ContinuousExecution` stream execution engine is requested to <<ContinuousExecution.md#getStartOffsets, get start offsets>> and <<ContinuousExecution.md#addOffset, addOffset>>

## Creating Instance

`OffsetSeq` takes the following when created:

* [[offsets]] Collection of optional [Offsets](Offset.md) (with `None` for <<toStreamProgress, streaming sources with no new data available>>)
* [[metadata]] Optional [OffsetSeqMetadata](OffsetSeqMetadata.md) (default: `None`)

=== [[toStreamProgress]] Converting to StreamProgress -- `toStreamProgress` Method

[source, scala]
----
toStreamProgress(
  sources: Seq[BaseStreamingSource]): StreamProgress
----

`toStreamProgress` creates a new [StreamProgress](StreamProgress.md) and adds the [streaming sources](Source.md) for which there are new [offsets](#offsets) available.

NOTE: <<offsets, Offsets>> is a collection with _holes_ (empty elements) for streaming sources with no new data available.

`toStreamProgress` throws an `AssertionError` if the number of the input `sources` does not match the <<offsets, offsets>>:

```text
There are [[offsets.size]] sources in the checkpoint offsets and now there are [[sources.size]] sources requested by the query. Cannot continue.
```

`toStreamProgress` is used when:

* `MicroBatchExecution` is requested to <<MicroBatchExecution.md#populateStartOffsets, populate start offsets from offsets and commits checkpoints>> and <<MicroBatchExecution.md#constructNextBatch, construct (or skip) the next streaming micro-batch>>

* `ContinuousExecution` is requested for <<ContinuousExecution.md#getStartOffsets, start offsets>>

=== [[toString]] Textual Representation -- `toString` Method

[source, scala]
----
toString: String
----

NOTE: `toString` is part of the ++https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#toString--++[java.lang.Object] contract for the string representation of the object.

`toString` simply converts the <<offsets, Offsets>> to JSON (if an offset is available) or `-` (a dash if an offset is not available for a streaming source at that position).
-->
