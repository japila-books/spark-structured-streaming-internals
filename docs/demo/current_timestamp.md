== Demo: current_timestamp Function For Processing Time in Streaming Queries

The demo shows what happens when you use `current_timestamp` function in your structured queries.

[NOTE]
====
The main motivation was to answer the question https://stackoverflow.com/q/46274593/1305344[How to achieve ingestion time?] in Spark Structured Streaming.

You're _very_ welcome to upvote the question and answers at your earliest convenience. Thanks!
====

Quoting the https://ci.apache.org/projects/flink/flink-docs-release-1.4/dev/event_time.html[Apache Flink documentation]:

> *Event time* is the time that each individual event occurred on its producing device. This time is typically embedded within the records before they enter Flink and that event timestamp can be extracted from the record.

That is exactly how event time is considered in [withWatermark](../operators/withWatermark.md) operator which you use to describe what column to use for event time. The column could be part of the input dataset or...generated.

And that is the moment where my confusion starts.

In order to generate the event time column for `withWatermark` operator you could use `current_timestamp` or `current_date` standard functions.

[source, scala]
----
// rate format gives event time
// but let's generate a brand new column with ours
// for demo purposes
val values = spark.
  readStream.
  format("rate").
  load.
  withColumn("current_timestamp", current_timestamp)
scala> values.printSchema
root
 |-- timestamp: timestamp (nullable = true)
 |-- value: long (nullable = true)
 |-- current_timestamp: timestamp (nullable = false)
----

Both are special for Spark Structured Streaming as `StreamExecution` spark-sql-streaming-MicroBatchExecution.md#runBatch-triggerLogicalPlan[replaces] their underlying Catalyst expressions, `CurrentTimestamp` and `CurrentDate` respectively, with `CurrentBatchTimestamp` expression and the time of the current batch.

[source, scala]
----
import org.apache.spark.sql.streaming.Trigger
import scala.concurrent.duration._
val sq = values.
  writeStream.
  format("console").
  option("truncate", false).
  trigger(Trigger.ProcessingTime(10.seconds)).
  start

// note the value of current_timestamp
// that corresponds to the batch time

-------------------------------------------
Batch: 1
-------------------------------------------
+-----------------------+-----+-------------------+
|timestamp              |value|current_timestamp  |
+-----------------------+-----+-------------------+
|2017-09-18 10:53:31.523|0    |2017-09-18 10:53:40|
|2017-09-18 10:53:32.523|1    |2017-09-18 10:53:40|
|2017-09-18 10:53:33.523|2    |2017-09-18 10:53:40|
|2017-09-18 10:53:34.523|3    |2017-09-18 10:53:40|
|2017-09-18 10:53:35.523|4    |2017-09-18 10:53:40|
|2017-09-18 10:53:36.523|5    |2017-09-18 10:53:40|
|2017-09-18 10:53:37.523|6    |2017-09-18 10:53:40|
|2017-09-18 10:53:38.523|7    |2017-09-18 10:53:40|
+-----------------------+-----+-------------------+

// Use web UI's SQL tab for the batch (Submitted column)
// or sq.recentProgress
scala> println(sq.recentProgress(1).timestamp)
2017-09-18T08:53:40.000Z

// Note current_batch_timestamp

scala> sq.explain(extended = true)
== Parsed Logical Plan ==
'Project [timestamp#2137, value#2138L, current_batch_timestamp(1505725650005, TimestampType, None) AS current_timestamp#50]
+- LogicalRDD [timestamp#2137, value#2138L], true

== Analyzed Logical Plan ==
timestamp: timestamp, value: bigint, current_timestamp: timestamp
Project [timestamp#2137, value#2138L, current_batch_timestamp(1505725650005, TimestampType, Some(Europe/Berlin)) AS current_timestamp#50]
+- LogicalRDD [timestamp#2137, value#2138L], true

== Optimized Logical Plan ==
Project [timestamp#2137, value#2138L, 1505725650005000 AS current_timestamp#50]
+- LogicalRDD [timestamp#2137, value#2138L], true

== Physical Plan ==
*Project [timestamp#2137, value#2138L, 1505725650005000 AS current_timestamp#50]
+- Scan ExistingRDD[timestamp#2137,value#2138L]
----

That _seems_ to be closer to processing time than ingestion time given the definition from the https://ci.apache.org/projects/flink/flink-docs-release-1.4/dev/event_time.html[Apache Flink documentation]:

> *Processing time* refers to the system time of the machine that is executing the respective operation.

> *Ingestion time* is the time that events enter Flink.

What do you think?
