== [[StreamingRelationStrategy]] StreamingRelationStrategy Execution Planning Strategy for StreamingRelation and StreamingExecutionRelation Logical Operators

[[apply]]
`StreamingRelationStrategy` is an execution planning strategy that can plan streaming queries with <<spark-sql-streaming-StreamingRelation.md#, StreamingRelation>>, <<spark-sql-streaming-StreamingExecutionRelation.md#, StreamingExecutionRelation>>, and <<spark-sql-streaming-StreamingRelationV2.md#, StreamingRelationV2>> logical operators to <<physical-operators/StreamingRelationExec.md#, StreamingRelationExec>> physical operators.

.StreamingRelationStrategy, StreamingRelation, StreamingExecutionRelation and StreamingRelationExec Operators
image::images/StreamingRelationStrategy-apply.png[align="center"]

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-SparkStrategy.html[Execution Planning Strategies] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

`StreamingRelationStrategy` is used exclusively when <<spark-sql-streaming-IncrementalExecution.md#, IncrementalExecution>> is requested to plan a streaming query.

`StreamingRelationStrategy` is available using `SessionState` (of a `SparkSession`).

[source, scala]
----
spark.sessionState.planner.StreamingRelationStrategy
----

=== [[demo]] Demo: Using StreamingRelationStrategy

[source, scala]
----
val rates = spark.
  readStream.
  format("rate").
  load // <-- gives a streaming Dataset with a logical plan with StreamingRelation logical operator

// StreamingRelation logical operator for the rate streaming source
scala> println(rates.queryExecution.logical.numberedTreeString)
00 StreamingRelation DataSource(org.apache.spark.sql.SparkSession@31ba0af0,rate,List(),None,List(),None,Map(),None), rate, [timestamp#0, value#1L]

// StreamingRelationExec physical operator (shown without "Exec" suffix)
scala> rates.explain
== Physical Plan ==
StreamingRelation rate, [timestamp#0, value#1L]

// Let's do the planning manually
import spark.sessionState.planner.StreamingRelationStrategy
val physicalPlan = StreamingRelationStrategy.apply(rates.queryExecution.logical).head
scala> println(physicalPlan.numberedTreeString)
00 StreamingRelation rate, [timestamp#0, value#1L]
----
