== [[StreamingJoinStrategy]] StreamingJoinStrategy Execution Planning Strategy -- Stream-Stream Equi-Joins

[[apply]]
`StreamingJoinStrategy` is an execution planning strategy that can plan streaming queries with `Join` logical operators of two streaming queries to a <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>> physical operator.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-SparkStrategy.html[Execution Planning Strategies] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] online book.

`StreamingJoinStrategy` throws an `AnalysisException` when applied to a `Join` logical operator with no equality predicate:

```
Stream-stream join without equality predicate is not supported
```

`StreamingJoinStrategy` is used exclusively when <<spark-sql-streaming-IncrementalExecution.md#, IncrementalExecution>> is requested to plan a streaming query.

[[logging]]
[TIP]
====
`StreamingJoinStrategy` does not print out any messages to the logs. `StreamingJoinStrategy` however uses `ExtractEquiJoinKeys` Scala extractor for destructuring `Join` logical operators that does print out DEBUG messages to the logs.

Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-ExtractEquiJoinKeys.html[ExtractEquiJoinKeys] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] online book.

Enable `ALL` logging level for `org.apache.spark.sql.catalyst.planning.ExtractEquiJoinKeys` to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.catalyst.planning.ExtractEquiJoinKeys=ALL
```

Refer to <<spark-sql-streaming-logging.md#, Logging>>.
====
