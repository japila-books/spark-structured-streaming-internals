== [[StreamingDeduplicationStrategy]] StreamingDeduplicationStrategy Execution Planning Strategy for Deduplicate Logical Operator

[[apply]]
`StreamingDeduplicationStrategy` is an execution planning strategy that can plan streaming queries with `Deduplicate` logical operators (over streaming queries) to <<spark-sql-streaming-StreamingDeduplicateExec.adoc#, StreamingDeduplicateExec>> physical operators.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-SparkStrategy.html[Execution Planning Strategies] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

NOTE: <<spark-sql-streaming-Deduplicate.adoc#, Deduplicate>> logical operator represents <<spark-sql-streaming-Dataset-operators.adoc#dropDuplicates, Dataset.dropDuplicates>> operator in a logical query plan.

`StreamingDeduplicationStrategy` is available using `SessionState`.

[source, scala]
----
spark.sessionState.planner.StreamingDeduplicationStrategy
----

=== [[demo]] Demo: Using StreamingDeduplicationStrategy

[source, scala]
----
FIXME
----
