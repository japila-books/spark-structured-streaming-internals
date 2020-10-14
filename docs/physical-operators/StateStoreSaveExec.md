# StateStoreSaveExec Unary Physical Operator

`StateStoreSaveExec` is a unary physical operator that [saves a streaming state to a state store](StateStoreWriter.md) with [support for streaming watermark](../spark-sql-streaming-WatermarkSupport.md).

[NOTE]
====
A unary physical operator (`UnaryExecNode`) is a physical operator with a single <<child, child>> physical operator.

Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-SparkPlan.html[UnaryExecNode] (and physical operators in general) in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.
====

`StateStoreSaveExec` is <<creating-instance, created>> exclusively when <<spark-sql-streaming-StatefulAggregationStrategy.md#, StatefulAggregationStrategy>> execution planning strategy is requested to plan a <<spark-sql-streaming-aggregation.md#, streaming aggregation>> for execution (`Aggregate` logical operators in the logical plan of a streaming query).

![StateStoreSaveExec and StatefulAggregationStrategy](../images/StateStoreSaveExec-StatefulAggregationStrategy.png)

The optional properties, i.e. the <<stateInfo, StatefulOperatorStateInfo>>, the <<outputMode, output mode>>, and the <<eventTimeWatermark, event-time watermark>>, are initially undefined when `StateStoreSaveExec` is <<creating-instance, created>>. `StateStoreSaveExec` is updated to hold execution-specific configuration when `IncrementalExecution` is requested to [prepare the logical plan (of a streaming query) for execution](../IncrementalExecution.md#preparing-for-execution) (when the [state preparation rule](../IncrementalExecution.md#state) is executed).

![StateStoreSaveExec and IncrementalExecution](../images/StateStoreSaveExec-IncrementalExecution.png)

NOTE: Unlike spark-sql-streaming-StateStoreRestoreExec.md[StateStoreRestoreExec] operator, `StateStoreSaveExec` takes <<outputMode, output mode>> and <<eventTimeWatermark, event time watermark>> when <<creating-instance, created>>.

When <<doExecute, executed>>, `StateStoreSaveExec` spark-sql-streaming-StateStoreOps.md#mapPartitionsWithStateStore[creates a StateStoreRDD to map over partitions] with `storeUpdateFunction` that manages the `StateStore`.

![StateStoreSaveExec creates StateStoreRDD](../images/StateStoreSaveExec-StateStoreRDD.png)

![StateStoreSaveExec and StateStoreRDD (after streamingBatch.toRdd.count)](../images/StateStoreSaveExec-StateStoreRDD-count.png)

[NOTE]
====
The number of partitions of spark-sql-streaming-StateStoreOps.md#mapPartitionsWithStateStore[StateStoreRDD] (and hence the number of Spark tasks) is what was defined for the <<child, child>> physical plan.

There will be that many `StateStores` as there are partitions in `StateStoreRDD`.
====

NOTE: `StateStoreSaveExec` <<doExecute, behaves>> differently per output mode.

When <<doExecute, executed>>, `StateStoreSaveExec` executes the <<child, child>> physical operator and spark-sql-streaming-StateStoreOps.md#mapPartitionsWithStateStore[creates a StateStoreRDD] (with `storeUpdateFunction` specific to the output mode).

[[output]]
The output schema of `StateStoreSaveExec` is exactly the <<child, child>>'s output schema.

[[outputPartitioning]]
The output partitioning of `StateStoreSaveExec` is exactly the <<child, child>>'s output partitioning.

[[stateManager]]
`StateStoreRestoreExec` uses a <<spark-sql-streaming-StreamingAggregationStateManager.md#, StreamingAggregationStateManager>> (that is <<spark-sql-streaming-StreamingAggregationStateManager.md#createStateManager, created>> for the <<keyExpressions, keyExpressions>>, the output of the <<child, child>> physical operator and the <<stateFormatVersion, stateFormatVersion>>).

[[logging]]
[TIP]
====
Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.StateStoreSaveExec` to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.execution.streaming.StateStoreSaveExec=ALL
```

Refer to <<spark-sql-streaming-spark-logging.md#, Logging>>.
====

=== [[metrics]] Performance Metrics (SQLMetrics)

`StateStoreSaveExec` uses the performance metrics as [other stateful physical operators that write to a state store](StateStoreWriter.md#metrics).

![StateStoreSaveExec in web UI (Details for Query)](../images/StateStoreSaveExec-webui-query-details.png)

The following table shows how the performance metrics are computed (and so their exact meaning).

[cols="30,70",options="header",width="100%"]
|===
| Name (in web UI)
| Description

| total time to update rows
a| [[allUpdatesTimeMs]] [Time taken](StateStoreWriter.md#timeTakenMs) to read the input rows and <<spark-sql-streaming-StreamingAggregationStateManager.md#put, store them in a state store>> (possibly filtering out expired rows per <<spark-sql-streaming-WatermarkSupport.md#watermarkPredicateForData, watermarkPredicateForData>> predicate)

The number of rows stored is the <<numUpdatedStateRows, number of updated state rows>> metric

* For <<outputMode, Append>> output mode, the [time taken](StateStoreWriter.md#timeTakenMs) to filter out expired rows (per the required <<spark-sql-streaming-WatermarkSupport.md#watermarkPredicateForData, watermarkPredicateForData>> predicate) and the <<stateManager, StreamingAggregationStateManager>> to <<spark-sql-streaming-StreamingAggregationStateManager.md#put, store rows in a state store>>

* For <<outputMode, Complete>> output mode, the [time taken](StateStoreWriter.md#timeTakenMs) to go over all the input rows and request the <<stateManager, StreamingAggregationStateManager>> to <<spark-sql-streaming-StreamingAggregationStateManager.md#put, store rows in a state store>>

* For <<outputMode, Update>> output mode, the [time taken](StateStoreWriter.md#timeTakenMs) to filter out expired rows (per the optional <<spark-sql-streaming-WatermarkSupport.md#watermarkPredicateForData, watermarkPredicateForData>> predicate) and the <<stateManager, StreamingAggregationStateManager>> to <<spark-sql-streaming-StreamingAggregationStateManager.md#put, store rows in a state store>>

| total time to remove rows
a| [[allRemovalsTimeMs]]

* For <<outputMode, Append>> output mode, the time taken for the <<stateManager, StreamingAggregationStateManager>> to <<spark-sql-streaming-StreamingAggregationStateManager.md#remove, remove all expired entries from a state store>> (per <<spark-sql-streaming-WatermarkSupport.md#watermarkPredicateForKeys, watermarkPredicateForKeys>> predicate) that is the total time of iterating over <<spark-sql-streaming-StreamingAggregationStateManager.md#iterator, all entries in the state store>> (the number of entries <<spark-sql-streaming-StreamingAggregationStateManager.md#remove, removed from a state store>> is the difference between the number of output rows of the <<child, child operator>> and the <<numTotalStateRows, number of total state rows>> metric)

* For <<outputMode, Complete>> output mode, always `0`

* For <<outputMode, Update>> output mode, the [time taken](StateStoreWriter.md#timeTakenMs) for the <<stateManager, StreamingAggregationStateManager>> to <<spark-sql-streaming-WatermarkSupport.md#removeKeysOlderThanWatermark, remove all expired entries from a state store>> (per <<spark-sql-streaming-WatermarkSupport.md#watermarkPredicateForKeys, watermarkPredicateForKeys>> predicate)

| time to commit changes
a| [[commitTimeMs]] [Time taken](StateStoreWriter.md#timeTakenMs) for the <<stateManager, StreamingAggregationStateManager>> to <<spark-sql-streaming-StreamingAggregationStateManager.md#commit, commit changes to a state store>>

| number of output rows
a| [[numOutputRows]]

* For <<outputMode, Append>> output mode, the metric does not seem to be used

* For <<outputMode, Complete>> output mode, the number of rows in a <<spark-sql-streaming-StateStore.md#, StateStore>> (i.e. all <<spark-sql-streaming-StreamingAggregationStateManager.md#values, values>> in a <<spark-sql-streaming-StateStore.md#, StateStore>> in the <<stateManager, StreamingAggregationStateManager>> that should be equivalent to the <<numTotalStateRows, number of total state rows>> metric)

* For <<outputMode, Update>> output mode, the number of rows that the <<stateManager, StreamingAggregationStateManager>> was requested to <<spark-sql-streaming-StreamingAggregationStateManager.md#put, store in a state store>> (that did not expire per the optional <<spark-sql-streaming-WatermarkSupport.md#watermarkPredicateForData, watermarkPredicateForData>> predicate) that is equivalent to the <<numUpdatedStateRows, number of updated state rows>> metric)

| number of total state rows
a| [[numTotalStateRows]] Number of entries in a <<spark-sql-streaming-StateStore.md#, state store>> at the very end of <<doExecute, executing the StateStoreSaveExec operator>> (aka _numTotalStateRows_)

Corresponds to `numRowsTotal` attribute in `stateOperators` in [StreamingQueryProgress](../monitoring/StreamingQueryProgress.md) (and is available as `sq.lastProgress.stateOperators` for an operator).

| number of updated state rows
a| [[numUpdatedStateRows]] Number of the entries that <<spark-sql-streaming-StateStore.md#put, were stored as updates in a state store>> in a trigger and for the keys in the result rows of the upstream physical operator (aka _numUpdatedStateRows_)

* For <<outputMode, Append>> output mode, the number of input rows that have not expired yet (per the required <<spark-sql-streaming-WatermarkSupport.md#watermarkPredicateForData, watermarkPredicateForData>> predicate) and that the <<stateManager, StreamingAggregationStateManager>> was requested to <<spark-sql-streaming-StreamingAggregationStateManager.md#put, store in a state store>> (the time taken is the <<allUpdatesTimeMs, total time to update rows>> metric)

* For <<outputMode, Complete>> output mode, the number of input rows (which should be exactly the number of output rows from the <<child, child operator>>)

* For <<outputMode, Update>> output mode, the number of rows that the <<stateManager, StreamingAggregationStateManager>> was requested to <<spark-sql-streaming-StreamingAggregationStateManager.md#put, store in a state store>> (that did not expire per the optional <<spark-sql-streaming-WatermarkSupport.md#watermarkPredicateForData, watermarkPredicateForData>> predicate) that is equivalent to the <<numOutputRows, number of output rows>> metric)

Corresponds to `numRowsUpdated` attribute in `stateOperators` in [StreamingQueryProgress](../monitoring/StreamingQueryProgress.md) (and is available as `sq.lastProgress.stateOperators` for an operator).

| memory used by state
a| [[stateMemory]] Estimated memory used by a <<spark-sql-streaming-StateStore.md#, StateStore>> (aka _stateMemory_) after `StateStoreSaveExec` finished <<doExecute, execution>> (per the <<spark-sql-streaming-StateStoreMetrics.md#memoryUsedBytes, StateStoreMetrics>> of the <<spark-sql-streaming-StateStore.md#metrics, StateStore>>)
|===

=== [[creating-instance]] Creating StateStoreSaveExec Instance

`StateStoreSaveExec` takes the following to be created:

* [[keyExpressions]] *Key expressions*, i.e. Catalyst attributes for the grouping keys
* [[stateInfo]] Execution-specific <<spark-sql-streaming-StatefulOperatorStateInfo.md#, StatefulOperatorStateInfo>> (default: `None`)
* [[outputMode]] Execution-specific <<spark-sql-streaming-OutputMode.md#, output mode>> (default: `None`)
* [[eventTimeWatermark]] <<spark-sql-streaming-watermark.md#, Event-time watermark>> (default: `None`)
* [[stateFormatVersion]] Version of the state format (based on the <<spark-sql-streaming-properties.md#spark.sql.streaming.aggregation.stateFormatVersion, spark.sql.streaming.aggregation.stateFormatVersion>> configuration property)
* [[child]] Child physical operator (`SparkPlan`)

## <span id="doExecute"> Executing Physical Operator

```scala
doExecute(): RDD[InternalRow]
```

`doExecute` is part of the `SparkPlan` abstraction (Spark SQL).

Internally, `doExecute` initializes [metrics](StateStoreWriter.md#metrics).

NOTE: `doExecute` requires that the optional <<outputMode, outputMode>> is at this point defined (that should have happened when `IncrementalExecution` [had prepared a streaming aggregation for execution](../IncrementalExecution.md#preparations)).

`doExecute` executes <<child, child>> physical operator and spark-sql-streaming-StateStoreOps.md#mapPartitionsWithStateStore[creates a StateStoreRDD] with `storeUpdateFunction` that:

1. Generates an unsafe projection to access the key field (using <<keyExpressions, keyExpressions>> and the output schema of <<child, child>>).

1. Branches off per <<outputMode, output mode>>: <<doExecute-Append, Append>>, <<doExecute-Complete, Complete>> and <<doExecute-Update, Update>>.

`doExecute` throws an `UnsupportedOperationException` when executed with an invalid <<outputMode, output mode>>:

```
Invalid output mode: [outputMode]
```

==== [[doExecute-Append]] Append Output Mode

NOTE: <<spark-sql-streaming-OutputMode.md#Append, Append>> is the default output mode when not specified explicitly.

NOTE: `Append` output mode requires that a streaming query defines <<spark-sql-streaming-watermark.md#, event-time watermark>> (e.g. using [withWatermark](../operators/withWatermark.md) operator) on the event-time column that is used in aggregation (directly or using <<spark-sql-streaming-window.md#, window>> standard function).

For <<spark-sql-streaming-OutputMode.md#Append, Append>> output mode, `doExecute` does the following:

1. Finds late (aggregate) rows from <<child, child>> physical operator (that have expired per <<spark-sql-streaming-WatermarkSupport.md#watermarkPredicateForData, watermark>>)

1. <<spark-sql-streaming-StateStore.md#put, Stores the late rows in the state store>> and increments the <<numUpdatedStateRows, numUpdatedStateRows>> metric

1. <<spark-sql-streaming-StateStore.md#getRange, Gets all the added (late) rows from the state store>>

1. Creates an iterator that <<spark-sql-streaming-StateStore.md#remove, removes the late rows from the state store>> when requested the next row and in the end <<spark-sql-streaming-StateStore.md#commit, commits the state updates>>

TIP: Refer to <<spark-sql-streaming-demo-watermark-aggregation-append.md#, Demo: Streaming Watermark with Aggregation in Append Output Mode>> for an example of `StateStoreSaveExec` with `Append` output mode.

CAUTION: FIXME When is "Filtering state store on:" printed out?

---

1. Uses spark-sql-streaming-WatermarkSupport.md#watermarkPredicateForData[watermarkPredicateForData] predicate to exclude matching rows and (like in <<doExecute-Complete, Complete>> output mode) spark-sql-streaming-StateStore.md#put[stores all the remaining rows] in `StateStore`.

1. (like in <<doExecute-Complete, Complete>> output mode) While storing the rows, increments <<numUpdatedStateRows, numUpdatedStateRows>> metric (for every row) and records the total time in <<allUpdatesTimeMs, allUpdatesTimeMs>> metric.

1. spark-sql-streaming-StateStore.md#getRange[Takes all the rows] from `StateStore` and returns a `NextIterator` that:

* In `getNext`, finds the first row that matches <<spark-sql-streaming-WatermarkSupport.md#watermarkPredicateForKeys, watermarkPredicateForKeys>> predicate, spark-sql-streaming-StateStore.md#remove[removes it] from `StateStore`, and returns it back.
+
If no row was found, `getNext` also marks the iterator as finished.

* In `close`, records the time to iterate over all the rows in <<allRemovalsTimeMs, allRemovalsTimeMs>> metric, spark-sql-streaming-StateStore.md#commit[commits the updates] to `StateStore` followed by recording the time in <<commitTimeMs, commitTimeMs>> metric and [recording StateStore metrics](StateStoreWriter.md#setStoreMetrics).

==== [[doExecute-Complete]] Complete Output Mode

For <<spark-sql-streaming-OutputMode.md#Complete, Complete>> output mode, `doExecute` does the following:

1. Takes all `UnsafeRow` rows (from the parent iterator)

1. <<spark-sql-streaming-StateStore.md#put, Stores the rows by key in the state store>> eagerly (i.e. all rows that are available in the parent iterator before proceeding)

1. <<spark-sql-streaming-StateStore.md#commit, Commits the state updates>>

1. In the end, <<spark-sql-streaming-StateStore.md#iterator, reads the key-row pairs from the state store>> and passes the rows along (i.e. to the following physical operator)

The number of keys stored in the state store is recorded in <<numUpdatedStateRows, numUpdatedStateRows>> metric.

NOTE: In `Complete` output mode the <<numOutputRows, numOutputRows>> metric is exactly the <<numTotalStateRows, numTotalStateRows>> metric.

TIP: Refer to <<spark-sql-streaming-StateStoreSaveExec-Complete.md#, Demo: StateStoreSaveExec with Complete Output Mode>> for an example of `StateStoreSaveExec` with `Complete` output mode.

---

1. spark-sql-streaming-StateStore.md#put[Stores all rows] (as `UnsafeRow`) in `StateStore`.

1. While storing the rows, increments <<numUpdatedStateRows, numUpdatedStateRows>> metric (for every row) and records the total time in <<allUpdatesTimeMs, allUpdatesTimeMs>> metric.

1. Records `0` in <<allRemovalsTimeMs, allRemovalsTimeMs>> metric.

1. spark-sql-streaming-StateStore.md#commit[Commits the state updates] to `StateStore` and records the time in <<commitTimeMs, commitTimeMs>> metric.

1. [Records StateStore metrics](StateStoreWriter.md#setStoreMetrics)

1. In the end, [takes all the rows stored](../spark-sql-streaming-StateStore.md#iterator) in `StateStore` and increments [numOutputRows](#numOutputRows) metric.

==== [[doExecute-Update]] Update Output Mode

For <<spark-sql-streaming-OutputMode.md#Update, Update>> output mode, `doExecute` returns an iterator that filters out late aggregate rows (per <<spark-sql-streaming-WatermarkSupport.md#watermarkPredicateForData, watermark>> if defined) and <<spark-sql-streaming-StateStore.md#put, stores the "young" rows in the state store>> (one by one, i.e. every `next`).

With no more rows available, that <<spark-sql-streaming-StateStore.md#remove, removes the late rows from the state store>> (all at once) and <<spark-sql-streaming-StateStore.md#commit, commits the state updates>>.

TIP: Refer to <<spark-sql-streaming-StateStoreSaveExec-Update.md#, Demo: StateStoreSaveExec with Update Output Mode>> for an example of `StateStoreSaveExec` with `Update` output mode.

---

`doExecute` returns `Iterator` of rows that uses <<spark-sql-streaming-WatermarkSupport.md#watermarkPredicateForData, watermarkPredicateForData>> predicate to filter out late rows.

In `hasNext`, when rows are no longer available:

1. Records the total time to iterate over all the rows in <<allUpdatesTimeMs, allUpdatesTimeMs>> metric.

1. spark-sql-streaming-WatermarkSupport.md#removeKeysOlderThanWatermark[removeKeysOlderThanWatermark] and records the time in <<allRemovalsTimeMs, allRemovalsTimeMs>> metric.

1. spark-sql-streaming-StateStore.md#commit[Commits the updates] to `StateStore` and records the time in <<commitTimeMs, commitTimeMs>> metric.

1. [Records StateStore metrics](StateStoreWriter.md#setStoreMetrics)

In `next`, [stores a row](../spark-sql-streaming-StateStore.md#put) in `StateStore` and increments [numOutputRows](#numOutputRows) and [numUpdatedStateRows](#numUpdatedStateRows) metrics.

=== [[shouldRunAnotherBatch]] Checking Out Whether Last Batch Execution Requires Another Non-Data Batch or Not -- `shouldRunAnotherBatch` Method

```scala
shouldRunAnotherBatch(
  newMetadata: OffsetSeqMetadata): Boolean
```

`shouldRunAnotherBatch` is positive (`true`) when all of the following are met:

* <<outputMode, Output mode>> is either <<spark-sql-streaming-OutputMode.md#Append, Append>> or <<spark-sql-streaming-OutputMode.md#Update, Update>>

* <<eventTimeWatermark, Event-time watermark>> is defined and is older (below) the current <<spark-sql-streaming-OffsetSeqMetadata.md#batchWatermarkMs, event-time watermark>> (of the given `OffsetSeqMetadata`)

Otherwise, `shouldRunAnotherBatch` is negative (`false`).

`shouldRunAnotherBatch` is part of the [StateStoreWriter](StateStoreWriter.md#shouldRunAnotherBatch) abstraction.
