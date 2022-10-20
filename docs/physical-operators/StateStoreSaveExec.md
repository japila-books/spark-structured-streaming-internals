# StateStoreSaveExec Physical Operator

`StateStoreSaveExec` is a unary physical operator ([Spark SQL]({{ book.spark_sql }}/physical-operators/UnaryExecNode)) that [saves (writes) a streaming state](StateStoreWriter.md) (to a [state store](../stateful-stream-processing/StateStore.md)) with [support for streaming watermark](WatermarkSupport.md).

`StateStoreSaveExec` is one of the physical operators used for [streaming aggregation](../streaming-aggregation/index.md).

![StateStoreSaveExec and StatefulAggregationStrategy](../images/StateStoreSaveExec-StatefulAggregationStrategy.png)

## Creating Instance

`StateStoreSaveExec` takes the following to be created:

* <span id="keyExpressions"> Grouping Key `Attribute`s ([Spark SQL]({{ book.spark_sql }}/expressions/Attribute))
* <span id="stateInfo"> [StatefulOperatorStateInfo](../stateful-stream-processing/StatefulOperatorStateInfo.md)
* <span id="outputMode"> [OutputMode](../OutputMode.md)
* <span id="eventTimeWatermark"> [Event-time Watermark](../streaming-watermark/index.md)
* <span id="stateFormatVersion"> [spark.sql.streaming.aggregation.stateFormatVersion](../configuration-properties.md#spark.sql.streaming.aggregation.stateFormatVersion)
* <span id="child"> Child Physical Operator ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan))

`StateStoreSaveExec` is created when:

