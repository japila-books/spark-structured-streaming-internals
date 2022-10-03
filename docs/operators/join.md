# join Operator &mdash; Streaming Join

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

[Streaming Join](../streaming-join/index.md)
