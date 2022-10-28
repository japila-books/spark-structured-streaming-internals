# MemoryStream

`MemoryStream` is a [MemoryStreamBase](MemoryStreamBase.md) and a [MicroBatchStream](../../MicroBatchStream.md) for [Micro-Batch Stream Processing](../../micro-batch-execution/index.md).

## Demo

```scala
import org.apache.spark.sql.execution.streaming.MemoryStream

implicit val sqlContext = spark.sqlContext
val input1 = MemoryStream[String](numPartitions = 8)
val input2 = MemoryStream[String]

val df1 = input1.toDF()
  .select($"value", $"value")

val stateFunc: (K, Iterator[V], GroupState[S]) => U): Dataset[U] = ???
val df2 = input2.toDS()
  .groupByKey(identity)
  .mapGroupsWithState(stateFunc)
  .toDF()
```

## Creating Instance

`MemoryStream` takes the following to be created:

* <span id="id"> ID
* <span id="sqlContext"> `SQLContext` ([Spark SQL]({{ book.spark_sql }}/SQLContext))
* [Number of partitions](#numPartitions)

`MemoryStream` is created using [apply](#apply) factory.

### <span id="numPartitions"> Number of Partitions

`MemoryStream` can be given the number of partitions when [created](#creating-instance). It is undefined (`None`) by default.

When specified, `MemoryStream` uses the number of partition in [planInputPartitions](#planInputPartitions) to redistribute rows into the given number of partition, via round-robin manner.

## <span id="apply"> Creating MemoryStream

```scala
apply[A : Encoder](
  numPartitions: Int)(
  implicit sqlContext: SQLContext): MemoryStream[A]
apply[A : Encoder](
  implicit sqlContext: SQLContext): MemoryStream[A]
```

`apply` creates a [MemoryStream](#creating-instance) with a unique [id](#id) (using an internal `AtomicInteger` counter).

## <span id="planInputPartitions"> Input Partitions

```scala
planInputPartitions(
  start: OffsetV2,
  end: OffsetV2): Array[InputPartition]
```

`planInputPartitions` is part of the [MicroBatchStream](../../MicroBatchStream.md#planInputPartitions) abstraction.

---

`planInputPartitions`...FIXME

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.MemoryStream` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.execution.streaming.MemoryStream=ALL
```

Refer to [Logging](../../spark-logging.md).

<!---
## Review Me

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
-->
