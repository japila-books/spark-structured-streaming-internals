# ProgressReporter

`ProgressReporter` is an [abstraction](#contract) of [execution progress reporters](#implementations) that report statistics of execution of a [streaming query](#logicalPlan).

## Contract

### <span id="currentBatchId"> currentBatchId

```scala
currentBatchId: Long
```

ID of the micro-batch

Used when:

* `MicroBatchExecution` is requested to [plan a query for the batch](micro-batch-execution/MicroBatchExecution.md#runBatch-queryPlanning) (while [running a batch](micro-batch-execution/MicroBatchExecution.md#runBatch))
* `ContinuousExecution` is requested to [plan a query for the epoch](continuous-execution/ContinuousExecution.md#runContinuous-queryPlanning) (while [running continuously](micro-batch-execution/MicroBatchExecution.md#runContinuous))
* `ProgressReporter` is requested for a new [StreamingQueryProgress](monitoring/StreamingQueryProgress.md) (while [finishing a trigger](#finishTrigger))
* _other usage_

### <span id="id"> id

```scala
id: UUID
```

[Universally unique identifier (UUID)]({{ java.api }}/java/util/UUID.html) of the streaming query (that remains unchanged between restarts)

### <span id="lastExecution"> lastExecution

```scala
lastExecution: QueryExecution
```

[IncrementalExecution](IncrementalExecution.md) of the streaming execution round (a batch or an epoch)

`IncrementalExecution` is created and executed in the **queryPlanning** phase of [MicroBatchExecution](micro-batch-execution/MicroBatchExecution.md) and [ContinuousExecution](continuous-execution/ContinuousExecution.md) stream execution engines.

### <span id="logicalPlan"> logicalPlan

```scala
logicalPlan: LogicalPlan
```

Logical query plan of the streaming query

!!! important
    The most interesting usage of the `LogicalPlan` is when stream execution engines replace (_transform_) input [StreamingExecutionRelation](logical-operators/StreamingExecutionRelation.md) and [StreamingDataSourceV2Relation](logical-operators/StreamingDataSourceV2Relation.md) operators with (operators with) data or `LocalRelation` (to represent no data at a source).

Used when `ProgressReporter` is requested for the following:

* [extract statistics from the most recent query execution](#extractExecutionStats) (to add `watermark` metric for [streaming watermark](logical-operators/EventTimeWatermark.md))
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

[SparkDataStream](SparkDataStream.md)s (from all data [sources](#sources)) with the more recent unprocessed input data (as `LogicalPlan`)

Used exclusively for [MicroBatchExecution](micro-batch-execution/MicroBatchExecution.md) (when requested to [run a single micro-batch](micro-batch-execution/MicroBatchExecution.md#runBatch))

Used when `ProgressReporter` is requested to [extractSourceToNumInputRows](#extractSourceToNumInputRows)

### <span id="offsetSeqMetadata"> offsetSeqMetadata

```scala
offsetSeqMetadata: OffsetSeqMetadata
```

[OffsetSeqMetadata](OffsetSeqMetadata.md) (with the current micro-batch [event-time watermark](OffsetSeqMetadata.md#batchWatermarkMs) and [timestamp](OffsetSeqMetadata.md#batchTimestampMs))

### <span id="postEvent"> postEvent

```scala
postEvent(
  event: StreamingQueryListener.Event): Unit
```

Posts [StreamingQueryListener.Event](monitoring/StreamingQueryListener.md#Event)

Used when:

* `ProgressReporter` is requested to [update progress](#updateProgress) (and posts a [QueryProgressEvent](monitoring/StreamingQueryListener.md#QueryProgressEvent))
* `StreamExecution` is requested to [run stream processing](StreamExecution.md#runStream) (and posts a [QueryStartedEvent](monitoring/StreamingQueryListener.md#QueryStartedEvent) at the beginning and a [QueryTerminatedEvent](monitoring/StreamingQueryListener.md#QueryTerminatedEvent) after a query has been stopped)

### <span id="runId"> runId

```scala
runId: UUID
```

[Universally unique identifier (UUID)](https://docs.oracle.com/javase/8/docs/api/java/util/UUID.html) of a single run of the streaming query (that changes every restart)

### <span id="sink"> Sink

```scala
sink: Table
```

`Table` ([Spark SQL]({{ book.spark_sql }}/connector/Table)) this streaming query writes to

Used when:

* `ProgressReporter` is requested to [finish a streaming batch](#finishTrigger)

### <span id="sinkCommitProgress"> sinkCommitProgress

```scala
sinkCommitProgress: Option[StreamWriterCommitProgress]
```

`StreamWriterCommitProgress` with number of output rows:

* `None` when `MicroBatchExecution` stream execution engine is requested to [populateStartOffsets](micro-batch-execution/MicroBatchExecution.md#populateStartOffsets)

* Assigned a `StreamWriterCommitProgress` when `MicroBatchExecution` stream execution engine is about to complete [running a micro-batch](micro-batch-execution/MicroBatchExecution.md#runBatch)

Used when [finishTrigger](#finishTrigger) (and [updating progress](#updateProgress))

### <span id="sources"> SparkDataStreams

```scala
sources: Seq[SparkDataStream]
```

[SparkDataStream](SparkDataStream.md)s of this streaming query

### <span id="sparkSession"> sparkSession

```scala
sparkSession: SparkSession
```

`SparkSession` ([Spark SQL]({{ book.spark_sql }}/SparkSession/)) of the streaming query

### <span id="triggerClock"> triggerClock

```scala
triggerClock: Clock
```

Clock of the streaming query

## Implementations

* [StreamExecution](StreamExecution.md)

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

`progressBuffer` is a [scala.collection.mutable.Queue](https://www.scala-lang.org/api/2.12.x/scala/collection/mutable/Queue.html) of [StreamingQueryProgress](monitoring/StreamingQueryProgress.md)es.

`progressBuffer` has a new `StreamingQueryProgress` added when `ProgressReporter` is requested to [update progress of a streaming query](#updateProgress).

The oldest `StreamingQueryProgress` is removed (_dequeued_) above [spark.sql.streaming.numRecentProgressUpdates](configuration-properties.md#spark.sql.streaming.numRecentProgressUpdates) threshold.

`progressBuffer` is used when `ProgressReporter` is requested for the [last](#lastProgress) and the [recent StreamingQueryProgresses](#recentProgress).

## <span id="status"><span id="currentStatus"> Current StreamingQueryStatus

```scala
status: StreamingQueryStatus
```

`status` is the current [StreamingQueryStatus](monitoring/StreamingQueryStatus.md).

`status` is used when `StreamingQueryWrapper` is requested for the [current status of a streaming query](StreamingQuery.md#status).

## <span id="startTrigger"> Starting (Initializing) New Trigger

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

* `MicroBatchExecution` stream execution engine is requested to [run an activated streaming query](micro-batch-execution/MicroBatchExecution.md#runActivatedStream) (at the [beginning of every trigger](micro-batch-execution/MicroBatchExecution.md#runActivatedStream-startTrigger))

* `ContinuousExecution` stream execution engine is requested to [run an activated streaming query](continuous-execution/ContinuousExecution.md#runContinuous) (at the beginning of every trigger)

`StreamExecution` starts [running batches](StreamExecution.md#runStream) (as part of [TriggerExecutor](StreamExecution.md#triggerExecutor) executing a batch runner).

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

`reportTimeTaken` is used when [stream execution engines](StreamExecution.md) are requested to execute the following phases (that appear as `triggerDetailKey` in the DEBUG message in the logs):

1. `MicroBatchExecution`
    1. [triggerExecution](micro-batch-execution/MicroBatchExecution.md#runActivatedStream-triggerExecution)
    1. [latestOffset](micro-batch-execution/MicroBatchExecution.md#constructNextBatch-latestOffset)
    1. [getOffset](micro-batch-execution/MicroBatchExecution.md#constructNextBatch-getOffset)
    1. [walCommit](micro-batch-execution/MicroBatchExecution.md#constructNextBatch-walCommit)
    1. [getBatch](micro-batch-execution/MicroBatchExecution.md#runBatch-getBatch)
    1. [queryPlanning](micro-batch-execution/MicroBatchExecution.md#runBatch-queryPlanning)
    1. [addBatch](micro-batch-execution/MicroBatchExecution.md#runBatch-addBatch)

1. `ContinuousExecution`
    1. [queryPlanning](continuous-execution/ContinuousExecution.md#runContinuous-queryPlanning)
    1. [runContinuous](continuous-execution/ContinuousExecution.md#runContinuous-runContinuous)

## <span id="updateStatusMessage"> Updating Status Message

```scala
updateStatusMessage(
  message: String): Unit
```

`updateStatusMessage` simply updates the `message` in the [StreamingQueryStatus](#currentStatus) internal registry.

`updateStatusMessage` is used when:

* `StreamExecution` is requested to [run stream processing](StreamExecution.md#runStream)

* `MicroBatchExecution` is requested to [run an activated streaming query](micro-batch-execution/MicroBatchExecution.md#runActivatedStream) or [construct the next streaming micro-batch](micro-batch-execution/MicroBatchExecution.md#constructNextBatch)

## <span id="finishTrigger"> Finishing Up Batch (Trigger)

```scala
finishTrigger(
  hasNewData: Boolean,
  hasExecuted: Boolean): Unit
```

`finishTrigger` [updates progress](#updateProgress) with a new [StreamingQueryProgress](monitoring/StreamingQueryProgress.md) and resets the [lastNoExecutionProgressEventTime](#lastNoExecutionProgressEventTime) (to the current time).

If the given `hasExecuted` flag is disabled, `finishTrigger` does the above only when [lastNoDataProgressEventTime](#lastNoDataProgressEventTime) elapsed.

---

The given `hasNewData` and `hasExecuted` flags indicate the state of [MicroBatchExecution](micro-batch-execution/MicroBatchExecution.md).

Input Flag | MicroBatchExecution
-----------|--------------------
 `hasNewData` | [currentBatchHasNewData](micro-batch-execution/MicroBatchExecution.md#currentBatchHasNewData)
 `hasExecuted` | [isCurrentBatchConstructed](micro-batch-execution/MicroBatchExecution.md#isCurrentBatchConstructed)

---

`finishTrigger` expects that the following are initialized or throws an `AssertionError`:

* [currentTriggerStartOffsets](#currentTriggerStartOffsets)
* [currentTriggerEndOffsets](#currentTriggerEndOffsets)
* [currentTriggerLatestOffsets](#currentTriggerLatestOffsets)

!!! note "FIXME"
    Why is this so important?

---

`finishTrigger` sets [currentTriggerEndTimestamp](#currentTriggerEndTimestamp) to the current time.

`finishTrigger` [extractExecutionStats](#extractExecutionStats).

`finishTrigger` prints out the following DEBUG message to the logs:

```text
Execution stats: [executionStats]
```

`finishTrigger` calculates the **processing time** (_batchDuration_) as the time difference between the [end](#currentTriggerEndTimestamp) and [start](#currentTriggerStartTimestamp) timestamps.

`finishTrigger` calculates the **input time** (in seconds) as the time difference between the start time of the [current](#currentTriggerStartTimestamp) and [last](#lastTriggerStartTimestamp) triggers (if there were two batches already) or the infinity.

![ProgressReporter's finishTrigger and Timestamps](images/ProgressReporter-finishTrigger-timestamps.png)

For every unique [SparkDataStream](SparkDataStream.md) (in [sources](#sources)), `finishTrigger` creates a [SourceProgress](monitoring/SourceProgress.md).

SourceProgress| Value
--------------|-------
 description | A string representation of this [SparkDataStream](SparkDataStream.md)
 startOffset | Looks up this [SparkDataStream](SparkDataStream.md) in [currentTriggerStartOffsets](#currentTriggerStartOffsets)
 endOffset | Looks up this [SparkDataStream](SparkDataStream.md) in [currentTriggerEndOffsets](#currentTriggerEndOffsets)
 latestOffset | Looks up this [SparkDataStream](SparkDataStream.md) in [currentTriggerLatestOffsets](#currentTriggerLatestOffsets)
 numInputRows | Looks up this [SparkDataStream](SparkDataStream.md) in the [inputRows](monitoring/ExecutionStats.md#inputRows) of [ExecutionStats](monitoring/ExecutionStats.md)
 inputRowsPerSecond | `numInputRows` divided by the input time
 processedRowsPerSecond | `numInputRows` divided by the processing time
 metrics | [metrics](ReportsSourceMetrics.md#metrics) for [ReportsSourceMetrics](ReportsSourceMetrics.md) data streams or empty

`finishTrigger` creates a [SinkProgress](#SinkProgress) (_sink statistics_) for the [sink Table](#sink).

`finishTrigger` [extractObservedMetrics](#extractObservedMetrics).

`finishTrigger` creates a [StreamingQueryProgress](monitoring/StreamingQueryProgress.md).

With the given `hasExecuted` flag enabled, `finishTrigger` resets the [lastNoExecutionProgressEventTime](#lastNoExecutionProgressEventTime) to the current time and [updates progress](#updateProgress) (with the new `StreamingQueryProgress`).

Otherwise, with the given `hasExecuted` disabled, `finishTrigger` resets the [lastNoExecutionProgressEventTime](#lastNoExecutionProgressEventTime) to the current time and [updates progress](#updateProgress) (with the new `StreamingQueryProgress`) only when [lastNoDataProgressEventTime](#lastNoDataProgressEventTime) elapsed.

In the end, `finishTrigger` turns `isTriggerActive` flag off of the [StreamingQueryStatus](#currentStatus).

---

`finishTrigger` is used when:

* `MicroBatchExecution` is requested to [run the activated streaming query](micro-batch-execution/MicroBatchExecution.md#runActivatedStream)

### <span id="extractExecutionStats"> Execution Statistics

```scala
extractExecutionStats(
  hasNewData: Boolean,
  hasExecuted: Boolean): ExecutionStats
```

`extractExecutionStats` generates an [ExecutionStats](monitoring/ExecutionStats.md) of the [last execution](#lastExecution) (of this streaming query).

---

`hasNewData` is exactly [finishTrigger](#finishTrigger)'s `hasNewData` that is exactly [isNewDataAvailable](micro-batch-execution/MicroBatchExecution.md#isNewDataAvailable).

---    

For the given `hasNewData` disabled, `extractExecutionStats` returns an [ExecutionStats](monitoring/ExecutionStats.md) with the execution statistics:

* Empty input rows per data source
* [State operator metrics](#extractStateOperatorMetrics)
* Event-time statistics with `watermark` only (and only if there is [EventTimeWatermark](logical-operators/EventTimeWatermark.md) in the query plan)

Otherwise, with the given `hasNewData` enabled, `extractExecutionStats` generates event-time statistics (with `max`, `min`, and `avg` statistics). `extractStateOperatorMetrics` collects all [EventTimeWatermarkExec](physical-operators/EventTimeWatermarkExec.md) operators with non-empty [EventTimeStats](watermark/EventTimeStatsAccum.md) (from the optimized execution plan of the [last QueryExecution](#lastExecution)).

In the end, `extractExecutionStats` creates an [ExecutionStats](monitoring/ExecutionStats.md) with the execution statistics:

* [Input rows per data source](#extractSourceToNumInputRows)
* [State operator metrics](#extractStateOperatorMetrics)
* Event-time statistics (`max`, `min`, `avg`, `watermark`)

### <span id="extractStateOperatorMetrics"> State Operators Metrics

```scala
extractStateOperatorMetrics(
  hasExecuted: Boolean): Seq[StateOperatorProgress]
```

`extractStateOperatorMetrics` returns no [StateOperatorProgress](monitoring/StateOperatorProgress.md)s when the [lastExecution](#lastExecution) is uninitialized.

`extractStateOperatorMetrics` requests the [last QueryExecution](#lastExecution) for the optimized execution plan ([Spark SQL]({{ book.spark_sql }}/QueryExecution/#executedPlan)).

`extractStateOperatorMetrics` traverses the execution plan and collects all [StateStoreWriter](physical-operators/StateStoreWriter.md) operators that are requested to [report progress](physical-operators/StateStoreWriter.md#getProgress).

When the given `hasExecuted` flag is enabled, `extractStateOperatorMetrics` leaves all the [progress reports](physical-operators/StateStoreWriter.md#getProgress) unchanged. Otherwise, `extractStateOperatorMetrics` clears (_zero'es_) the `newNumRowsUpdated` and `newNumRowsDroppedByWatermark` metrics.

### <span id="extractObservedMetrics"> ObservedMetrics

```scala
extractObservedMetrics(
  hasNewData: Boolean,
  lastExecution: QueryExecution): Map[String, Row]
```

`extractObservedMetrics` returns the `observedMetrics` from the given `QueryExecution` when either the given `hasNewData` flag is enabled (`true`) or the given `QueryExecution` is initialized.

### <span id="updateProgress"> Updating Stream Progress

```scala
updateProgress(
  newProgress: StreamingQueryProgress): Unit
```

`updateProgress` records the input `newProgress` and posts a [QueryProgressEvent](monitoring/StreamingQueryListener.md#QueryProgressEvent) event.

![ProgressReporter's Reporting Query Progress](images/ProgressReporter-updateProgress.png)

`updateProgress` adds the input `newProgress` to [progressBuffer](#progressBuffer).

`updateProgress` removes elements from [progressBuffer](#progressBuffer) if their number is or exceeds the value of [spark.sql.streaming.numRecentProgressUpdates](configuration-properties.md#spark.sql.streaming.numRecentProgressUpdates) configuration property.

`updateProgress` [posts a QueryProgressEvent](#postEvent) (with the input `newProgress`).

`updateProgress` prints out the following INFO message to the logs:

```text
Streaming query made progress: [newProgress]
```

### <span id="lastNoExecutionProgressEventTime"> lastNoExecutionProgressEventTime

```scala
lastNoExecutionProgressEventTime: Long
```

`ProgressReporter` initializes `lastNoExecutionProgressEventTime` internal time marker to the minimum timestamp when created.

`lastNoExecutionProgressEventTime` is the time when [finishTrigger](#finishTrigger) happens and `hasExecuted` flag is enabled. Otherwise, `lastNoExecutionProgressEventTime` is reset only when [noDataProgressEventInterval](#noDataProgressEventInterval) threshold has been reached.

### <span id="noDataProgressEventInterval"> noDataProgressEventInterval

`ProgressReporter` uses [spark.sql.streaming.noDataProgressEventInterval](configuration-properties.md#spark.sql.streaming.noDataProgressEventInterval) configuration property to control whether to [updateProgress](#updateProgress) or not when requested to [finish up a trigger](#finishTrigger).

Whether to [updateProgress](#updateProgress) or not is driven by whether a batch was executed based on [isCurrentBatchConstructed](#isCurrentBatchConstructed) and how much time passed since the last [updateProgress](#updateProgress).

## <span id="recordTriggerOffsets"> Recording Trigger Offsets (StreamProgresses)

```scala
recordTriggerOffsets(
  from: StreamProgress,
  to: StreamProgress,
  latest: StreamProgress): Unit
```

`recordTriggerOffsets` updates (_records_) the following registries with the given [StreamProgress](StreamProgress.md) (and with the values [JSON](Offset.md#json)-fied).

Registry | StreamProgress
---------|---------------
 [currentTriggerStartOffsets](#currentTriggerStartOffsets) | `from`
 [currentTriggerEndOffsets](#currentTriggerEndOffsets) | `to`
 [currentTriggerLatestOffsets](#currentTriggerLatestOffsets) | `latest`

In the end, `recordTriggerOffsets` updates (_records_) the [latestStreamProgress](#latestStreamProgress) registry to be `to`.

---

`recordTriggerOffsets` is used when:

* `MicroBatchExecution` is requested to [run the activated streaming query](micro-batch-execution/MicroBatchExecution.md#runActivatedStream)
* `ContinuousExecution` is requested to [commit an epoch](continuous-execution/ContinuousExecution.md#commit)

## <span id="lastProgress"> Last StreamingQueryProgress

```scala
lastProgress: StreamingQueryProgress
```

The last [StreamingQueryProgress](monitoring/StreamingQueryProgress.md)

## <span id="currentDurationsMs"> currentDurationsMs

```scala
currentDurationsMs: HashMap[String, Long]
```

`ProgressReporter` creates a `currentDurationsMs` registry (Scala's [collection.mutable.HashMap]({{ scala.api }}/index.html#scala.collection.mutable.HashMap)) with action names (aka _triggerDetailKey_) and their cumulative execution times (in millis).

![currentDurationsMs](images/ProgressReporter-currentDurationsMs.png)

`currentDurationsMs` is initialized empty when `ProgressReporter` [sets the state for a new batch](#startTrigger) with new entries added or updated when [reporting execution time](#reportTimeTaken) (of an execution phase).

`currentDurationsMs` is available as [durationMs](monitoring/StreamingQueryProgress.md#durationMs) of a [StreamingQueryProgress](monitoring/StreamingQueryProgress.md) of a [StreamingQuery](StreamingQuery.md).

```text
assert(stream.isInstanceOf[org.apache.spark.sql.streaming.StreamingQuery])
assert(stream.lastProgress.isInstanceOf[org.apache.spark.sql.streaming.StreamingQueryProgress])

scala> println(stream.lastProgress.durationMs)
{triggerExecution=122, queryPlanning=3, getBatch=1, latestOffset=0, addBatch=36, walCommit=42}

scala> println(stream.lastProgress)
{
  "id" : "4976adb3-e8a6-4c4d-b895-912808013992",
  "runId" : "9cdd9f2c-15bc-41c3-899b-42bcc1e994de",
  "name" : null,
  "timestamp" : "2022-10-13T10:03:30.000Z",
  "batchId" : 10,
  "numInputRows" : 15,
  "inputRowsPerSecond" : 1.000333444481494,
  "processedRowsPerSecond" : 122.95081967213115,
  "durationMs" : {
    "addBatch" : 37,
    "getBatch" : 0,
    "latestOffset" : 0,
    "queryPlanning" : 4,
    "triggerExecution" : 122,
    "walCommit" : 42
  },
  "stateOperators" : [ ],
  "sources" : [ {
    "description" : "RateStreamV2[rowsPerSecond=1, rampUpTimeSeconds=0, numPartitions=default",
    "startOffset" : 127,
    "endOffset" : 142,
    "latestOffset" : 142,
    "numInputRows" : 15,
    "inputRowsPerSecond" : 1.000333444481494,
    "processedRowsPerSecond" : 122.95081967213115
  } ],
  "sink" : {
    "description" : "org.apache.spark.sql.execution.streaming.ConsoleTable$@396a0033",
    "numOutputRows" : 15
  }
}
```

## <span id="currentTriggerStartOffsets"> Current Trigger's Start Offsets (by SparkDataStream)

```scala
currentTriggerStartOffsets: Map[SparkDataStream, String]
```

`currentTriggerStartOffsets` is a collection of [Offset](Offset.md)s (in [JSON format](Offset.md#json)) of every [SparkDataStream](SparkDataStream.md) (in this streaming query) at the beginning of the following:

* [triggerExecution](micro-batch-execution/MicroBatchExecution.md#runActivatedStream-batchRunner-triggerExecution) phase in [Micro-Batch Stream Processing](micro-batch-execution/index.md)
* [commit](continuous-execution/ContinuousExecution.md#commit) in [Continuous Stream Processing](continuous-execution/index.md)

`currentTriggerStartOffsets` is reset (_null_-ified) upon [starting a new trigger](#startTrigger).

`currentTriggerStartOffsets` is updated (_initialized_) to [committedOffsets](StreamExecution.md#committedOffsets) upon [recording offsets](#recordTriggerOffsets).

`ProgressReporter` makes sure that `currentTriggerStartOffsets` is initialized when [finishing a trigger](#finishTrigger) to create a [SourceProgress](monitoring/SourceProgress.md) ([startOffset](monitoring/SourceProgress.md#startOffset)).

## Internal Properties

### currentTriggerEndTimestamp

Timestamp of when the current batch/trigger has ended

Default: `-1L`

### currentTriggerStartTimestamp

Timestamp of when the current batch/trigger has started

Default: `-1L`

### lastTriggerStartTimestamp

Timestamp of when the last batch/trigger started

Default: `-1L`

## Logging

`ProgressReporter` is an abstract class and logging is configured using the logger of the [implementations](#implementations).
