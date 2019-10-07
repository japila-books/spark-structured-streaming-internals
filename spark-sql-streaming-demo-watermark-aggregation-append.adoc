== Demo: Streaming Watermark with Aggregation in Append Output Mode

The following demo shows the behaviour and the internals of <<spark-sql-streaming-watermark.adoc#, streaming watermark>> with a <<spark-sql-streaming-aggregation.adoc#, streaming aggregation>> in <<spark-sql-streaming-OutputMode.adoc#Append, Append>> output mode.

The demo also shows the behaviour and the internals of <<spark-sql-streaming-StateStoreSaveExec.adoc#, StateStoreSaveExec>> physical operator in <<spark-sql-streaming-StateStoreSaveExec.adoc#doExecute-Append, Append output mode>>.

TIP: The below code is part of https://github.com/jaceklaskowski/spark-structured-streaming-book/blob/v{{ book.version }}/examples/src/main/scala/pl/japila/spark/StreamingAggregationAppendMode.scala[StreamingAggregationAppendMode] streaming application.

[source, scala]
----
// Reduce the number of partitions and hence the state stores
// That is supposed to make debugging state checkpointing easier
val numShufflePartitions = 1
import org.apache.spark.sql.internal.SQLConf.SHUFFLE_PARTITIONS
spark.sessionState.conf.setConf(SHUFFLE_PARTITIONS, numShufflePartitions)
assert(spark.sessionState.conf.numShufflePartitions == numShufflePartitions)

// Define event "format"
// Use :paste mode in spark-shell
import java.sql.Timestamp
case class Event(time: Timestamp, value: Long, batch: Long)
import scala.concurrent.duration._
object Event {
  def apply(secs: Long, value: Long, batch: Long): Event = {
    Event(new Timestamp(secs.seconds.toMillis), value, batch)
  }
}

// Using memory data source for full control of the input
import org.apache.spark.sql.execution.streaming.MemoryStream
implicit val sqlCtx = spark.sqlContext
val events = MemoryStream[Event]
val values = events.toDS
assert(values.isStreaming, "values must be a streaming Dataset")

values.printSchema
/**
root
 |-- time: timestamp (nullable = true)
 |-- value: long (nullable = false)
 |-- batch: long (nullable = false)
*/

// Streaming aggregation using groupBy operator to demo StateStoreSaveExec operator
// Define required watermark for late events for Append output mode

import scala.concurrent.duration._
val delayThreshold = 10.seconds
val eventTime = "time"

val valuesWatermarked = values
  .withWatermark(eventTime, delayThreshold.toString) // defines watermark (before groupBy!)

// EventTimeWatermark logical operator is planned as EventTimeWatermarkExec physical operator
// Note that as a physical operator EventTimeWatermarkExec shows itself without the Exec suffix
valuesWatermarked.explain
/**
== Physical Plan ==
EventTimeWatermark time#3: timestamp, interval 10 seconds
+- StreamingRelation MemoryStream[time#3,value#4L,batch#5L], [time#3, value#4L, batch#5L]
*/

val windowDuration = 5.seconds
import org.apache.spark.sql.functions.window
val countsPer5secWindow = valuesWatermarked
  .groupBy(window(col(eventTime), windowDuration.toString) as "sliding_window")
  .agg(collect_list("batch") as "batches", collect_list("value") as "values")

countsPer5secWindow.printSchema
/**
root
 |-- sliding_window: struct (nullable = false)
 |    |-- start: timestamp (nullable = true)
 |    |-- end: timestamp (nullable = true)
 |-- batches: array (nullable = true)
 |    |-- element: long (containsNull = true)
 |-- values: array (nullable = true)
 |    |-- element: long (containsNull = true)
*/

