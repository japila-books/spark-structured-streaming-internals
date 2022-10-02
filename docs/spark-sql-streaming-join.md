# Streaming Join

[[operators]]
In Spark Structured Streaming, a *streaming join* is a streaming query that was described (_build_) using the [high-level streaming operators](operators/):

* [Dataset.crossJoin](operators/crossJoin.md)

* [Dataset.join](operators/join.md)

* [Dataset.joinWith](operators/joinWith.md)

* SQL's `JOIN` clause

Streaming joins can be *stateless* or <<spark-sql-streaming-stateful-stream-processing.md#, stateful>>:

* Joins of a streaming query and a batch query (_stream-static joins_) are stateless and no state management is required

* Joins of two streaming queries (<<stream-stream-joins, stream-stream joins>>) are stateful and require streaming state (with an optional <<join-state-watermark, join state watermark for state removal>>).

## Stream-Stream Joins

Spark Structured Streaming supports *stream-stream joins* with the following:

* [Equality predicate](execution-planning-strategies/StreamingJoinStrategy.md) ([equi-joins](https://en.wikipedia.org/wiki/Join_(SQL)#Equi-join) that use only equality comparisons in the join predicate)

* `Inner`, `LeftOuter`, and `RightOuter` <<physical-operators/StreamingSymmetricHashJoinExec.md#supported-join-types, join types only>>

Stream-stream equi-joins are planned as <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>> physical operators of two `ShuffleExchangeExec` physical operators (per <<physical-operators/StreamingSymmetricHashJoinExec.md#requiredChildDistribution, Required Partition Requirements>>).

=== [[join-state-watermark]] Join State Watermark for State Removal

Stream-stream joins may optionally define *Join State Watermark* for state removal (cf. <<physical-operators/StreamingSymmetricHashJoinExec.md#stateWatermarkPredicates, Watermark Predicates for State Removal>>).

A join state watermark can be specified on the following:

. <<spark-sql-streaming-JoinStateWatermarkPredicate.md#JoinStateKeyWatermarkPredicate, Join keys>> (_key state_)

. <<spark-sql-streaming-JoinStateWatermarkPredicate.md#JoinStateValueWatermarkPredicate, Columns of the left and right sides>> (_value state_)

A join state watermark can be specified on key state, value state or both.

=== [[IncrementalExecution]] IncrementalExecution -- QueryExecution of Streaming Queries

Under the covers, the <<operators, high-level operators>> create a logical query plan with one or more `Join` logical operators.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-LogicalPlan-Join.html[Join Logical Operator] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] online book.

In Spark Structured Streaming [IncrementalExecution](IncrementalExecution.md) is responsible for planning streaming queries for execution.

At [query planning](IncrementalExecution.md#executedPlan), `IncrementalExecution` uses the [StreamingJoinStrategy](execution-planning-strategies/StreamingJoinStrategy.md) execution planning strategy for planning [stream-stream joins](#stream-stream-joins) as [StreamingSymmetricHashJoinExec](physical-operators/StreamingSymmetricHashJoinExec.md) physical operators.

## Demos

* [StreamStreamJoinApp](https://github.com/jaceklaskowski/spark-structured-streaming-book/tree/v{{spark.version}}/examples/src/main/scala/pl/japila/spark/StreamStreamJoinApp.scala)

## Further Reading Or Watching

* [Stream-stream Joins](https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html#stream-stream-joins) in the official documentation of Apache Spark for Structured Streaming

* [Introducing Stream-Stream Joins in Apache Spark 2.3](https://databricks.com/blog/2018/03/13/introducing-stream-stream-joins-in-apache-spark-2-3.html) by Databricks

* (video) [Deep Dive into Stateful Stream Processing in Structured Streaming](https://databricks.com/session/deep-dive-into-stateful-stream-processing-in-structured-streaming) by Tathagata Das
