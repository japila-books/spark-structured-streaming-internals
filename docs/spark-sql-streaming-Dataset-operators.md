# Streaming Operators &mdash; High-Level Declarative Streaming Dataset API

Dataset API comes with a set of [operators](#operators) that are of particular use in Spark Structured Streaming and together constitute the **High-Level Declarative Streaming Dataset API**.

## <span id="crossJoin"> crossJoin

```scala
crossJoin(
  right: Dataset[_]): DataFrame
```

## <span id="dropDuplicates"> dropDuplicates

```scala
dropDuplicates(): Dataset[T]
dropDuplicates(
  colNames: Seq[String]): Dataset[T]
dropDuplicates(
  col1: String,
  cols: String*): Dataset[T]
```

[dropDuplicates](spark-sql-streaming-Dataset-dropDuplicates.md)

Drops duplicate records (given a subset of columns)

## <span id="explain"> explain

```scala
explain(): Unit
explain(extended: Boolean): Unit
```

[explain](spark-sql-streaming-Dataset-explain.md)

Explains query plans

## <span id="groupBy"> groupBy

```scala
groupBy(
  cols: Column*): RelationalGroupedDataset
groupBy(
  col1: String,
  cols: String*): RelationalGroupedDataset
```

[groupBy](spark-sql-streaming-Dataset-groupBy.md)

Aggregates rows by zero, one or more columns

## <span id="join"> join

```scala
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
```

[Streaming Join](spark-sql-streaming-join.md)

## <span id="joinWith"> joinWith

```scala
joinWith[U](
  other: Dataset[U],
  condition: Column): Dataset[(T, U)]
joinWith[U](
  other: Dataset[U],
  condition: Column,
  joinType: String): Dataset[(T, U)]
```

[Streaming Join](spark-sql-streaming-join.md)

## <span id="withWatermark"> withWatermark

```scala
withWatermark(
  eventTime: String,
  delayThreshold: String): Dataset[T]
```

[withWatermark](spark-sql-streaming-Dataset-withWatermark.md)

Defines a [streaming watermark](spark-sql-streaming-watermark.md) (on the given `eventTime` column with a delay threshold)