// valuesPerGroupWindowed is a streaming Dataset with just one source
// It knows nothing about output mode or watermark yet
// That's why StatefulOperatorStateInfo is generic
// and no batch-specific values are printed out
// That will be available after the first streaming batch
// Use sq.explain to know the runtime-specific values
countsPer5secWindow.explain
/**
== Physical Plan ==
ObjectHashAggregate(keys=[window#23-T10000ms], functions=[collect_list(batch#5L, 0, 0), collect_list(value#4L, 0, 0)])
+- StateStoreSave [window#23-T10000ms], state info [ checkpoint = <unknown>, runId = 50e62943-fe5d-4a02-8498-7134ecbf5122, opId = 0, ver = 0, numPartitions = 1], Append, 0, 2
   +- ObjectHashAggregate(keys=[window#23-T10000ms], functions=[merge_collect_list(batch#5L, 0, 0), merge_collect_list(value#4L, 0, 0)])
      +- StateStoreRestore [window#23-T10000ms], state info [ checkpoint = <unknown>, runId = 50e62943-fe5d-4a02-8498-7134ecbf5122, opId = 0, ver = 0, numPartitions = 1], 2
         +- ObjectHashAggregate(keys=[window#23-T10000ms], functions=[merge_collect_list(batch#5L, 0, 0), merge_collect_list(value#4L, 0, 0)])
            +- Exchange hashpartitioning(window#23-T10000ms, 1)
               +- ObjectHashAggregate(keys=[window#23-T10000ms], functions=[partial_collect_list(batch#5L, 0, 0), partial_collect_list(value#4L, 0, 0)])
                  +- *(1) Project [named_struct(start, precisetimestampconversion(((((CASE WHEN (cast(CEIL((cast((precisetimestampconversion(time#3-T10000ms, TimestampType, LongType) - 0) as double) / 5000000.0)) as double) = (cast((precisetimestampconversion(time#3-T10000ms, TimestampType, LongType) - 0) as double) / 5000000.0)) THEN (CEIL((cast((precisetimestampconversion(time#3-T10000ms, TimestampType, LongType) - 0) as double) / 5000000.0)) + 1) ELSE CEIL((cast((precisetimestampconversion(time#3-T10000ms, TimestampType, LongType) - 0) as double) / 5000000.0)) END + 0) - 1) * 5000000) + 0), LongType, TimestampType), end, precisetimestampconversion(((((CASE WHEN (cast(CEIL((cast((precisetimestampconversion(time#3-T10000ms, TimestampType, LongType) - 0) as double) / 5000000.0)) as double) = (cast((precisetimestampconversion(time#3-T10000ms, TimestampType, LongType) - 0) as double) / 5000000.0)) THEN (CEIL((cast((precisetimestampconversion(time#3-T10000ms, TimestampType, LongType) - 0) as double) / 5000000.0)) + 1) ELSE CEIL((cast((precisetimestampconversion(time#3-T10000ms, TimestampType, LongType) - 0) as double) / 5000000.0)) END + 0) - 1) * 5000000) + 5000000), LongType, TimestampType)) AS window#23-T10000ms, value#4L, batch#5L]
                     +- *(1) Filter isnotnull(time#3-T10000ms)
                        +- EventTimeWatermark time#3: timestamp, interval 10 seconds
                           +- StreamingRelation MemoryStream[time#3,value#4L,batch#5L], [time#3, value#4L, batch#5L]
*/

val queryName = "watermark_demo"
val checkpointLocation = s"/tmp/checkpoint-$queryName"

// Delete the checkpoint location from previous executions
import java.nio.file.{Files, FileSystems}
import java.util.Comparator
import scala.collection.JavaConverters._
val path = FileSystems.getDefault.getPath(checkpointLocation)
if (Files.exists(path)) {
  Files.walk(path)
    .sorted(Comparator.reverseOrder())
    .iterator
    .asScala
    .foreach(p => p.toFile.delete)
}

// FIXME Use foreachBatch for batchId and the output Dataset
// Start the query and hence StateStoreSaveExec
import scala.concurrent.duration._
import org.apache.spark.sql.streaming.OutputMode
val streamingQuery = countsPer5secWindow
  .writeStream
  .format("memory")
  .queryName(queryName)
  .option("checkpointLocation", checkpointLocation)
  .outputMode(OutputMode.Append) // <-- Use Append output mode
  .start

