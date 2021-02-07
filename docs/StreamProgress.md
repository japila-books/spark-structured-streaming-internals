# StreamProgress &mdash; Collection of Offsets per Streaming Source

`StreamProgress` is a collection of [Offset](Offset.md)s per streaming source.

`StreamProgress` is <<creating-instance, created>> when:

* [StreamExecution](StreamExecution.md) is created (and creates [committed](StreamExecution.md#committedOffsets) and [available](StreamExecution.md#availableOffsets) offsets)

* `OffsetSeq` is requested to [convert to StreamProgress](OffsetSeq.md#toStreamProgress)

`StreamProgress` is an extension of Scala's [scala.collection.immutable.Map]({{ scala.api }}/index.html#scala.collection.immutable.Map) with streaming sources as keys and their [Offset](Offset.md)s as values.

## Creating Instance

`StreamProgress` takes the following to be created:

* [[baseMap]] [Offset](Offset.md)s per streaming source (`Map[BaseStreamingSource, Offset]`) (default: empty)

=== [[get]] Looking Up Offset by Streaming Source -- `get` Method

[source, scala]
----
get(key: BaseStreamingSource): Option[Offset]
----

NOTE: `get` is part of the Scala's `scala.collection.MapLike` to...FIXME.

`get` simply looks up an [Offset](Offset.md)s for the given streaming source in the <<baseMap, baseMap>>.

=== [[plusplus]] `++` Method

[source, scala]
----
++(
  updates: GenTraversableOnce[(BaseStreamingSource, Offset)]): StreamProgress
----

`++` simply creates a new <<creating-instance, StreamProgress>> with the <<baseMap, baseMap>> and the given updates.

`++` is used exclusively when `OffsetSeq` is requested to [convert to StreamProgress](OffsetSeq.md#toStreamProgress).

=== [[toOffsetSeq]] Converting to OffsetSeq -- `toOffsetSeq` Method

[source, scala]
----
toOffsetSeq(
  sources: Seq[BaseStreamingSource],
  metadata: OffsetSeqMetadata): OffsetSeq
----

`toOffsetSeq` creates a [OffsetSeq](OffsetSeq.md) with offsets that are <<get, looked up>> for every streaming source.

`toOffsetSeq` is used when:

* `MicroBatchExecution` stream execution engine is requested to [construct the next streaming micro-batch](micro-batch-execution/MicroBatchExecution.md#constructNextBatch) (to [commit available offsets for a batch to the write-ahead log](micro-batch-execution/MicroBatchExecution.md#constructNextBatch-walCommit))

* `StreamExecution` is requested to [run stream processing](StreamExecution.md#runStream) (that [failed with a Throwable](StreamExecution.md#runStream-catch-Throwable))
