# StreamExecution &mdash; Stream Execution Engines

`StreamExecution` is the <<contract, base>> of <<extensions, stream execution engines>> (aka _streaming query processing engines_) that can <<runActivatedStream, run>> a <<logicalPlan, structured query>> (on a <<queryExecutionThread, stream execution thread>>).

NOTE: *Continuous query*, *streaming query*, *continuous Dataset*, *streaming Dataset* are all considered high-level synonyms for an executable entity that stream execution engines run using the <<logicalPlan, analyzed logical plan>> internally.

[[contract]]
.StreamExecution Contract (Abstract Methods Only)
[cols="30m,70",options="header",width="100%"]
|===
| Property
| Description

| logicalPlan
a| [[logicalPlan]]

[source, scala]
----
logicalPlan: LogicalPlan
----

Analyzed logical plan of the streaming query to execute

Used when `StreamExecution` is requested to <<runStream, run stream processing>>

`logicalPlan` is part of the [ProgressReporter](monitoring/ProgressReporter.md#logicalPlan) abstraction.

| runActivatedStream
a| [[runActivatedStream]]

[source, scala]
----
runActivatedStream(
  sparkSessionForStream: SparkSession): Unit
----

Executes (_runs_) the activated <<StreamingQuery.md#, streaming query>>

Used exclusively when `StreamExecution` is requested to <<runStream, run the streaming query>> (when transitioning from `INITIALIZING` to `ACTIVE` state)

|===

.Streaming Query and Stream Execution Engine
[source, scala]
----
import org.apache.spark.sql.streaming.StreamingQuery
assert(sq.isInstanceOf[StreamingQuery])

import org.apache.spark.sql.execution.streaming.StreamingQueryWrapper
val se = sq.asInstanceOf[StreamingQueryWrapper].streamingQuery

scala> :type se
org.apache.spark.sql.execution.streaming.StreamExecution
----

[[minLogEntriesToMaintain]][[spark.sql.streaming.minBatchesToRetain]]
`StreamExecution` uses the <<spark-sql-streaming-properties.md#spark.sql.streaming.minBatchesToRetain, spark.sql.streaming.minBatchesToRetain>> configuration property to allow the <<extensions, StreamExecutions>> to discard old log entries (from the <<offsetLog, offset>> and <<commitLog, commit>> logs).

[[extensions]]
.StreamExecutions
[cols="30,70",options="header",width="100%"]
|===
| StreamExecution
| Description

| <<ContinuousExecution.md#, ContinuousExecution>>
| [[ContinuousExecution]] Used in <<spark-sql-streaming-continuous-stream-processing.md#, Continuous Stream Processing>>

| <<MicroBatchExecution.md#, MicroBatchExecution>>
| [[MicroBatchExecution]] Used in <<micro-batch-stream-processing.md#, Micro-Batch Stream Processing>>
|===

NOTE: `StreamExecution` does not support adaptive query execution and cost-based optimizer (and turns them off when requested to <<runStream, run stream processing>>).

`StreamExecution` is the *execution environment* of a StreamingQuery.md[single streaming query] (aka _streaming Dataset_) that is executed every <<trigger, trigger>> and in the end <<MicroBatchExecution.md#runBatch-addBatch, adds the results to a sink>>.

NOTE: `StreamExecution` corresponds to a StreamingQuery.md[single streaming query] with one or more [streaming sources](Source.md) and exactly one [streaming sink](Sink.md).

[source, scala]
----
import org.apache.spark.sql.streaming.Trigger
import scala.concurrent.duration._
val q = spark.
  readStream.
  format("rate").
  load.
  writeStream.
  format("console").
  trigger(Trigger.ProcessingTime(10.minutes)).
  start
scala> :type q
org.apache.spark.sql.streaming.StreamingQuery

// Pull out StreamExecution off StreamingQueryWrapper
import org.apache.spark.sql.execution.streaming.{StreamExecution, StreamingQueryWrapper}
val se = q.asInstanceOf[StreamingQueryWrapper].streamingQuery
scala> :type se
org.apache.spark.sql.execution.streaming.StreamExecution
----

![Creating Instance of StreamExecution](images/StreamExecution-creating-instance.png)

When <<start, started>>, `StreamExecution` starts a <<queryExecutionThread, stream execution thread>> that simply <<runStream, runs stream processing>> (and hence the streaming query).

.StreamExecution's Starting Streaming Query (on Execution Thread)
image::images/StreamExecution-start.png[align="center"]

`StreamExecution` is a [ProgressReporter](monitoring/ProgressReporter.md) and <<postEvent, reports status of the streaming query>> (i.e. when it starts, progresses and terminates) by posting `StreamingQueryListener` events.

```text
import org.apache.spark.sql.streaming.Trigger
import scala.concurrent.duration._
val sq = spark
  .readStream
  .text("server-logs")
  .writeStream
  .format("console")
  .queryName("debug")
  .trigger(Trigger.ProcessingTime(20.seconds))
  .start

// Enable the log level to see the INFO and DEBUG messages
// log4j.logger.org.apache.spark.sql.execution.streaming.StreamExecution=DEBUG

17/06/18 21:21:07 INFO StreamExecution: Starting new streaming query.
17/06/18 21:21:07 DEBUG StreamExecution: getOffset took 5 ms
17/06/18 21:21:07 DEBUG StreamExecution: Stream running from {} to {}
17/06/18 21:21:07 DEBUG StreamExecution: triggerExecution took 9 ms
17/06/18 21:21:07 DEBUG StreamExecution: Execution stats: ExecutionStats(Map(),List(),Map())
17/06/18 21:21:07 INFO StreamExecution: Streaming query made progress: {
  "id" : "8b57b0bd-fc4a-42eb-81a3-777d7ba5e370",
  "runId" : "920b227e-6d02-4a03-a271-c62120258cea",
  "name" : "debug",
  "timestamp" : "2017-06-18T19:21:07.693Z",
  "numInputRows" : 0,
  "processedRowsPerSecond" : 0.0,
  "durationMs" : {
    "getOffset" : 5,
    "triggerExecution" : 9
  },
  "stateOperators" : [ ],
  "sources" : [ {
    "description" : "FileStreamSource[file:/Users/jacek/dev/oss/spark/server-logs]",
    "startOffset" : null,
    "endOffset" : null,
    "numInputRows" : 0,
    "processedRowsPerSecond" : 0.0
  } ],
  "sink" : {
    "description" : "org.apache.spark.sql.execution.streaming.ConsoleSink@2460208a"
  }
}
17/06/18 21:21:10 DEBUG StreamExecution: Starting Trigger Calculation
17/06/18 21:21:10 DEBUG StreamExecution: getOffset took 3 ms
17/06/18 21:21:10 DEBUG StreamExecution: triggerExecution took 3 ms
17/06/18 21:21:10 DEBUG StreamExecution: Execution stats: ExecutionStats(Map(),List(),Map())
```

`StreamExecution` tracks streaming data sources in <<uniqueSources, uniqueSources>> internal registry.

.StreamExecution's uniqueSources Registry of Streaming Data Sources
image::images/StreamExecution-uniqueSources.png[align="center"]

`StreamExecution` collects `durationMs` for the execution units of streaming batches.

.StreamExecution's durationMs
image::images/StreamExecution-durationMs.png[align="center"]

[source, scala]
----
scala> :type q
org.apache.spark.sql.streaming.StreamingQuery

scala> println(q.lastProgress)
{
  "id" : "03fc78fc-fe19-408c-a1ae-812d0e28fcee",
  "runId" : "8c247071-afba-40e5-aad2-0e6f45f22488",
  "name" : null,
  "timestamp" : "2017-08-14T20:30:00.004Z",
  "batchId" : 1,
  "numInputRows" : 432,
  "inputRowsPerSecond" : 0.9993568953312452,
  "processedRowsPerSecond" : 1380.1916932907347,
  "durationMs" : {
    "addBatch" : 237,
    "getBatch" : 26,
    "getOffset" : 0,
    "queryPlanning" : 1,
    "triggerExecution" : 313,
    "walCommit" : 45
  },
  "stateOperators" : [ ],
  "sources" : [ {
    "description" : "RateSource[rowsPerSecond=1, rampUpTimeSeconds=0, numPartitions=8]",
    "startOffset" : 0,
    "endOffset" : 432,
    "numInputRows" : 432,
    "inputRowsPerSecond" : 0.9993568953312452,
    "processedRowsPerSecond" : 1380.1916932907347
  } ],
  "sink" : {
    "description" : "ConsoleSink[numRows=20, truncate=true]"
  }
}
----

`StreamExecution` uses <<offsetLog, OffsetSeqLog>> and <<batchCommitLog, BatchCommitLog>> metadata logs for *write-ahead log* (to record offsets to be processed) and that have already been processed and committed to a streaming sink, respectively.

TIP: Monitor `offsets` and `commits` metadata logs to know the progress of a streaming query.

`StreamExecution` <<runBatches-batchRunner-no-data, delays polling for new data>> for 10 milliseconds (when no data was available to process in a batch). Use spark-sql-streaming-properties.md#spark.sql.streaming.pollingDelay[spark.sql.streaming.pollingDelay] Spark property to control the delay.

[[id]]
Every `StreamExecution` is uniquely identified by an *ID of the streaming query* (which is the `id` of the <<streamMetadata, StreamMetadata>>).

NOTE: Since the <<streamMetadata, StreamMetadata>> is persisted (to the `metadata` file in the <<checkpointFile, checkpoint directory>>), the streaming query ID "survives" query restarts as long as the checkpoint directory is preserved.

[[runId]]
`StreamExecution` is also uniquely identified by a *run ID of the streaming query*. A run ID is a randomly-generated 128-bit universally unique identifier (UUID) that is assigned at the time `StreamExecution` is created.

NOTE: `runId` does not "survive" query restarts and will always be different yet unique (across all active queries).

[NOTE]
====
The <<name, name>>, <<id, id>> and <<runId, runId>> are all unique across all active queries (in a [StreamingQueryManager](StreamingQueryManager.md)). The difference is that:

* <<name, name>> is optional and user-defined

* <<id, id>> is a UUID that is auto-generated at the time `StreamExecution` is created and persisted to `metadata` checkpoint file

* <<runId, runId>> is a UUID that is auto-generated every time `StreamExecution` is created
====

[[streamMetadata]]
`StreamExecution` uses a <<spark-sql-streaming-StreamMetadata.md#, StreamMetadata>> that is <<spark-sql-streaming-StreamMetadata.md#write, persisted>> in the `metadata` file in the <<checkpointFile, checkpoint directory>>. If the `metadata` file is available it is <<spark-sql-streaming-StreamMetadata.md#read, read>> and is the way to recover the <<id, ID>> of a streaming query when resumed (i.e. restarted after a failure or a planned stop).

[[IS_CONTINUOUS_PROCESSING]]
`StreamExecution` uses *__is_continuous_processing* local property (default: `false`) to differentiate between <<ContinuousExecution.md#, ContinuousExecution>> (`true`) and <<MicroBatchExecution.md#, MicroBatchExecution>> (`false`) which is used when `StateStoreRDD` is requested to <<spark-sql-streaming-StateStoreRDD.md#compute, compute a partition>> (and <<spark-sql-streaming-StateStore.md#get, finds a StateStore>> for a given version).

[[logging]]
[TIP]
====
Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.StreamExecution` to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.execution.streaming.StreamExecution=ALL
```

Refer to <<spark-sql-streaming-spark-logging.md#, Logging>>.
====

## Creating Instance

`StreamExecution` takes the following to be created:

* [[sparkSession]] `SparkSession`
* [[name]] Name of the streaming query (can also be `null`)
* [[checkpointRoot]] Path of the checkpoint directory (aka _metadata directory_)
* [[analyzedPlan]] Streaming query (as an analyzed logical query plan, i.e. `LogicalPlan`)
* [[sink]] [Streaming sink](spark-sql-streaming-BaseStreamingSink.md)
* [[trigger]] [Trigger](spark-sql-streaming-Trigger.md)
* [[triggerClock]] `Clock`
* [[outputMode]] <<spark-sql-streaming-OutputMode.md#, Output mode>>
* [[deleteCheckpointOnStop]] `deleteCheckpointOnStop` flag (to control whether to delete the checkpoint directory on stop)

`StreamExecution` initializes the <<internal-properties, internal properties>>.

NOTE: `StreamExecution` is a Scala abstract class and cannot be <<creating-instance, created>> directly. It is created indirectly when the <<extensions, concrete StreamExecutions>> are.

=== [[offsetLog]] Write-Ahead Log (WAL) of Offsets -- `offsetLog` Property

[source, scala]
----
offsetLog: OffsetSeqLog
----

`offsetLog` is a <<spark-sql-streaming-OffsetSeqLog.md#, Hadoop DFS-based metadata storage>> (of <<spark-sql-streaming-OffsetSeq.md#, OffsetSeqs>>) with `offsets` <<checkpointFile, metadata directory>>.

`offsetLog` is used as *Write-Ahead Log of Offsets* to <<spark-sql-streaming-HDFSMetadataLog.md#add, persist offsets>> of the data about to be processed in every trigger.

NOTE: *Metadata log* or *metadata checkpoint* are synonyms and are often used interchangeably.

The number of entries in the `OffsetSeqLog` is controlled using <<spark-sql-streaming-properties.md#spark.sql.streaming.minBatchesToRetain, spark.sql.streaming.minBatchesToRetain>> configuration property (default: `100`). <<extensions, Stream execution engines>> discard (_purge_) offsets from the `offsets` metadata log when the <<currentBatchId, current batch ID>> (in <<MicroBatchExecution.md#, MicroBatchExecution>>) or the <<ContinuousExecution.md#commit, epoch committed>> (in <<ContinuousExecution.md#, ContinuousExecution>>) is above the threshold.

[NOTE]
====
`offsetLog` is used when:

* `ContinuousExecution` stream execution engine is requested to <<ContinuousExecution.md#commit, commit an epoch>>, <<ContinuousExecution.md#getStartOffsets, getStartOffsets>>, and <<ContinuousExecution.md#addOffset, addOffset>>

* `MicroBatchExecution` stream execution engine is requested to <<MicroBatchExecution.md#populateStartOffsets, populate start offsets>> and <<MicroBatchExecution.md#constructNextBatch, construct (or skip) the next streaming micro-batch>>
====

=== [[state]] State of Streaming Query (Execution) -- `state` Property

[source, scala]
----
state: AtomicReference[State]
----

`state` indicates the internal state of execution of the streaming query (as https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/AtomicReference.html[java.util.concurrent.atomic.AtomicReference]).

[[states]]
.States
[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| ACTIVE
a| [[ACTIVE]] `StreamExecution` has been requested to <<runStream, run stream processing>> (and is about to <<runActivatedStream, run the activated streaming query>>)

| INITIALIZING
a| [[INITIALIZING]] `StreamExecution` has been <<creating-instance, created>>

| TERMINATED
a| [[TERMINATED]] Used to indicate that:

* `MicroBatchExecution` has been requested to <<MicroBatchExecution.md#stop, stop>>

* `ContinuousExecution` has been requested to <<ContinuousExecution.md#stop, stop>>

* `StreamExecution` has been requested to <<runStream, run stream processing>> (and has finished <<runActivatedStream, running the activated streaming query>>)

| RECONFIGURING
a| [[RECONFIGURING]] Used only when `ContinuousExecution` is requested to <<ContinuousExecution.md#runContinuous, run a streaming query in continuous mode>> (and the <<spark-sql-streaming-ContinuousReader.md#, ContinuousReader>> indicated a <<spark-sql-streaming-ContinuousReader.md#needsReconfiguration, need for reconfiguration>>)

|===

=== [[availableOffsets]] Available Offsets (StreamProgress) -- `availableOffsets` Property

[source, scala]
----
availableOffsets: StreamProgress
----

`availableOffsets` is a <<spark-sql-streaming-StreamProgress.md#, collection of offsets per streaming source>> to track what data (by <<spark-sql-streaming-Offset.md#, offset>>) is available for processing for every [streaming source](monitoring/ProgressReporter.md#sources) in the <<analyzedPlan, streaming query>> (and have not yet been <<committedOffsets, committed>>).

`availableOffsets` works in tandem with the <<committedOffsets, committedOffsets>> internal registry.

`availableOffsets` is <<spark-sql-streaming-StreamProgress.md#creating-instance, empty>> when `StreamExecution` is <<creating-instance, created>> (i.e. no offsets are reported for any streaming source in the streaming query).

`availableOffsets` is used when:

* `MicroBatchExecution` stream execution engine is requested to <<MicroBatchExecution.md#populateStartOffsets, resume and fetch the start offsets from checkpoint>>, <<MicroBatchExecution.md#isNewDataAvailable, check whether new data is available>>, <<MicroBatchExecution.md#constructNextBatch, construct the next streaming micro-batch>> and <<MicroBatchExecution.md#runBatch, run a single streaming micro-batch>>

* `ContinuousExecution` stream execution engine is requested to <<ContinuousExecution.md#commit, commit an epoch>>

* `StreamExecution` is requested for the <<toDebugString, internal string representation>>

=== [[committedOffsets]] Committed Offsets (StreamProgress) -- `committedOffsets` Property

[source, scala]
----
committedOffsets: StreamProgress
----

`committedOffsets` is a <<spark-sql-streaming-StreamProgress.md#, collection of offsets per streaming source>> to track what data (by <<spark-sql-streaming-Offset.md#, offset>>) has already been processed and committed (to the sink or state stores) for every [streaming source](monitoring/ProgressReporter.md#sources) in the <<analyzedPlan, streaming query>>.

`committedOffsets` works in tandem with the <<availableOffsets, availableOffsets>> internal registry.

`committedOffsets` is used when:

* `MicroBatchExecution` stream execution engine is requested for the <<MicroBatchExecution.md#populateStartOffsets, start offsets (from checkpoint)>>, to <<MicroBatchExecution.md#isNewDataAvailable, check whether new data is available>> and <<MicroBatchExecution.md#runBatch, run a single streaming micro-batch>>

* `ContinuousExecution` stream execution engine is requested for the <<ContinuousExecution.md#getStartOffsets, start offsets (from checkpoint)>> and to <<ContinuousExecution.md#commit, commit an epoch>>

* `StreamExecution` is requested for the <<toDebugString, internal string representation>>

=== [[resolvedCheckpointRoot]] Fully-Qualified (Resolved) Path to Checkpoint Root Directory -- `resolvedCheckpointRoot` Property

[source, scala]
----
resolvedCheckpointRoot: String
----

`resolvedCheckpointRoot` is a fully-qualified path of the given <<checkpointRoot, checkpoint root directory>>.

The given <<checkpointRoot, checkpoint root directory>> is defined using *checkpointLocation* option or the <<spark-sql-streaming-properties.md#spark.sql.streaming.checkpointLocation, spark.sql.streaming.checkpointLocation>> configuration property with `queryName` option.

`checkpointLocation` and `queryName` options are defined when `StreamingQueryManager` is requested to [create a streaming query](StreamingQueryManager.md#createQuery).

`resolvedCheckpointRoot` is used when <<checkpointFile, creating the path to the checkpoint directory>> and when `StreamExecution` finishes <<runBatches, running streaming batches>>.

`resolvedCheckpointRoot` is used for the <<logicalPlan, logicalPlan>> (while transforming <<analyzedPlan, analyzedPlan>> and planning `StreamingRelation` logical operators to corresponding `StreamingExecutionRelation` physical operators with the streaming data sources created passing in the path to `sources` directory to store checkpointing metadata).

[TIP]
====
You can see `resolvedCheckpointRoot` in the INFO message when `StreamExecution` is <<start, started>>.

```
Starting [prettyIdString]. Use [resolvedCheckpointRoot] to store the query checkpoint.
```
====

Internally, `resolvedCheckpointRoot` creates a Hadoop `org.apache.hadoop.fs.Path` for <<checkpointRoot, checkpointRoot>> and makes it qualified.

NOTE: `resolvedCheckpointRoot` uses `SparkSession` to access `SessionState` for a Hadoop configuration.

=== [[commitLog]] Offset Commit Log -- `commits` Metadata Checkpoint Directory

`StreamExecution` uses *offset commit log* (<<spark-sql-streaming-CommitLog.md#, CommitLog>> with `commits` <<checkpointFile, metadata checkpoint directory>>) for streaming batches successfully executed (with a single file per batch with a file name being the batch id) or committed epochs.

NOTE: *Metadata log* or *metadata checkpoint* are synonyms and are often used interchangeably.

`commitLog` is used by the <<extensions, stream execution engines>> for the following:

* `MicroBatchExecution` is requested to <<MicroBatchExecution.md#runActivatedStream, run an activated streaming query>> (that in turn requests to <<MicroBatchExecution.md#populateStartOffsets, populate the start offsets>> at the very beginning of the streaming query execution and later regularly every <<MicroBatchExecution.md#runBatch, single batch>>)

* `ContinuousExecution` is requested to <<ContinuousExecution.md#runActivatedStream, run an activated streaming query in continuous mode>> (that in turn requests to <<ContinuousExecution.md#getStartOffsets, retrieve the start offsets>> at the very beginning of the streaming query execution and later regularly every <<ContinuousExecution.md#commit, commit>>)

## <span id="lastExecution"> Last Query Execution Of Streaming Query (IncrementalExecution)

```scala
lastExecution: IncrementalExecution
```

`lastExecution` is part of the [ProgressReporter](monitoring/ProgressReporter.md#lastExecution) abstraction.

`lastExecution` is a [IncrementalExecution](IncrementalExecution.md) (a `QueryExecution` of a streaming query) of the most recent (_last_) execution.

`lastExecution` is created when the <<extensions, stream execution engines>> are requested for the following:

* `MicroBatchExecution` is requested to <<MicroBatchExecution.md#runBatch, run a single streaming micro-batch>> (when in <<MicroBatchExecution.md#runBatch-queryPlanning, queryPlanning Phase>>)

* `ContinuousExecution` stream execution engine is requested to <<ContinuousExecution.md#runContinuous, run a streaming query>> (when in <<ContinuousExecution.md#runContinuous-queryPlanning, queryPlanning Phase>>)

`lastExecution` is used when:

* `StreamExecution` is requested to <<explain, explain a streaming query>> (via <<explainInternal, explainInternal>>)

* `ProgressReporter` is requested to [extractStateOperatorMetrics](monitoring/ProgressReporter.md#extractStateOperatorMetrics), [extractExecutionStats](monitoring/ProgressReporter.md#extractExecutionStats), and [extractSourceToNumInputRows](monitoring/ProgressReporter.md#extractSourceToNumInputRows)

* `MicroBatchExecution` stream execution engine is requested to <<MicroBatchExecution.md#constructNextBatch-shouldConstructNextBatch, construct or skip the next streaming micro-batch>> (based on [StateStoreWriters in a streaming query](IncrementalExecution.md#shouldRunAnotherBatch)), <<MicroBatchExecution.md#runBatch, run a single streaming micro-batch>> (when in <<MicroBatchExecution.md#runBatch-addBatch, addBatch Phase>> and <<MicroBatchExecution.md#runBatch-updateWatermark-commitLog, updating watermark and committing offsets to offset commit log>>)

* `ContinuousExecution` stream execution engine is requested to <<ContinuousExecution.md#runContinuous, run a streaming query>> (when in <<ContinuousExecution.md#runContinuous-runContinuous, runContinuous Phase>>)

* For debugging query execution of streaming queries (using `debugCodegen`)

## <span id="explain"> Explaining Streaming Query

```scala
explain(): Unit // <1>
explain(extended: Boolean): Unit
```
<1> Turns the `extended` flag off (`false`)

`explain` simply prints out <<explainInternal, explainInternal>> to the standard output.

=== [[explainInternal]] `explainInternal` Method

[source, scala]
----
explainInternal(extended: Boolean): String
----

`explainInternal`...FIXME

[NOTE]
====
`explainInternal` is used when:

* `StreamExecution` is requested to <<explain, explain a streaming query>>

* `StreamingQueryWrapper` is requested to <<spark-sql-streaming-StreamingQueryWrapper.md#explainInternal, explainInternal>>
====

=== [[stopSources]] Stopping Streaming Sources and Readers -- `stopSources` Method

[source, scala]
----
stopSources(): Unit
----

`stopSources` requests every <<uniqueSources, streaming source>> (in the <<analyzedPlan, streaming query>>) to <<spark-sql-streaming-BaseStreamingSource.md#stop, stop>>.

In case of an non-fatal exception, `stopSources` prints out the following WARN message to the logs:

```
Failed to stop streaming source: [source]. Resources may have leaked.
```

[NOTE]
====
`stopSources` is used when:

* `StreamExecution` is requested to <<runStream, run stream processing>> (and <<runStream-finally, terminates>> successfully or not)

* `ContinuousExecution` is requested to <<ContinuousExecution.md#runContinuous, run the streaming query in continuous mode>> (and terminates)
====

## <span id="runStream"> Running Stream Processing

```scala
runStream(): Unit
```

`runStream` simply prepares the environment to <<runActivatedStream, execute the activated streaming query>>.

NOTE: `runStream` is used exclusively when the <<queryExecutionThread, stream execution thread>> is requested to <<start, start>> (when `DataStreamWriter` is requested to [start an execution of the streaming query](DataStreamWriter.md#start)).

Internally, `runStream` sets the job group (to all the Spark jobs started by this thread) as follows:

* <<runId, runId>> for the job group ID

* <<getBatchDescriptionString, getBatchDescriptionString>> for the job group description (to display in web UI)

* `interruptOnCancel` flag on

[NOTE]
====
`runStream` uses the <<sparkSession, SparkSession>> to access `SparkContext` and assign the job group id.

Read up on https://jaceklaskowski.gitbooks.io/mastering-apache-spark/spark-SparkContext.html#setJobGroup[SparkContext.setJobGroup] method in https://bit.ly/apache-spark-internals[The Internals of Apache Spark] book.
====

`runStream` sets `sql.streaming.queryId` local property to <<id, id>>.

`runStream` requests the `MetricsSystem` to register the <<streamMetrics, MetricsReporter>> when <<spark-sql-streaming-properties.md#spark.sql.streaming.metricsEnabled, spark.sql.streaming.metricsEnabled>> configuration property is on (default: off / `false`).

`runStream` notifies <<spark-sql-streaming-StreamingQueryListener.md#, StreamingQueryListeners>> that the streaming query has been started (by <<postEvent, posting>> a new <<spark-sql-streaming-StreamingQueryListener.md#QueryStartedEvent, QueryStartedEvent>> event with <<id, id>>, <<runId, runId>>, and <<name, name>>).

.StreamingQueryListener Notified about Query's Start (onQueryStarted)
image::images/StreamingQueryListener-onQueryStarted.png[align="center"]

`runStream` unblocks the <<start, main starting thread>> (by decrementing the count of the <<startLatch, startLatch>> that when `0` lets the starting thread continue).

CAUTION: FIXME A picture with two parallel lanes for the starting thread and daemon one for the query.

`runStream` [updates the status message](monitoring/ProgressReporter.md#updateStatusMessage) to be **Initializing sources**.

[[runStream-initializing-sources]]
`runStream` initializes the <<logicalPlan, analyzed logical plan>>.

NOTE: The <<logicalPlan, analyzed logical plan>> is a lazy value in Scala and is initialized when requested the very first time.

`runStream` disables *adaptive query execution* and *cost-based join optimization* (by turning `spark.sql.adaptive.enabled` and `spark.sql.cbo.enabled` configuration properties off, respectively).

`runStream` creates a new "zero" <<offsetSeqMetadata, OffsetSeqMetadata>>.

(Only when in <<state, INITIALIZING>> state) `runStream` enters <<state, ACTIVE>> state:

* Decrements the count of <<initializationLatch, initializationLatch>>

* [[runStream-runActivatedStream]] <<runActivatedStream, Executes the activated streaming query>> (which is different per <<extensions, StreamExecution>>, i.e. <<ContinuousExecution.md#, ContinuousExecution>> or <<MicroBatchExecution.md#, MicroBatchExecution>>).

NOTE: `runBatches` does the main work only when first started (i.e. when <<state, state>> is `INITIALIZING`).

[[runStream-stopped]]
`runStream`...FIXME (describe the failed and stop states)

Once <<triggerExecutor, TriggerExecutor>> has finished executing batches, `runBatches` [updates the status message](monitoring/ProgressReporter.md#updateStatusMessage) to **Stopped**.

NOTE: <<triggerExecutor, TriggerExecutor>> finishes executing batches when <<runBatches-batch-runner, batch runner>> returns whether the streaming query is stopped or not (which is when the internal <<state, state>> is not `TERMINATED`).

[[runBatches-catch-isInterruptedByStop]]
[[runBatches-catch-IOException]]
[[runStream-catch-Throwable]]
CAUTION: FIXME Describe `catch` block for exception handling

==== [[runStream-finally]] Running Stream Processing -- `finally` Block

`runStream` releases the <<startLatch, startLatch>> and <<initializationLatch, initializationLatch>> locks.

`runStream` <<stopSources, stopSources>>.

`runStream` sets the <<state, state>> to <<TERMINATED, TERMINATED>>.

`runStream` sets the [StreamingQueryStatus](monitoring/ProgressReporter.md#currentStatus) with the `isTriggerActive` and `isDataAvailable` flags off (`false`).

`runStream` removes the <<streamMetrics, stream metrics reporter>> from the application's `MetricsSystem`.

`runStream` requests the `StreamingQueryManager` to [handle termination of a streaming query](StreamingQueryManager.md#notifyQueryTermination).

`runStream` creates a new <<spark-sql-streaming-StreamingQueryListener.md#QueryTerminatedEvent, QueryTerminatedEvent>> (with the <<id, id>> and <<runId, run id>> of the streaming query) and <<postEvent, posts it>>.

[[runStream-finally-deleteCheckpointOnStop]]
With the <<deleteCheckpointOnStop, deleteCheckpointOnStop>> flag enabled and no <<exception, StreamingQueryException>> reported, `runStream` deletes the <<resolvedCheckpointRoot, checkpoint directory>> recursively.

In the end, `runStream` releases the <<terminationLatch, terminationLatch>> lock.

==== [[runBatches-batch-runner]] TriggerExecutor's Batch Runner

*Batch Runner* (aka `batchRunner`) is an executable block executed by <<triggerExecutor, TriggerExecutor>> in <<runBatches, runBatches>>.

`batchRunner` <<startTrigger, starts trigger calculation>>.

As long as the query is not stopped (i.e. <<state, state>> is not `TERMINATED`), `batchRunner` executes the streaming batch for the trigger.

In **triggerExecution** [time-tracking section](monitoring/ProgressReporter.md#reportTimeTaken), `runBatches` branches off per <<currentBatchId, currentBatchId>>.

.Current Batch Execution per currentBatchId
[cols="1,1",options="header",width="100%"]
|===
| currentBatchId < 0
| currentBatchId >= 0

a|

1. <<populateStartOffsets, populateStartOffsets>>
1. Setting Job Description as <<getBatchDescriptionString, getBatchDescriptionString>>

```
DEBUG Stream running from [committedOffsets] to [availableOffsets]
```

| 1. <<constructNextBatch, Constructing the next streaming micro-batch>>
|===

If there is <<dataAvailable, data available>> in the sources, `batchRunner` marks <<currentStatus, currentStatus>> with `isDataAvailable` enabled.

[NOTE]
====
You can check out the status of a StreamingQuery.md[streaming query] using StreamingQuery.md#status[status] method.

[source, scala]
----
scala> spark.streams.active(0).status
res1: org.apache.spark.sql.streaming.StreamingQueryStatus =
{
  "message" : "Waiting for next trigger",
  "isDataAvailable" : false,
  "isTriggerActive" : false
}
----
====

`batchRunner` then [updates the status message](monitoring/ProgressReporter.md#updateStatusMessage) to **Processing new data** and <<runBatch, runs the current streaming batch>>.

.StreamExecution's Running Batches (on Execution Thread)
image::images/StreamExecution-runBatches.png[align="center"]

[[runBatches-batch-runner-finishTrigger]]
After **triggerExecution** section has finished, `batchRunner` [finishes the streaming batch for the trigger](monitoring/ProgressReporter.md#finishTrigger) (and collects query execution statistics).

When there was <<dataAvailable, data available>> in the sources, `batchRunner` updates committed offsets (by spark-sql-streaming-CommitLog.md#add[adding] the <<currentBatchId, current batch id>> to <<batchCommitLog, BatchCommitLog>> and adding <<availableOffsets, availableOffsets>> to <<committedOffsets, committedOffsets>>).

You should see the following DEBUG message in the logs:

```
DEBUG batch $currentBatchId committed
```

`batchRunner` increments the <<currentBatchId, current batch id>> and sets the job description for all the following Spark jobs to <<getBatchDescriptionString, include the new batch id>>.

[[runBatches-batchRunner-no-data]]
When no <<dataAvailable, data was available>> in the sources to process, `batchRunner` does the following:

1. Marks <<currentStatus, currentStatus>> with `isDataAvailable` disabled

1. [Updates the status message](monitoring/ProgressReporter.md#updateStatusMessage) to **Waiting for data to arrive**

1. Sleeps the current thread for <<pollingDelayMs, pollingDelayMs>> milliseconds.

`batchRunner` [updates the status message](monitoring/ProgressReporter.md#updateStatusMessage) to **Waiting for next trigger** and returns whether the query is currently active or not (so <<triggerExecutor, TriggerExecutor>> can decide whether to finish executing the batches or not)

=== [[start]] Starting Streaming Query (on Stream Execution Thread) -- `start` Method

[source, scala]
----
start(): Unit
----

When called, `start` prints out the following INFO message to the logs:

```
Starting [prettyIdString]. Use [resolvedCheckpointRoot] to store the query checkpoint.
```

`start` then starts the <<queryExecutionThread, stream execution thread>> (as a daemon thread).

NOTE: `start` uses Java's ++https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html#start--++[java.lang.Thread.start] to run the streaming query on a separate execution thread.

NOTE: When started, a streaming query runs in its own execution thread on JVM.

In the end, `start` pauses the main thread (using the <<startLatch, startLatch>> until `StreamExecution` is requested to <<runStream, run the streaming query>> that in turn sends a <<spark-sql-streaming-StreamingQueryListener.md#QueryStartedEvent, QueryStartedEvent>> to all streaming listeners followed by decrementing the count of the <<startLatch, startLatch>>).

`start` is used when `StreamingQueryManager` is requested to [start a streaming query](StreamingQueryManager.md#startQuery) (when `DataStreamWriter` is requested to [start an execution of the streaming query](DataStreamWriter.md#start)).

=== [[checkpointFile]] Path to Checkpoint Directory -- `checkpointFile` Internal Method

[source, scala]
----
checkpointFile(name: String): String
----

`checkpointFile` gives the path of a directory with `name` in <<resolvedCheckpointRoot, checkpoint directory>>.

NOTE: `checkpointFile` uses Hadoop's `org.apache.hadoop.fs.Path`.

NOTE: `checkpointFile` is used for <<streamMetadata, streamMetadata>>, <<offsetLog, OffsetSeqLog>>, <<batchCommitLog, BatchCommitLog>>, and <<lastExecution, lastExecution>> (for <<runBatch, runBatch>>).

## <span id="postEvent"> Posting StreamingQueryListener Event

```scala
postEvent(
  event: StreamingQueryListener.Event): Unit
```

`postEvent` is a part of [ProgressReporter](monitoring/ProgressReporter.md#postEvent) abstraction.

`postEvent` simply requests the `StreamingQueryManager` to [post](StreamingQueryManager.md#postListenerEvent) the input event (to the [StreamingQueryListenerBus](StreamingQueryListenerBus.md) in the current `SparkSession`).

!!! note
    `postEvent` uses `SparkSession` to access the current `StreamingQueryManager`.

`postEvent` is used when:

* `ProgressReporter` is requested to [report update progress](monitoring/ProgressReporter.md#updateProgress) (while [finishing a trigger](monitoring/ProgressReporter.md#finishTrigger))

* `StreamExecution` <<runBatches, runs streaming batches>> (and announces starting a streaming query by posting a spark-sql-streaming-StreamingQueryListener.md#QueryStartedEvent[QueryStartedEvent] and query termination by posting a spark-sql-streaming-StreamingQueryListener.md#QueryTerminatedEvent[QueryTerminatedEvent])

=== [[processAllAvailable]] Waiting Until No New Data Available in Sources or Query Has Been Terminated -- `processAllAvailable` Method

[source, scala]
----
processAllAvailable(): Unit
----

NOTE: `processAllAvailable` is a part of <<StreamingQuery.md#processAllAvailable, StreamingQuery Contract>>.

`processAllAvailable` reports the <<streamDeathCause, StreamingQueryException>> if reported (and returns immediately).

NOTE: <<streamDeathCause, streamDeathCause>> is reported exclusively when `StreamExecution` is requested to <<runStream, run stream execution>> (that terminated with an exception).

`processAllAvailable` returns immediately when `StreamExecution` is no longer <<isActive, active>> (in `TERMINATED` state).

`processAllAvailable` acquires a lock on the <<awaitProgressLock, awaitProgressLock>> and turns the <<noNewData, noNewData>> internal flag off (`false`).

`processAllAvailable` keeps polling with 10-second pauses (locked on <<awaitProgressLockCondition, awaitProgressLockCondition>>) until <<noNewData, noNewData>> flag is turned on (`true`) or `StreamExecution` is no longer <<isActive, active>> (in `TERMINATED` state).

NOTE: The 10-second pause is hardcoded and cannot be changed.

In the end, `processAllAvailable` releases <<awaitProgressLock, awaitProgressLock>> lock.

`processAllAvailable` throws an `IllegalStateException` when executed on the <<queryExecutionThread, stream execution thread>>:

```
Cannot wait for a query state from the same thread that is running the query
```

=== [[queryExecutionThread]] Stream Execution Thread -- `queryExecutionThread` Property

[source, scala]
----
queryExecutionThread: QueryExecutionThread
----

`queryExecutionThread` is a Java thread of execution (https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html[java.util.Thread]) that <<runStream, runs a streaming query>>.

`queryExecutionThread` is started (as a daemon thread) when `StreamExecution` is requested to <<start, start>>. At that time, `start` prints out the following INFO message to the logs (with the <<prettyIdString, prettyIdString>> and the <<resolvedCheckpointRoot, resolvedCheckpointRoot>>):

```
Starting [prettyIdString]. Use [resolvedCheckpointRoot] to store the query checkpoint.
```

When started, `queryExecutionThread` sets the <<callSite, call site>> and <<runStream, runs the streaming query>>.

`queryExecutionThread` uses the name *stream execution thread for [id]* (that uses <<prettyIdString, prettyIdString>> for the id, i.e. `queryName [id = [id], runId = [runId]]`).

`queryExecutionThread` is a `QueryExecutionThread` that is a custom `UninterruptibleThread` from Apache Spark with `runUninterruptibly` method for running a block of code without being interrupted by `Thread.interrupt()`.

[TIP]
====
Use Java's http://docs.oracle.com/javase/8/docs/technotes/guides/management/jconsole.html[jconsole] or https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jstack.html[jstack] to monitor stream execution threads.

```
$ jstack <driver-pid> | grep -e "stream execution thread"
"stream execution thread for kafka-topic1 [id =...
```
====

=== [[toDebugString]] Internal String Representation -- `toDebugString` Internal Method

[source, scala]
----
toDebugString(includeLogicalPlan: Boolean): String
----

`toDebugString`...FIXME

`toDebugString` is used when `StreamExecution` is requested to <<runStream, run stream processing>> (and an exception is caught).

=== [[offsetSeqMetadata]] Current Batch Metadata (Event-Time Watermark and Timestamp) -- `offsetSeqMetadata` Internal Property

```scala
offsetSeqMetadata: OffsetSeqMetadata
```

`offsetSeqMetadata` is a [OffsetSeqMetadata](spark-sql-streaming-OffsetSeqMetadata.md).

`offsetSeqMetadata` is used to create an [IncrementalExecution](IncrementalExecution.md) in the **queryPlanning** phase of the [MicroBatchExecution](MicroBatchExecution.md#runBatch-queryPlanning) and [ContinuousExecution](ContinuousExecution.md#runContinuous-queryPlanning) execution engines.

`offsetSeqMetadata` is initialized (with `0` for `batchWatermarkMs` and `batchTimestampMs`) when `StreamExecution` is requested to <<runStream, run stream processing>>.

`offsetSeqMetadata` is then updated (with the current event-time watermark and timestamp) when `MicroBatchExecution` is requested to <<MicroBatchExecution.md#constructNextBatch, construct the next streaming micro-batch>>.

NOTE: `MicroBatchExecution` uses the <<MicroBatchExecution.md#watermarkTracker, WatermarkTracker>> for the current event-time watermark and the <<MicroBatchExecution.md#triggerClock, trigger clock>> for the current batch timestamp.

`offsetSeqMetadata` is stored (_checkpointed_) in <<MicroBatchExecution.md#constructNextBatch-walCommit, walCommit phase>> of `MicroBatchExecution` (and printed out as INFO message to the logs).

`offsetSeqMetadata` is restored (_re-created_) from a checkpointed state when `MicroBatchExecution` is requested to <<MicroBatchExecution.md#populateStartOffsets, populate start offsets>>.

`offsetSeqMetadata` is part of the [ProgressReporter](monitoring/ProgressReporter.md#offsetSeqMetadata) abstraction.

=== [[isActive]] `isActive` Method

[source, scala]
----
isActive: Boolean
----

NOTE: `isActive` is part of the <<StreamingQuery.md#isActive, StreamingQuery Contract>> to indicate whether a streaming query is active (`true`) or not (`false`).

`isActive` is enabled (`true`) as long as the <<state, State>> is not <<TERMINATED, TERMINATED>>.

=== [[exception]] `exception` Method

[source, scala]
----
exception: Option[StreamingQueryException]
----

NOTE: `exception` is part of the <<StreamingQuery.md#exception, StreamingQuery Contract>> to indicate whether a streaming query...FIXME

`exception`...FIXME

=== [[getBatchDescriptionString]] Human-Readable HTML Description of Spark Jobs (for web UI) -- `getBatchDescriptionString` Method

[source, scala]
----
getBatchDescriptionString: String
----

`getBatchDescriptionString` is a human-readable description (in HTML format) that uses the optional <<name, name>> if defined, the <<id, id>>, the <<runId, runId>> and `batchDescription` that can be *init* (for the <<currentBatchId, current batch ID>> negative) or the <<currentBatchId, current batch ID>> itself.

`getBatchDescriptionString` is of the following format:

[subs=-macros]
----
[name]<br/>id = [id]<br/>runId = [runId]<br/>batch = [batchDescription]
----

.Monitoring Streaming Query using web UI (Spark Jobs)
image::images/StreamExecution-getBatchDescriptionString-webUI.png[align="center"]

[NOTE]
====
`getBatchDescriptionString` is used when:

* `MicroBatchExecution` stream execution engine is requested to <<MicroBatchExecution.md#runActivatedStream, run an activated streaming query>> (as the job description of any Spark jobs triggerred as part of query execution)

* `StreamExecution` is requested to <<runStream, run stream processing>> (as the job group description of any Spark jobs triggerred as part of query execution)
====

=== [[noNewData]] No New Data Available -- `noNewData` Internal Flag

[source, scala]
----
noNewData: Boolean
----

`noNewData` is a flag that indicates that a batch has completed with no new data left and <<processAllAvailable, processAllAvailable>> could stop waiting till all streaming data is processed.

Default: `false`

Turned on (`true`) when:

* `MicroBatchExecution` stream execution engine is requested to <<constructNextBatch, construct or skip the next streaming micro-batch>> (while <<MicroBatchExecution.md#constructNextBatch-shouldConstructNextBatch-disabled, skipping the next micro-batch>>)

* `ContinuousExecution` stream execution engine is requested to <<ContinuousExecution.md#addOffset, addOffset>>

Turned off (`false`) when:

* `MicroBatchExecution` stream execution engine is requested to <<constructNextBatch, construct or skip the next streaming micro-batch>> (right after the <<MicroBatchExecution.md#constructNextBatch-walCommit, walCommit>> phase)

* `StreamExecution` is requested to <<processAllAvailable, processAllAvailable>>

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| awaitProgressLock
| [[awaitProgressLock]] Java's fair reentrant mutual exclusion https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/locks/ReentrantLock.html[java.util.concurrent.locks.ReentrantLock] (that favors granting access to the longest-waiting thread under contention)

| awaitProgressLockCondition
a| [[awaitProgressLockCondition]] Lock

| callSite
| [[callSite]]

| currentBatchId
a| [[currentBatchId]] Current batch ID

* Starts at `-1` when `StreamExecution` is <<creating-instance, created>>

* `0` when `StreamExecution` <<populateStartOffsets, populates start offsets>> (and <<offsetLog, OffsetSeqLog>> is empty, i.e. no offset files in `offsets` directory in checkpoint)

* Incremented when `StreamExecution` <<runBatches, runs streaming batches>> and finishes a trigger that had <<dataAvailable, data available from sources>> (right after <<batchCommitLog, committing the batch>>).

| initializationLatch
| [[initializationLatch]]

| newData
a| [[newData]]

```scala
newData: Map[BaseStreamingSource, LogicalPlan]
```

Registry of the <<spark-sql-streaming-BaseStreamingSource.md#, streaming sources>> (in the <<logicalPlan, logical query plan>>) that have new data available in the current batch. The new data is a streaming `DataFrame`.

`newData` is part of the [ProgressReporter](monitoring/ProgressReporter.md#newData) abstraction.

Set exclusively when `StreamExecution` is requested to <<MicroBatchExecution.md#runBatch-getBatch, requests unprocessed data from streaming sources>> (while <<runBatch, running a single streaming batch>>).

Used exclusively when `StreamExecution` is requested to <<MicroBatchExecution.md#runBatch-newBatchesPlan, transform the logical plan (of the streaming query) to include the Sources and the MicroBatchReaders with new data>> (while <<runBatch, running a single streaming batch>>).

| pollingDelayMs
| [[pollingDelayMs]] Time delay before polling new data again when no data was available

Set to spark-sql-streaming-properties.md#spark.sql.streaming.pollingDelay[spark.sql.streaming.pollingDelay] Spark property.

Used when `StreamExecution` has started <<runBatches, running streaming batches>> (and <<runBatches-batchRunner-no-data, no data was available to process in a trigger>>).

| prettyIdString
a| [[prettyIdString]] Pretty-identified string for identification in logs (with <<name, name>> if defined).

```
// query name set
queryName [id = xyz, runId = abc]

// no query name
[id = xyz, runId = abc]
```

| startLatch
| [[startLatch]] Java's https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CountDownLatch.html[java.util.concurrent.CountDownLatch] with count `1`.

Used when `StreamExecution` is requested to <<start, start>> to pause the main thread until `StreamExecution` was requested to <<runStream, run the streaming query>>.

| streamDeathCause
| [[streamDeathCause]] `StreamingQueryException`

| uniqueSources
a| [[uniqueSources]] Unique <<spark-sql-streaming-BaseStreamingSource.md#, streaming sources>> (after being collected as `StreamingExecutionRelation` from the <<logicalPlan, logical query plan>>).

Used when `StreamExecution`:

* <<constructNextBatch, Constructs the next streaming micro-batch>> (and gets new offsets for every streaming data source)

* <<stopSources, Stops all streaming data sources>>
|===

## <span id="streamMetrics"> Streaming Metrics

`StreamExecution` uses [MetricsReporter](monitoring/MetricsReporter.md) for reporting streaming metrics.

`MetricsReporter` is created with the following source name (with [name](#name) if defined or [id](#id)):

```text
spark.streaming.[name or id]
```

`MetricsReporter` is registered only when [spark.sql.streaming.metricsEnabled](spark-sql-streaming-properties.md#spark.sql.streaming.metricsEnabled) configuration property is enabled (when `StreamExecution` is requested to [runStream](#runStream)).

`MetricsReporter` is deactivated (_removed_) when a streaming query is stopped (when `StreamExecution` is requested to [runStream](#runStream)).
