# StreamingGlobalLimitStrategy Execution Planning Strategy

`StreamingGlobalLimitStrategy` is an execution planning strategy that can plan streaming queries with `ReturnAnswer` and `Limit` logical operators (over streaming queries) with the [Append](#outputMode) output mode to [StreamingGlobalLimitExec](../physical-operators/StreamingGlobalLimitExec.md) physical operator.

`StreamingGlobalLimitStrategy` is used (and created) when [IncrementalExecution](../IncrementalExecution.md) is requested to plan a streaming query.

## Creating Instance

`StreamingGlobalLimitStrategy` takes a single [OutputMode](../OutputMode.md) to be created (which is the [OutputMode](../IncrementalExecution.md#outputMode) of the [IncrementalExecution](../IncrementalExecution.md)).
