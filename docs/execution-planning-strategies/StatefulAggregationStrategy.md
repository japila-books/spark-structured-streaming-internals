# StatefulAggregationStrategy Execution Planning Strategy

`StatefulAggregationStrategy` is an execution planning strategy ([Spark SQL]({{ book.spark_sql }}/execution-planning-strategies/SparkStrategy)) to plan streaming queries with [EventTimeWatermark](../logical-operators/EventTimeWatermark.md) and `Aggregate` ([Spark SQL]({{ book.spark_sql }}/logical-operators/Aggregate)) logical operators.

Logical Operator | Physical Operator
-----------------|-------------------
 [EventTimeWatermark](../logical-operators/EventTimeWatermark.md) | [EventTimeWatermarkExec](../physical-operators/EventTimeWatermarkExec.md)
 `Aggregate` ([Spark SQL]({{ book.spark_sql }}/logical-operators/Aggregate)) | One of the following per [selection requirements](#createStreamingAggregate):<ul><li>`HashAggregateExec` ([Spark SQL]({{ book.spark_sql }}/physical-operators/HashAggregateExec))</li><li>`ObjectHashAggregateExec` ([Spark SQL]({{ book.spark_sql }}/physical-operators/ObjectHashAggregateExec))</li><li>`SortAggregateExec` ([Spark SQL]({{ book.spark_sql }}/physical-operators/SortAggregateExec))</li></ul>

`StatefulAggregationStrategy` is used when [IncrementalExecution](../IncrementalExecution.md) is requested to plan a streaming query.

## Accessing StatefulAggregationStrategy

`StatefulAggregationStrategy` is available using `SessionState`.

```scala
spark.sessionState.planner.StatefulAggregationStrategy
```

## <span id="planStreamingAggregation"> planStreamingAggregation

```scala
planStreamingAggregation(
  groupingExpressions: Seq[NamedExpression],
  functionsWithoutDistinct: Seq[AggregateExpression],
  resultExpressions: Seq[NamedExpression],
  stateFormatVersion: Int,
  child: SparkPlan): Seq[SparkPlan]
```

`planStreamingAggregation` [creates a streaming aggregate physical operator](#createStreamingAggregate) for `Partial` aggregation (with the given `child` physical operator as the child). The given `functionsWithoutDistinct` expressions are set up to work in `Partial` execution mode.

`planStreamingAggregation` [creates another streaming aggregate physical operator](#createStreamingAggregate) for `PartialMerge` aggregation (with the partial aggregate physical operator as the child). The given `functionsWithoutDistinct` expressions are set up to work in `PartialMerge` execution mode.

`planStreamingAggregation` creates a [StateStoreRestoreExec](../physical-operators/StateStoreRestoreExec.md) physical operator (with the partial-merge aggregate physical operator as the child).

`planStreamingAggregation` [creates another streaming aggregate physical operator](#createStreamingAggregate) for `PartialMerge` aggregation (with the `StateStoreRestoreExec` physical operator as the child). The given `functionsWithoutDistinct` expressions are set up to work in `PartialMerge` execution mode.

`planStreamingAggregation` creates a [StateStoreSaveExec](../physical-operators/StateStoreSaveExec.md) physical operator (with the last partial-merge aggregate physical operator as the child).

In the end, `planStreamingAggregation` [creates another streaming aggregate physical operator](#createStreamingAggregate) for `Final` aggregation (with the `StateStoreSaveExec` physical operator as the child). The given `functionsWithoutDistinct` expressions are set up to work in `Final` execution mode.

---

`planStreamingAggregation` is used when:

* `StatefulAggregationStrategy` execution planning strategy is planning a streaming query with `Aggregate` ([Spark SQL]({{ book.spark_sql }}/logical-operators/Aggregate)) logical operator with no session window

## <span id="planStreamingAggregationForSession"> planStreamingAggregationForSession

```scala
planStreamingAggregationForSession(
  groupingExpressions: Seq[NamedExpression],
  sessionExpression: NamedExpression,
  functionsWithoutDistinct: Seq[AggregateExpression],
  resultExpressions: Seq[NamedExpression],
  stateFormatVersion: Int,
  mergeSessionsInLocalPartition: Boolean,
  child: SparkPlan): Seq[SparkPlan]
```

`planStreamingAggregationForSession`...FIXME

---

`planStreamingAggregationForSession` is used when:

* `StatefulAggregationStrategy` execution planning strategy is planning a streaming query with `Aggregate` ([Spark SQL]({{ book.spark_sql }}/logical-operators/Aggregate)) logical operator with session window

## <span id="createStreamingAggregate"> Creating Streaming Aggregate Physical Operator

```scala
createStreamingAggregate(
  requiredChildDistributionExpressions: Option[Seq[Expression]] = None,
  groupingExpressions: Seq[NamedExpression] = Nil,
  aggregateExpressions: Seq[AggregateExpression] = Nil,
  aggregateAttributes: Seq[Attribute] = Nil,
  initialInputBufferOffset: Int = 0,
  resultExpressions: Seq[NamedExpression] = Nil,
  child: SparkPlan): SparkPlan
```

`createStreamingAggregate` creates one of the following physical operators:

* `HashAggregateExec` ([Spark SQL]({{ book.spark_sql }}/physical-operators/HashAggregateExec))
* `ObjectHashAggregateExec` ([Spark SQL]({{ book.spark_sql }}/physical-operators/ObjectHashAggregateExec))
* `SortAggregateExec` ([Spark SQL]({{ book.spark_sql }}/physical-operators/SortAggregateExec))

!!! note
    Learn more about the selection requirements in [The Internals of Spark SQL]({{ book.spark_sql }}/AggUtils/#createAggregate).

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
