# EventTimeWatermark Logical Operator

`EventTimeWatermark` is a unary logical operator ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan#UnaryNode)) that represents [Dataset.withWatermark](../operators/withWatermark.md) operator (in a logical query plan of a streaming query).

`EventTimeWatermark` marks a [user-specified column](#eventTime) as holding the event time for a row.

## Creating Instance

`EventTimeWatermark` takes the following to be created:

* [User-Defined Event-Time Watermark Column](#eventTime)
* <span id="delay"> Watermark Delay Threshold
* <span id="child"> Child `LogicalPlan` ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan))

`EventTimeWatermark` is created when:

* [Dataset.withWatermark](../operators/withWatermark.md) operator is used

### <span id="eventTime"> User-Defined Event-Time Watermark Column

`EventTimeWatermark` is given an event-time `Attribute` ([Spark SQL]({{ book.spark_sql }}/expressions/Attribute)) when [created](#creating-instance).

The `Attribute` is an `UnresolvedAttribute` from [Dataset.withWatermark](../operators/withWatermark.md) operator (that is the target of and can immediately be removed using [EliminateEventTimeWatermark](#EliminateEventTimeWatermark) logical optimization).

The event time column has to be defined on a window or a timestamp and so the data type of an event time column can be as follows:

* `StructType` with `end` field of `TimestampType` type (for windowed aggregation)
* `TimestampType`

`EventTimeWatermark` is used when:

* `ProgressReporter` is requested to [extract execution statistics](../monitoring/ProgressReporter.md#extractExecutionStats) (that adds `watermark` entry with the [batchWatermarkMs](../OffsetSeqMetadata.md#batchWatermarkMs) of the [OffsetSeqMetadata](../monitoring/ProgressReporter.md#offsetSeqMetadata) of a micro-batch)

## Logical Optimizations

### <span id="EliminateEventTimeWatermark"> EliminateEventTimeWatermark

`EliminateEventTimeWatermark` logical optimization removes `EventTimeWatermark` logical operator from a logical plan if the [child](#child) logical operator is not streaming (i.e., when [Dataset.withWatermark](../operators/withWatermark.md) operator is used in a batch query).

```scala
val logs = spark.
  read. // <-- batch non-streaming query that makes `EliminateEventTimeWatermark` rule applicable
  format("text").
  load("logs")

// logs is a batch Dataset
assert(!logs.isStreaming)

val q = logs.
  withWatermark(eventTime = "timestamp", delayThreshold = "30 seconds") // <-- creates EventTimeWatermark
```

```text
scala> println(q.queryExecution.logical.numberedTreeString) // <-- no EventTimeWatermark as it was removed immediately
00 Relation[value#0] text
```

### <span id="PushPredicateThroughNonJoin"> PushPredicateThroughNonJoin

`PushPredicateThroughNonJoin` can optimize streaming queries with `EventTimeWatermark` (and push predicates down if they don't reference the [eventTime](#eventTime) column).

## Execution Planning

`EventTimeWatermark` is planned as [EventTimeWatermarkExec](../physical-operators/EventTimeWatermarkExec.md) physical operator by [StatefulAggregationStrategy](../execution-planning-strategies/StatefulAggregationStrategy.md) execution planning strategy.

## <span id="output"> Output Schema

```scala
output: Seq[Attribute]
```

`output` is part of the `QueryPlan` ([Spark SQL]({{ book.spark_sql }}/catalyst/QueryPlan#output)) abstraction.

---

When requested for the [output attributes](#output), `EventTimeWatermark` logical operator scans the output attributes of the [child](#child) logical operator to find the matching attribute based on the [eventTime](#eventTime) attribute and adds `spark.watermarkDelayMs` metadata key with the [watermark delay](#delay) interval (converted to milliseconds).

---

`output` finds the [eventTime](#eventTime) column in the output schema of the [child](#child) logical operator and updates the `Metadata` of the column with [spark.watermarkDelayMs](#delayKey) key and the milliseconds for the [watermark delay](#delay).

`output` removes [spark.watermarkDelayMs](#delayKey) key from the other columns (if there is any)

```text
// FIXME How to access/show the eventTime column with the metadata updated to include spark.watermarkDelayMs?
import org.apache.spark.sql.catalyst.plans.logical.EventTimeWatermark
val etw = q.queryExecution.logical.asInstanceOf[EventTimeWatermark]
scala> etw.output.toStructType.printTreeString
root
 |-- timestamp: timestamp (nullable = true)
 |-- value: long (nullable = true)
```

## <span id="watermarkDelayMs"><span id="delayKey"> Watermark Delay Metadata Marker

`spark.watermarkDelayMs` metadata key is used to mark one of the [output attributes](#output) as the **watermark attribute** (**eventTime watermark**).