assert(streamingQuery.status.message == "Waiting for data to arrive")

type Millis = Long
def toMillis(datetime: String): Millis = {
  import java.time.format.DateTimeFormatter
  import java.time.LocalDateTime
  import java.time.ZoneOffset
  LocalDateTime
    .parse(datetime, DateTimeFormatter.ISO_DATE_TIME)
    .toInstant(ZoneOffset.UTC)
    .toEpochMilli
}

// Use web UI to monitor the state of state (no pun intended)
// StateStoreSave and StateStoreRestore operators all have state metrics
// Go to http://localhost:4040/SQL/ and click one of the Completed Queries with Job IDs

// You may also want to check out checkpointed state
// in /tmp/checkpoint-watermark_demo/state/0/0

// The demo is aimed to show the following:
// 1. The current watermark
// 2. Check out the stats:
// - expired state (below the current watermark, goes to output and purged later)
// - late state (dropped as if never received and processed)
// - saved state rows (above the current watermark)

val batch = Seq(
  Event(1,  1, batch = 1),
  Event(15, 2, batch = 1))
events.addData(batch)
streamingQuery.processAllAvailable()

println(streamingQuery.lastProgress.stateOperators(0).prettyJson)
/**
{
  "numRowsTotal" : 1,
  "numRowsUpdated" : 0,
  "memoryUsedBytes" : 1102,
  "customMetrics" : {
    "loadedMapCacheHitCount" : 2,
    "loadedMapCacheMissCount" : 0,
    "stateOnCurrentVersionSizeBytes" : 414
  }
}
*/

val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
val currentWatermarkMs = toMillis(currentWatermark)

val maxTime = batch.maxBy(_.time.toInstant.toEpochMilli).time.toInstant.toEpochMilli.millis.toSeconds
val expectedMaxTime = 15
assert(maxTime == expectedMaxTime, s"Maximum time across events per batch is $maxTime, but should be $expectedMaxTime")

val expectedWatermarkMs = 5.seconds.toMillis
assert(currentWatermarkMs == expectedWatermarkMs, s"Current event-time watermark is $currentWatermarkMs, but should be $expectedWatermarkMs (maximum event time ${maxTime.seconds.toMillis} minus delayThreshold ${delayThreshold.toMillis})")

// FIXME Saved State Rows
// Use the metrics of the StateStoreSave operator
// Or simply streamingQuery.lastProgress.stateOperators.head
spark.table(queryName).orderBy("sliding_window").show(truncate = false)
/**
+------------------------------------------+-------+------+
|sliding_window                            |batches|values|
+------------------------------------------+-------+------+
|[1970-01-01 01:00:00, 1970-01-01 01:00:05]|[1]    |[1]   |
+------------------------------------------+-------+------+
*/

