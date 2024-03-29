# Streaming Join

In Spark Structured Streaming, a **streaming join** is a structured query that is described (_built_) using the [high-level streaming operators](../operators/):

* [Dataset.crossJoin](../operators/crossJoin.md)

* [Dataset.join](../operators/join.md)

* [Dataset.joinWith](../operators/joinWith.md)

* SQL's `JOIN` clause

Streaming joins can be **stateless** or [stateful](../stateful-stream-processing/index.md):

* Joins of a streaming query and a batch query (_stream-static joins_) are stateless and no state management is required

* Joins of two streaming queries ([stream-stream joins](#stream-stream-joins)) are stateful and require streaming state (with an optional [join state watermark for state removal](#join-state-watermark)).

## Stream-Stream Joins

Spark Structured Streaming supports **stream-stream joins** with the following:

* [Equality predicate](../execution-planning-strategies/StreamingJoinStrategy.md) ([equi-joins](https://en.wikipedia.org/wiki/Join_(SQL)#Equi-join) that use only equality comparisons in the join predicate)

* `Inner`, `LeftOuter`, and `RightOuter` [join types only](../physical-operators/StreamingSymmetricHashJoinExec.md#supported-join-types)

Stream-stream equi-joins are planned as [StreamingSymmetricHashJoinExec](../physical-operators/StreamingSymmetricHashJoinExec.md) physical operators of two `ShuffleExchangeExec` physical operators (per [Required Partition Requirements](../physical-operators/StreamingSymmetricHashJoinExec.md#requiredChildDistribution)).

Learn more in [Demo: Stream-Stream Inner Join](../demo/stream-stream-inner-join.md).

## Join State Watermark

Stream-stream joins may have an optional **Join State Watermark** defined for state removal (cf. [Watermark Predicates for State Removal](../physical-operators/StreamingSymmetricHashJoinExec.md#stateWatermarkPredicates)).

A join state watermark can be specified on the following:

1. [Join keys](JoinStateWatermarkPredicate.md#JoinStateKeyWatermarkPredicate) (_key state_)

1. [Columns of the left and right sides](JoinStateWatermarkPredicate.md#JoinStateValueWatermarkPredicate) (_value state_)

A join state watermark can be specified on key state, value state or both.

## IncrementalExecution

Under the covers, the [high-level operators](../operators/index.md) create a logical query plan with one or more `Join` logical operators.

Spark Structured Streaming uses [IncrementalExecution](../IncrementalExecution.md) for planning streaming queries for execution.

At [query planning](../IncrementalExecution.md#executedPlan), `IncrementalExecution` uses the [StreamingJoinStrategy](../execution-planning-strategies/StreamingJoinStrategy.md) execution planning strategy for planning [stream-stream joins](#stream-stream-joins) as [StreamingSymmetricHashJoinExec](../physical-operators/StreamingSymmetricHashJoinExec.md) physical operators.

## Further Reading Or Watching

* [Stream-stream Joins](https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html#stream-stream-joins) in the official documentation of Apache Spark for Structured Streaming

* [Introducing Stream-Stream Joins in Apache Spark 2.3](https://databricks.com/blog/2018/03/13/introducing-stream-stream-joins-in-apache-spark-2-3.html) by Databricks

* (video) [Deep Dive into Stateful Stream Processing in Structured Streaming](https://databricks.com/session/deep-dive-into-stateful-stream-processing-in-structured-streaming) by Tathagata Das
