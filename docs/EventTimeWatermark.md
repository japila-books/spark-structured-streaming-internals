# EventTimeWatermark Unary Logical Operator

`EventTimeWatermark` is a unary logical operator that is <<creating-instance, created>> to represent [Dataset.withWatermark](operators/withWatermark.md) operator in a logical query plan of a streaming query.

[NOTE]
====
A unary logical operator (`UnaryNode`) is a logical operator with a single <<child, child>> logical operator.

Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-LogicalPlan.html[UnaryNode] (and logical operators in general) in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.
====

When requested for the <<output, output attributes>>, `EventTimeWatermark` logical operator goes over the output attributes of the <<child, child>> logical operator to find the matching attribute based on the <<eventTime, eventTime>> attribute and updates it to include `spark.watermarkDelayMs` metadata key with the <<delay, watermark delay>> interval (<<getDelayMs, converted to milliseconds>>).

`EventTimeWatermark` is resolved (_planned_) to [EventTimeWatermarkExec](physical-operators/EventTimeWatermarkExec.md) physical operator in [StatefulAggregationStrategy](StatefulAggregationStrategy.md) execution planning strategy.

[NOTE]
====
`EliminateEventTimeWatermark` logical optimization rule (i.e. `Rule[LogicalPlan]`) removes `EventTimeWatermark` logical operator from a logical plan if the <<child, child>> logical operator is not streaming, i.e. when [Dataset.withWatermark](operators/withWatermark.md) operator is used on a batch query.

```text
val logs = spark.
  read. // <-- batch non-streaming query that makes `EliminateEventTimeWatermark` rule applicable
  format("text").
  load("logs")

// logs is a batch Dataset
assert(!logs.isStreaming)

val q = logs.
  withWatermark(eventTime = "timestamp", delayThreshold = "30 seconds") // <-- creates EventTimeWatermark
scala> println(q.queryExecution.logical.numberedTreeString) // <-- no EventTimeWatermark as it was removed immediately
00 Relation[value#0] text
```
====

=== [[creating-instance]] Creating EventTimeWatermark Instance

`EventTimeWatermark` takes the following to be created:

* [[eventTime]] Watermark column (`Attribute`)
* [[delay]] Watermark delay (`CalendarInterval`)
* [[child]] Child logical operator (`LogicalPlan`)

=== [[output]] Output Schema -- `output` Property

[source, scala]
----
output: Seq[Attribute]
----

NOTE: `output` is part of the `QueryPlan` Contract to describe the attributes of (the schema of) the output.

`output` finds <<eventTime, eventTime>> column in the output schema of the <<child, child>> logical operator and updates the `Metadata` of the column with <<delayKey, spark.watermarkDelayMs>> key and the milliseconds for the delay.

`output` removes <<delayKey, spark.watermarkDelayMs>> key from the other columns.

[source, scala]
----
// FIXME How to access/show the eventTime column with the metadata updated to include spark.watermarkDelayMs?
import org.apache.spark.sql.catalyst.plans.logical.EventTimeWatermark
val etw = q.queryExecution.logical.asInstanceOf[EventTimeWatermark]
scala> etw.output.toStructType.printTreeString
root
 |-- timestamp: timestamp (nullable = true)
 |-- value: long (nullable = true)
----

=== [[watermarkDelayMs]][[delayKey]] Watermark Metadata (Marker) -- `spark.watermarkDelayMs` Metadata Key

`spark.watermarkDelayMs` metadata key is used to mark one of the <<output, output attributes>> as the *watermark attribute* (_eventTime watermark_).

=== [[getDelayMs]] Converting Human-Friendly CalendarInterval to Milliseconds -- `getDelayMs` Object Method

[source, scala]
----
getDelayMs(
  delay: CalendarInterval): Long
----

`getDelayMs`...FIXME

NOTE: `getDelayMs` is used when...FIXME
