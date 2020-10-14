# MemoryPlan Logical Operator

`MemoryPlan` is a leaf logical operator (i.e. `LogicalPlan`) that is used to query the data that has been written into a [MemorySink](spark-sql-streaming-MemorySink.md). `MemoryPlan` is created when [starting continuous writing](DataStreamWriter.md#start) (to a `MemorySink`).

TIP: See the example in spark-sql-streaming-MemoryStream.md[MemoryStream].

```text
scala> intsOut.explain(true)
== Parsed Logical Plan ==
SubqueryAlias memstream
+- MemoryPlan org.apache.spark.sql.execution.streaming.MemorySink@481bf251, [value#21]

== Analyzed Logical Plan ==
value: int
SubqueryAlias memstream
+- MemoryPlan org.apache.spark.sql.execution.streaming.MemorySink@481bf251, [value#21]

== Optimized Logical Plan ==
MemoryPlan org.apache.spark.sql.execution.streaming.MemorySink@481bf251, [value#21]

== Physical Plan ==
LocalTableScan [value#21]
```

When executed, `MemoryPlan` is translated to `LocalTableScanExec` physical operator (similar to `LocalRelation` logical operator) in `BasicOperators` execution planning strategy.
