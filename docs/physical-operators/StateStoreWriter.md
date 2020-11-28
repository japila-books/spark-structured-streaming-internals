# StateStoreWriter Physical Operators

`StateStoreWriter` is an [extension](#contract) of the [StatefulOperator](StatefulOperator.md) abstraction for [stateful physical operators](#implementations) that write to a state store and collect the [write metrics](#metrics) for [execution progress reporting](#getProgress).

## Implementations

* [FlatMapGroupsWithStateExec](FlatMapGroupsWithStateExec.md)
* [StateStoreSaveExec](StateStoreSaveExec.md)
* [StreamingDeduplicateExec](StreamingDeduplicateExec.md)
* [StreamingGlobalLimitExec](StreamingGlobalLimitExec.md)
* [StreamingSymmetricHashJoinExec](StreamingSymmetricHashJoinExec.md)

## <span id="metrics"> Performance Metrics

ID | Name
---------|----------
 numOutputRows | number of output rows
 numTotalStateRows | number of total state rows
 numUpdatedStateRows | number of updated state rows
 allUpdatesTimeMs | time to update
 allRemovalsTimeMs | time to remove
 commitTimeMs | time to commit changes
 stateMemory | memory used by state

## <span id="setStoreMetrics"> Setting StateStore-Specific Metrics for Stateful Physical Operator

```scala
setStoreMetrics(
  store: StateStore): Unit
```

`setStoreMetrics` requests the specified [StateStore](../StateStore.md) for the [metrics](../StateStore.md#metrics) and records the following metrics of a physical operator:

* [numTotalStateRows](#numTotalStateRows) as the [number of keys](../spark-sql-streaming-StateStoreMetrics.md#numKeys)

* [stateMemory](#stateMemory) as the [memory used (in bytes)](../spark-sql-streaming-StateStoreMetrics.md#memoryUsedBytes)

`setStoreMetrics` records the [custom metrics](../spark-sql-streaming-StateStoreMetrics.md#customMetrics).

`setStoreMetrics` is used when the following physical operators are executed:

* [FlatMapGroupsWithStateExec](FlatMapGroupsWithStateExec.md)
* [StateStoreSaveExec](StateStoreSaveExec.md)
* [StreamingDeduplicateExec](StreamingDeduplicateExec.md)
* [StreamingGlobalLimitExec](StreamingGlobalLimitExec.md)

## <span id="getProgress"> StateOperatorProgress

```scala
getProgress(): StateOperatorProgress
```

`getProgress`...FIXME

`getProgress` is used when `ProgressReporter` is requested to [extractStateOperatorMetrics](../monitoring/ProgressReporter.md#extractStateOperatorMetrics) (when `MicroBatchExecution` is requested to [run the activated streaming query](../MicroBatchExecution.md#runActivatedStream)).

## <span id="shouldRunAnotherBatch"> Checking Out Whether Last Batch Execution Requires Another Non-Data Batch or Not

```scala
shouldRunAnotherBatch(
  newMetadata: OffsetSeqMetadata): Boolean
```

`shouldRunAnotherBatch` is negative (`false`) by default (to indicate that another non-data batch is not required given the [OffsetSeqMetadata](../OffsetSeqMetadata.md) with the event-time watermark and the batch timestamp).

`shouldRunAnotherBatch` is used when `IncrementalExecution` is requested to [check out whether the last batch execution requires another batch](../IncrementalExecution.md#shouldRunAnotherBatch) (when `MicroBatchExecution` is requested to [run the activated streaming query](../MicroBatchExecution.md#runActivatedStream)).

## <span id="stateStoreCustomMetrics"> stateStoreCustomMetrics Internal Method

```scala
stateStoreCustomMetrics: Map[String, SQLMetric]
```

`stateStoreCustomMetrics`...FIXME

`stateStoreCustomMetrics` is used when `StateStoreWriter` is requested for the [metrics](#metrics) and [getProgress](#getProgress).
