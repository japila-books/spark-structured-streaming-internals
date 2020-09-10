== Streaming Operators -- High-Level Declarative Streaming Dataset API

Dataset API comes with a set of <<operators, operators>> that are of particular use in Spark Structured Streaming that together constitute so-called *High-Level Declarative Streaming Dataset API*.

[[operators]]
.Streaming Operators
[cols="30m,70",options="header",width="100%"]
|===
| Operator
| Description

| <<spark-sql-streaming-Dataset-crossJoin.md#, crossJoin>>
a| [[crossJoin]]

[source, scala]
----
crossJoin(
  right: Dataset[_]): DataFrame
----

| <<spark-sql-streaming-Dataset-dropDuplicates.md#, dropDuplicates>>
a| [[dropDuplicates]]

[source, scala]
----
dropDuplicates(): Dataset[T]
dropDuplicates(colNames: Seq[String]): Dataset[T]
dropDuplicates(col1: String, cols: String*): Dataset[T]
----

Drops duplicate records (given a subset of columns)

| <<spark-sql-streaming-Dataset-explain.md#, explain>>
a| [[explain]]

[source, scala]
----
explain(): Unit
explain(extended: Boolean): Unit
----

Explains query plans

| <<spark-sql-streaming-Dataset-groupBy.md#, groupBy>>
a| [[groupBy]]

[source, scala]
----
groupBy(cols: Column*): RelationalGroupedDataset
groupBy(col1: String, cols: String*): RelationalGroupedDataset
----

Aggregates rows by zero, one or more columns

| <<spark-sql-streaming-Dataset-groupByKey.md#, groupByKey>>
a| [[groupByKey]]

[source, scala]
----
groupByKey(func: T => K): KeyValueGroupedDataset[K, T]
----

Aggregates rows by a typed grouping function (and gives a <<spark-sql-streaming-KeyValueGroupedDataset.md#, KeyValueGroupedDataset>>)

| <<spark-sql-streaming-Dataset-join.md#, join>>
a| [[join]]

[source, scala]
----
join(
  right: Dataset[_]): DataFrame
join(
  right: Dataset[_],
  joinExprs: Column): DataFrame
join(
  right: Dataset[_],
  joinExprs: Column,
  joinType: String): DataFrame
join(
  right: Dataset[_],
  usingColumns: Seq[String]): DataFrame
join(
  right: Dataset[_],
  usingColumns: Seq[String],
  joinType: String): DataFrame
join(
  right: Dataset[_],
  usingColumn: String): DataFrame
----

| <<spark-sql-streaming-Dataset-joinWith.md#, joinWith>>
a| [[joinWith]]

[source, scala]
----
joinWith[U](
  other: Dataset[U],
  condition: Column): Dataset[(T, U)]
joinWith[U](
  other: Dataset[U],
  condition: Column,
  joinType: String): Dataset[(T, U)]
----

| <<spark-sql-streaming-Dataset-withWatermark.md#, withWatermark>>
a| [[withWatermark]]

[source, scala]
----
withWatermark(
  eventTime: String,
  delayThreshold: String): Dataset[T]
----

Defines a <<spark-sql-streaming-watermark.md#, streaming watermark>> (on the given `eventTime` column with a delay threshold)

| `writeStream`
a| [[writeStream]]

[source, scala]
----
writeStream: DataStreamWriter[T]
----

Creates a <<spark-sql-streaming-DataStreamWriter.md#, DataStreamWriter>> for persisting the result of a streaming query to an external data system

|===

[source, scala]
----
val rates = spark
  .readStream
  .format("rate")
  .option("rowsPerSecond", 1)
  .load

// stream processing
// replace [operator] with the operator of your choice
rates.[operator]

// output stream
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import scala.concurrent.duration._
val sq = rates
  .writeStream
  .format("console")
  .option("truncate", false)
  .trigger(Trigger.ProcessingTime(10.seconds))
  .outputMode(OutputMode.Complete)
  .queryName("rate-console")
  .start

// eventually...
sq.stop
----