* `StatefulAggregationStrategy` execution planning strategy is requested to [plan a streaming aggregation](../execution-planning-strategies/StatefulAggregationStrategy.md#planStreamingAggregation)
* `IncrementalExecution` is [created](../IncrementalExecution.md#state)

## <span id="stateManager"> StreamingAggregationStateManager

`StateStoreSaveExec` [creates a StreamingAggregationStateManager](../streaming-aggregation/StreamingAggregationStateManager.md#createStateManager) when [created](#creating-instance).

The `StreamingAggregationStateManager` is created using the [grouping key expressions](#keyExpressions) and the output schema of the [child physical operator](#child).

The `StreamingAggregationStateManager` is used in [doExecute](#doExecute) for the following:

* [State Value Schema](../streaming-aggregation/StreamingAggregationStateManager.md#getStateValueSchema) to [mapPartitionsWithStateStore](../stateful-stream-processing/StateStoreOps.md#mapPartitionsWithStateStore)

For [Complete](#outputMode):

1. [Store](../streaming-aggregation/StreamingAggregationStateManager.md#put) all input rows
1. [Commit the state changes](../streaming-aggregation/StreamingAggregationStateManager.md#commit)
1. [Fetch the values](../streaming-aggregation/StreamingAggregationStateManager.md#values)

For [Append](#outputMode):

1. [Store](../streaming-aggregation/StreamingAggregationStateManager.md#put) all updated rows
1. [Fetch all the key-value pairs](../streaming-aggregation/StreamingAggregationStateManager.md#iterator)
1. [Remove old "watermarked" aggregates](../streaming-aggregation/StreamingAggregationStateManager.md#remove) for all removed pairs
1. [Commit the state changes](../streaming-aggregation/StreamingAggregationStateManager.md#commit)

For [Update](#outputMode):

1. [Store](../streaming-aggregation/StreamingAggregationStateManager.md#put) all updated rows
1. [removeKeysOlderThanWatermark](WatermarkSupport.md#removeKeysOlderThanWatermark) of old "watermarked" aggregates
1. [Commit the state changes](../streaming-aggregation/StreamingAggregationStateManager.md#commit)

## <span id="requiredChildDistribution"> Required Child Output Distribution

```scala
requiredChildDistribution: Seq[Distribution]
```

`requiredChildDistribution` is part of the `SparkPlan` ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan/#requiredChildDistribution)) abstraction.

---

`requiredChildDistribution`...FIXME

## Performance Metrics

`StateStoreSaveExec` uses the performance metrics as the other stateful physical operators that [write to a state store](StateStoreWriter.md#metrics).

![StateStoreSaveExec in web UI (Details for Query)](../images/StateStoreSaveExec-webui-query-details.png)

### <span id="allUpdatesTimeMs"> total time to update rows

[Time taken](StateStoreWriter.md#timeTakenMs) to read the input rows and [store them in a state store](../streaming-aggregation/StreamingAggregationStateManager.md#put) (possibly filtering out expired "watermarked" rows per [watermarkPredicateForData](WatermarkSupport.md#watermarkPredicateForData) predicate)

The number of rows stored is the [number of updated state rows](#numUpdatedStateRows) metric

* For [Append](#outputMode) output mode, the [time taken](StateStoreWriter.md#timeTakenMs) to filter out expired rows (per the required [watermarkPredicateForData](WatermarkSupport.md#watermarkPredicateForData) predicate) and the [StreamingAggregationStateManager](#stateManager) to [store rows in a state store](../streaming-aggregation/StreamingAggregationStateManager.md#put)

* For [Complete](#outputMode) output mode, the [time taken](StateStoreWriter.md#timeTakenMs) to go over all the input rows and request the [StreamingAggregationStateManager](#stateManager) to [store rows in a state store](../streaming-aggregation/StreamingAggregationStateManager.md#put)

* For [Update](#outputMode) output mode, the [time taken](StateStoreWriter.md#timeTakenMs) to filter out expired rows (per the optional [watermarkPredicateForData](WatermarkSupport.md#watermarkPredicateForData) predicate) and the [StreamingAggregationStateManager](#stateManager) to [store rows in a state store](../streaming-aggregation/StreamingAggregationStateManager.md#put)

### <span id="allRemovalsTimeMs"> total time to remove rows

* For [Append](#outputMode) output mode, the time taken for the [StreamingAggregationStateManager](#stateManager) to [remove all expired entries from a state store](../streaming-aggregation/StreamingAggregationStateManager.md#remove) (per [watermarkPredicateForKeys](WatermarkSupport.md#watermarkPredicateForKeys) predicate) that is the total time of iterating over [all entries in the state store](../streaming-aggregation/StreamingAggregationStateManager.md#iterator) (the number of entries [removed from a state store](../streaming-aggregation/StreamingAggregationStateManager.md#remove) is the difference between the number of output rows of the [child](#child) operator and the [number of total state rows](#numTotalStateRows) metric)

* For [Complete](#outputMode) output mode, always `0`

* For [Update](#outputMode) output mode, the [time taken](StateStoreWriter.md#timeTakenMs) for the [StreamingAggregationStateManager](#stateManager) to [remove all expired entries from a state store](WatermarkSupport.md#removeKeysOlderThanWatermark) (per [watermarkPredicateForKeys](WatermarkSupport.md#watermarkPredicateForKeys) predicate)

### <span id="commitTimeMs"> time to commit changes

[Time taken](StateStoreWriter.md#timeTakenMs) for the [StreamingAggregationStateManager](#stateManager) to [commit changes to a state store](../streaming-aggregation/StreamingAggregationStateManager.md#commit)

---

When requested to [commit changes to a state store](../streaming-aggregation/StreamingAggregationStateManager.md#commit), a [StreamingAggregationStateManager](#stateManager) (as [StreamingAggregationStateManagerBaseImpl](../streaming-aggregation/StreamingAggregationStateManagerBaseImpl.md)) requests the given [StateStore](../stateful-stream-processing/StateStore.md) to [commit state changes](../stateful-stream-processing/StateStore.md#commit).

For [RocksDBStateStore](../stateful-stream-processing/RocksDBStateStore.md), it means for [RocksDB](../stateful-stream-processing/RocksDBStateStoreProvider.md#rocksDB) to [commit state changes](../stateful-stream-processing/RocksDB.md#commit) which is made up of the time-tracked phases that are available using the [performance metrics](../stateful-stream-processing/RocksDBMetrics.md#lastCommitLatencyMs).

In other words, the total of the [time-tracked phases](../stateful-stream-processing/RocksDBMetrics.md#lastCommitLatencyMs) is close approximation of this metric (there are some file-specific ops though that contribute to the metric but are not part of the phases).

### <span id="numOutputRows"> number of output rows

* For [Append](#outputMode) output mode, the metric does not seem to be used

* For [Complete](#outputMode) output mode, the number of rows in a [StateStore](../stateful-stream-processing/StateStore.md) (i.e. all [values](../streaming-aggregation/StreamingAggregationStateManager.md#values) in a [StateStore](../stateful-stream-processing/StateStore.md) in the [StreamingAggregationStateManager](#stateManager) that should be equivalent to the [number of total state rows](#numTotalStateRows) metric)

* For [Update](#outputMode) output mode, the number of rows that the [StreamingAggregationStateManager](#stateManager) was requested to [store in a state store](../streaming-aggregation/StreamingAggregationStateManager.md#put) (that did not expire per the optional [watermarkPredicateForData](WatermarkSupport.md#watermarkPredicateForData) predicate) that is equivalent to the [number of updated state rows](#numUpdatedStateRows) metric)

### <span id="numTotalStateRows"> number of total state rows

[number of total state rows](StateStoreWriter.md#numTotalStateRows)

### <span id="numUpdatedStateRows"> number of updated state rows

Number of the entries that [were stored as updates in a state store](../stateful-stream-processing/StateStore.md#put) in a trigger and for the keys in the result rows of the upstream physical operator (aka _numUpdatedStateRows_)

* For [Append](#outputMode) output mode, the number of input rows that have not expired yet (per the required [watermarkPredicateForData](WatermarkSupport.md#watermarkPredicateForData) predicate) and that the [StreamingAggregationStateManager](#stateManager) was requested to [store in a state store](../streaming-aggregation/StreamingAggregationStateManager.md#put) (the time taken is the [total time to update rows](#allUpdatesTimeMs) metric)

* For [Complete](#outputMode) output mode, the number of input rows (which should be exactly the number of output rows from the [child operator](#child))

* For [Update](#outputMode) output mode, the number of rows that the [StreamingAggregationStateManager](#stateManager) was requested to [store in a state store](../streaming-aggregation/StreamingAggregationStateManager.md#put) (that did not expire per the optional [watermarkPredicateForData](WatermarkSupport.md#watermarkPredicateForData) predicate) that is equivalent to the [number of output rows](#numOutputRows) metric)

Corresponds to `numRowsUpdated` attribute in `stateOperators` in [StreamingQueryProgress](../monitoring/StreamingQueryProgress.md) (and is available as `sq.lastProgress.stateOperators` for an operator).

### <span id="stateMemory"> memory used by state

Estimated memory used by a [StateStore](../stateful-stream-processing/StateStore.md) (aka _stateMemory_) after `StateStoreSaveExec` finished [execution](#doExecute) (per the [StateStoreMetrics](../stateful-stream-processing/StateStoreMetrics.md#memoryUsedBytes) of the [StateStore](../stateful-stream-processing/StateStore.md#metrics))

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.StateStoreSaveExec` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.execution.streaming.StateStoreSaveExec=ALL
```

Refer to [Logging](../spark-logging.md).

<!---
## Review Me

The optional properties, i.e. the <<stateInfo, StatefulOperatorStateInfo>>, the <<outputMode, output mode>>, and the <<eventTimeWatermark, event-time watermark>>, are initially undefined when `StateStoreSaveExec` is <<creating-instance, created>>. `StateStoreSaveExec` is updated to hold execution-specific configuration when `IncrementalExecution` is requested to [prepare the logical plan (of a streaming query) for execution](../IncrementalExecution.md#preparing-for-execution) (when the [state preparation rule](../IncrementalExecution.md#state) is executed).

![StateStoreSaveExec and IncrementalExecution](../images/StateStoreSaveExec-IncrementalExecution.png)

!!! note
    Unlike [StateStoreRestoreExec](StateStoreRestoreExec.md) operator, `StateStoreSaveExec` takes [output mode](#outputMode) and [event time watermark](#eventTimeWatermark) when [created](#creating-instance).

When <<doExecute, executed>>, `StateStoreSaveExec` [creates a StateStoreRDD to map over partitions](../stateful-stream-processing/StateStoreOps.md#mapPartitionsWithStateStore) with `storeUpdateFunction` that manages the `StateStore`.

![StateStoreSaveExec creates StateStoreRDD](../images/StateStoreSaveExec-StateStoreRDD.png)

![StateStoreSaveExec and StateStoreRDD (after streamingBatch.toRdd.count)](../images/StateStoreSaveExec-StateStoreRDD-count.png)

!!! note
    The number of partitions of [StateStoreRDD](../stateful-stream-processing/StateStoreOps.md#mapPartitionsWithStateStore) (and hence the number of Spark tasks) is what was defined for the <<child, child>> physical plan.

    There will be that many `StateStores` as there are partitions in `StateStoreRDD`.

When <<doExecute, executed>>, `StateStoreSaveExec` executes the <<child, child>> physical operator and [creates a StateStoreRDD](../stateful-stream-processing/StateStoreOps.md#mapPartitionsWithStateStore) (with `storeUpdateFunction` specific to the output mode).

[[stateManager]]
`StateStoreRestoreExec` uses a [StreamingAggregationStateManager](../streaming-aggregation/StreamingAggregationStateManager.md) (that is [created](../streaming-aggregation/StreamingAggregationStateManager.md#createStateManager) for the [keyExpressions](#keyExpressions), the output of the [child](#child) physical operator and the [stateFormatVersion](#stateFormatVersion)).

## <span id="doExecute"> Executing Physical Operator

```scala
doExecute(): RDD[InternalRow]
```

`doExecute` is part of the `SparkPlan` abstraction (Spark SQL).

Internally, `doExecute` initializes [metrics](StateStoreWriter.md#metrics).

NOTE: `doExecute` requires that the optional <<outputMode, outputMode>> is at this point defined (that should have happened when `IncrementalExecution` [had prepared a streaming aggregation for execution](../IncrementalExecution.md#preparations)).

`doExecute` executes <<child, child>> physical operator and [creates a StateStoreRDD](../stateful-stream-processing/StateStoreOps.md#mapPartitionsWithStateStore) with `storeUpdateFunction` that:

1. Generates an unsafe projection to access the key field (using <<keyExpressions, keyExpressions>> and the output schema of <<child, child>>).

1. Branches off per <<outputMode, output mode>>: <<doExecute-Append, Append>>, <<doExecute-Complete, Complete>> and <<doExecute-Update, Update>>.

`doExecute` throws an `UnsupportedOperationException` when executed with an invalid <<outputMode, output mode>>:

```text
Invalid output mode: [outputMode]
```

==== [[doExecute-Append]] Append Output Mode

NOTE: [Append](../OutputMode.md#Append) is the default output mode when not specified explicitly.

NOTE: `Append` output mode requires that a streaming query defines [event-time watermark](../streaming-watermark/index.md) (e.g. using [withWatermark](../operators/withWatermark.md) operator) on the event-time column that is used in aggregation (directly or using [window](../operators/window.md) standard function).

For [Append](../OutputMode.md#Append) output mode, `doExecute` does the following:

1. Finds late (aggregate) rows from <<child, child>> physical operator (that have expired per [watermark](WatermarkSupport.md#watermarkPredicateForData))

1. [Stores the late rows in the state store](../stateful-stream-processing/StateStore.md#put) and increments the <<numUpdatedStateRows, numUpdatedStateRows>> metric

1. [Gets all the added (late) rows from the state store](../stateful-stream-processing/StateStore.md#getRange)

1. Creates an iterator that [removes the late rows from the state store](../stateful-stream-processing/StateStore.md#remove) when requested the next row and in the end [commits the state updates](../stateful-stream-processing/StateStore.md#commit)

TIP: Refer to <<spark-sql-streaming-demo-watermark-aggregation-append.md#, Demo: Streaming Watermark with Aggregation in Append Output Mode>> for an example of `StateStoreSaveExec` with `Append` output mode.

CAUTION: FIXME When is "Filtering state store on:" printed out?

---

1. Uses [watermarkPredicateForData](WatermarkSupport.md#watermarkPredicateForData) predicate to exclude matching rows and (like in [Complete](#doExecute-Complete) output mode) [stores all the remaining rows](../stateful-stream-processing/StateStore.md#put) in `StateStore`.

1. (like in <<doExecute-Complete, Complete>> output mode) While storing the rows, increments <<numUpdatedStateRows, numUpdatedStateRows>> metric (for every row) and records the total time in <<allUpdatesTimeMs, allUpdatesTimeMs>> metric.

1. [Takes all the rows](../stateful-stream-processing/StateStore.md#getRange) from `StateStore` and returns a `NextIterator` that:

* In `getNext`, finds the first row that matches [watermarkPredicateForKeys](WatermarkSupport.md#watermarkPredicateForKeys) predicate, [removes it](../stateful-stream-processing/StateStore.md#remove) from `StateStore`, and returns it back.
+
If no row was found, `getNext` also marks the iterator as finished.

* In `close`, records the time to iterate over all the rows in <<allRemovalsTimeMs, allRemovalsTimeMs>> metric, [commits the updates](../stateful-stream-processing/StateStore.md#commit) to `StateStore` followed by recording the time in <<commitTimeMs, commitTimeMs>> metric and [recording StateStore metrics](StateStoreWriter.md#setStoreMetrics).

### <span id="doExecute-Complete"> Complete Output Mode

For [Complete](../OutputMode.md#Complete) output mode, `doExecute` does the following:

1. Takes all `UnsafeRow` rows (from the parent iterator)

1. [Stores the rows by key in the state store](../stateful-stream-processing/StateStore.md#put) eagerly (i.e. all rows that are available in the parent iterator before proceeding)

1. [Commits the state updates](../stateful-stream-processing/StateStore.md#commit)

1. In the end, [reads the key-row pairs from the state store](../stateful-stream-processing/StateStore.md#iterator) and passes the rows along (i.e. to the following physical operator)

The number of keys stored in the state store is recorded in <<numUpdatedStateRows, numUpdatedStateRows>> metric.

NOTE: In `Complete` output mode the <<numOutputRows, numOutputRows>> metric is exactly the <<numTotalStateRows, numTotalStateRows>> metric.

TIP: Refer to <<spark-sql-streaming-StateStoreSaveExec-Complete.md#, Demo: StateStoreSaveExec with Complete Output Mode>> for an example of `StateStoreSaveExec` with `Complete` output mode.

---

1. [Stores all rows](../stateful-stream-processing/StateStore.md#put) (as `UnsafeRow`) in `StateStore`.

1. While storing the rows, increments <<numUpdatedStateRows, numUpdatedStateRows>> metric (for every row) and records the total time in <<allUpdatesTimeMs, allUpdatesTimeMs>> metric.

1. Records `0` in <<allRemovalsTimeMs, allRemovalsTimeMs>> metric.

1. [Commits the state updates](../stateful-stream-processing/StateStore.md#commit) to `StateStore` and records the time in <<commitTimeMs, commitTimeMs>> metric.

1. [Records StateStore metrics](StateStoreWriter.md#setStoreMetrics)

1. In the end, [takes all the rows stored](../stateful-stream-processing/StateStore.md#iterator) in `StateStore` and increments [numOutputRows](#numOutputRows) metric.

### <span id="doExecute-Update"> Update Output Mode

For [Update](../OutputMode.md#Update) output mode, `doExecute` returns an iterator that filters out late aggregate rows (per [watermark](WatermarkSupport.md#watermarkPredicateForData) if defined) and [stores the "young" rows in the state store](../stateful-stream-processing/StateStore.md#put) (one by one, i.e. every `next`).

With no more rows available, that [removes the late rows from the state store](../stateful-stream-processing/StateStore.md#remove) (all at once) and [commits the state updates](../stateful-stream-processing/StateStore.md#commit).

TIP: Refer to <<spark-sql-streaming-StateStoreSaveExec-Update.md#, Demo: StateStoreSaveExec with Update Output Mode>> for an example of `StateStoreSaveExec` with `Update` output mode.

---

`doExecute` returns `Iterator` of rows that uses [watermarkPredicateForData](WatermarkSupport.md#watermarkPredicateForData) predicate to filter out late rows.

In `hasNext`, when rows are no longer available:

1. Records the total time to iterate over all the rows in <<allUpdatesTimeMs, allUpdatesTimeMs>> metric.

1. [removeKeysOlderThanWatermark](WatermarkSupport.md#removeKeysOlderThanWatermark) and records the time in <<allRemovalsTimeMs, allRemovalsTimeMs>> metric.

1. [Commits the updates](../stateful-stream-processing/StateStore.md#commit) to `StateStore` and records the time in <<commitTimeMs, commitTimeMs>> metric.

1. [Records StateStore metrics](StateStoreWriter.md#setStoreMetrics)

In `next`, [stores a row](../stateful-stream-processing/StateStore.md#put) in `StateStore` and increments [numOutputRows](#numOutputRows) and [numUpdatedStateRows](#numUpdatedStateRows) metrics.

=== [[shouldRunAnotherBatch]] Checking Out Whether Last Batch Execution Requires Another Non-Data Batch or Not -- `shouldRunAnotherBatch` Method

```scala
shouldRunAnotherBatch(
  newMetadata: OffsetSeqMetadata): Boolean
```

`shouldRunAnotherBatch` is positive (`true`) when all of the following are met:

* <<outputMode, Output mode>> is either [Append](../OutputMode.md#Append) or [Update](../OutputMode.md#Update)

* <<eventTimeWatermark, Event-time watermark>> is defined and is older (below) the current [event-time watermark](../OffsetSeqMetadata.md#batchWatermarkMs) (of the given `OffsetSeqMetadata`)

Otherwise, `shouldRunAnotherBatch` is negative (`false`).

`shouldRunAnotherBatch` is part of the [StateStoreWriter](StateStoreWriter.md#shouldRunAnotherBatch) abstraction.
-->
