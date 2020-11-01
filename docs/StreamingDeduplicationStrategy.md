# StreamingDeduplicationStrategy Execution Planning Strategy

[[apply]]
`StreamingDeduplicationStrategy` is an execution planning strategy that can plan streaming queries with `Deduplicate` logical operators (over streaming queries) to [StreamingDeduplicateExec](physical-operators/StreamingDeduplicateExec.md) physical operators.

!!! tip
    Learn more about [Execution Planning Strategies]({{ book.spark_sql }}/SparkStrategy) in [The Internals of Spark SQL]({{ book.spark_sql }}) online book.

NOTE: <<spark-sql-streaming-Deduplicate.md#, Deduplicate>> logical operator represents [Dataset.dropDuplicates](operators/dropDuplicates.md) operator in a logical query plan.

`StreamingDeduplicationStrategy` is available using `SessionState`.

```scala
spark.sessionState.planner.StreamingDeduplicationStrategy
```
