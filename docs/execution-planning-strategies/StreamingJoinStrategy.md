# StreamingJoinStrategy Execution Planning Strategy &mdash; Stream-Stream Equi-Joins

`StreamingJoinStrategy` is an execution planning strategy that can plan streaming queries with `Join` logical operators of two streaming queries to a [StreamingSymmetricHashJoinExec](../physical-operators/StreamingSymmetricHashJoinExec.md) physical operator.

`StreamingJoinStrategy` throws an `AnalysisException` when applied to a `Join` logical operator with no equality predicate:

```text
Stream-stream join without equality predicate is not supported
```

`StreamingJoinStrategy` is used when [IncrementalExecution](../IncrementalExecution.md) is requested to plan a streaming query.

[[logging]]
[TIP]
====
`StreamingJoinStrategy` does not print out any messages to the logs. `StreamingJoinStrategy` however uses `ExtractEquiJoinKeys` ([Spark SQL]({{ book.spark_sql }}/ExtractEquiJoinKeys)) Scala extractor for destructuring `Join` logical operators that does print out DEBUG messages to the logs.

Enable `ALL` logging level for `org.apache.spark.sql.catalyst.planning.ExtractEquiJoinKeys` to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.catalyst.planning.ExtractEquiJoinKeys=ALL
```

Refer to <<spark-sql-streaming-spark-logging.md#, Logging>>.
====
