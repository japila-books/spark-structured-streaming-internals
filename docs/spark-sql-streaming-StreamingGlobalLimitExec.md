== [[StreamingGlobalLimitExec]] StreamingGlobalLimitExec Unary Physical Operator

`StreamingGlobalLimitExec` is a unary physical operator that represents a `Limit` logical operator of a streaming query at execution time.

[NOTE]
====
A unary physical operator (`UnaryExecNode`) is a physical operator with a single <<child, child>> physical operator.

Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-SparkPlan.html[UnaryExecNode] (and physical operators in general) in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.
====

`StreamingGlobalLimitExec` is <<creating-instance, created>> exclusively when <<spark-sql-streaming-StreamingGlobalLimitStrategy.md#, StreamingGlobalLimitStrategy>> execution planning strategy is requested to plan a `Limit` logical operator (in the logical plan of a streaming query) for execution.

[NOTE]
====
`Limit` logical operator represents `Dataset.limit` operator in a logical query plan.

Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-LogicalPlan-Limit.html[Limit Logical Operator] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.
====

`StreamingGlobalLimitExec` is a <<StateStoreWriter, stateful physical operator that can write to a state store>>.

`StreamingGlobalLimitExec` supports <<outputMode, Append>> output mode only.

The optional properties, i.e. the <<stateInfo, StatefulOperatorStateInfo>> and the <<outputMode, output mode>>, are initially undefined when `StreamingGlobalLimitExec` is <<creating-instance, created>>. `StreamingGlobalLimitExec` is updated to hold execution-specific configuration when `IncrementalExecution` is requested to <<spark-sql-streaming-IncrementalExecution.md#preparing-for-execution, prepare the logical plan (of a streaming query) for execution>> (when the <<spark-sql-streaming-IncrementalExecution.md#state, state preparation rule>> is executed).

=== [[creating-instance]] Creating StreamingGlobalLimitExec Instance

`StreamingGlobalLimitExec` takes the following to be created:

* [[streamLimit]] *Streaming Limit*
* [[child]] Child physical operator (`SparkPlan`)
* [[stateInfo]] <<spark-sql-streaming-StatefulOperatorStateInfo.md#, StatefulOperatorStateInfo>> (default: `None`)
* [[outputMode]] <<spark-sql-streaming-OutputMode.md#, OutputMode>> (default: `None`)

`StreamingGlobalLimitExec` initializes the <<internal-properties, internal properties>>.

=== [[StateStoreWriter]] StreamingGlobalLimitExec as StateStoreWriter

`StreamingGlobalLimitExec` is a <<spark-sql-streaming-StateStoreWriter.md#, stateful physical operator that can write to a state store>>.

=== [[metrics]] Performance Metrics

`StreamingGlobalLimitExec` uses the performance metrics of the parent <<spark-sql-streaming-StateStoreWriter.md#metrics, StateStoreWriter>>.

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
