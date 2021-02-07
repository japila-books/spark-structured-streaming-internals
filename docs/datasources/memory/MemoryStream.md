# MemoryStream -- Streaming Reader for Micro-Batch Stream Processing

`MemoryStream` is a concrete [streaming source](MemoryStreamBase.md) of [memory data source](index.md) that supports [reading](../../micro-batch-execution/MicroBatchReader.md) in [Micro-Batch Stream Processing](../../micro-batch-execution/index.md).

[[logging]]
[TIP]
====
Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.MemoryStream` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.execution.streaming.MemoryStream=ALL
```

Refer to <<spark-sql-streaming-spark-logging.md#, Logging>>.
====

=== [[creating-instance]] Creating MemoryStream Instance

`MemoryStream` takes the following to be created:

* [[id]] ID
* [[sqlContext]] `SQLContext`

`MemoryStream` initializes the <<internal-properties, internal properties>>.

=== [[apply]] Creating MemoryStream Instance -- `apply` Object Factory

[source, scala]
----
apply[A : Encoder](
  implicit sqlContext: SQLContext): MemoryStream[A]
----

`apply` uses an `memoryStreamId` internal counter to <<creating-instance, create a new MemoryStream>> with a unique <<id, ID>> and the implicit `SQLContext`.

=== [[addData]] Adding Data to Source -- `addData` Method

[source, scala]
----
addData(
  data: TraversableOnce[A]): Offset
----

`addData` adds the given `data` to the <<batches, batches>> internal registry.

Internally, `addData` prints out the following DEBUG message to the logs:

```
Adding: [data]
```

In the end, `addData` increments the <<currentOffset, current offset>> and adds the data to the <<batches, batches>> internal registry.

=== [[getBatch]] Generating Next Streaming Batch -- `getBatch` Method

`getBatch` is a part of the [Source](../../Source.md#getBatch) abstraction.

When executed, `getBatch` uses the internal <<batches, batches>> collection to return requested offsets.

You should see the following DEBUG message in the logs:

```
DEBUG MemoryStream: MemoryBatch [[startOrdinal], [endOrdinal]]: [newBlocks]
```

=== [[logicalPlan]] Logical Plan -- `logicalPlan` Internal Property

[source, scala]
----
logicalPlan: LogicalPlan
----

`logicalPlan` is part of the [MemoryStreamBase](MemoryStreamBase.md#logicalPlan) abstraction.

`logicalPlan` is simply a [StreamingExecutionRelation](../../logical-operators/StreamingExecutionRelation.md) (for this memory source and the [attributes](MemoryStreamBase.md#attributes)).

`MemoryStream` uses [StreamingExecutionRelation](../../logical-operators/StreamingExecutionRelation.md) logical plan to build Datasets or DataFrames when requested.

```text
scala> val ints = MemoryStream[Int]
ints: org.apache.spark.sql.execution.streaming.MemoryStream[Int] = MemoryStream[value#13]

scala> ints.toDS.queryExecution.logical.isStreaming
res14: Boolean = true

scala> ints.toDS.queryExecution.logical
res15: org.apache.spark.sql.catalyst.plans.logical.LogicalPlan = MemoryStream[value#13]
```

=== [[schema]] Schema (schema method)

`MemoryStream` works with the data of the spark-sql-schema.md[schema] as described by the spark-sql-Encoder.md[Encoder] (of the `Dataset`).

=== [[toString]] Textual Representation -- `toString` Method

[source, scala]
----
toString: String
----

NOTE: `toString` is part of the ++https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#toString--++[java.lang.Object] contract for the string representation of the object.

`toString` uses the <<output, output schema>> to return the following textual representation:

```
MemoryStream[[output]]
```

=== [[planInputPartitions]] Plan Input Partitions -- `planInputPartitions` Method

[source, scala]
----
planInputPartitions(): java.util.List[InputPartition[InternalRow]]
----

NOTE: `planInputPartitions` is part of the `DataSourceReader` contract in Spark SQL for the number of `InputPartitions` to use as RDD partitions (when `DataSourceV2ScanExec` physical operator is requested for the partitions of the input RDD).

`planInputPartitions`...FIXME

`planInputPartitions` prints out a DEBUG message to the logs with the <<generateDebugString, generateDebugString>> (with the batches after the <<lastOffsetCommitted, last committed offset>>).

`planInputPartitions`...FIXME

=== [[generateDebugString]] `generateDebugString` Internal Method

[source, scala]
----
generateDebugString(
  rows: Seq[UnsafeRow],
  startOrdinal: Int,
  endOrdinal: Int): String
----

`generateDebugString` resolves and binds the [encoder](MemoryStreamBase.md#encoder) for the data.

In the end, `generateDebugString` returns the following string:

```text
MemoryBatch [[startOrdinal], [endOrdinal]]: [rows]
```

NOTE: `generateDebugString` is used exclusively when `MemoryStream` is requested to <<planInputPartitions, planInputPartitions>>.

## Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| batches
a| [[batches]] Batch data (`ListBuffer[Array[UnsafeRow]]`)

| currentOffset
a| [[currentOffset]] Current [offset](../../Offset.md)

| lastOffsetCommitted
a| [[lastOffsetCommitted]] Last committed [offset](../../Offset.md)

| output
a| [[output]] Output schema (`Seq[Attribute]`) of the [logical query plan](#logicalPlan)

Used exclusively for <<toString, toString>>

|===
