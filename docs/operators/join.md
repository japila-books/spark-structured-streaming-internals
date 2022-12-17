# join Operator

`join` is part of `Dataset` API for [Streaming Join](../join/index.md).

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

`join` creates a `Dataset` with a `Join` ([Spark SQL]({{ book.spark_sql }}/logical-operators/Join)) logical operator.
