# joinWith Operator &mdash; Streaming Join

```scala
joinWith[U](
  other: Dataset[U],
  condition: Column): Dataset[(T, U)]
joinWith[U](
  other: Dataset[U],
  condition: Column,
  joinType: String): Dataset[(T, U)]
```

[Streaming Join](../streaming-join/index.md)
