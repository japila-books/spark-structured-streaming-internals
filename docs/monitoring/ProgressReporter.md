# ProgressReporter

`ProgressReporter` is an [abstraction](#contract) of [stream execution progress reporters](#implementations) that report the statistics of execution of a streaming query.

## Contract

### <span id="currentBatchId"> currentBatchId

```scala
currentBatchId: Long
```

ID of the active (_current_) streaming micro-batch

Used when...FIXME

### <span id="id"> id

```scala
id: UUID
```

[Universally unique identifier (UUID)](https://docs.oracle.com/javase/8/docs/api/java/util/UUID.html) of the streaming query (that remains unchanged between restarts)

Used when...FIXME

### <span id="lastExecution"> lastExecution

```scala
lastExecution: QueryExecution
```

`QueryExecution` of the streaming query

Used when...FIXME

### <span id="logicalPlan"> logicalPlan

```scala
logicalPlan: LogicalPlan
```

Logical query plan of the streaming query

Used when `ProgressReporter` is requested for the following:

* [extract statistics from the most recent query execution](#extractExecutionStats) (to add `watermark` metric for [streaming watermark](../EventTimeWatermark.md))

* [extractSourceToNumInputRows](#extractSourceToNumInputRows)

### <span id="name"> name

```scala
name: String
```

Name of the streaming query

### <span id="newData"> newData

```scala
newData: Map[SparkDataStream, LogicalPlan]
```

[SparkDataStream](../SparkDataStream.md)s with the new data (as a `LogicalPlan`)

### <span id="offsetSeqMetadata"> offsetSeqMetadata

```scala
offsetSeqMetadata: OffsetSeqMetadata
```

[OffsetSeqMetadata](../spark-sql-streaming-OffsetSeqMetadata.md) (with the current micro-batch [event-time watermark](../spark-sql-streaming-OffsetSeqMetadata.md#batchWatermarkMs) and [timestamp](../spark-sql-streaming-OffsetSeqMetadata.md#batchTimestampMs))

### <span id="postEvent"> postEvent

```scala
postEvent(
  event: StreamingQueryListener.Event): Unit
```

Posts [StreamingQueryListener.Event](StreamingQueryListener.md)

### <span id="runId"> runId

```scala
runId: UUID
```

[Universally unique identifier (UUID)](https://docs.oracle.com/javase/8/docs/api/java/util/UUID.html) of a single run of the streaming query (that changes every restart)

### <span id="sink"> sink

```scala
sink: Table
```

The one and only `Table` of the streaming query

### <span id="sinkCommitProgress"> sinkCommitProgress

```scala
sinkCommitProgress: Option[StreamWriterCommitProgress]
```

### <span id="sources"> sources

```scala
sources: Seq[SparkDataStream]
```

### <span id="sparkSession"> sparkSession

```scala
sparkSession: SparkSession
```

`SparkSession` of the streaming query

!!! tip
    Find out more on [SparkSession](https://jaceklaskowski.github.io/mastering-spark-sql-book/SparkSession/) in [The Internals of Spark SQL](https://jaceklaskowski.github.io/mastering-spark-sql-book) online book.

### <span id="triggerClock"> triggerClock

```scala
triggerClock: Clock
```

Clock of the streaming query

## Implementations

* [StreamExecution](../StreamExecution.md)

## <span id="noDataProgressEventInterval"> spark.sql.streaming.noDataProgressEventInterval

`ProgressReporter` uses the [spark.sql.streaming.noDataProgressEventInterval](../spark-sql-streaming-properties.md#spark.sql.streaming.noDataProgressEventInterval) configuration property to control how long to wait between two progress events when there is no data (default: `10000L`) when [finishing a trigger](#finishTrigger).

## Demo

```text
import org.apache.spark.sql.streaming.Trigger
import scala.concurrent.duration._
val sampleQuery = spark
  .readStream
  .format("rate")
  .load
  .writeStream
  .format("console")
  .option("truncate", false)
  .trigger(Trigger.ProcessingTime(10.seconds))
  .start

// Using public API
import org.apache.spark.sql.streaming.SourceProgress
scala> sampleQuery.
     |   lastProgress.
     |   sources.
     |   map { case sp: SourceProgress =>
     |     s"source = ${sp.description} => endOffset = ${sp.endOffset}" }.
     |   foreach(println)
source = RateSource[rowsPerSecond=1, rampUpTimeSeconds=0, numPartitions=8] => endOffset = 663

scala> println(sampleQuery.lastProgress.sources(0))
res40: org.apache.spark.sql.streaming.SourceProgress =
{
  "description" : "RateSource[rowsPerSecond=1, rampUpTimeSeconds=0, numPartitions=8]",
  "startOffset" : 333,
  "endOffset" : 343,
  "numInputRows" : 10,
  "inputRowsPerSecond" : 0.9998000399920015,
  "processedRowsPerSecond" : 200.0
}

// With a hack
import org.apache.spark.sql.execution.streaming.StreamingQueryWrapper
val offsets = sampleQuery.
  asInstanceOf[StreamingQueryWrapper].
  streamingQuery.
  availableOffsets.
  map { case (source, offset) =>
    s"source = $source => offset = $offset" }
scala> offsets.foreach(println)
source = RateSource[rowsPerSecond=1, rampUpTimeSeconds=0, numPartitions=8] => offset = 293
```

## <span id="progressBuffer"> StreamingQueryProgress Queue

```scala
progressBuffer: Queue[StreamingQueryProgress]
```

`progressBuffer` is a [scala.collection.mutable.Queue](https://www.scala-lang.org/api/2.12.x/scala/collection/mutable/Queue.html) of [StreamingQueryProgress](StreamingQueryProgress.md)es.

`progressBuffer` has a new `StreamingQueryProgress` added when `ProgressReporter` is requested to [update progress of a streaming query](#updateProgress).

The oldest `StreamingQueryProgress` is removed (_dequeued_) above [spark.sql.streaming.numRecentProgressUpdates](../spark-sql-streaming-properties.md#spark.sql.streaming.numRecentProgressUpdates) threshold.

`progressBuffer` is used when `ProgressReporter` is requested for the [last](#lastProgress) and the [recent StreamingQueryProgresses](#recentProgress).

## <span id="status"><span id="currentStatus"> Current StreamingQueryStatus

```scala
status: StreamingQueryStatus
```

`status` is the current [StreamingQueryStatus](StreamingQueryStatus.md).

`status` is used when `StreamingQueryWrapper` is requested for the [current status of a streaming query](../StreamingQuery.md#status).

## <span id="updateProgress"> Updating Progress of Streaming Query

```scala
updateProgress(
  newProgress: StreamingQueryProgress): Unit
```

`updateProgress` records the input `newProgress` and posts a [QueryProgressEvent](StreamingQueryListener.md#QueryProgressEvent) event.

![ProgressReporter's Reporting Query Progress](../images/ProgressReporter-updateProgress.png)

`updateProgress` adds the input `newProgress` to [progressBuffer](#progressBuffer).

`updateProgress` removes elements from [progressBuffer](#progressBuffer) if their number is or exceeds the value of [spark.sql.streaming.numRecentProgressUpdates](../spark-sql-streaming-properties.md#spark.sql.streaming.numRecentProgressUpdates) configuration property.

`updateProgress` [posts a QueryProgressEvent](#postEvent) (with the input `newProgress`).

`updateProgress` prints out the following INFO message to the logs:

```text
Streaming query made progress: [newProgress]
```

`updateProgress` is used when `ProgressReporter` is requested to [finish up a trigger](#finishTrigger).

## <span id="startTrigger"> Initializing Query Progress for New Trigger

```scala
startTrigger(): Unit
```

`startTrigger` prints out the following DEBUG message to the logs:

```text
Starting Trigger Calculation
```

.startTrigger's Internal Registry Changes For New Trigger
[cols="30,70",options="header",width="100%"]
|===
| Registry
| New Value

| <<lastTriggerStartTimestamp, lastTriggerStartTimestamp>>
| <<currentTriggerStartTimestamp, currentTriggerStartTimestamp>>

| <<currentTriggerStartTimestamp, currentTriggerStartTimestamp>>
| Requests the <<triggerClock, trigger clock>> for the current timestamp (in millis)

| <<currentStatus, currentStatus>>
| Enables (`true`) the `isTriggerActive` flag of the <<currentStatus, currentStatus>>

| <<currentTriggerStartOffsets, currentTriggerStartOffsets>>
| `null`

| <<currentTriggerEndOffsets, currentTriggerEndOffsets>>
| `null`

| <<currentDurationsMs, currentDurationsMs>>
| Clears the <<currentDurationsMs, currentDurationsMs>>

|===

`startTrigger` is used when:

* `MicroBatchExecution` stream execution engine is requested to [run an activated streaming query](../MicroBatchExecution.md#runActivatedStream) (at the [beginning of every trigger](../MicroBatchExecution.md#runActivatedStream-startTrigger))

* `ContinuousExecution` stream execution engine is requested to [run an activated streaming query](../ContinuousExecution.md#runContinuous) (at the beginning of every trigger)

`StreamExecution` starts [running batches](../StreamExecution.md#runStream) (as part of [TriggerExecutor](../StreamExecution.md#triggerExecutor) executing a batch runner).

## <span id="finishTrigger"> Finishing Up Streaming Batch (Trigger)

```scala
finishTrigger(hasNewData: Boolean): Unit
```

`finishTrigger` sets [currentTriggerEndTimestamp](#currentTriggerEndTimestamp) to the current time (using [triggerClock](#triggerClock)).

`finishTrigger` <<extractExecutionStats, extractExecutionStats>>.

`finishTrigger` calculates the *processing time* (in seconds) as the difference between the <<currentTriggerEndTimestamp, end>> and <<currentTriggerStartTimestamp, start>> timestamps.

`finishTrigger` calculates the *input time* (in seconds) as the difference between the start time of the <<currentTriggerStartTimestamp, current>> and <<lastTriggerStartTimestamp, last>> triggers.

.ProgressReporter's finishTrigger and Timestamps
image::images/ProgressReporter-finishTrigger-timestamps.png[align="center"]

`finishTrigger` prints out the following DEBUG message to the logs:

```text
Execution stats: [executionStats]
```

`finishTrigger` creates a <<SourceProgress, SourceProgress>> (aka source statistics) for <<sources, every source used>>.

`finishTrigger` creates a <<SinkProgress, SinkProgress>> (aka sink statistics) for the <<sink, sink>>.

`finishTrigger` creates a [StreamingQueryProgress](StreamingQueryProgress.md).

If there was any data (using the input `hasNewData` flag), `finishTrigger` resets <<lastNoDataProgressEventTime, lastNoDataProgressEventTime>> (i.e. becomes the minimum possible time) and <<updateProgress, updates query progress>>.

Otherwise, when no data was available (using the input `hasNewData` flag), `finishTrigger` <<updateProgress, updates query progress>> only when <<lastNoDataProgressEventTime, lastNoDataProgressEventTime>> passed.

In the end, `finishTrigger` disables `isTriggerActive` flag of <<currentStatus, StreamingQueryStatus>> (i.e. sets it to `false`).

NOTE: `finishTrigger` is used exclusively when `MicroBatchExecution` is requested to <<MicroBatchExecution.md#runActivatedStream, run the activated streaming query>> (after <<MicroBatchExecution.md#runActivatedStream-triggerExecution, triggerExecution Phase>> at the end of a streaming batch).

## <span id="reportTimeTaken"> Time-Tracking Section (Recording Execution Time)

```scala
reportTimeTaken[T](
  triggerDetailKey: String)(
  body: => T): T
```

`reportTimeTaken` measures the time to execute `body` and records it in the [currentDurationsMs](#currentDurationsMs) internal registry under `triggerDetailKey` key. If the `triggerDetailKey` key was recorded already, the current execution time is added.

In the end, `reportTimeTaken` prints out the following DEBUG message to the logs and returns the result of executing `body`.

```text
[triggerDetailKey] took [time] ms
```

`reportTimeTaken` is used when [stream execution engines](../StreamExecution.md) are requested to execute the following phases (that appear as `triggerDetailKey` in the DEBUG message in the logs):

* `MicroBatchExecution`
  * [triggerExecution](../MicroBatchExecution.md#runActivatedStream-triggerExecution)
  * [getOffset](../MicroBatchExecution.md#constructNextBatch-getOffset)
  * [setOffsetRange](../MicroBatchExecution.md#constructNextBatch-setOffsetRange)
  * [getEndOffset](../MicroBatchExecution.md#constructNextBatch-getEndOffset)
  * [walCommit](../MicroBatchExecution.md#constructNextBatch-walCommit)
  * [getBatch](../MicroBatchExecution.md#runBatch-getBatch)
  * [queryPlanning](../MicroBatchExecution.md#runBatch-queryPlanning)
  * [addBatch](../MicroBatchExecution.md#runBatch-addBatch)

* `ContinuousExecution`
  * [queryPlanning](../ContinuousExecution.md#runContinuous-queryPlanning)
  * [runContinuous](../ContinuousExecution.md#runContinuous-runContinuous)

## <span id="updateStatusMessage"> Updating Status Message

```scala
updateStatusMessage(
  message: String): Unit
```

`updateStatusMessage` simply updates the `message` in the [StreamingQueryStatus](#currentStatus) internal registry.

`updateStatusMessage` is used when:

* `StreamExecution` is requested to [run stream processing](../StreamExecution.md#runStream)

* `MicroBatchExecution` is requested to [run an activated streaming query](../MicroBatchExecution.md#runActivatedStream) or [construct the next streaming micro-batch](../MicroBatchExecution.md#constructNextBatch)

=== [[extractExecutionStats]] Generating Execution Statistics -- `extractExecutionStats` Internal Method

[source, scala]
----
extractExecutionStats(hasNewData: Boolean): ExecutionStats
----

`extractExecutionStats` generates an <<spark-sql-streaming-ExecutionStats.md#, ExecutionStats>> of the <<lastExecution, last execution>> of the streaming query.

Internally, `extractExecutionStats` generate *watermark* metric (using the <<spark-sql-streaming-OffsetSeqMetadata.md#batchWatermarkMs, event-time watermark>> of the <<offsetSeqMetadata, OffsetSeqMetadata>>) if there is a [EventTimeWatermark](../EventTimeWatermark.md) unary logical operator in the <<logicalPlan, logical plan>> of the streaming query.

`extractExecutionStats` <<extractStateOperatorMetrics, extractStateOperatorMetrics>>.

`extractExecutionStats` <<extractSourceToNumInputRows, extractSourceToNumInputRows>>.

`extractExecutionStats` finds the [EventTimeWatermarkExec](../EventTimeWatermarkExec.md) unary physical operator (with non-zero [EventTimeStats](../spark-sql-streaming-EventTimeStatsAccum.md)) and generates *max*, *min*, and *avg* statistics.

In the end, `extractExecutionStats` creates a <<spark-sql-streaming-ExecutionStats.md#, ExecutionStats>> with the execution statistics.

If the input `hasNewData` flag is turned off (`false`), `extractExecutionStats` returns an <<spark-sql-streaming-ExecutionStats.md#, ExecutionStats>> with no input rows and event-time statistics (that require data to be processed to have any sense).

NOTE: `extractExecutionStats` is used exclusively when `ProgressReporter` is requested to <<finishTrigger, finish up a streaming batch (trigger) and generate a StreamingQueryProgress>>.

=== [[extractStateOperatorMetrics]] Generating StateStoreWriter Metrics (StateOperatorProgress) -- `extractStateOperatorMetrics` Internal Method

[source, scala]
----
extractStateOperatorMetrics(
  hasNewData: Boolean): Seq[StateOperatorProgress]
----

`extractStateOperatorMetrics` requests the <<lastExecution, QueryExecution>> for the optimized execution plan (`executedPlan`) and finds all <<spark-sql-streaming-StateStoreWriter.md#, StateStoreWriter>> physical operators and requests them for <<spark-sql-streaming-StateStoreWriter.md#getProgress, StateOperatorProgress>>.

`extractStateOperatorMetrics` clears (_zeros_) the *numRowsUpdated* metric for the given `hasNewData` turned off (`false`).

`extractStateOperatorMetrics` returns an empty collection for the <<lastExecution, QueryExecution>> uninitialized (`null`).

NOTE: `extractStateOperatorMetrics` is used exclusively when `ProgressReporter` is requested to <<extractExecutionStats, generate execution statistics>>.

=== [[recordTriggerOffsets]] Recording Trigger Offsets (StreamProgress) -- `recordTriggerOffsets` Method

[source, scala]
----
recordTriggerOffsets(
  from: StreamProgress,
  to: StreamProgress): Unit
----

`recordTriggerOffsets` simply sets (_records_) the <<currentTriggerStartOffsets, currentTriggerStartOffsets>> and <<currentTriggerEndOffsets, currentTriggerEndOffsets>> internal registries to the <<spark-sql-streaming-Offset.md#json, json>> representations of the `from` and `to` <<spark-sql-streaming-StreamProgress.md#, StreamProgresses>>.

[NOTE]
====
`recordTriggerOffsets` is used when:

* `MicroBatchExecution` is requested to <<MicroBatchExecution.md#runActivatedStream, run the activated streaming query>>

* `ContinuousExecution` is requested to <<ContinuousExecution.md#commit, commit an epoch>>
====

## <span id="lastProgress"> Last StreamingQueryProgress

```scala
lastProgress: StreamingQueryProgress
```

The last [StreamingQueryProgress](StreamingQueryProgress.md)

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| currentDurationsMs
a| [[currentDurationsMs]] http://www.scala-lang.org/api/2.11.11/index.html#scala.collection.mutable.HashMap[scala.collection.mutable.HashMap] of action names (aka _triggerDetailKey_) and their cumulative times (in milliseconds).

Starts empty when `ProgressReporter` <<startTrigger, sets the state for a new batch>> with new entries added or updated when <<reportTimeTaken, reporting execution time>> (of an action).

[TIP]
====
You can see the current value of `currentDurationsMs` in progress reports under `durationMs`.

[options="wrap"]
----
scala> query.lastProgress.durationMs
res3: java.util.Map[String,Long] = {triggerExecution=60, queryPlanning=1, getBatch=5, getOffset=0, addBatch=30, walCommit=23}
----
====

| currentStatus
a| [[currentStatus]] [StreamingQueryStatus](StreamingQueryStatus.md) with the current status of the streaming query

Available using <<status, status>> method

* `message` updated with <<updateStatusMessage, updateStatusMessage>>

| currentTriggerEndOffsets
a| [[currentTriggerEndOffsets]]

| currentTriggerEndTimestamp
a| [[currentTriggerEndTimestamp]] Timestamp of when the current batch/trigger has ended

Default: `-1L`

| currentTriggerStartOffsets
a| [[currentTriggerStartOffsets]]

[source, scala]
----
currentTriggerStartOffsets: Map[BaseStreamingSource, String]
----

Start offsets (in <<spark-sql-streaming-Offset.md#json, JSON format>>) per <<spark-sql-streaming-BaseStreamingSource.md#, source>>

Used exclusively when <<finishTrigger, finishing up a streaming batch (trigger) and generating StreamingQueryProgress>> (for a [SourceProgress](SourceProgress.md))

Reset (`null`) when <<startTrigger, initializing a query progress for a new trigger>>

Initialized when <<recordTriggerOffsets, recording trigger offsets (StreamProgress)>>

| currentTriggerStartTimestamp
a| [[currentTriggerStartTimestamp]] Timestamp of when the current batch/trigger has started

Default: `-1L`

| lastNoDataProgressEventTime
a| [[lastNoDataProgressEventTime]]

Default: `Long.MinValue`

| lastTriggerStartTimestamp
a| [[lastTriggerStartTimestamp]] Timestamp of when the last batch/trigger started

Default: `-1L`

| metricWarningLogged
a| [[metricWarningLogged]] Flag to...FIXME

Default: `false`

|===

## Logging

Configure logging of the [concrete stream execution progress reporters](#implementations) to see what happens inside:

* [ContinuousExecution](../ContinuousExecution.md#logging)

* [MicroBatchExecution](../MicroBatchExecution.md#logging)
