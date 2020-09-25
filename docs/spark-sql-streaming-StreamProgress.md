# StreamProgress &mdash; Collection of Offsets per Streaming Source

`StreamProgress` is a collection of <<spark-sql-streaming-Offset.md#, Offsets>> per <<spark-sql-streaming-BaseStreamingSource.md#, streaming source>>.

`StreamProgress` is <<creating-instance, created>> when:

* [StreamExecution](StreamExecution.md) is created (and creates [committed](StreamExecution.md#committedOffsets) and [available](StreamExecution.md#availableOffsets) offsets)

* `OffsetSeq` is requested to <<spark-sql-streaming-OffsetSeq.md#toStreamProgress, convert to StreamProgress>>

`StreamProgress` is an extension of Scala's https://www.scala-lang.org/api/2.11.11/index.html#scala.collection.immutable.Map[scala.collection.immutable.Map] with <<spark-sql-streaming-BaseStreamingSource.md#, streaming sources>> as keys and their <<spark-sql-streaming-Offset.md#, Offsets>> as values.

=== [[creating-instance]] Creating StreamProgress Instance

`StreamProgress` takes the following to be created:

* [[baseMap]] Optional collection of <<spark-sql-streaming-Offset.md#, offsets>> per <<spark-sql-streaming-BaseStreamingSource.md#, streaming source>> (`Map[BaseStreamingSource, Offset]`) (default: empty)

=== [[get]] Looking Up Offset by Streaming Source -- `get` Method

[source, scala]
----
get(key: BaseStreamingSource): Option[Offset]
----

NOTE: `get` is part of the Scala's `scala.collection.MapLike` to...FIXME.

`get` simply looks up an <<spark-sql-streaming-Offset.md#, Offsets>> for the given <<spark-sql-streaming-BaseStreamingSource.md#, BaseStreamingSource>> in the <<baseMap, baseMap>>.

=== [[plusplus]] `++` Method

[source, scala]
----
++(
  updates: GenTraversableOnce[(BaseStreamingSource, Offset)]): StreamProgress
----

`++` simply creates a new <<creating-instance, StreamProgress>> with the <<baseMap, baseMap>> and the given updates.

NOTE: `++` is used exclusively when `OffsetSeq` is requested to <<spark-sql-streaming-OffsetSeq.md#toStreamProgress, convert to StreamProgress>>.

=== [[toOffsetSeq]] Converting to OffsetSeq -- `toOffsetSeq` Method

[source, scala]
----
toOffsetSeq(
  sources: Seq[BaseStreamingSource],
  metadata: OffsetSeqMetadata): OffsetSeq
----

`toOffsetSeq` creates a <<spark-sql-streaming-OffsetSeq.md#, OffsetSeq>> with offsets that are <<get, looked up>> for every <<spark-sql-streaming-BaseStreamingSource.md#, BaseStreamingSource>>.

`toOffsetSeq` is used when:

* `MicroBatchExecution` stream execution engine is requested to [construct the next streaming micro-batch](MicroBatchExecution.md#constructNextBatch) (to [commit available offsets for a batch to the write-ahead log](MicroBatchExecution.md#constructNextBatch-walCommit))

* `StreamExecution` is requested to [run stream processing](StreamExecution.md#runStream) (that [failed with a Throwable](StreamExecution.md#runStream-catch-Throwable))