// With at least one execution we can review the execution plan
streamingQuery.explain
/**
scala> streamingQuery.explain
== Physical Plan ==
ObjectHashAggregate(keys=[window#18-T10000ms], functions=[collect_list(batch#5L, 0, 0), collect_list(value#4L, 0, 0)])
+- StateStoreSave [window#18-T10000ms], state info [ checkpoint = file:/tmp/checkpoint-watermark_demo/state, runId = 73bb0ede-20f2-400d-8003-aa2fbebdd2e1, opId = 0, ver = 1, numPartitions = 1], Append, 5000, 2
   +- ObjectHashAggregate(keys=[window#18-T10000ms], functions=[merge_collect_list(batch#5L, 0, 0), merge_collect_list(value#4L, 0, 0)])
      +- StateStoreRestore [window#18-T10000ms], state info [ checkpoint = file:/tmp/checkpoint-watermark_demo/state, runId = 73bb0ede-20f2-400d-8003-aa2fbebdd2e1, opId = 0, ver = 1, numPartitions = 1], 2
         +- ObjectHashAggregate(keys=[window#18-T10000ms], functions=[merge_collect_list(batch#5L, 0, 0), merge_collect_list(value#4L, 0, 0)])
            +- Exchange hashpartitioning(window#18-T10000ms, 1)
               +- ObjectHashAggregate(keys=[window#18-T10000ms], functions=[partial_collect_list(batch#5L, 0, 0), partial_collect_list(value#4L, 0, 0)])
                  +- *(1) Project [named_struct(start, precisetimestampconversion(((((CASE WHEN (cast(CEIL((cast((precisetimestampconversion(time#3-T10000ms, TimestampType, LongType) - 0) as double) / 5000000.0)) as double) = (cast((precisetimestampconversion(time#3-T10000ms, TimestampType, LongType) - 0) as double) / 5000000.0)) THEN (CEIL((cast((precisetimestampconversion(time#3-T10000ms, TimestampType, LongType) - 0) as double) / 5000000.0)) + 1) ELSE CEIL((cast((precisetimestampconversion(time#3-T10000ms, TimestampType, LongType) - 0) as double) / 5000000.0)) END + 0) - 1) * 5000000) + 0), LongType, TimestampType), end, precisetimestampconversion(((((CASE WHEN (cast(CEIL((cast((precisetimestampconversion(time#3-T10000ms, TimestampType, LongType) - 0) as double) / 5000000.0)) as double) = (cast((precisetimestampconversion(time#3-T10000ms, TimestampType, LongType) - 0) as double) / 5000000.0)) THEN (CEIL((cast((precisetimestampconversion(time#3-T10000ms, TimestampType, LongType) - 0) as double) / 5000000.0)) + 1) ELSE CEIL((cast((precisetimestampconversion(time#3-T10000ms, TimestampType, LongType) - 0) as double) / 5000000.0)) END + 0) - 1) * 5000000) + 5000000), LongType, TimestampType)) AS window#18-T10000ms, value#4L, batch#5L]
                     +- *(1) Filter isnotnull(time#3-T10000ms)
                        +- EventTimeWatermark time#3: timestamp, interval 10 seconds
                           +- LocalTableScan <empty>, [time#3, value#4L, batch#5L]
*/

import org.apache.spark.sql.execution.streaming.StreamingQueryWrapper
val engine = streamingQuery
  .asInstanceOf[StreamingQueryWrapper]
  .streamingQuery
import org.apache.spark.sql.execution.streaming.StreamExecution
assert(engine.isInstanceOf[StreamExecution])

val lastMicroBatch = engine.lastExecution
import org.apache.spark.sql.execution.streaming.IncrementalExecution
assert(lastMicroBatch.isInstanceOf[IncrementalExecution])

// Access executedPlan that is the optimized physical query plan ready for execution
// All streaming optimizations have been applied at this point
// We just need the EventTimeWatermarkExec physical operator
val plan = lastMicroBatch.executedPlan

// Let's find the EventTimeWatermarkExec physical operator in the plan
// There should be one only
import org.apache.spark.sql.execution.streaming.EventTimeWatermarkExec
val watermarkOp = plan.collect { case op: EventTimeWatermarkExec => op }.head

// Let's check out the event-time watermark stats
// They correspond to the concrete EventTimeWatermarkExec operator for a micro-batch
val stats = watermarkOp.eventTimeStats.value
import org.apache.spark.sql.execution.streaming.EventTimeStats
assert(stats.isInstanceOf[EventTimeStats])

println(stats)
/**
EventTimeStats(-9223372036854775808,9223372036854775807,0.0,0)
*/

val batch = Seq(
  Event(1,  1, batch = 2),
  Event(15, 2, batch = 2),
  Event(35, 3, batch = 2))
events.addData(batch)
streamingQuery.processAllAvailable()

val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
val currentWatermarkMs = toMillis(currentWatermark)

