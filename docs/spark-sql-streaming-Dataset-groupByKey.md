== [[groupByKey]] groupByKey Operator -- Streaming Aggregation

* <<introduction, Introduction>>
* <<example-aggregating-orders-per-zip-code, Example: Aggregating Orders Per Zip Code>>
* <<example-aggregating-metrics-per-device, Example: Aggregating Metrics Per Device>>

=== [[introduction]] Introduction

[source, scala]
----
groupByKey[K: Encoder](func: T => K): KeyValueGroupedDataset[K, T]
----

`groupByKey` operator creates a <<spark-sql-streaming-KeyValueGroupedDataset.md#, KeyValueGroupedDataset>> (with keys of type `K` and rows of type `T`) to apply aggregation functions over groups of rows (of type `T`) by key (of type `K`) per the given `func` key-generating function.

NOTE: The type of the input argument of `func` is the type of rows in the Dataset (i.e. `Dataset[T]`).

`groupByKey` simply applies the `func` function to every row (of type `T`) and associates it with a logical group per key (of type `K`).

[source, scala]
----
func: T => K
----

Internally, `groupByKey` creates a structured query with the `AppendColumns` unary logical operator (with the given `func` and the analyzed logical plan of the target `Dataset` that `groupByKey` was executed on) and creates a new `QueryExecution`.

In the end, `groupByKey` creates a <<spark-sql-streaming-KeyValueGroupedDataset.md#, KeyValueGroupedDataset>> with the following:

* Encoders for `K` keys and `T` rows

* The new `QueryExecution` (with the `AppendColumns` unary logical operator)

* The output schema of the analyzed logical plan

* The new columns of the `AppendColumns` logical operator (i.e. the attributes of the key)

[source, scala]
----
scala> :type sq
org.apache.spark.sql.Dataset[Long]

val baseCode = 'A'.toInt
val byUpperChar = (n: java.lang.Long) => (n % 3 + baseCode).toString
val kvs = sq.groupByKey(byUpperChar)

scala> :type kvs
org.apache.spark.sql.KeyValueGroupedDataset[String,Long]

// Peeking under the surface of KeyValueGroupedDataset
import org.apache.spark.sql.catalyst.plans.logical.AppendColumns
val appendColumnsOp = kvs.queryExecution.analyzed.collect { case ac: AppendColumns => ac }.head
scala> println(appendColumnsOp.newColumns)
List(value#7)
----

=== [[example-aggregating-orders-per-zip-code]] Example: Aggregating Orders Per Zip Code

Go to <<spark-sql-streaming-demo-groupByKey-count-Update.md#, Demo: groupByKey Streaming Aggregation in Update Mode>>.

=== [[example-aggregating-metrics-per-device]] Example: Aggregating Metrics Per Device

The following example code shows how to apply `groupByKey` operator to a structured stream of timestamped values of different devices.

[source, scala]
----
// input stream
import java.sql.Timestamp
val signals = spark.
  readStream.
  format("rate").
  option("rowsPerSecond", 1).
  load.
  withColumn("value", $"value" % 10)  // <-- randomize the values (just for fun)
  withColumn("deviceId", lit(util.Random.nextInt(10))). // <-- 10 devices randomly assigned to values
  as[(Timestamp, Long, Int)] // <-- convert to a "better" type (from "unpleasant" Row)

// stream processing using groupByKey operator
// groupByKey(func: ((Timestamp, Long, Int)) => K): KeyValueGroupedDataset[K, (Timestamp, Long, Int)]
// K becomes Int which is a device id
val deviceId: ((Timestamp, Long, Int)) => Int = { case (_, _, deviceId) => deviceId }
scala> val signalsByDevice = signals.groupByKey(deviceId)
signalsByDevice: org.apache.spark.sql.KeyValueGroupedDataset[Int,(java.sql.Timestamp, Long, Int)] = org.apache.spark.sql.KeyValueGroupedDataset@19d40bc6
----
