---
hide:
  - navigation
---

# Demo: Internals of FlatMapGroupsWithStateExec Physical Operator

The following demo shows the internals of [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md) physical operator in a [Arbitrary Stateful Streaming Aggregation](../arbitrary-stateful-streaming-aggregation/index.md).

```text
// Reduce the number of partitions and hence the state stores
// That is supposed to make debugging state checkpointing easier
val numShufflePartitions = 1
import org.apache.spark.sql.internal.SQLConf.SHUFFLE_PARTITIONS
spark.sessionState.conf.setConf(SHUFFLE_PARTITIONS, numShufflePartitions)
assert(spark.sessionState.conf.numShufflePartitions == numShufflePartitions)

// Define event "format"
// Use :paste mode in spark-shell
import java.sql.Timestamp
case class Event(time: Timestamp, value: Long)
import scala.concurrent.duration._
object Event {
  def apply(secs: Long, value: Long): Event = {
    Event(new Timestamp(secs.seconds.toMillis), value)
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
*/

import scala.concurrent.duration._
val delayThreshold = 10.seconds
val valuesWatermarked = values
  .withWatermark(eventTime = "time", delayThreshold.toString) // required for EventTimeTimeout

// Could use Long directly, but...
// Let's use case class to make the demo a bit more advanced
case class Count(value: Long)

import java.sql.Timestamp
import org.apache.spark.sql.streaming.GroupState
val keyCounts = (key: Long, values: Iterator[(Timestamp, Long)], state: GroupState[Count]) => {
  println(s""">>> keyCounts(key = $key, state = ${state.getOption.getOrElse("<empty>")})""")
  println(s">>> >>> currentProcessingTimeMs: ${state.getCurrentProcessingTimeMs}")
  println(s">>> >>> currentWatermarkMs: ${state.getCurrentWatermarkMs}")
  println(s">>> >>> hasTimedOut: ${state.hasTimedOut}")
  val count = Count(values.length)
  Iterator((key, count))
}

import org.apache.spark.sql.streaming.{GroupStateTimeout, OutputMode}
val valuesCounted = valuesWatermarked
  .as[(Timestamp, Long)] // convert DataFrame to Dataset to make groupByKey easier to write
  .groupByKey { case (time, value) => value }
  .flatMapGroupsWithState(
    OutputMode.Update,
    timeoutConf = GroupStateTimeout.EventTimeTimeout)(func = keyCounts)
  .toDF("value", "count")

valuesCounted.explain
/**
== Physical Plan ==
*(2) Project [_1#928L AS value#931L, _2#929 AS count#932]
+- *(2) SerializeFromObject [assertnotnull(input[0, scala.Tuple2, true])._1 AS _1#928L, if (isnull(assertnotnull(input[0, scala.Tuple2, true])._2)) null else named_struct(value, assertnotnull(assertnotnull(input[0, scala.Tuple2, true])._2).value) AS _2#929]
   +- FlatMapGroupsWithState $line140.$read$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$Lambda$4117/181063008@d2cdc82, value#923: bigint, newInstance(class scala.Tuple2), [value#923L], [time#915-T10000ms, value#916L], obj#927: scala.Tuple2, state info [ checkpoint = <unknown>, runId = 9af3d00c-fe1f-46a0-8630-4e0d0af88042, opId = 0, ver = 0, numPartitions = 1], class[value[0]: bigint], 2, Update, EventTimeTimeout, 0, 0
      +- *(1) Sort [value#923L ASC NULLS FIRST], false, 0
         +- Exchange hashpartitioning(value#923L, 1)
            +- AppendColumns $line140.$read$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$Lambda$4118/2131767153@3e606b4c, newInstance(class scala.Tuple2), [input[0, bigint, false] AS value#923L]
               +- EventTimeWatermark time#915: timestamp, interval 10 seconds
                  +- StreamingRelation MemoryStream[time#915,value#916L], [time#915, value#916L]
*/

val queryName = "FlatMapGroupsWithStateExec_demo"
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

import org.apache.spark.sql.streaming.OutputMode.Update
val streamingQuery = valuesCounted
  .writeStream
  .format("memory")
  .queryName(queryName)
  .option("checkpointLocation", checkpointLocation)
  .outputMode(Update)
  .start

assert(streamingQuery.status.message == "Waiting for data to arrive")

// Use web UI to monitor the metrics of the streaming query
// Go to http://localhost:4040/SQL/ and click one of the Completed Queries with Job IDs

// You may also want to check out checkpointed state
// in /tmp/checkpoint-FlatMapGroupsWithStateExec_demo/state/0/0

val batch = Seq(
  Event(secs = 1,  value = 1),
  Event(secs = 15, value = 2))
events.addData(batch)
streamingQuery.processAllAvailable()

/**
>>> keyCounts(key = 1, state = <empty>)
>>> >>> currentProcessingTimeMs: 1561881557237
>>> >>> currentWatermarkMs: 0
>>> >>> hasTimedOut: false
>>> keyCounts(key = 2, state = <empty>)
>>> >>> currentProcessingTimeMs: 1561881557237
>>> >>> currentWatermarkMs: 0
>>> >>> hasTimedOut: false
*/

spark.table(queryName).show(truncate = false)
/**
+-----+-----+
|value|count|
+-----+-----+
|1    |[1]  |
|2    |[1]  |
+-----+-----+
*/

// With at least one execution we can review the execution plan
streamingQuery.explain
/**
== Physical Plan ==
*(2) Project [_1#928L AS value#931L, _2#929 AS count#932]
+- *(2) SerializeFromObject [assertnotnull(input[0, scala.Tuple2, true])._1 AS _1#928L, if (isnull(assertnotnull(input[0, scala.Tuple2, true])._2)) null else named_struct(value, assertnotnull(assertnotnull(input[0, scala.Tuple2, true])._2).value) AS _2#929]
   +- FlatMapGroupsWithState $line140.$read$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$Lambda$4117/181063008@d2cdc82, value#923: bigint, newInstance(class scala.Tuple2), [value#923L], [time#915-T10000ms, value#916L], obj#927: scala.Tuple2, state info [ checkpoint = file:/tmp/checkpoint-FlatMapGroupsWithStateExec_demo/state, runId = 95c3917c-2fd7-45b2-86f6-6c01f0115e1d, opId = 0, ver = 1, numPartitions = 1], class[value[0]: bigint], 2, Update, EventTimeTimeout, 1561881557499, 5000
      +- *(1) Sort [value#923L ASC NULLS FIRST], false, 0
         +- Exchange hashpartitioning(value#923L, 1)
            +- AppendColumns $line140.$read$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$Lambda$4118/2131767153@3e606b4c, newInstance(class scala.Tuple2), [input[0, bigint, false] AS value#923L]
               +- EventTimeWatermark time#915: timestamp, interval 10 seconds
                  +- LocalTableScan <empty>, [time#915, value#916L]
*/

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

val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
val currentWatermarkSecs = toMillis(currentWatermark).millis.toSeconds.seconds

val expectedWatermarkSecs = 5.seconds
assert(currentWatermarkSecs == expectedWatermarkSecs, s"Current event-time watermark is $currentWatermarkSecs, but should be $expectedWatermarkSecs (maximum event time - delayThreshold ${delayThreshold.toMillis})")

// Let's access the FlatMapGroupsWithStateExec physical operator
import org.apache.spark.sql.execution.streaming.StreamingQueryWrapper
import org.apache.spark.sql.execution.streaming.StreamExecution
val engine: StreamExecution = streamingQuery
  .asInstanceOf[StreamingQueryWrapper]
  .streamingQuery

import org.apache.spark.sql.execution.streaming.IncrementalExecution
val lastMicroBatch: IncrementalExecution = engine.lastExecution

// Access executedPlan that is the optimized physical query plan ready for execution
// All streaming optimizations have been applied at this point
val plan = lastMicroBatch.executedPlan

// Find the FlatMapGroupsWithStateExec physical operator
import org.apache.spark.sql.execution.streaming.FlatMapGroupsWithStateExec
val flatMapOp = plan.collect { case op: FlatMapGroupsWithStateExec => op }.head

// Display metrics
import org.apache.spark.sql.execution.metric.SQLMetric
def formatMetrics(name: String, metric: SQLMetric) = {
  val desc = metric.name.getOrElse("")
  val value = metric.value
  f"| $name%-30s | $desc%-69s | $value%-10s"
}
flatMapOp.metrics.map { case (name, metric) => formatMetrics(name, metric) }.foreach(println)
/**
| numTotalStateRows              | number of total state rows                                            | 0
| stateMemory                    | memory used by state total (min, med, max)                            | 390
| loadedMapCacheHitCount         | count of cache hit on states cache in provider                        | 1
| numOutputRows                  | number of output rows                                                 | 0
| stateOnCurrentVersionSizeBytes | estimated size of state only on current version total (min, med, max) | 102
| loadedMapCacheMissCount        | count of cache miss on states cache in provider                       | 0
| commitTimeMs                   | time to commit changes total (min, med, max)                          | -2
| allRemovalsTimeMs              | total time to remove rows total (min, med, max)                       | -2
| numUpdatedStateRows            | number of updated state rows                                          | 0
| allUpdatesTimeMs               | total time to update rows total (min, med, max)                       | -2
*/

val batch = Seq(
  Event(secs = 1,  value = 1),  // under the watermark (5000 ms) so it's disregarded
  Event(secs = 6,  value = 3))  // above the watermark so it should be counted
events.addData(batch)
streamingQuery.processAllAvailable()

/**
>>> keyCounts(key = 3, state = <empty>)
>>> >>> currentProcessingTimeMs: 1561881643568
>>> >>> currentWatermarkMs: 5000
>>> >>> hasTimedOut: false
*/

spark.table(queryName).show(truncate = false)
/**
+-----+-----+
|value|count|
+-----+-----+
|1    |[1]  |
|2    |[1]  |
|3    |[1]  |
+-----+-----+
*/

val batch = Seq(
  Event(secs = 17,  value = 3))  // advances the watermark
events.addData(batch)
streamingQuery.processAllAvailable()

/**
>>> keyCounts(key = 3, state = <empty>)
>>> >>> currentProcessingTimeMs: 1561881672887
>>> >>> currentWatermarkMs: 5000
>>> >>> hasTimedOut: false
*/

val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
val currentWatermarkSecs = toMillis(currentWatermark).millis.toSeconds.seconds

val expectedWatermarkSecs = 7.seconds
assert(currentWatermarkSecs == expectedWatermarkSecs, s"Current event-time watermark is $currentWatermarkSecs, but should be $expectedWatermarkSecs (maximum event time - delayThreshold ${delayThreshold.toMillis})")

spark.table(queryName).show(truncate = false)
/**
+-----+-----+
|value|count|
+-----+-----+
|1    |[1]  |
|2    |[1]  |
|3    |[1]  |
|3    |[1]  |
+-----+-----+
*/

val batch = Seq(
  Event(secs = 18,  value = 3))  // advances the watermark
events.addData(batch)
streamingQuery.processAllAvailable()

/**
>>> keyCounts(key = 3, state = <empty>)
>>> >>> currentProcessingTimeMs: 1561881778165
>>> >>> currentWatermarkMs: 7000
>>> >>> hasTimedOut: false
*/

// Eventually...
streamingQuery.stop()
```
