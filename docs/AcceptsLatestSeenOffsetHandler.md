# AcceptsLatestSeenOffsetHandler

`AcceptsLatestSeenOffsetHandler` is a utility that can [setLatestSeenOffsetOnSources](#setLatestSeenOffsetOnSources) on [SparkDataStream](SparkDataStream.md)s with support for [AcceptsLatestSeenOffset](AcceptsLatestSeenOffset.md).

## <span id="setLatestSeenOffsetOnSources"> setLatestSeenOffsetOnSources

```scala
setLatestSeenOffsetOnSources(
  offsets: Option[OffsetSeq],
  sources: Seq[SparkDataStream]): Unit
```

`setLatestSeenOffsetOnSources`...FIXME

---

`setLatestSeenOffsetOnSources` is used when:

* `MicroBatchExecution` is requested to [run an activated streaming query](micro-batch-execution/MicroBatchExecution.md#runActivatedStream) (and the [currentBatchId](StreamExecution.md#currentBatchId) has not been initialized yet)
* `ContinuousExecution` is requested to [run a streaming query in continuous mode](continuous-execution/ContinuousExecution.md#runContinuous) (and the [currentBatchId](StreamExecution.md#currentBatchId) has been initialized already)
