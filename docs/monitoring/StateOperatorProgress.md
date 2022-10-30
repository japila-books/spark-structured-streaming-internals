# StateOperatorProgress

`StateOperatorProgress` is a progress of (_updates made to_) all the stateful operators in a micro-batch of a [StreamingQuery](../StreamingQuery.md).

## Creating Instance

`StateOperatorProgress` takes the following to be created:

* <span id="operatorName"> Operator Name
* [numRowsTotal](#numRowsTotal)
* <span id="numRowsUpdated"> numRowsUpdated
* <span id="allUpdatesTimeMs"> allUpdatesTimeMs
* <span id="numRowsRemoved"> numRowsRemoved
* <span id="allRemovalsTimeMs"> allRemovalsTimeMs
* <span id="commitTimeMs"> commitTimeMs
* <span id="memoryUsedBytes"> memoryUsedBytes
* <span id="numRowsDroppedByWatermark"> numRowsDroppedByWatermark
* <span id="numShufflePartitions"> numShufflePartitions
* <span id="numStateStoreInstances"> numStateStoreInstances
* [Custom Metrics](#customMetrics)

`StateOperatorProgress` is created when:

* `StateStoreWriter` is requested to [report progress](../physical-operators/StateStoreWriter.md#getProgress)
* `StateOperatorProgress` is requested for a [copy](#copy)

### <span id="numRowsTotal"> numRowsTotal

`numRowsTotal` is the value of [numTotalStateRows](../physical-operators/StateStoreWriter.md#numTotalStateRows) metric of a [StateStoreWriter](../physical-operators/StateStoreWriter.md) physical operator (when requested to [get progress](../physical-operators/StateStoreWriter.md#getProgress)).

### <span id="customMetrics"> Custom Metrics

```scala
customMetrics: Map[String, Long]
```

`StateOperatorProgress` can be given a collection of custom metrics (of the stateful operator it reports progress of). There are no metrics defined by default.

The custom metrics are [stateStoreCustomMetrics](../physical-operators/StateStoreWriter.md#stateStoreCustomMetrics) and [statefulOperatorCustomMetrics](../physical-operators/StateStoreWriter.md#statefulOperatorCustomMetrics)

Any custom metric included in [spark.sql.streaming.ui.enabledCustomMetricList](../configuration-properties.md#spark.sql.streaming.ui.enabledCustomMetricList) is displayed in [Structured Streaming UI](../webui/StreamingQueryStatisticsPage.md#generateAggregatedCustomMetrics).

Included in [jsonValue](#jsonValue)

## <span id="copy"> copy

```scala
copy(
  newNumRowsUpdated: Long,
  newNumRowsDroppedByWatermark: Long): StateOperatorProgress
```

`copy` creates a copy of this `StateOperatorProgress` with the [numRowsUpdated](#numRowsUpdated) and [numRowsDroppedByWatermark](#numRowsDroppedByWatermark) metrics updated.

---

`copy` is used when:

* `ProgressReporter` is requested to [extractStateOperatorMetrics](ProgressReporter.md#extractStateOperatorMetrics)
* `SessionWindowStateStoreSaveExec` is requested for a [StateOperatorProgress](../physical-operators/SessionWindowStateStoreSaveExec.md#extractStateOperatorMetrics)

## <span id="jsonValue"> jsonValue

```scala
jsonValue: JValue
```

`jsonValue`...FIXME

---

`jsonValue` is used when:

* `StateOperatorProgress` is requested to [json](#json), [prettyJson](#prettyJson)
* `StreamingQueryProgress` is requested to [jsonValue](StreamingQueryProgress.md#jsonValue)
