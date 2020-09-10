== [[StateStoreRestoreExec]] StateStoreRestoreExec Unary Physical Operator -- Restoring Streaming State From State Store

`StateStoreRestoreExec` is a unary physical operator that <<spark-sql-streaming-StateStoreReader.md#, restores (reads) a streaming state from a state store>> (for the keys from the <<child, child>> physical operator).

[NOTE]
====
A unary physical operator (`UnaryExecNode`) is a physical operator with a single <<child, child>> physical operator.

Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-SparkPlan.html[UnaryExecNode] (and physical operators in general) in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.
====

`StateStoreRestoreExec` is <<creating-instance, created>> exclusively when <<spark-sql-streaming-StatefulAggregationStrategy.md#, StatefulAggregationStrategy>> execution planning strategy is requested to plan a <<spark-sql-streaming-aggregation.md#, streaming aggregation>> for execution (`Aggregate` logical operators in the logical plan of a streaming query).

.StateStoreRestoreExec and StatefulAggregationStrategy
image::images/StateStoreRestoreExec-StatefulAggregationStrategy.png[align="center"]

The optional <<stateInfo, StatefulOperatorStateInfo>> is initially undefined (i.e. when `StateStoreRestoreExec` is <<creating-instance, created>>). `StateStoreRestoreExec` is updated to hold the streaming batch-specific execution property when `IncrementalExecution` link:spark-sql-streaming-IncrementalExecution.md#preparations[prepares a streaming physical plan for execution] (and link:spark-sql-streaming-IncrementalExecution.md#state[state] preparation rule is executed when `StreamExecution` link:spark-sql-streaming-MicroBatchExecution.md#runBatch-queryPlanning[plans a streaming query] for a streaming batch).

.StateStoreRestoreExec and IncrementalExecution
image::images/StateStoreRestoreExec-IncrementalExecution.png[align="center"]

When <<doExecute, executed>>, `StateStoreRestoreExec` executes the <<child, child>> physical operator and <<spark-sql-streaming-StateStoreOps.md#mapPartitionsWithStateStore, creates a StateStoreRDD to map over partitions>> with `storeUpdateFunction` that restores the state for the keys in the input rows if available.

[[output]]
The output schema of `StateStoreRestoreExec` is exactly the <<child, child>>'s output schema.

[[outputPartitioning]]
The output partitioning of `StateStoreRestoreExec` is exactly the <<child, child>>'s output partitioning.

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

.StateStoreRestoreExec in web UI (Details for Query)
image::images/StateStoreRestoreExec-webui-query-details.png[align="center"]

=== [[creating-instance]] Creating StateStoreRestoreExec Instance

`StateStoreRestoreExec` takes the following to be created:

* [[keyExpressions]] *Key expressions*, i.e. Catalyst attributes for the grouping keys
* [[stateInfo]] Optional <<spark-sql-streaming-StatefulOperatorStateInfo.md#, StatefulOperatorStateInfo>> (default: `None`)
* [[stateFormatVersion]] Version of the state format (based on the <<spark-sql-streaming-properties.md#spark.sql.streaming.aggregation.stateFormatVersion, spark.sql.streaming.aggregation.stateFormatVersion>> configuration property)
* [[child]] Child physical operator (`SparkPlan`)

=== [[stateManager]] StateStoreRestoreExec and StreamingAggregationStateManager -- `stateManager` Property

[source, scala]
----
stateManager: StreamingAggregationStateManager
----

`stateManager` is a <<spark-sql-streaming-StreamingAggregationStateManager.md#, StreamingAggregationStateManager>> that is created together with `StateStoreRestoreExec`.

The `StreamingAggregationStateManager` is created for the <<keyExpressions, keys>>, the output schema of the <<child, child>> physical operator and the <<stateFormatVersion, version of the state format>>.

The `StreamingAggregationStateManager` is used when `StateStoreRestoreExec` is requested to <<doExecute, generate a recipe for a distributed computation (as a RDD[InternalRow])>> for the following:

* <<spark-sql-streaming-StreamingAggregationStateManager.md#getStateValueSchema, Schema of the values in a state store>>

* <<spark-sql-streaming-StreamingAggregationStateManager.md#getKey, Extracting the columns for the key from the input row>>

* <<spark-sql-streaming-StreamingAggregationStateManager.md#get, Looking up the value of a key from a state store>>

=== [[doExecute]] Executing Physical Operator (Generating RDD[InternalRow]) -- `doExecute` Method

[source, scala]
----
doExecute(): RDD[InternalRow]
----

NOTE: `doExecute` is part of `SparkPlan` Contract to generate the runtime representation of an physical operator as a distributed computation over internal binary rows on Apache Spark (i.e. `RDD[InternalRow]`).

Internally, `doExecute` executes <<child, child>> physical operator and link:spark-sql-streaming-StateStoreOps.md#mapPartitionsWithStateStore[creates a StateStoreRDD] with `storeUpdateFunction` that does the following per <<child, child>> operator's RDD partition:

1. Generates an unsafe projection to access the key field (using <<keyExpressions, keyExpressions>> and the output schema of <<child, child>> operator).

1. For every input row (as `InternalRow`)

* Extracts the key from the row (using the unsafe projection above)

* link:spark-sql-streaming-StateStore.md#get[Gets the saved state] in `StateStore` for the key if available (it might not be if the key appeared in the input the first time)

* Increments <<numOutputRows, numOutputRows>> metric (that in the end is the number of rows from the <<child, child>> operator)

* Generates collection made up of the current row and possibly the state for the key if available

NOTE: The number of rows from `StateStoreRestoreExec` is the number of rows from the <<child, child>> operator with additional rows for the saved state.

NOTE: There is no way in `StateStoreRestoreExec` to find out how many rows had associated state available in a state store. You would have to use the corresponding `StateStoreSaveExec` operator's link:spark-sql-streaming-StateStoreSaveExec.md#metrics[metrics] (most likely `number of total state rows` but that could depend on the output mode).
