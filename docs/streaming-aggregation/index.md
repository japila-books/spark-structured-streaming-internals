# Streaming Aggregation

In Spark Structured Streaming, **Streaming Aggregation** is an operation in a streaming query that was described (_build_) using the following [high-level streaming operators](../operators/index.md):

* [Dataset.groupBy](../operators/groupBy.md), `Dataset.rollup`, `Dataset.cube` (that all create a `RelationalGroupedDataset` ([Spark SQL]({{ book.spark_sql }}/basic-aggregation/RelationalGroupedDataset)))

* [Dataset.groupByKey](../operators/groupByKey.md) (that creates a `KeyValueGroupedDataset` ([Spark SQL]({{ book.spark_sql }}/basic-aggregation/KeyValueGroupedDataset)))

* SQL's `GROUP BY` clause (including `WITH CUBE` and `WITH ROLLUP` ([Spark SQL]({{ book.spark_sql }}/sql/AstBuilder/#withAggregationClause)))

Streaming aggregation is part of [Stateful Stream Processing](../stateful-stream-processing/index.md).

## <span id="IncrementalExecution"> IncrementalExecution

Under the covers, these high-level operators create logical query plans with `Aggregate` ([Spark SQL]({{ book.spark_sql }}/logical-operators/Aggregate)) logical operators.

Spark Structured Streaming uses [IncrementalExecution](../IncrementalExecution.md) for planning streaming queries for execution.

At [query planning](../IncrementalExecution.md#executedPlan), `IncrementalExecution` uses the [StatefulAggregationStrategy](../execution-planning-strategies/StatefulAggregationStrategy.md) execution planning strategy for planning streaming aggregations (`Aggregate` unary logical operators) as pairs of [StateStoreRestoreExec](../physical-operators/StateStoreRestoreExec.md) and [StateStoreSaveExec](../physical-operators/StateStoreSaveExec.md) physical operators.

## Demo

```text
// input data from a data source
// it's rate data source
// but that does not really matter
// We need a streaming Dataset
val input = spark
  .readStream
  .format("rate")
  .load

// Streaming aggregation with groupBy
val counts = input
  .groupBy($"value" % 2)
  .count

counts.explain(extended = true)
/**
== Parsed Logical Plan ==
'Aggregate [('value % 2)], [('value % 2) AS (value % 2)#23, count(1) AS count#22L]
+- StreamingRelationV2 org.apache.spark.sql.execution.streaming.sources.RateStreamProvider@7879348, rate, [timestamp#15, value#16L]

== Analyzed Logical Plan ==
(value % 2): bigint, count: bigint
Aggregate [(value#16L % cast(2 as bigint))], [(value#16L % cast(2 as bigint)) AS (value % 2)#23L, count(1) AS count#22L]
+- StreamingRelationV2 org.apache.spark.sql.execution.streaming.sources.RateStreamProvider@7879348, rate, [timestamp#15, value#16L]

== Optimized Logical Plan ==
Aggregate [(value#16L % 2)], [(value#16L % 2) AS (value % 2)#23L, count(1) AS count#22L]
+- Project [value#16L]
   +- StreamingRelationV2 org.apache.spark.sql.execution.streaming.sources.RateStreamProvider@7879348, rate, [timestamp#15, value#16L]

== Physical Plan ==
*(4) HashAggregate(keys=[(value#16L % 2)#27L], functions=[count(1)], output=[(value % 2)#23L, count#22L])
+- StateStoreSave [(value#16L % 2)#27L], state info [ checkpoint = <unknown>, runId = 8c0ae2be-5eaa-4038-bc29-a176abfaf885, opId = 0, ver = 0, numPartitions = 200], Append, 0, 2
   +- *(3) HashAggregate(keys=[(value#16L % 2)#27L], functions=[merge_count(1)], output=[(value#16L % 2)#27L, count#29L])
      +- StateStoreRestore [(value#16L % 2)#27L], state info [ checkpoint = <unknown>, runId = 8c0ae2be-5eaa-4038-bc29-a176abfaf885, opId = 0, ver = 0, numPartitions = 200], 2
         +- *(2) HashAggregate(keys=[(value#16L % 2)#27L], functions=[merge_count(1)], output=[(value#16L % 2)#27L, count#29L])
            +- Exchange hashpartitioning((value#16L % 2)#27L, 200)
               +- *(1) HashAggregate(keys=[(value#16L % 2) AS (value#16L % 2)#27L], functions=[partial_count(1)], output=[(value#16L % 2)#27L, count#29L])
                  +- *(1) Project [value#16L]
                     +- StreamingRelation rate, [timestamp#15, value#16L]
*/
```

## More Demos

Learn more in the following demos:

* [Streaming Watermark with Aggregation in Append Output Mode](../demo/watermark-aggregation-append.md)

* [Streaming Query for Running Counts (Socket Source and Complete Output Mode)](../demo/groupBy-running-count-complete.md)

* [Streaming Aggregation with Kafka Data Source](../demo/kafka-data-source.md)

* [groupByKey Streaming Aggregation in Update Mode](../demo/groupByKey-count-Update.md)
