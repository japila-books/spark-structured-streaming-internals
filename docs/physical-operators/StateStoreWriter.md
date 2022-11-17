# StateStoreWriter Physical Operators

`StateStoreWriter` is an [extension](#contract) of the [StatefulOperator](StatefulOperator.md) abstraction for [stateful physical operators](#implementations) that write to a state store.

`StateStoreWriter` operators collect [performance metrics](#metrics) for [execution progress reporting](#getProgress).

## Implementations

* [FlatMapGroupsWithStateExec](FlatMapGroupsWithStateExec.md)
* [SessionWindowStateStoreSaveExec](SessionWindowStateStoreSaveExec.md)
* [StateStoreSaveExec](StateStoreSaveExec.md)
* [StreamingDeduplicateExec](StreamingDeduplicateExec.md)
* [StreamingGlobalLimitExec](StreamingGlobalLimitExec.md)
* [StreamingSymmetricHashJoinExec](StreamingSymmetricHashJoinExec.md)

## <span id="metrics"> Performance Metrics

ID | Name
---|----------
 [allRemovalsTimeMs](#allRemovalsTimeMs) | time to remove
 [allUpdatesTimeMs](#allUpdatesTimeMs) | time to update
 [commitTimeMs](#commitTimeMs) | time to commit changes
 numOutputRows | number of output rows
 numRemovedStateRows | number of removed state rows
 [numRowsDroppedByWatermark](#numRowsDroppedByWatermark) | number of rows which are dropped by watermark
 numShufflePartitions | number of shuffle partitions
 [numStateStoreInstances](#numStateStoreInstances) | number of state store instances
 [numTotalStateRows](#numTotalStateRows) | number of total state rows
 [numUpdatedStateRows](#numUpdatedStateRows) | number of updated state rows
 [statefulOperatorCustomMetrics](#statefulOperatorCustomMetrics) |
 stateMemory | memory used by state
 [stateStoreCustomMetrics](#stateStoreCustomMetrics) |

### <span id="numRowsDroppedByWatermark"> number of rows which are dropped by watermark

Incremented in [applyRemovingRowsOlderThanWatermark](#applyRemovingRowsOlderThanWatermark) (to drop late rows based on a watermark)

Reported in web UI as [Aggregated Number Of Rows Dropped By Watermark](../webui/StreamingQueryStatisticsPage.md#generateAggregatedStateOperators)

Reported as [numRowsDroppedByWatermark](../monitoring/StateOperatorProgress.md#numRowsDroppedByWatermark) when [reporting progress](#getProgress)

### <span id="numStateStoreInstances"> number of state store instances

Default: `1` (and can only be greater for [StreamingSymmetricHashJoinExec](StreamingSymmetricHashJoinExec.md))

Updated using [setOperatorMetrics](#setOperatorMetrics)

Reported as [numStateStoreInstances](../monitoring/StateOperatorProgress.md#numStateStoreInstances) when [reporting progress](#getProgress)

### <span id="numTotalStateRows"> number of total state rows

[Number of the keys](../stateful-stream-processing/StateStoreMetrics.md#numKeys) across all [state stores](../stateful-stream-processing/StateStore.md):

* For [unary stateful operators](#implementations), [updated](#setStoreMetrics) based on the [numKeys](../stateful-stream-processing/StateStoreMetrics.md#numKeys) metric of a [StateStore](../stateful-stream-processing/StateStore.md)
* For [StreamingSymmetricHashJoinExec](StreamingSymmetricHashJoinExec.md), updated at [onOutputCompletion](StreamingSymmetricHashJoinExec.md#onOutputCompletion) (of [processPartitions](StreamingSymmetricHashJoinExec.md#processPartitions))

Reported as [numRowsTotal](../monitoring/StateOperatorProgress.md#numRowsTotal) when [reporting progress](#getProgress)

Can be disabled (for performance reasons) using [spark.sql.streaming.stateStore.rocksdb.trackTotalNumberOfRows](../configuration-properties.md#spark.sql.streaming.stateStore.rocksdb.trackTotalNumberOfRows)

### <span id="numUpdatedStateRows"> number of updated state rows

Number of state rows (_entries_) that were updated or removed (for which [StateManager](../arbitrary-stateful-streaming-aggregation/StateManager.md#putState), [StreamingAggregationStateManager](../streaming-aggregation/StreamingAggregationStateManager.md#put) or [StreamingSessionWindowStateManager](../stateful-stream-processing/StreamingSessionWindowStateManager.md#updateSessions) were requested to put or update a state value)

State rows are stored or updated for the keys in the result rows of an upstream physical operator.

Displayed in [Structured Streaming UI](../webui/index.md) as [Aggregated Number Of Updated State Rows](../webui/StreamingQueryStatisticsPage.md#aggregated-number-of-updated-state-rows)

Reported as [numRowsUpdated](../monitoring/StateOperatorProgress.md#numRowsUpdated) when [reporting progress](#getProgress)

!!! note "StreamingSymmetricHashJoinExec"
    For [StreamingSymmetricHashJoinExec](StreamingSymmetricHashJoinExec.md), the metric is a sum of the left and right side's `numUpdatedStateRows`.

### <span id="commitTimeMs"> time to commit changes

Time for a [StateStore](../stateful-stream-processing/StateStore.md) to [commit state changes](../stateful-stream-processing/StateStore.md#commit)

Reported as [commitTimeMs](../monitoring/StateOperatorProgress.md#commitTimeMs) when [reporting progress](#getProgress)

### <span id="allRemovalsTimeMs"> time to remove

[Time taken](#timeTakenMs) to...FIXME

Reported as [allRemovalsTimeMs](../monitoring/StateOperatorProgress.md#allRemovalsTimeMs) when [reporting progress](#getProgress)

### <span id="allUpdatesTimeMs"> time to update

[Time taken](#timeTakenMs) to read the input rows and [store them in a state store](../streaming-aggregation/StreamingAggregationStateManager.md#put) (possibly dropping expired rows per [watermarkPredicateForData](WatermarkSupport.md#watermarkPredicateForData) predicate)

Reported as [allUpdatesTimeMs](../monitoring/StateOperatorProgress.md#allUpdatesTimeMs) when [reporting progress](#getProgress)

!!! note "number of rows which are dropped by watermark"
    Use [number of rows which are dropped by watermark](#numRowsDroppedByWatermark) for the number of rows dropped (per the [watermarkPredicateForData](WatermarkSupport.md#watermarkPredicateForData)).

!!! note "number of updated state rows"
    Use [number of updated state rows](#numUpdatedStateRows) for the number of rows stored in a state store.

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

## <span id="getProgress"> Reporting Operator Progress

```scala
getProgress(): StateOperatorProgress
```

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
 customMetrics | Current values of the custom metrics ([stateStoreCustomMetrics](#stateStoreCustomMetrics) and [statefulOperatorCustomMetrics](#statefulOperatorCustomMetrics))

---

`getProgress` is used when:

* `ProgressReporter` is requested to [extractStateOperatorMetrics](../ProgressReporter.md#extractStateOperatorMetrics) (when `MicroBatchExecution` is requested to [run the activated streaming query](../micro-batch-execution/MicroBatchExecution.md#runActivatedStream))

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

`statefulOperatorCustomMetrics` converts the [customStatefulOperatorMetrics](#customStatefulOperatorMetrics) to pairs of `name`s and `SQLMetric`s ([Spark SQL]({{ book.spark_sql }}/physical-operators/SQLMetric)).

---

`statefulOperatorCustomMetrics` is used when:

* `StateStoreWriter` is requested for the [performance metrics](#metrics) and a [progress](#getProgress)

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

## <span id="applyRemovingRowsOlderThanWatermark"> Dropping Late Rows (Older Than Watermark)

```scala
applyRemovingRowsOlderThanWatermark(
  iter: Iterator[InternalRow],
  predicateDropRowByWatermark: BasePredicate): Iterator[InternalRow]
```

`applyRemovingRowsOlderThanWatermark` filters out (_drops_) late rows based on the given `predicateDropRowByWatermark` (i.e., when holds `true`).

Every time a row is dropped [number of rows which are dropped by watermark](#numRowsDroppedByWatermark) metric is incremented.

---

`applyRemovingRowsOlderThanWatermark` is used when:

* `FlatMapGroupsWithStateExec` is requested to [processDataWithPartition](FlatMapGroupsWithStateExec.md#processDataWithPartition)
* `StateStoreSaveExec` is [executed](StateStoreSaveExec.md#doExecute) (for `Append` and `Update` output modes)
* `StreamingDeduplicateExec` is [executed](StreamingDeduplicateExec.md#doExecute)
* `StreamingSymmetricHashJoinExec.OneSideHashJoiner` is requested to [storeAndJoinWithOtherSide](../streaming-join/OneSideHashJoiner.md#storeAndJoinWithOtherSide)
