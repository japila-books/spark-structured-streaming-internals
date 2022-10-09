# StateStoreRestoreExec Physical Operator

`StateStoreRestoreExec` is a unary physical operator ([Spark SQL]({{ book.spark_sql }}/physical-operators/UnaryExecNode)) to [restore (read) a streaming state (from a state store)](StateStoreReader.md) (for the keys from the [child](#child) physical operator).

`StateStoreRestoreExec` is among the physical operators used for execute [streaming aggregations](../streaming-aggregation/index.md).

![StateStoreRestoreExec and StatefulAggregationStrategy](../images/StateStoreRestoreExec-StatefulAggregationStrategy.png)

## Creating Instance

`StateStoreRestoreExec` takes the following to be created:

* <span id="keyExpressions"> Grouping Key `Attribute`s ([Spark SQL]({{ book.spark_sql }}/expressions/Attribute))
* [StatefulOperatorStateInfo](#stateInfo)
* <span id="stateFormatVersion"> [spark.sql.streaming.aggregation.stateFormatVersion](../configuration-properties.md#spark.sql.streaming.aggregation.stateFormatVersion)
* <span id="child"> Child Physical Operator ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan))

`StateStoreRestoreExec` is created when:

* `StatefulAggregationStrategy` execution planning strategy is requested to [plan a streaming aggregation](../execution-planning-strategies/StatefulAggregationStrategy.md#planStreamingAggregation)
* `IncrementalExecution` is [created](../IncrementalExecution.md#state)

### <span id="stateInfo"> StatefulOperatorStateInfo

`StateStoreRestoreExec` can be given a [StatefulOperatorStateInfo](../stateful-stream-processing/StatefulOperatorStateInfo.md) when [created](#creating-instance).

The `StatefulOperatorStateInfo` is initially undefined when `StateStoreRestoreExec` is [created](#creating-instance).

The `StatefulOperatorStateInfo` is specified (so this `StateStoreRestoreExec` gets a streaming batch-specific execution property) when `IncrementalExecution` is requested to [prepare a streaming physical plan for execution](../IncrementalExecution.md#preparations) (and [state](../IncrementalExecution.md#state) preparation rule is executed when `StreamExecution` [plans a streaming query](../micro-batch-execution/MicroBatchExecution.md#runBatch-queryPlanning) for a streaming batch).

![StateStoreRestoreExec and IncrementalExecution](../images/StateStoreRestoreExec-IncrementalExecution.png)

## Performance Metrics

![StateStoreRestoreExec in web UI (Details for Query)](../images/StateStoreRestoreExec-webui-query-details.png)

### <span id="numOutputRows"> number of output rows

The number of input rows from the [child physical operator](#child) this `StateStoreRestoreExec` found the state value for when [doExecute](#doExecute)

!!! note "FIXME number of output rows"
    The number of output rows metric seems to be always an even number of the `restoredRow` from a state store and the `row` itself (from the [child physical operator](#child)).

## <span id="stateManager"> StreamingAggregationStateManager

`StateStoreRestoreExec` [creates a StreamingAggregationStateManager](../streaming-aggregation/StreamingAggregationStateManager.md#createStateManager) when [created](#creating-instance).

The `StreamingAggregationStateManager` is created using the [grouping key expressions](#keyExpressions) and the output schema of the [child physical operator](#child).

The `StreamingAggregationStateManager` is used in [doExecute](#doExecute) for the following:

* [State Value Schema](../streaming-aggregation/StreamingAggregationStateManager.md#getStateValueSchema) to [mapPartitionsWithReadStateStore](../stateful-stream-processing/StateStoreOps.md#mapPartitionsWithReadStateStore)
* [Extract a key](../streaming-aggregation/StreamingAggregationStateManager.md#getKey) (from an input row) and [get the value](../streaming-aggregation/StreamingAggregationStateManager.md#get) (for the key) for every input row

## <span id="requiredChildDistribution"> Required Child Output Distribution

```scala
requiredChildDistribution: Seq[Distribution]
```

`requiredChildDistribution` is part of the `SparkPlan` ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan/#requiredChildDistribution)) abstraction.

---

`requiredChildDistribution`...FIXME

## <span id="doExecute"> Executing Operator

```scala
doExecute(): RDD[InternalRow]
```

`doExecute` is part of the `SparkPlan` ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan#doExecute)) abstraction.

---

`doExecute` executes the [child operator](#child) and [creates a StateStoreRDD](../stateful-stream-processing/StateStoreOps.md#mapPartitionsWithStateStore) with `storeUpdateFunction` that does the following per partition:

1. Generates an unsafe projection to access the key field (using [keyExpressions](#keyExpressions) and the output schema of [child operator](#child)).

1. For every input row (as `InternalRow`)
    * Extracts the key from the row (using the unsafe projection above)
    * [Gets the saved state](../stateful-stream-processing/StateStore.md#get) in `StateStore` for the key if available (it might not be if the key appeared in the input the first time)
    * Increments [numOutputRows](#numOutputRows) metric
    * Generates collection made up of the current row and possibly the state for the key if available
