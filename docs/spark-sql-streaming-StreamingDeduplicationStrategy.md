# StreamingDeduplicationStrategy Execution Planning Strategy

[[apply]]
`StreamingDeduplicationStrategy` is an execution planning strategy that can plan streaming queries with `Deduplicate` logical operators (over streaming queries) to [StreamingDeduplicateExec](physical-operators/StreamingDeduplicateExec.md) physical operators.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-SparkStrategy.html[Execution Planning Strategies] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

NOTE: <<spark-sql-streaming-Deduplicate.md#, Deduplicate>> logical operator represents [Dataset.dropDuplicates](operators/dropDuplicates.md) operator in a logical query plan.

`StreamingDeduplicationStrategy` is available using `SessionState`.

```scala
spark.sessionState.planner.StreamingDeduplicationStrategy
```
