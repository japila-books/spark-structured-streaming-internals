# StateOperatorProgress

`StateOperatorProgress` is metadata about updates made to stateful operators of a single micro-batch (_progress_) of a [StreamingQuery](../StreamingQuery.md).

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
* <span id="customMetrics"> Custom Metrics (default: empty)

`StateOperatorProgress` is created when:

* `StateStoreWriter` is requested for a [StateOperatorProgress](../physical-operators/StateStoreWriter.md#getProgress)
* `StateOperatorProgress` is requested for a [copy](#copy)

### <span id="numRowsTotal"> numRowsTotal

`numRowsTotal` is the value of [numTotalStateRows](../physical-operators/StateStoreWriter.md##numTotalStateRows) metric of [StateStoreWriter](../physical-operators/StateStoreWriter.md) physical operator (when requested to [get progress](../physical-operators/StateStoreWriter.md#getProgress)).

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
