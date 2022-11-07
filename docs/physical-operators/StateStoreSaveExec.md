# StateStoreSaveExec Physical Operator

`StateStoreSaveExec` is a unary physical operator ([Spark SQL]({{ book.spark_sql }}/physical-operators/UnaryExecNode)) that [saves (writes) a streaming state](StateStoreWriter.md) (to a [state store](../stateful-stream-processing/StateStore.md)) with [support for streaming watermark](WatermarkSupport.md).

`StateStoreSaveExec` is one of the physical operators used for [streaming aggregation](../streaming-aggregation/index.md).

![StateStoreSaveExec and StatefulAggregationStrategy](../images/StateStoreSaveExec-StatefulAggregationStrategy.png)

## Creating Instance

`StateStoreSaveExec` takes the following to be created:

* <span id="keyExpressions"> Grouping Key `Attribute`s ([Spark SQL]({{ book.spark_sql }}/expressions/Attribute))
* <span id="stateInfo"> [StatefulOperatorStateInfo](../stateful-stream-processing/StatefulOperatorStateInfo.md)
* [OutputMode](#outputMode)
* <span id="eventTimeWatermark"> [Event-time Watermark](../watermark/index.md)
* <span id="stateFormatVersion"> [spark.sql.streaming.aggregation.stateFormatVersion](../configuration-properties.md#spark.sql.streaming.aggregation.stateFormatVersion)
* <span id="child"> Child Physical Operator ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan))

`StateStoreSaveExec` is created when:

