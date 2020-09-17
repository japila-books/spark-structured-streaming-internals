== [[WatermarkSupport]] WatermarkSupport Contract -- Unary Physical Operators with Streaming Watermark Support

`WatermarkSupport` is the <<contract, abstraction>> of unary physical operators (`UnaryExecNode`) with support for streaming event-time watermark.

[NOTE]
====
*Watermark* (aka *"allowed lateness"*) is a moving threshold of *event time* and specifies what data to consider for aggregations, i.e. the threshold of late data so the engine can automatically drop incoming late data given event time and clean up old state accordingly.

Read the official documentation of Spark in http://spark.apache.org/docs/latest/structured-streaming-programming-guide.html#handling-late-data-and-watermarking[Handling Late Data and Watermarking].
====

[[properties]]
.WatermarkSupport's (Lazily-Initialized) Properties
[cols="1,3",options="header",width="100%"]
|===
| Property
| Description

| [[watermarkExpression]] `watermarkExpression`
a| Optional Catalyst expression that matches rows older than the event time watermark.

!!! note
    Use [withWatermark](operators/withWatermark.md) operator to specify streaming watermark.

---

When initialized, `watermarkExpression` finds spark-sql-streaming-EventTimeWatermark.md#watermarkDelayMs[spark.watermarkDelayMs] watermark attribute in the child output's metadata.

If found, `watermarkExpression` creates `evictionExpression` with the watermark attribute that is less than or equal <<eventTimeWatermark, eventTimeWatermark>>.

The watermark attribute may be of type `StructType`. If it is, `watermarkExpression` uses the first field as the watermark.

`watermarkExpression` prints out the following INFO message to the logs when spark-sql-streaming-EventTimeWatermark.md#watermarkDelayMs[spark.watermarkDelayMs] watermark attribute is found.

```text
INFO [physicalOperator]Exec: Filtering state store on: [evictionExpression]
```

NOTE: `physicalOperator` can be [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md), spark-sql-streaming-StateStoreSaveExec.md[StateStoreSaveExec] or spark-sql-streaming-StreamingDeduplicateExec.md[StreamingDeduplicateExec].

TIP: Enable INFO logging level for one of the stateful physical operators to see the INFO message in the logs.

| [[watermarkPredicateForData]] `watermarkPredicateForData`
| Optional `Predicate` that uses <<watermarkExpression, watermarkExpression>> and the child output to match rows older than the event-time watermark

| [[watermarkPredicateForKeys]] `watermarkPredicateForKeys`
| Optional `Predicate` that uses <<keyExpressions, keyExpressions>> to match rows older than the event time watermark.
|===

=== [[contract]] WatermarkSupport Contract

[source, scala]
----
package org.apache.spark.sql.execution.streaming

trait WatermarkSupport extends UnaryExecNode {
  // only required methods that have no implementation
  def eventTimeWatermark: Option[Long]
  def keyExpressions: Seq[Attribute]
}
----

.WatermarkSupport Contract
[cols="1,2",options="header",width="100%"]
|===
| Method
| Description

| [[eventTimeWatermark]] `eventTimeWatermark`
| Used mainly in <<watermarkExpression, watermarkExpression>> to create a `LessThanOrEqual` Catalyst binary expression that matches rows older than the watermark.

| [[keyExpressions]] `keyExpressions`
| Grouping keys (in [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md#keyExpressions)), duplicate keys (in spark-sql-streaming-StreamingDeduplicateExec.md#keyExpressions[StreamingDeduplicateExec]) or key attributes (in spark-sql-streaming-StateStoreSaveExec.md#keyExpressions[StateStoreSaveExec]) with at most one that may have spark-sql-streaming-EventTimeWatermark.md#watermarkDelayMs[spark.watermarkDelayMs] watermark attribute in metadata

Used in <<watermarkPredicateForKeys, watermarkPredicateForKeys>> to create a `Predicate` to match rows older than the event time watermark.

Used also when spark-sql-streaming-StateStoreSaveExec.md#doExecute[StateStoreSaveExec] and spark-sql-streaming-StreamingDeduplicateExec.md#doExecute[StreamingDeduplicateExec] physical operators are executed.
|===

=== [[removeKeysOlderThanWatermark]][[removeKeysOlderThanWatermark-StateStore]] Removing Keys From StateStore Older Than Watermark -- `removeKeysOlderThanWatermark` Method

[source, scala]
----
removeKeysOlderThanWatermark(store: StateStore): Unit
----

`removeKeysOlderThanWatermark` requests the input `store` for spark-sql-streaming-StateStore.md#getRange[all rows].

`removeKeysOlderThanWatermark` then uses <<watermarkPredicateForKeys, watermarkPredicateForKeys>> to spark-sql-streaming-StateStore.md#remove[remove matching rows from the store].

NOTE: `removeKeysOlderThanWatermark` is used exclusively when `StreamingDeduplicateExec` physical operator is requested to <<spark-sql-streaming-StreamingDeduplicateExec.md#doExecute, execute and generate a recipe for a distributed computation (as an RDD[InternalRow])>>.

=== [[removeKeysOlderThanWatermark-StreamingAggregationStateManager-store]] `removeKeysOlderThanWatermark` Method

[source, scala]
----
removeKeysOlderThanWatermark(
  storeManager: StreamingAggregationStateManager,
  store: StateStore): Unit
----

`removeKeysOlderThanWatermark`...FIXME

NOTE: `removeKeysOlderThanWatermark` is used exclusively when `StateStoreSaveExec` physical operator is requested to <<spark-sql-streaming-StateStoreSaveExec.md#doExecute, execute and generate a recipe for a distributed computation (as an RDD[InternalRow])>>.
