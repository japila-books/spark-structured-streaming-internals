== [[explain]] Dataset.explain High-Level Operator -- Explaining Streaming Query Plans

[source, scala]
----
explain(): Unit // <1>
explain(extended: Boolean): Unit
----
<1> Calls `explain` with `extended` flag disabled

`Dataset.explain` is a high-level operator that prints the link:spark-sql-LogicalPlan.md[logical] and (with `extended` flag enabled) link:spark-sql-SparkPlan.md[physical] plans to the console.

[source, scala]
----
val records = spark.
  readStream.
  format("rate").
  load
scala> records.explain
== Physical Plan ==
StreamingRelation rate, [timestamp#0, value#1L]

scala> records.explain(extended = true)
== Parsed Logical Plan ==
StreamingRelation DataSource(org.apache.spark.sql.SparkSession@4071aa13,rate,List(),None,List(),None,Map(),None), rate, [timestamp#0, value#1L]

== Analyzed Logical Plan ==
timestamp: timestamp, value: bigint
StreamingRelation DataSource(org.apache.spark.sql.SparkSession@4071aa13,rate,List(),None,List(),None,Map(),None), rate, [timestamp#0, value#1L]

== Optimized Logical Plan ==
StreamingRelation DataSource(org.apache.spark.sql.SparkSession@4071aa13,rate,List(),None,List(),None,Map(),None), rate, [timestamp#0, value#1L]

== Physical Plan ==
StreamingRelation rate, [timestamp#0, value#1L]
----

Internally, `explain` creates a `ExplainCommand` runnable command with the logical plan and `extended` flag.

`explain` then executes the plan with `ExplainCommand` runnable command and collects the results that are printed out to the standard output.

[NOTE]
====
`explain` uses `SparkSession` to access the current `SessionState` to execute the plan.

[source, scala]
----
import org.apache.spark.sql.execution.command.ExplainCommand
val explain = ExplainCommand(...)
spark.sessionState.executePlan(explain)
----
====

For streaming Datasets, `ExplainCommand` command simply creates a link:spark-sql-streaming-IncrementalExecution.md[IncrementalExecution] for the `SparkSession` and the logical plan.

NOTE: For the purpose of `explain`, `IncrementalExecution` is created with the output mode `Append`, checkpoint location `<unknown>`, run id a random number, current batch id `0` and offset metadata empty. They do not really matter when explaining the load-part of a streaming query.
