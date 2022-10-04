# StreamingGlobalLimitExec Physical Operator

`StreamingGlobalLimitExec` is a unary physical operator that represents a `Limit` ([Spark SQL]({{ book.spark_sql }}/logical-operators/Limit)) logical operator of a streaming query at execution time.

`StreamingGlobalLimitExec` is a <<StateStoreWriter, stateful physical operator that can write to a state store>>.

`StreamingGlobalLimitExec` supports <<outputMode, Append>> output mode only.

The optional properties, i.e. the <<stateInfo, StatefulOperatorStateInfo>> and the <<outputMode, output mode>>, are initially undefined when `StreamingGlobalLimitExec` is <<creating-instance, created>>. `StreamingGlobalLimitExec` is updated to hold execution-specific configuration when `IncrementalExecution` is requested to [prepare the logical plan (of a streaming query) for execution](../IncrementalExecution.md#preparing-for-execution) (when the [state preparation rule](../IncrementalExecution.md#state) is executed).

## Creating Instance

`StreamingGlobalLimitExec` takes the following to be created:

* [[streamLimit]] **Streaming Limit**
* [[child]] Child physical operator (`SparkPlan`)
* [[stateInfo]] [StatefulOperatorStateInfo](../StatefulOperatorStateInfo.md) (default: `None`)
* [[outputMode]] [OutputMode](../OutputMode.md) (default: `None`)

`StreamingGlobalLimitExec` is created when [StreamingGlobalLimitStrategy](../execution-planning-strategies/StreamingGlobalLimitStrategy.md) execution planning strategy is requested to plan a `Limit` logical operator (in the logical plan of a streaming query) for execution.

=== [[StateStoreWriter]] StreamingGlobalLimitExec as StateStoreWriter

`StreamingGlobalLimitExec` is a [stateful physical operator that can write to a state store](StateStoreWriter.md).

=== [[metrics]] Performance Metrics

`StreamingGlobalLimitExec` uses the performance metrics of the parent [StateStoreWriter](StateStoreWriter.md#metrics).

=== [[doExecute]] Executing Physical Operator (Generating RDD[InternalRow]) -- `doExecute` Method

[source, scala]
----
doExecute(): RDD[InternalRow]
----

NOTE: `doExecute` is part of `SparkPlan` Contract to generate the runtime representation of an physical operator as a recipe for distributed computation over internal binary rows on Apache Spark (`RDD[InternalRow]`).

`doExecute`...FIXME

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| keySchema
a| [[keySchema]] FIXME

Used when...FIXME

| valueSchema
a| [[valueSchema]] FIXME

Used when...FIXME

|===
