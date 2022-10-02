# StreamingDeduplicationStrategy Execution Planning Strategy

`StreamingDeduplicationStrategy` is an execution planning strategy that can plan streaming queries with `Deduplicate` logical operators (over streaming queries) to [StreamingDeduplicateExec](../physical-operators/StreamingDeduplicateExec.md) physical operators.

`StreamingDeduplicationStrategy` is available using `SessionState`.

```scala
spark.sessionState.planner.StreamingDeduplicationStrategy
```
