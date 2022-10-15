# StateStoreWriter Physical Operators

`StateStoreWriter` is an [extension](#contract) of the [StatefulOperator](StatefulOperator.md) abstraction for [stateful physical operators](#implementations) that write to a state store and collect the [write metrics](#metrics) for [execution progress reporting](#getProgress).

## Implementations

* [FlatMapGroupsWithStateExec](FlatMapGroupsWithStateExec.md)
* [SessionWindowStateStoreSaveExec](SessionWindowStateStoreSaveExec.md)
* [StateStoreSaveExec](StateStoreSaveExec.md)
* [StreamingDeduplicateExec](StreamingDeduplicateExec.md)
* [StreamingGlobalLimitExec](StreamingGlobalLimitExec.md)
* [StreamingSymmetricHashJoinExec](StreamingSymmetricHashJoinExec.md)

## <span id="metrics"> Performance Metrics

ID | Name
---------|----------
 [statefulOperatorCustomMetrics](#statefulOperatorCustomMetrics) |
 numOutputRows | number of output rows
 numRowsDroppedByWatermark | number of rows which are dropped by watermark
 [numTotalStateRows](#numTotalStateRows) | number of total state rows
 numUpdatedStateRows | number of updated state rows
 allUpdatesTimeMs | time to update
 numRemovedStateRows | number of removed state rows
 allRemovalsTimeMs | time to remove
 [commitTimeMs](#commitTimeMs) | time to commit changes
 stateMemory | memory used by state
 numShufflePartitions | number of shuffle partitions
 [numStateStoreInstances](#numStateStoreInstances) | number of state store instances
 [stateStoreCustomMetrics](#stateStoreCustomMetrics) |

### <span id="commitTimeMs"> time to commit changes

Time for a [StateStore](../stateful-stream-processing/StateStore.md) to [commit state changes](../stateful-stream-processing/StateStore.md#commit)

Measured when the following physical operators are executed:

* [FlatMapGroupsWithStateExec](FlatMapGroupsWithStateExec.md#processDataWithPartition)
* [SessionWindowStateStoreSaveExec](SessionWindowStateStoreSaveExec.md#doExecute)
* [StateStoreSaveExec](StateStoreSaveExec.md#doExecute)
* [StreamingDeduplicateExec](StreamingDeduplicateExec.md#doExecute)
* [StreamingGlobalLimitExec](StreamingGlobalLimitExec.md#doExecute)
* [StreamingSymmetricHashJoinExec](StreamingSymmetricHashJoinExec.md#processPartitions)

Reported as [commitTimeMs](../monitoring/StateOperatorProgress.md#commitTimeMs) when [reporting progress](#getProgress)

### <span id="numStateStoreInstances"> numStateStoreInstances

Updated in [setOperatorMetrics](#setOperatorMetrics)

Reported as [numStateStoreInstances](../monitoring/StateOperatorProgress.md#numStateStoreInstances) when [reporting progress](#getProgress)

### <span id="numTotalStateRows"> numTotalStateRows

Sum of the [number of keys](../stateful-stream-processing/StateStoreMetrics.md#numKeys) of all state stores

Updated in [setStoreMetrics](#setStoreMetrics) based on the [numKeys](../stateful-stream-processing/StateStoreMetrics.md#numKeys) metric of a [StateStore](../stateful-stream-processing/StateStore.md).

Reported as [numRowsTotal](../monitoring/StateOperatorProgress.md#numRowsTotal) when [reporting progress](#getProgress)

## <span id="shortName"> Short Name

```scala
shortName: String
```

`shortName` is `defaultName` (and is expected to be overriden by the [implementations](#implementations)).

---

`shortName` is used when:

* `StateStoreWriter` is requested for a [StateOperatorProgress](#getProgress)

## <span id="customStatefulOperatorMetrics"> Custom Metrics

```scala
customStatefulOperatorMetrics: Seq[StatefulOperatorCustomMetric]
```

`customStatefulOperatorMetrics` is empty (and is expected to be overriden by the [implementations](#implementations)).

!!! note "StreamingDeduplicateExec"
    [StreamingDeduplicateExec](StreamingDeduplicateExec.md) physical operator is the only implementation with [custom metrics](StreamingDeduplicateExec.md#customStatefulOperatorMetrics).

---

`customStatefulOperatorMetrics` is used when:

* `StateStoreWriter` is requested for the [custom metrics of this stateful operator](#statefulOperatorCustomMetrics)

## <span id="getProgress"> Reporting Progress

```scala
getProgress(): StateOperatorProgress
```

`getProgress` collects the current values of the custom metrics ([stateStoreCustomMetrics](#stateStoreCustomMetrics) and [statefulOperatorCustomMetrics](#statefulOperatorCustomMetrics)).

`getProgress` creates a [StateOperatorProgress](../monitoring/StateOperatorProgress.md) with the [shortName](#shortName) and the following metrics:

Property | Metric
---------|-------
 numRowsTotal | [numTotalStateRows](#numTotalStateRows)
 numRowsUpdated | [numUpdatedStateRows](#numUpdatedStateRows)
 allUpdatesTimeMs | [allUpdatesTimeMs](#allUpdatesTimeMs)
 numRowsRemoved | [numRemovedStateRows](#numRemovedStateRows)
 allRemovalsTimeMs | [allRemovalsTimeMs](#allRemovalsTimeMs)
 commitTimeMs | [commitTimeMs](#commitTimeMs)
 memoryUsedBytes | [stateMemory](#stateMemory)
 numRowsDroppedByWatermark | [numRowsDroppedByWatermark](#numRowsDroppedByWatermark)
 numShufflePartitions | [numShufflePartitions](#numShufflePartitions)
 numStateStoreInstances | [numStateStoreInstances](#numStateStoreInstances)
 customMetrics | [stateStoreCustomMetrics](#stateStoreCustomMetrics) and [statefulOperatorCustomMetrics](#statefulOperatorCustomMetrics)

---

`getProgress` is used when:

* `ProgressReporter` is requested to [extractStateOperatorMetrics](../monitoring/ProgressReporter.md#extractStateOperatorMetrics) (when `MicroBatchExecution` is requested to [run the activated streaming query](../micro-batch-execution/MicroBatchExecution.md#runActivatedStream))

## <span id="shouldRunAnotherBatch"> Does Last Batch Execution Require Extra Non-Data Batch

```scala
shouldRunAnotherBatch(
  newMetadata: OffsetSeqMetadata): Boolean
```

`shouldRunAnotherBatch` is negative (`false`) by default (to indicate that another non-data batch is not required given the [OffsetSeqMetadata](../OffsetSeqMetadata.md) with the event-time watermark and the batch timestamp).

`shouldRunAnotherBatch` is used when `IncrementalExecution` is requested to [check out whether the last batch execution requires another batch](../IncrementalExecution.md#shouldRunAnotherBatch) (when `MicroBatchExecution` is requested to [run the activated streaming query](../micro-batch-execution/MicroBatchExecution.md#runActivatedStream)).

## Custom Metrics

### <span id="statefulOperatorCustomMetrics"> Stateful Operator

```scala
statefulOperatorCustomMetrics: Map[String, SQLMetric]
```

`statefulOperatorCustomMetrics` is the [customStatefulOperatorMetrics](#customStatefulOperatorMetrics).

---

`statefulOperatorCustomMetrics` is used when:

* `StateStoreWriter` is requested for the [metrics](#metrics) and a [progress](#getProgress)

### <span id="stateStoreCustomMetrics"> StateStore

```scala
stateStoreCustomMetrics: Map[String, SQLMetric]
```

`stateStoreCustomMetrics` [creates a StateStoreProvider](../stateful-stream-processing/StateStoreProvider.md#create) (based on [spark.sql.streaming.stateStore.providerClass](../configuration-properties.md#spark.sql.streaming.stateStore.providerClass)).

`stateStoreCustomMetrics` requests the `StateStoreProvider` for [supportedCustomMetrics](../stateful-stream-processing/StateStoreProvider.md#supportedCustomMetrics).

---

`stateStoreCustomMetrics` is used when:

* `StateStoreWriter` is requested for the [metrics](#metrics) and a [progress](#getProgress)

## Recording Metrics

### <span id="setOperatorMetrics"> Stateful Operator

```scala
setOperatorMetrics(
  numStateStoreInstances: Int = 1): Unit
```

`setOperatorMetrics` updates the following metrics:

* Increments [numShufflePartitions](#numShufflePartitions)
* Adds the given `numStateStoreInstances` to [numStateStoreInstances](#numStateStoreInstances) metric

---

`setOperatorMetrics` is used when the following physical operators are executed:

* [FlatMapGroupsWithStateExec](FlatMapGroupsWithStateExec.md#processDataWithPartition)
* [StateStoreSaveExec](StateStoreSaveExec.md)
* [SessionWindowStateStoreSaveExec](SessionWindowStateStoreSaveExec.md)
* [StreamingDeduplicateExec](StreamingDeduplicateExec.md)
* [StreamingGlobalLimitExec](StreamingGlobalLimitExec.md)
* [StreamingSymmetricHashJoinExec](StreamingSymmetricHashJoinExec.md#processPartitions)

### <span id="setStoreMetrics"> StateStore

```scala
setStoreMetrics(
  store: StateStore): Unit
```

`setStoreMetrics` requests the given [StateStore](../stateful-stream-processing/StateStore.md) for the [metrics](../stateful-stream-processing/StateStore.md#metrics) and records the following metrics:

* Adds the [number of keys](../stateful-stream-processing/StateStoreMetrics.md#numKeys) to [numTotalStateRows](#numTotalStateRows) metric
* Adds the [memory used (in bytes)](../stateful-stream-processing/StateStoreMetrics.md#memoryUsedBytes) to [stateMemory](#stateMemory) metric

`setStoreMetrics` records (_adds_) the values of the [custom metrics](../stateful-stream-processing/StateStoreMetrics.md#customMetrics).

---

`setStoreMetrics` is used when the following physical operators are executed:

* [FlatMapGroupsWithStateExec](FlatMapGroupsWithStateExec.md#processDataWithPartition)
* [StateStoreSaveExec](StateStoreSaveExec.md)
* [SessionWindowStateStoreSaveExec](SessionWindowStateStoreSaveExec.md)
* [StreamingDeduplicateExec](StreamingDeduplicateExec.md)
* [StreamingGlobalLimitExec](StreamingGlobalLimitExec.md)
