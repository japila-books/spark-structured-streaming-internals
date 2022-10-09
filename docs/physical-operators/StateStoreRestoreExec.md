# StateStoreRestoreExec Physical Operator

`StateStoreRestoreExec` is a unary physical operator ([Spark SQL]({{ book.spark_sql }}/physical-operators/UnaryExecNode)) to [restore (read) a streaming state (from a state store)](StateStoreReader.md) (for the keys from the [child](#child) physical operator).

`StateStoreRestoreExec` is among the physical operators used for execute [streaming aggregations](../streaming-aggregation/index.md).

![StateStoreRestoreExec and StatefulAggregationStrategy](../images/StateStoreRestoreExec-StatefulAggregationStrategy.png)

## Creating Instance

`StateStoreRestoreExec` takes the following to be created:

* <span id="keyExpressions"> Grouping Key `Attribute`s ([Spark SQL]({{ book.spark_sql }}/expressions/Attribute))
* <span id="stateInfo"> [StatefulOperatorStateInfo](../stateful-stream-processing/StatefulOperatorStateInfo.md)
* <span id="stateFormatVersion"> [spark.sql.streaming.aggregation.stateFormatVersion](../configuration-properties.md#spark.sql.streaming.aggregation.stateFormatVersion)
* <span id="child"> Child Physical Operator ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan))

`StateStoreRestoreExec` is created when:

* `StatefulAggregationStrategy` execution planning strategy is requested to [plan a streaming aggregation](../execution-planning-strategies/StatefulAggregationStrategy.md#planStreamingAggregation)
* `IncrementalExecution` is [created](../IncrementalExecution.md#state)

## <span id="requiredChildDistribution"> Required Child Output Distribution

```scala
requiredChildDistribution: Seq[Distribution]
```

`requiredChildDistribution` is part of the `SparkPlan` ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan/#requiredChildDistribution)) abstraction.

---

`requiredChildDistribution`...FIXME

## Review Me

The optional <<stateInfo, StatefulOperatorStateInfo>> is initially undefined (i.e. when `StateStoreRestoreExec` is <<creating-instance, created>>). `StateStoreRestoreExec` is updated to hold the streaming batch-specific execution property when `IncrementalExecution` [prepares a streaming physical plan for execution](../IncrementalExecution.md#preparations) (and [state](../IncrementalExecution.md#state) preparation rule is executed when `StreamExecution` MicroBatchExecution.md#runBatch-queryPlanning[plans a streaming query] for a streaming batch).

![StateStoreRestoreExec and IncrementalExecution](../images/StateStoreRestoreExec-IncrementalExecution.png)

When <<doExecute, executed>>, `StateStoreRestoreExec` executes the <<child, child>> physical operator and [creates a StateStoreRDD to map over partitions](../stateful-stream-processing/StateStoreOps.md#mapPartitionsWithStateStore) with `storeUpdateFunction` that restores the state for the keys in the input rows if available.

=== [[metrics]] Performance Metrics (SQLMetrics)

[cols="1m,1,3",options="header",width="100%"]
|===
| Key
| Name (in UI)
| Description

| numOutputRows
| number of output rows
| [[numOutputRows]] The number of input rows from the <<child, child>> physical operator (for which `StateStoreRestoreExec` tried to find the state)
|===

![StateStoreRestoreExec in web UI (Details for Query)](../images/StateStoreRestoreExec-webui-query-details.png)

=== [[stateManager]] StateStoreRestoreExec and StreamingAggregationStateManager -- `stateManager` Property

[source, scala]
----
stateManager: StreamingAggregationStateManager
----

`stateManager` is a [StreamingAggregationStateManager](../streaming-aggregation/StreamingAggregationStateManager.md) that is created together with `StateStoreRestoreExec`.

The `StreamingAggregationStateManager` is created for the <<keyExpressions, keys>>, the output schema of the <<child, child>> physical operator and the <<stateFormatVersion, version of the state format>>.

The `StreamingAggregationStateManager` is used when `StateStoreRestoreExec` is requested to <<doExecute, generate a recipe for a distributed computation (as a RDD[InternalRow])>> for the following:

* [Schema of the values in a state store](../streaming-aggregation/StreamingAggregationStateManager.md#getStateValueSchema)

* [Extracting the columns for the key from the input row](../streaming-aggregation/StreamingAggregationStateManager.md#getKey)

* [Looking up the value of a key from a state store](../streaming-aggregation/StreamingAggregationStateManager.md#get)

## <span id="doExecute"> Executing Physical Operator (Generating RDD[InternalRow])

```scala
doExecute(): RDD[InternalRow]
```

`doExecute` is part of the `SparkPlan` abstraction.

Internally, `doExecute` executes <<child, child>> physical operator and [creates a StateStoreRDD](../stateful-stream-processing/StateStoreOps.md#mapPartitionsWithStateStore) with `storeUpdateFunction` that does the following per <<child, child>> operator's RDD partition:

1. Generates an unsafe projection to access the key field (using <<keyExpressions, keyExpressions>> and the output schema of <<child, child>> operator).

1. For every input row (as `InternalRow`)

* Extracts the key from the row (using the unsafe projection above)

* [Gets the saved state](../stateful-stream-processing/StateStore.md#get) in `StateStore` for the key if available (it might not be if the key appeared in the input the first time)

* Increments <<numOutputRows, numOutputRows>> metric (that in the end is the number of rows from the <<child, child>> operator)

* Generates collection made up of the current row and possibly the state for the key if available

NOTE: The number of rows from `StateStoreRestoreExec` is the number of rows from the <<child, child>> operator with additional rows for the saved state.

NOTE: There is no way in `StateStoreRestoreExec` to find out how many rows had associated state available in a state store. You would have to use the corresponding `StateStoreSaveExec` operator's StateStoreSaveExec.md#metrics[metrics] (most likely `number of total state rows` but that could depend on the output mode).
