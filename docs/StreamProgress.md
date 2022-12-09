# StreamProgress

`StreamProgress` is an immutable `Map` ([Scala]({{ scala.api }}/scala/collection/immutable/Map.html)) of [Offset](Offset.md)s by [SparkDataStream](SparkDataStream.md) (`Map[SparkDataStream, OffsetV2]`)

```scala
class StreamProgress(...)
extends Map[SparkDataStream, OffsetV2]
```

In other words, `StreamProgress` represents [source data stream](SparkDataStream.md)s of a streaming query with their [position](Offset.md).

## Creating Instance

`StreamProgress` takes the following to be created:

* <span id="baseMap"> Base Map (default: empty)

`StreamProgress` is created when:

* `OffsetSeq` is requested to [toStreamProgress](OffsetSeq.md#toStreamProgress)
* [StreamExecution](StreamExecution.md) is created (and creates [committed](StreamExecution.md#committedOffsets), [available](StreamExecution.md#availableOffsets), [latest](StreamExecution.md#latestOffsets) offset trackers)
* `StreamProgress` is requested to [++](#concat)

## <span id="toOffsetSeq"> toOffsetSeq

```scala
toOffsetSeq(
  source: Seq[SparkDataStream],
  metadata: OffsetSeqMetadata): OffsetSeq
```

`toOffsetSeq` creates an [OffsetSeq](OffsetSeq.md).

---

`toOffsetSeq` is used when:

* `StreamExecution` is requested to [runStream](StreamExecution.md#runStream) (to create a `StreamingQueryException` when a streaming query fails)