val maxTime = batch.maxBy(_.time.toInstant.toEpochMilli).time.toInstant.toEpochMilli.millis.toSeconds
val expectedMaxTime = 35
assert(maxTime == expectedMaxTime, s"Maximum time across events per batch is $maxTime, but should be $expectedMaxTime")

val expectedWatermarkMs = 25.seconds.toMillis
assert(currentWatermarkMs == expectedWatermarkMs, s"Current event-time watermark is $currentWatermarkMs, but should be $expectedWatermarkMs (maximum event time ${maxTime.seconds.toMillis} minus delayThreshold ${delayThreshold.toMillis})")

// FIXME Expired State
// FIXME Late Events
// FIXME Saved State Rows
spark.table(queryName).orderBy("sliding_window").show(truncate = false)
/**
+------------------------------------------+-------+------+
|sliding_window                            |batches|values|
+------------------------------------------+-------+------+
|[1970-01-01 01:00:00, 1970-01-01 01:00:05]|[1]    |[1]   |
|[1970-01-01 01:00:15, 1970-01-01 01:00:20]|[1, 2] |[2, 2]|
+------------------------------------------+-------+------+
*/

// Check out the event-time watermark stats
val plan = engine.lastExecution.executedPlan
import org.apache.spark.sql.execution.streaming.EventTimeWatermarkExec
val watermarkOp = plan.collect { case op: EventTimeWatermarkExec => op }.head
val stats = watermarkOp.eventTimeStats.value
import org.apache.spark.sql.execution.streaming.EventTimeStats
assert(stats.isInstanceOf[EventTimeStats])

println(stats)
/**
EventTimeStats(-9223372036854775808,9223372036854775807,0.0,0)
*/

val batch = Seq(
  Event(15,1, batch = 3),
  Event(15,2, batch = 3),
  Event(20,3, batch = 3),
  Event(26,4, batch = 3))
events.addData(batch)
streamingQuery.processAllAvailable()

val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
val currentWatermarkMs = toMillis(currentWatermark)

val maxTime = batch.maxBy(_.time.toInstant.toEpochMilli).time.toInstant.toEpochMilli.millis.toSeconds
val expectedMaxTime = 26
assert(maxTime == expectedMaxTime, s"Maximum time across events per batch is $maxTime, but should be $expectedMaxTime")

// Current event-time watermark should be the same as previously
// val expectedWatermarkMs = 25.seconds.toMillis
// The current max time is merely 26 so subtracting delayThreshold gives merely 16
assert(currentWatermarkMs == expectedWatermarkMs, s"Current event-time watermark is $currentWatermarkMs, but should be $expectedWatermarkMs (maximum event time ${maxTime.seconds.toMillis} minus delayThreshold ${delayThreshold.toMillis})")

// FIXME Expired State
// FIXME Late Events
// FIXME Saved State Rows
spark.table(queryName).orderBy("sliding_window").show(truncate = false)
/**
+------------------------------------------+-------+------+
|sliding_window                            |batches|values|
+------------------------------------------+-------+------+
|[1970-01-01 01:00:00, 1970-01-01 01:00:05]|[1]    |[1]   |
|[1970-01-01 01:00:15, 1970-01-01 01:00:20]|[1, 2] |[2, 2]|
+------------------------------------------+-------+------+
*/

// Check out the event-time watermark stats
val plan = engine.lastExecution.executedPlan
import org.apache.spark.sql.execution.streaming.EventTimeWatermarkExec
val watermarkOp = plan.collect { case op: EventTimeWatermarkExec => op }.head
val stats = watermarkOp.eventTimeStats.value
import org.apache.spark.sql.execution.streaming.EventTimeStats
assert(stats.isInstanceOf[EventTimeStats])

println(stats)
/**
EventTimeStats(26000,15000,19000.0,4)
*/

val batch = Seq(
  Event(36, 1, batch = 4))
events.addData(batch)
streamingQuery.processAllAvailable()

