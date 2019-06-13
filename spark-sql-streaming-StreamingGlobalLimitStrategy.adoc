== [[StreamingGlobalLimitStrategy]] StreamingGlobalLimitStrategy Execution Planning Strategy

`StreamingGlobalLimitStrategy` is an execution planning strategy that can plan streaming queries with `ReturnAnswer` and `Limit` logical operators (over streaming queries) with the <<outputMode, Append>> output mode to <<spark-sql-streaming-StreamingGlobalLimitExec.adoc#, StreamingGlobalLimitExec>> physical operator.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-SparkStrategy.html[Execution Planning Strategies] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

`StreamingGlobalLimitStrategy` is used (and <<creating-instance, created>>) exclusively when <<spark-sql-streaming-IncrementalExecution.adoc#, IncrementalExecution>> is requested to plan a streaming query.

[[creating-instance]][[outputMode]]
`StreamingGlobalLimitStrategy` takes a single <<spark-sql-streaming-OutputMode.adoc#, OutputMode>> to be created (which is the <<spark-sql-streaming-IncrementalExecution.adoc#outputMode, OutputMode>> of the <<spark-sql-streaming-IncrementalExecution.adoc#, IncrementalExecution>>).

=== [[demo]] Demo: Using StreamingGlobalLimitStrategy

[source, scala]
----
FIXME
----