* `StatefulAggregationStrategy` execution planning strategy is requested to [plan a streaming aggregation](../execution-planning-strategies/StatefulAggregationStrategy.md#planStreamingAggregation)
* `IncrementalExecution` is [created](../IncrementalExecution.md#state)

### <span id="outputMode"> OutputMode

```scala
outputMode: Option[OutputMode]
```

`StateStoreSaveExec` can be given an [OutputMode](../OutputMode.md) when [created](#creating-instance).

`StateStoreSaveExec` supports all three [OutputMode](../OutputMode.md)s when [executed](#doExecute):

* [Append](#doExecute-storeUpdateFunction-Append)
* [Complete](#doExecute-storeUpdateFunction-Complete)
* [Update](#doExecute-storeUpdateFunction-Update)

For `Append` and `Update` output modes with [event-time watermark](#eventTimeWatermark) defined, `StateStoreSaveExec` can [run another batch](#shouldRunAnotherBatch) if the watermark has advanced.

`OutputMode` is undefined (`None`) by default and when `AggUtils` is requested to `planStreamingAggregation` (when [StatefulAggregationStrategy](../execution-planning-strategies/StatefulAggregationStrategy.md) execution planning strategy is requested to plan a [streaming aggregation](../streaming-aggregation/index.md)).

`OutputMode` is required for [executing StateStoreSaveExec](#doExecute). It is specified to be the [OutputMode](../IncrementalExecution.md#outputMode) of the [IncrementalExecution](../IncrementalExecution.md) (when [state preparation rule](../IncrementalExecution.md#state) is executed and fills in the execution-specific configuration).

## <span id="shortName"> Short Name

```scala
shortName: String
```

`shortName` is part of the [StateStoreWriter](StateStoreWriter.md#shortName) abstraction.

---

`shortName` is the following text:

```text
stateStoreSave
```

## <span id="doExecute"> Executing Physical Operator

```scala
doExecute(): RDD[InternalRow]
```

`doExecute` is part of the `SparkPlan` ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan#doExecute)) abstraction.

---

`doExecute` creates the [metrics](StateStoreWriter.md#metrics) (to happen on the driver).

`doExecute` executes the [child physical operator](#child) (that produces a `RDD[InternalRow]`) and [creates a StateStoreRDD](../stateful-stream-processing/StateStoreOps.md#mapPartitionsWithStateStore) with the following:

mapPartitionsWithStateStore | Value
----------------------------|---------
 stateInfo | [getStateInfo](StatefulOperator.md#getStateInfo)
 keySchema | [Grouping Keys](#keyExpressions)
 valueSchema | [State Value Schema](../streaming-aggregation/StreamingAggregationStateManager.md#getStateValueSchema) of the [StreamingAggregationStateManager](#stateManager)
 numColsPrefixKey | 0
 storeCoordinator | [StateStoreCoordinator](../StreamingQueryManager.md#stateStoreCoordinator)
 storeUpdateFunction | [storeUpdateFunction](#doExecute-storeUpdateFunction)

### <span id="doExecute-storeUpdateFunction"> storeUpdateFunction

```scala
storeUpdateFunction: (StateStore, Iterator[T]) => Iterator[U]
```

`storeUpdateFunction` branches off based on the given [OutputMode](#outputMode):

* [Append](#doExecute-storeUpdateFunction-Append)
* [Complete](#doExecute-storeUpdateFunction-Complete)
* [Update](#doExecute-storeUpdateFunction-Update)

#### <span id="doExecute-storeUpdateFunction-Append"><span id="doExecute-Append"> Append

`storeUpdateFunction` [drops late rows](StateStoreWriter.md#applyRemovingRowsOlderThanWatermark) (from the input partition) using the [watermark predicate for data](WatermarkSupport.md#watermarkPredicateForData).

For every (remaining, non-late) row, `storeUpdateFunction` [stores the row](../streaming-aggregation/StreamingAggregationStateManager.md#put) in the input [StateStore](../stateful-stream-processing/StateStore.md) (using the [StreamingAggregationStateManager](#stateManager)) and increments the [number of updated state rows](#numUpdatedStateRows) metric.

`storeUpdateFunction` tracks the time taken (to drop late rows and store non-late rows) in [time to update](#allUpdatesTimeMs) metric.

??? tip "Performance Metrics"
    Monitor the following metrics:
    
    1. [number of rows which are dropped by watermark](#numRowsDroppedByWatermark)
    1. [number of updated state rows](#numUpdatedStateRows)
    1. [time to update](#allUpdatesTimeMs)

`storeUpdateFunction` requests the [StreamingAggregationStateManager](#stateManager) for [all the aggregates by grouping keys](../streaming-aggregation/StreamingAggregationStateManager.md#iterator).

`storeUpdateFunction` creates a new `NextIterator`:

* When requested for next state value (row), the iterator traverses over the key-value aggregate state pairs until the [watermark predicate for keys](WatermarkSupport.md#watermarkPredicateForKeys) holds `true` for a key.

    If so, the iterator [removes the key](../streaming-aggregation/StreamingAggregationStateManager.md#remove) from the state store (via the [StreamingAggregationStateManager](#stateManager)) and increments the [number of removed state rows](#numRemovedStateRows) and [number of output rows](#numOutputRows) metrics.
    
    The value of the removed key is returned as the next element.

* When requested to close, the iterator updates the [time to remove](#allRemovalsTimeMs) metric (to be the time to process all the state rows) and [time to commit changes](#commitTimeMs) (to be the time taken for the [StreamingAggregationStateManager](#stateManager) to [commit state changes](../streaming-aggregation/StreamingAggregationStateManager.md#commit)).

    In the end, the iterator records metrics of the [StateStore](StateStoreWriter.md#setStoreMetrics) and this [stateful operator](StateStoreWriter.md#setOperatorMetrics).

#### <span id="doExecute-storeUpdateFunction-Complete"> Complete

!!! note "FIXME"

#### <span id="doExecute-storeUpdateFunction-Update"> Update

!!! note "FIXME"

### <span id="doExecute-AssertionError"> AssertionError: outputMode has not been set

`doExecute` makes sure that [outputMode](#outputMode) is specified or throws an `AssertionError`:

```text
Incorrect planning in IncrementalExecution, outputMode has not been set
```

## <span id="stateManager"> StreamingAggregationStateManager

`StateStoreSaveExec` [creates a StreamingAggregationStateManager](../streaming-aggregation/StreamingAggregationStateManager.md#createStateManager) when [created](#creating-instance).

The `StreamingAggregationStateManager` is created using the following:

* [Grouping Keys](#keyExpressions)
* Output schema of the [child physical operator](#child)

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

`StateStoreSaveExec` is a [StateStoreWriter](StateStoreWriter.md) that defines the performance metrics for [writes to a state store](StateStoreWriter.md#metrics).

![StateStoreSaveExec in web UI (Details for Query)](../images/StateStoreSaveExec-webui-query-details.png)

### <span id="stateMemory"> memory used by state

Estimated memory used by a [StateStore](../stateful-stream-processing/StateStore.md) (aka _stateMemory_) after `StateStoreSaveExec` finished [execution](#doExecute) (per the [StateStoreMetrics](../stateful-stream-processing/StateStoreMetrics.md#memoryUsedBytes) of the [StateStore](../stateful-stream-processing/StateStore.md#metrics))

### <span id="numOutputRows"> number of output rows

=== "Append"
    Number of aggregates (state rows) that were [removed from a state store](../streaming-aggregation/StreamingAggregationStateManager.md#remove) because the [watermark predicate for keys](WatermarkSupport.md#watermarkPredicateForKeys) held `true` (i.e., the watermark threshold for the keys was reached).

    Equivalent to [number of removed state rows](#numRemovedStateRows).

=== "Complete"
    Number of rows in a [StateStore](../stateful-stream-processing/StateStore.md) (i.e., all the [values](../streaming-aggregation/StreamingAggregationStateManager.md#values) in a [StateStore](../stateful-stream-processing/StateStore.md) in the [StreamingAggregationStateManager](#stateManager))
    
    Equivalent to the [number of total state rows](#numTotalStateRows) metric

=== "Update"
    Number of rows that the [StreamingAggregationStateManager](#stateManager) was requested to [store in a state store](../streaming-aggregation/StreamingAggregationStateManager.md#put) (that did not expire per the optional [watermarkPredicateForData](WatermarkSupport.md#watermarkPredicateForData) predicate)
    
    Equivalent to the [number of updated state rows](#numUpdatedStateRows) metric

### <span id="numRemovedStateRows"> number of removed state rows

=== "Append"
    Number of aggregates (state rows) that were [removed from a state store](../streaming-aggregation/StreamingAggregationStateManager.md#remove) because the [watermark predicate for keys](WatermarkSupport.md#watermarkPredicateForKeys) held `true` (i.e., the watermark threshold for the keys was reached).

    Equivalent to [number of output rows](#numOutputRows) metric.

=== "Complete"
    Not used

=== "Update"
    Not used

See [number of removed state rows](StateStoreWriter.md#numRemovedStateRows)

### <span id="numRowsDroppedByWatermark"> number of rows which are dropped by watermark

See [number of rows which are dropped by watermark](StateStoreWriter.md#numRowsDroppedByWatermark)

### <span id="numUpdatedStateRows"> number of updated state rows

The metric is computed differently based on the given [OutputMode](#outputMode).

=== "Append"
    Number of input rows that have not expired yet (per the required [watermarkPredicateForData](WatermarkSupport.md#watermarkPredicateForData) predicate) and that the [StreamingAggregationStateManager](#stateManager) was requested to [store in a state store](../streaming-aggregation/StreamingAggregationStateManager.md#put) (the time taken is the [total time to update rows](#allUpdatesTimeMs) metric)

=== "Complete"
    Number of input rows (which should be exactly the number of output rows from the [child operator](#child))

=== "Update"
    Number of rows that the [StreamingAggregationStateManager](#stateManager) was requested to [store in a state store](../streaming-aggregation/StreamingAggregationStateManager.md#put) (that did not expire per the optional [watermarkPredicateForData](WatermarkSupport.md#watermarkPredicateForData) predicate) that is equivalent to the [number of output rows](#numOutputRows) metric)

`StateStoreSaveExec` is a [StateStoreWriter](StateStoreWriter.md) so consult [number of updated state rows](StateStoreWriter.md#numUpdatedStateRows), too.

### <span id="commitTimeMs"> time to commit changes

[Time taken](StateStoreWriter.md#timeTakenMs) for the [StreamingAggregationStateManager](#stateManager) to [commit changes to a state store](../streaming-aggregation/StreamingAggregationStateManager.md#commit)

---

When requested to [commit changes to a state store](../streaming-aggregation/StreamingAggregationStateManager.md#commit), a [StreamingAggregationStateManager](#stateManager) (as [StreamingAggregationStateManagerBaseImpl](../streaming-aggregation/StreamingAggregationStateManagerBaseImpl.md)) requests the given [StateStore](../stateful-stream-processing/StateStore.md) to [commit state changes](../stateful-stream-processing/StateStore.md#commit).

For [RocksDBStateStore](../rocksdb/RocksDBStateStore.md), it means for [RocksDB](../rocksdb/RocksDBStateStoreProvider.md#rocksDB) to [commit state changes](../rocksdb/RocksDB.md#commit) which is made up of the time-tracked phases that are available using the [performance metrics](../rocksdb/RocksDBMetrics.md#lastCommitLatencyMs).

In other words, the total of the [time-tracked phases](../rocksdb/RocksDBMetrics.md#lastCommitLatencyMs) is close approximation of this metric (there are some file-specific ops though that contribute to the metric but are not part of the phases).

### <span id="allRemovalsTimeMs"> time to remove

The metric is computed differently based on the given [OutputMode](#outputMode).

=== "Append"
    For [Append](#outputMode), [time taken](StateStoreWriter.md#timeTakenMs) for the entire input row and state processing (except [time to commit changes](#commitTimeMs))

=== "Complete"
    For [Complete](#outputMode), always `0`

=== "Update"
    For [Update](#outputMode), [time taken](StateStoreWriter.md#timeTakenMs) to [removeKeysOlderThanWatermark](WatermarkSupport.md#removeKeysOlderThanWatermark) (using the [StreamingAggregationStateManager](#stateManager))

`StateStoreSaveExec` is a [StateStoreWriter](StateStoreWriter.md) so consult [time to remove](StateStoreWriter.md#allRemovalsTimeMs), too.

### <span id="allUpdatesTimeMs"> time to update

The metric is computed differently based on the given [OutputMode](#outputMode).

=== "Append"
    [Time taken](StateStoreWriter.md#timeTakenMs) to drop (_filter out_) expired rows (per the required [watermarkPredicateForData](WatermarkSupport.md#watermarkPredicateForData) predicate) and then [store the remaining ones in a state store](../streaming-aggregation/StreamingAggregationStateManager.md#put) (using the [StreamingAggregationStateManager](#stateManager))

    !!! tip
        Consult the correlated metrics (e.g., [number of rows which are dropped by watermark](#numRowsDroppedByWatermark) and [number of updated state rows](#numUpdatedStateRows)).

=== "Complete"
    [Time taken](StateStoreWriter.md#timeTakenMs) to [store all input rows in a state store](../streaming-aggregation/StreamingAggregationStateManager.md#put) (using the [StreamingAggregationStateManager](#stateManager))

=== "Update"
    [Time taken](StateStoreWriter.md#timeTakenMs) to filter out expired rows (per the optional [watermarkPredicateForData](WatermarkSupport.md#watermarkPredicateForData) predicate) and [store them in a state store](../streaming-aggregation/StreamingAggregationStateManager.md#put) (using the [StreamingAggregationStateManager](#stateManager))

    [number of output rows](#numOutputRows) and [number of updated state rows](#numUpdatedStateRows) metrics are the same and exactly the number of rows that were stored in a state store.

!!! note
    Consult [StateStoreWriter](StateStoreWriter.md#allUpdatesTimeMs) to learn more about correlated metrics.

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

## <span id="doExecute"> Executing Physical Operator

==== [[doExecute-Append]] Append Output Mode

NOTE: [Append](../OutputMode.md#Append) is the default output mode when not specified explicitly.

NOTE: `Append` output mode requires that a streaming query defines [event-time watermark](../watermark/index.md) (e.g. using [withWatermark](../operators/withWatermark.md) operator) on the event-time column that is used in aggregation (directly or using [window](../operators/window.md) standard function).

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