val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
val currentWatermarkMs = toMillis(currentWatermark)

val maxTime = batch.maxBy(_.time.toInstant.toEpochMilli).time.toInstant.toEpochMilli.millis.toSeconds
val expectedMaxTime = 36
assert(maxTime == expectedMaxTime, s"Maximum time across events per batch is $maxTime, but should be $expectedMaxTime")

val expectedWatermarkMs = 26.seconds.toMillis
assert(currentWatermarkMs == expectedWatermarkMs, s"Current event-time watermark is $currentWatermarkMs, but should be $expectedWatermarkMs (maximum event time ${maxTime.seconds.toMillis} minus delayThreshold ${delayThreshold.toMillis})")

// FIXME Expired State
// FIXME Late Events
// FIXME Saved State Rows
spark.table(queryName).orderBy("sliding_window").show(truncate = false)
/**
+------------------------------------------+-------+------+
|sliding_window                            |batches|values|
+------------------------------------------+-------+------+
|[1970-01-01 01:00:00, 1970-01-01 01:00:05]|[1]    |[1]   |
|[1970-01-01 01:00:15, 1970-01-01 01:00:20]|[1, 2] |[2, 2]|
+------------------------------------------+-------+------+
*/

// Check out the event-time watermark stats
val plan = engine.lastExecution.executedPlan
import org.apache.spark.sql.execution.streaming.EventTimeWatermarkExec
val watermarkOp = plan.collect { case op: EventTimeWatermarkExec => op }.head
val stats = watermarkOp.eventTimeStats.value
import org.apache.spark.sql.execution.streaming.EventTimeStats
assert(stats.isInstanceOf[EventTimeStats])

println(stats)
/**
EventTimeStats(-9223372036854775808,9223372036854775807,0.0,0)
*/

val batch = Seq(
  Event(50, 1, batch = 5)
)
events.addData(batch)
streamingQuery.processAllAvailable()

val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
val currentWatermarkMs = toMillis(currentWatermark)

val maxTime = batch.maxBy(_.time.toInstant.toEpochMilli).time.toInstant.toEpochMilli.millis.toSeconds
val expectedMaxTime = 50
assert(maxTime == expectedMaxTime, s"Maximum time across events per batch is $maxTime, but should be $expectedMaxTime")

val expectedWatermarkMs = 40.seconds.toMillis
assert(currentWatermarkMs == expectedWatermarkMs, s"Current event-time watermark is $currentWatermarkMs, but should be $expectedWatermarkMs (maximum event time ${maxTime.seconds.toMillis} minus delayThreshold ${delayThreshold.toMillis})")

// FIXME Expired State
// FIXME Late Events
// FIXME Saved State Rows
spark.table(queryName).orderBy("sliding_window").show(truncate = false)
/**
+------------------------------------------+-------+------+
|sliding_window                            |batches|values|
+------------------------------------------+-------+------+
|[1970-01-01 01:00:00, 1970-01-01 01:00:05]|[1]    |[1]   |
|[1970-01-01 01:00:15, 1970-01-01 01:00:20]|[1, 2] |[2, 2]|
|[1970-01-01 01:00:25, 1970-01-01 01:00:30]|[3]    |[4]   |
|[1970-01-01 01:00:35, 1970-01-01 01:00:40]|[2, 4] |[3, 1]|
+------------------------------------------+-------+------+
*/

// Check out the event-time watermark stats
val plan = engine.lastExecution.executedPlan
import org.apache.spark.sql.execution.streaming.EventTimeWatermarkExec
val watermarkOp = plan.collect { case op: EventTimeWatermarkExec => op }.head
val stats = watermarkOp.eventTimeStats.value
import org.apache.spark.sql.execution.streaming.EventTimeStats
assert(stats.isInstanceOf[EventTimeStats])

println(stats)
/**
EventTimeStats(-9223372036854775808,9223372036854775807,0.0,0)
*/

// Eventually...
streamingQuery.stop()
----
