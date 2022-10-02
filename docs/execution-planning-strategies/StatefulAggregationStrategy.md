# StatefulAggregationStrategy Execution Planning Strategy

`StatefulAggregationStrategy` is an execution planning strategy that is used to plan streaming queries with the following logical operators:

* [EventTimeWatermark](../logical-operators/EventTimeWatermark.md) logical operator ([Dataset.withWatermark](../operators/withWatermark.md) operator)

* `Aggregate` logical operator (for [Dataset.groupBy](../operators/groupBy.md) and [Dataset.groupByKey](../operators/groupByKey.md) operators, and `GROUP BY` SQL clause)

`StatefulAggregationStrategy` is used exclusively when [IncrementalExecution](../IncrementalExecution.md) is requested to plan a streaming query.

`StatefulAggregationStrategy` is available using `SessionState`.

```scala
spark.sessionState.planner.StatefulAggregationStrategy
```

[[apply]]
[[selection-requirements]]
.StatefulAggregationStrategy's Logical to Physical Operator Conversions
[cols="1,2",options="header",width="100%"]
|===
| Logical Operator
| Physical Operator

| [EventTimeWatermark](../logical-operators/EventTimeWatermark.md)
a| [[EventTimeWatermark]] [EventTimeWatermarkExec](../physical-operators/EventTimeWatermarkExec.md)

| `Aggregate`
a| [[Aggregate]]

In the order of preference:

1. `HashAggregateExec`
1. `ObjectHashAggregateExec`
1. `SortAggregateExec`

|===

## Demo

```text
val counts = spark.
  readStream.
  format("rate").
  load.
  groupBy(window($"timestamp", "5 seconds") as "group").
  agg(count("value") as "count").
  orderBy("group")
scala> counts.explain
== Physical Plan ==
*Sort [group#6 ASC NULLS FIRST], true, 0
+- Exchange rangepartitioning(group#6 ASC NULLS FIRST, 200)
   +- *HashAggregate(keys=[window#13], functions=[count(value#1L)])
      +- StateStoreSave [window#13], StatefulOperatorStateInfo(<unknown>,736d67c2-6daa-4c4c-9c4b-c12b15af20f4,0,0), Append, 0
         +- *HashAggregate(keys=[window#13], functions=[merge_count(value#1L)])
            +- StateStoreRestore [window#13], StatefulOperatorStateInfo(<unknown>,736d67c2-6daa-4c4c-9c4b-c12b15af20f4,0,0)
               +- *HashAggregate(keys=[window#13], functions=[merge_count(value#1L)])
                  +- Exchange hashpartitioning(window#13, 200)
                     +- *HashAggregate(keys=[window#13], functions=[partial_count(value#1L)])
                        +- *Project [named_struct(start, precisetimestampconversion(((((CASE WHEN (cast(CEIL((cast((precisetimestampconversion(timestamp#0, TimestampType, LongType) - 0) as double) / 5000000.0)) as double) = (cast((precisetimestampconversion(timestamp#0, TimestampType, LongType) - 0) as double) / 5000000.0)) THEN (CEIL((cast((precisetimestampconversion(timestamp#0, TimestampType, LongType) - 0) as double) / 5000000.0)) + 1) ELSE CEIL((cast((precisetimestampconversion(timestamp#0, TimestampType, LongType) - 0) as double) / 5000000.0)) END + 0) - 1) * 5000000) + 0), LongType, TimestampType), end, precisetimestampconversion(((((CASE WHEN (cast(CEIL((cast((precisetimestampconversion(timestamp#0, TimestampType, LongType) - 0) as double) / 5000000.0)) as double) = (cast((precisetimestampconversion(timestamp#0, TimestampType, LongType) - 0) as double) / 5000000.0)) THEN (CEIL((cast((precisetimestampconversion(timestamp#0, TimestampType, LongType) - 0) as double) / 5000000.0)) + 1) ELSE CEIL((cast((precisetimestampconversion(timestamp#0, TimestampType, LongType) - 0) as double) / 5000000.0)) END + 0) - 1) * 5000000) + 5000000), LongType, TimestampType)) AS window#13, value#1L]
                           +- *Filter isnotnull(timestamp#0)
                              +- StreamingRelation rate, [timestamp#0, value#1L]

import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import scala.concurrent.duration._
val consoleOutput = counts.
  writeStream.
  format("console").
  option("truncate", false).
  trigger(Trigger.ProcessingTime(10.seconds)).
  queryName("counts").
  outputMode(OutputMode.Complete).  // <-- required for groupBy
  start

// Eventually...
consoleOutput.stop
```

=== [[planStreamingAggregation]][[AggUtils-planStreamingAggregation]] Selecting Aggregate Physical Operator Given Aggregate Expressions — `AggUtils.planStreamingAggregation` Internal Method

[source, scala]
----
planStreamingAggregation(
  groupingExpressions: Seq[NamedExpression],
  functionsWithoutDistinct: Seq[AggregateExpression],
  resultExpressions: Seq[NamedExpression],
  child: SparkPlan): Seq[SparkPlan]
----

`planStreamingAggregation` takes the grouping attributes (from `groupingExpressions`).

NOTE: `groupingExpressions` corresponds to the grouping function in [groupBy](../operators/groupBy.md) operator.

[[partialAggregate]]
`planStreamingAggregation` creates an aggregate physical operator (called `partialAggregate`) with:

* `requiredChildDistributionExpressions` undefined (i.e. `None`)
* `initialInputBufferOffset` as `0`
* `functionsWithoutDistinct` in `Partial` mode
* `child` operator as the input `child`

[NOTE]
====
`planStreamingAggregation` creates one of the following aggregate physical operators (in the order of preference):

1. `HashAggregateExec`
1. `ObjectHashAggregateExec`
1. `SortAggregateExec`

`planStreamingAggregation` uses `AggUtils.createAggregate` method to select an aggregate physical operator that you can read about in https://jaceklaskowski.gitbooks.io/mastering-apache-spark/spark-sql-SparkStrategy-Aggregation.html#AggUtils-createAggregate[Selecting Aggregate Physical Operator Given Aggregate Expressions -- `AggUtils.createAggregate` Internal Method] in *Mastering Apache Spark 2* gitbook.
====

[[partialMerged1]]
`planStreamingAggregation` creates an aggregate physical operator (called `partialMerged1`) with:

* `requiredChildDistributionExpressions` based on the input `groupingExpressions`
* `initialInputBufferOffset` as the length of `groupingExpressions`
* `functionsWithoutDistinct` in `PartialMerge` mode
* `child` operator as <<partialAggregate, partialAggregate>> aggregate physical operator created above

[[restored]]
`planStreamingAggregation` creates [StateStoreRestoreExec](../physical-operators/StateStoreRestoreExec.md) physical operator with the grouping attributes, undefined `StatefulOperatorStateInfo`, and <<partialMerged1, partialMerged1>> aggregate physical operator created above.

[[partialMerged2]]
`planStreamingAggregation` creates an aggregate physical operator (called `partialMerged2`) with:

* `child` operator as <<restored, StateStoreRestoreExec>> physical operator created above

NOTE: The only difference between <<partialMerged1, partialMerged1>> and <<partialMerged2, partialMerged2>> steps is the child physical operator.

[[saved]]
`planStreamingAggregation` creates [StateStoreSaveExec](../physical-operators/StateStoreSaveExec.md#creating-instance) with:

* the grouping attributes based on the input `groupingExpressions`
* No `stateInfo`, `outputMode` and `eventTimeWatermark`
* `child` operator as <<partialMerged2, partialMerged2>> aggregate physical operator created above

[[finalAndCompleteAggregate]]
In the end, `planStreamingAggregation` creates the final aggregate physical operator (called `finalAndCompleteAggregate`) with:

* `requiredChildDistributionExpressions` based on the input `groupingExpressions`
* `initialInputBufferOffset` as the length of `groupingExpressions`
* `functionsWithoutDistinct` in `Final` mode
* `child` operator as <<saved, StateStoreSaveExec>> physical operator created above
