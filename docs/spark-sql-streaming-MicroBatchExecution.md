# MicroBatchExecution &mdash; Stream Execution Engine of Micro-Batch Stream Processing

`MicroBatchExecution` is the [stream execution engine](StreamExecution.md) in [Micro-Batch Stream Processing](spark-sql-streaming-micro-batch-stream-processing.md).

`MicroBatchExecution` is <<creating-instance, created>> when `StreamingQueryManager` is requested to <<spark-sql-streaming-StreamingQueryManager.md#createQuery, create a streaming query>> (when `DataStreamWriter` is requested to [start an execution of the streaming query](DataStreamWriter.md#start)) with the following:

* Any type of <<sink, sink>> but <<spark-sql-streaming-StreamWriteSupport.md#, StreamWriteSupport>>

* Any type of <<trigger, trigger>> but <<spark-sql-streaming-Trigger.md#ContinuousTrigger, ContinuousTrigger>>

```text
import org.apache.spark.sql.streaming.Trigger
val query = spark
  .readStream
  .format("rate")
  .load
  .writeStream
  .format("console")          // <-- not a StreamWriteSupport sink
  .option("truncate", false)
  .trigger(Trigger.Once)      // <-- Gives MicroBatchExecution
  .queryName("rate2console")
  .start

// The following gives access to the internals
// And to MicroBatchExecution
import org.apache.spark.sql.execution.streaming.StreamingQueryWrapper
val engine = query.asInstanceOf[StreamingQueryWrapper].streamingQuery
import org.apache.spark.sql.execution.streaming.StreamExecution
assert(engine.isInstanceOf[StreamExecution])

import org.apache.spark.sql.execution.streaming.MicroBatchExecution
val microBatchEngine = engine.asInstanceOf[MicroBatchExecution]
assert(microBatchEngine.trigger == Trigger.Once)
```

Once <<creating-instance, created>>, `MicroBatchExecution` (as a [stream execution engine](StreamExecution.md)) is requested to <<runActivatedStream, run an activated streaming query>>.

[[logging]]
[TIP]
====
Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.MicroBatchExecution` to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.execution.streaming.MicroBatchExecution=ALL
```

Refer to <<spark-sql-streaming-logging.md#, Logging>>.
====

## Creating Instance

`MicroBatchExecution` takes the following to be created:

* [[sparkSession]] `SparkSession`
* [[name]] Name of the streaming query
* [[checkpointRoot]] Path of the checkpoint directory
* [[analyzedPlan]] Analyzed logical query plan of the streaming query (`LogicalPlan`)
* [[sink]] <<spark-sql-streaming-BaseStreamingSink.md#, Streaming sink>>
* [[trigger]] <<spark-sql-streaming-Trigger.md#, Trigger>>
* [[triggerClock]] Trigger clock (`Clock`)
* [[outputMode]] <<spark-sql-streaming-OutputMode.md#, Output mode>>
* [[extraOptions]] Extra options (`Map[String, String]`)
* [[deleteCheckpointOnStop]] `deleteCheckpointOnStop` flag to control whether to delete the checkpoint directory on stop

`MicroBatchExecution` initializes the <<internal-properties, internal properties>>.

=== [[triggerExecutor]] MicroBatchExecution and TriggerExecutor -- `triggerExecutor` Property

[source, scala]
----
triggerExecutor: TriggerExecutor
----

`triggerExecutor` is the <<spark-sql-streaming-TriggerExecutor.md#, TriggerExecutor>> of the streaming query that is how micro-batches are executed at regular intervals.

`triggerExecutor` is initialized based on the given <<trigger, Trigger>> (that was used to create the `MicroBatchExecution`):

* <<spark-sql-streaming-TriggerExecutor.md#, ProcessingTimeExecutor>> for <<spark-sql-streaming-Trigger.md#ProcessingTime, Trigger.ProcessingTime>>

* <<spark-sql-streaming-TriggerExecutor.md#, OneTimeExecutor>> for <<spark-sql-streaming-Trigger.md#OneTimeTrigger, OneTimeTrigger>> (aka <<spark-sql-streaming-Trigger.md#Once, Trigger.Once>> trigger)

`triggerExecutor` throws an `IllegalStateException` when the <<trigger, Trigger>> is not one of the <<spark-sql-streaming-Trigger.md#available-implementations, built-in implementations>>.

```
Unknown type of trigger: [trigger]
```

NOTE: `triggerExecutor` is used exclusively when `StreamExecution` is requested to <<runActivatedStream, run an activated streaming query>> (at regular intervals).

=== [[runActivatedStream]] Running Activated Streaming Query -- `runActivatedStream` Method

[source, scala]
----
runActivatedStream(
  sparkSessionForStream: SparkSession): Unit
----

`runActivatedStream` simply requests the <<triggerExecutor, TriggerExecutor>> to execute micro-batches using the <<batchRunner, batch runner>> (until `MicroBatchExecution` is [terminated](StreamExecution.md#isActive) due to a query stop or a failure).

`runActivatedStream` is part of [StreamExecution](StreamExecution.md#runActivatedStream) abstraction.

==== [[batchRunner]][[batch-runner]] TriggerExecutor's Batch Runner

The batch runner (of the <<triggerExecutor, TriggerExecutor>>) is executed as long as the `MicroBatchExecution` is [active](StreamExecution.md#isActive).

NOTE: _trigger_ and _batch_ are considered equivalent and used interchangeably.

[[runActivatedStream-startTrigger]]
The batch runner [initializes query progress for the new trigger](ProgressReporter.md#startTrigger) (aka _startTrigger_).

[[runActivatedStream-triggerExecution]][[runActivatedStream-triggerExecution-populateStartOffsets]]
The batch runner starts *triggerExecution* [execution phase](ProgressReporter.md#reportTimeTaken) that is made up of the following steps:

. <<populateStartOffsets, Populating start offsets from checkpoint>> before the first "zero" batch (at every start or restart)

. <<constructNextBatch, Constructing or skipping the next streaming micro-batch>>

. <<runBatch, Running the streaming micro-batch>>

At the start or restart (_resume_) of a streaming query (when the <<currentBatchId, current batch ID>> is uninitialized and `-1`), the batch runner <<populateStartOffsets, populates start offsets from checkpoint>> and then prints out the following INFO message to the logs (using the [committedOffsets](StreamExecution.md#committedOffsets) internal registry):

```text
Stream started from [committedOffsets]
```

The batch runner sets the human-readable description for any Spark job submitted (that streaming sources may submit to get new data) as the [batch description](StreamExecution.md#getBatchDescriptionString).

[[runActivatedStream-triggerExecution-isCurrentBatchConstructed]]
The batch runner <<constructNextBatch, constructs the next streaming micro-batch>> (when the <<isCurrentBatchConstructed, isCurrentBatchConstructed>> internal flag is off).

The batch runner <<recordTriggerOffsets, records trigger offsets>> (with the [committed](StreamExecution.md#committedOffsets) and [available](StreamExecution.md#availableOffsets) offsets).

The batch runner updates the [current StreamingQueryStatus](ProgressReporter.md#currentStatus) with the <<isNewDataAvailable, isNewDataAvailable>> for <<spark-sql-streaming-StreamingQueryStatus.md#isDataAvailable, isDataAvailable>> property.

[[runActivatedStream-triggerExecution-runBatch]]
With the <<isCurrentBatchConstructed, isCurrentBatchConstructed>> flag enabled (`true`), the batch runner [updates the status message](ProgressReporter.md#updateStatusMessage) to one of the following (per <<isNewDataAvailable, isNewDataAvailable>>) and <<runBatch, runs the streaming micro-batch>>.

```text
Processing new data
```

```text
No new data but cleaning up state
```

With the <<isCurrentBatchConstructed, isCurrentBatchConstructed>> flag disabled (`false`), the batch runner simply [updates the status message](ProgressReporter.md#updateStatusMessage) to the following:

```text
Waiting for data to arrive
```

[[runActivatedStream-triggerExecution-finishTrigger]]
The batch runner [finalizes query progress for the trigger](ProgressReporter.md#finishTrigger) (with a flag that indicates whether the current batch had new data).

With the <<isCurrentBatchConstructed, isCurrentBatchConstructed>> flag enabled (`true`), the batch runner increments the <<currentBatchId, currentBatchId>> and turns the <<isCurrentBatchConstructed, isCurrentBatchConstructed>> flag off (`false`).

With the <<isCurrentBatchConstructed, isCurrentBatchConstructed>> flag disabled (`false`), the batch runner simply sleeps (as long as configured using the [spark.sql.streaming.pollingDelay](StreamExecution.md#pollingDelayMs) configuration property).

In the end, the batch runner [updates the status message](ProgressReporter.md#updateStatusMessage) to the following status and returns whether the `MicroBatchExecution` is [active](StreamExecution.md#isActive) or not.

```text
Waiting for next trigger
```

=== [[populateStartOffsets]] Populating Start Offsets From Checkpoint (Resuming from Checkpoint) -- `populateStartOffsets` Internal Method

[source, scala]
----
populateStartOffsets(
  sparkSessionToRunBatches: SparkSession): Unit
----

`populateStartOffsets` requests the [Offset Write-Ahead Log](StreamExecution.md#offsetLog) for the <<spark-sql-streaming-HDFSMetadataLog.md#getLatest, latest committed batch id with metadata>> (i.e. <<spark-sql-streaming-OffsetSeq.md#, OffsetSeq>>).

NOTE: The batch id could not be available in the write-ahead log when a streaming query started with a new log or no batch was persisted (_added_) to the log before.

`populateStartOffsets` branches off based on whether the latest committed batch was <<populateStartOffsets-getLatest-available, available>> or <<populateStartOffsets-getLatest-not-available, not>>.

NOTE: `populateStartOffsets` is used exclusively when `MicroBatchExecution` is requested to <<runActivatedStream, run an activated streaming query>> ([before the first "zero" micro-batch](#runActivatedStream-triggerExecution-populateStartOffsets)).

==== [[populateStartOffsets-getLatest-available]] Latest Committed Batch Available

When the latest committed batch id with the metadata was available in the [Offset Write-Ahead Log](StreamExecution.md#offsetLog), `populateStartOffsets` (re)initializes the internal state as follows:

* Sets the [current batch ID](StreamExecution.md#currentBatchId) to the latest committed batch ID found

* Turns the <<isCurrentBatchConstructed, isCurrentBatchConstructed>> internal flag on (`true`)

* Sets the <<availableOffsets, available offsets>> to the offsets (from the metadata)

When the latest batch ID found is greater than `0`, `populateStartOffsets` requests the [Offset Write-Ahead Log](StreamExecution.md#offsetLog) for the <<spark-sql-streaming-HDFSMetadataLog.md#get, second latest batch ID with metadata>> or throws an `IllegalStateException` if not found.

```text
batch [latestBatchId - 1] doesn't exist
```

`populateStartOffsets` sets the <<committedOffsets, committed offsets>> to the second latest committed offsets.

[[populateStartOffsets-getLatest-available-offsetSeqMetadata]]
`populateStartOffsets` updates the offset metadata.

CAUTION: FIXME Describe me

`populateStartOffsets` requests the [Offset Commit Log](StreamExecution.md#commitLog) for the <<spark-sql-streaming-HDFSMetadataLog.md#getLatest, latest committed batch id with metadata>> (i.e. <<spark-sql-streaming-CommitMetadata.md#, CommitMetadata>>).

CAUTION: FIXME Describe me

When the latest committed batch id with metadata was found which is exactly the latest batch ID (found in the [Offset Commit Log](StreamExecution.md#commitLog)), `populateStartOffsets`...FIXME

When the latest committed batch id with metadata was found, but it is not exactly the second latest batch ID (found in the [Offset Commit Log](StreamExecution.md#commitLog)), `populateStartOffsets` prints out the following WARN message to the logs:

[options="wrap"]
----
Batch completion log latest batch id is [latestCommittedBatchId], which is not trailing batchid [latestBatchId] by one
----

When no commit log present in the [Offset Commit Log](StreamExecution.md#commitLog), `populateStartOffsets` prints out the following INFO message to the logs:

```text
no commit log present
```

In the end, `populateStartOffsets` prints out the following DEBUG message to the logs:

```text
Resuming at batch [currentBatchId] with committed offsets [committedOffsets] and available offsets [availableOffsets]
```

==== [[populateStartOffsets-getLatest-not-available]] No Latest Committed Batch

When the latest committed batch id with the metadata could not be found in the [Offset Write-Ahead Log](StreamExecution.md#offsetLog), it is assumed that the streaming query is started for the very first time (or the [checkpoint location](StreamExecution.md#checkpointRoot) has changed).

`populateStartOffsets` prints out the following INFO message to the logs:

```text
Starting new streaming query.
```

[[populateStartOffsets-currentBatchId-0]]
`populateStartOffsets` sets the [current batch ID](StreamExecution.md#currentBatchId) to `0` and creates a new <<watermarkTracker, WatermarkTracker>>.

=== [[constructNextBatch]] Constructing Or Skipping Next Streaming Micro-Batch -- `constructNextBatch` Internal Method

[source, scala]
----
constructNextBatch(
  noDataBatchesEnabled: Boolean): Boolean
----

NOTE: `constructNextBatch` will only be executed when the <<isCurrentBatchConstructed, isCurrentBatchConstructed>> internal flag is enabled (`true`).

`constructNextBatch` performs the following steps:

. <<constructNextBatch-latestOffsets, Requesting the latest offsets from every streaming source>> (of the streaming query)

. <<constructNextBatch-availableOffsets, Updating availableOffsets StreamProgress with the latest available offsets>>

. <<constructNextBatch-offsetSeqMetadata, Updating batch metadata with the current event-time watermark and batch timestamp>>

. <<constructNextBatch-shouldConstructNextBatch, Checking whether to construct the next micro-batch or not (skip it)>>

In the end, `constructNextBatch` returns <<constructNextBatch-shouldConstructNextBatch, whether the next streaming micro-batch was constructed or skipped>>.

NOTE: `constructNextBatch` is used exclusively when `MicroBatchExecution` is requested to <<runActivatedStream, run the activated streaming query>>.

==== [[constructNextBatch-latestOffsets]] Requesting Latest Offsets from Streaming Sources (getOffset, setOffsetRange and getEndOffset Phases)

`constructNextBatch` firstly requests every [streaming source](StreamExecution.md#uniqueSources) for the latest offsets.

NOTE: `constructNextBatch` checks out the latest offset in every streaming data source sequentially, i.e. one data source at a time.

.MicroBatchExecution's Getting Offsets From Streaming Sources
image::images/MicroBatchExecution-constructNextBatch.png[align="center"]

For every [streaming source](Source.md) (Data Source API V1), `constructNextBatch` [updates the status message](ProgressReporter.md#updateStatusMessage) to the following:

```text
Getting offsets from [source]
```

[[constructNextBatch-getOffset]]
In *getOffset* [time-tracking section](ProgressReporter.md#reportTimeTaken), `constructNextBatch` requests the `Source` for the <<getOffset, latest offset>>.

For every <<spark-sql-streaming-MicroBatchReader.md#, MicroBatchReader>> (Data Source API V2), `constructNextBatch` [updates the status message](ProgressReporter.md#updateStatusMessage) to the following:

```text
Getting offsets from [source]
```

[[constructNextBatch-setOffsetRange]]
In *setOffsetRange* [time-tracking section](ProgressReporter.md#reportTimeTaken), `constructNextBatch` finds the available offsets of the source (in the <<availableOffsets, available offset>> internal registry) and, if found, requests the `MicroBatchReader` to <<spark-sql-streaming-MicroBatchReader.md#deserializeOffset, deserialize the offset>> (from <<spark-sql-streaming-Offset.md#json, JSON format>>). `constructNextBatch` requests the `MicroBatchReader` to <<spark-sql-streaming-MicroBatchReader.md#setOffsetRange, set the desired offset range>>.

[[constructNextBatch-getEndOffset]]
In *getEndOffset* [time-tracking section](ProgressReporter.md#reportTimeTaken), `constructNextBatch` requests the `MicroBatchReader` for the <<spark-sql-streaming-MicroBatchReader.md#getEndOffset, end offset>>.

### <span id="constructNextBatch-availableOffsets"> Updating availableOffsets StreamProgress with Latest Available Offsets

`constructNextBatch` updates the [availableOffsets StreamProgress](StreamExecution.md#availableOffsets) with the latest reported offsets.

### <span id="constructNextBatch-offsetSeqMetadata"> Updating Batch Metadata with Current Event-Time Watermark and Batch Timestamp

`constructNextBatch` updates the [batch metadata](StreamExecution.md#offsetSeqMetadata) with the current <<spark-sql-streaming-WatermarkTracker.md#currentWatermark, event-time watermark>> (from the <<watermarkTracker, WatermarkTracker>>) and the batch timestamp.

==== [[constructNextBatch-shouldConstructNextBatch]] Checking Whether to Construct Next Micro-Batch or Not (Skip It)

`constructNextBatch` checks whether or not the next streaming micro-batch should be constructed (`lastExecutionRequiresAnotherBatch`).

`constructNextBatch` uses the [last IncrementalExecution](StreamExecution.md#lastExecution) if the <<spark-sql-streaming-IncrementalExecution.md#shouldRunAnotherBatch, last execution requires another micro-batch>> (using the [batch metadata](StreamExecution.md#offsetSeqMetadata)) and the given `noDataBatchesEnabled` flag is enabled (`true`).

`constructNextBatch` also <<isNewDataAvailable, checks out whether new data is available (based on available and committed offsets)>>.

NOTE: `shouldConstructNextBatch` local flag is enabled (`true`) when <<isNewDataAvailable, there is new data available (based on offsets)>> or the <<spark-sql-streaming-IncrementalExecution.md#shouldRunAnotherBatch, last execution requires another micro-batch>> (and the given `noDataBatchesEnabled` flag is enabled).

`constructNextBatch` prints out the following TRACE message to the logs:

```text
noDataBatchesEnabled = [noDataBatchesEnabled], lastExecutionRequiresAnotherBatch = [lastExecutionRequiresAnotherBatch], isNewDataAvailable = [isNewDataAvailable], shouldConstructNextBatch = [shouldConstructNextBatch]
```

`constructNextBatch` branches off per whether to <<constructNextBatch-shouldConstructNextBatch-enabled, constructs>> or <<constructNextBatch-shouldConstructNextBatch-disabled, skip>> the next batch (per `shouldConstructNextBatch` flag in the above TRACE message).

==== [[constructNextBatch-shouldConstructNextBatch-enabled]] Constructing Next Micro-Batch -- `shouldConstructNextBatch` Flag Enabled

With the <<constructNextBatch-shouldConstructNextBatch, shouldConstructNextBatch>> flag enabled (`true`), `constructNextBatch` [updates the status message](ProgressReporter.md#updateStatusMessage) to the following:

```text
Writing offsets to log
```

[[constructNextBatch-walCommit]]
In *walCommit* [time-tracking section](ProgressReporter.md#reportTimeTaken), `constructNextBatch` requests the [availableOffsets StreamProgress](StreamExecution.md#availableOffsets) to <<spark-sql-streaming-StreamProgress.md#toOffsetSeq, convert to OffsetSeq>> (with the <<sources, BaseStreamingSources>> and the [current batch metadata (event-time watermark and timestamp)](StreamExecution.md#offsetSeqMetadata)) that is in turn [added](spark-sql-streaming-HDFSMetadataLog.md#add) to the [write-ahead log](StreamExecution.md#offsetLog) for the [current batch ID](StreamExecution.md#currentBatchId).

`constructNextBatch` prints out the following INFO message to the logs:

```text
Committed offsets for batch [currentBatchId]. Metadata [offsetSeqMetadata]
```

NOTE: FIXME (`if (currentBatchId != 0) ...`)

NOTE: FIXME (`if (minLogEntriesToMaintain < currentBatchId) ...`)

`constructNextBatch` turns the [noNewData](StreamExecution.md#noNewData) internal flag off (`false`).

In case of a failure while <<spark-sql-streaming-HDFSMetadataLog.md#add, adding the available offsets>> to the [write-ahead log](StreamExecution.md#offsetLog), `constructNextBatch` throws an `AssertionError`:

```text
Concurrent update to the log. Multiple streaming jobs detected for [currentBatchId]
```

### <span id="constructNextBatch-shouldConstructNextBatch-disabled"> Skipping Next Micro-Batch -- `shouldConstructNextBatch` Flag Disabled

With the <<constructNextBatch-shouldConstructNextBatch, shouldConstructNextBatch>> flag disabled (`false`), `constructNextBatch` turns the [noNewData](StreamExecution.md#noNewData) flag on (`true`) and wakes up (_notifies_) all threads waiting for the [awaitProgressLockCondition](StreamExecution.md#awaitProgressLockCondition) lock.

## <span id="runBatch"> Running Single Streaming Micro-Batch

```scala
runBatch(
  sparkSessionToRunBatch: SparkSession): Unit
```

`runBatch` prints out the following DEBUG message to the logs (with the [current batch ID](StreamExecution.md#currentBatchId)):

```text
Running batch [currentBatchId]
```

`runBatch` then performs the following steps (aka _phases_):

. <<runBatch-getBatch, getBatch Phase -- Creating Logical Query Plans For Unprocessed Data From Sources and MicroBatchReaders>>
. <<runBatch-newBatchesPlan, Transforming Logical Plan to Include Sources and MicroBatchReaders with New Data>>
. <<runBatch-newAttributePlan, Transforming CurrentTimestamp and CurrentDate Expressions (Per Batch Metadata)>>
. <<runBatch-triggerLogicalPlan, Adapting Transformed Logical Plan to Sink with StreamWriteSupport>>
. <<runBatch-setLocalProperty, Setting Local Properties>>
. <<runBatch-queryPlanning, queryPlanning Phase -- Creating and Preparing IncrementalExecution for Execution>>
. <<runBatch-nextBatch, nextBatch Phase -- Creating DataFrame (with IncrementalExecution for New Data)>>
. <<runBatch-addBatch, addBatch Phase -- Adding DataFrame With New Data to Sink>>
. <<runBatch-updateWatermark-commitLog, Updating Watermark and Committing Offsets to Offset Commit Log>>

In the end, `runBatch` prints out the following DEBUG message to the logs (with the [current batch ID](StreamExecution.md#currentBatchId)):

```text
Completed batch [currentBatchId]
```

NOTE: `runBatch` is used exclusively when `MicroBatchExecution` is requested to <<runActivatedStream, run an activated streaming query>> (and there is new data to process).

### <span id="runBatch-getBatch"> getBatch Phase -- Creating Logical Query Plans For Unprocessed Data From Sources and MicroBatchReaders

In *getBatch* [time-tracking section](ProgressReporter.md#reportTimeTaken), `runBatch` goes over the [available offsets](StreamExecution.md#availableOffsets) and processes every <<runBatch-getBatch-Source, Source>> and <<runBatch-getBatch-MicroBatchReader, MicroBatchReader>> (associated with the available offsets) to create logical query plans (`newData`) for data processing (per offset ranges).

NOTE: `runBatch` requests sources and readers for data per offset range sequentially, one by one.

.StreamExecution's Running Single Streaming Batch (getBatch Phase)
image::images/StreamExecution-runBatch-getBatch.png[align="center"]

### <span id="runBatch-getBatch-Source"> getBatch Phase and Sources

For a [Source](Source.md) (with the available <<spark-sql-streaming-Offset.md#, offsets>> different from the [committedOffsets](StreamExecution.md#committedOffsets) registry), `runBatch` does the following:

* Requests the [committedOffsets](StreamExecution.md#committedOffsets) for the committed offsets for the `Source` (if available)

* Requests the `Source` for a [dataframe for the offset range](Source.md#getBatch) (the current and available offsets)

`runBatch` prints out the following DEBUG message to the logs.

```text
Retrieving data from [source]: [current] -> [available]
```

In the end, `runBatch` returns the `Source` and the logical plan of the streaming dataset (for the offset range).

In case the `Source` returns a dataframe that is not streaming, `runBatch` throws an `AssertionError`:

```
DataFrame returned by getBatch from [source] did not have isStreaming=true\n[logicalQueryPlan]
```

==== [[runBatch-getBatch-MicroBatchReader]] getBatch Phase and MicroBatchReaders

For a <<spark-sql-streaming-MicroBatchReader.md#, MicroBatchReader>> (with the available <<spark-sql-streaming-Offset.md#, offsets>> different from the [committedOffsets](StreamExecution.md#committedOffsets) registry),  `runBatch` does the following:

* Requests the [committedOffsets](StreamExecution.md#committedOffsets) for the committed offsets for the `MicroBatchReader` (if available)

* Requests the `MicroBatchReader` to <<spark-sql-streaming-MicroBatchReader.md#deserializeOffset, deserialize the committed offsets>> (if available)

* Requests the `MicroBatchReader` to <<spark-sql-streaming-MicroBatchReader.md#deserializeOffset, deserialize the available offsets>> (only for <<spark-sql-streaming-Offset.md#SerializedOffset, SerializedOffsets>>)

* Requests the `MicroBatchReader` to <<spark-sql-streaming-MicroBatchReader.md#setOffsetRange, set the offset range>> (the current and available offsets)

`runBatch` prints out the following DEBUG message to the logs.

```text
Retrieving data from [reader]: [current] -> [availableV2]
```

`runBatch` looks up the `DataSourceV2` and the options for the `MicroBatchReader` (in the <<readerToDataSourceMap, readerToDataSourceMap>> internal registry).

In the end, `runBatch` requests the `MicroBatchReader` for the <<spark-sql-streaming-MicroBatchReader.md#readSchema, read schema>> and creates a `StreamingDataSourceV2Relation` logical operator (with the read schema, the `DataSourceV2`, options, and the `MicroBatchReader`).

==== [[runBatch-newBatchesPlan]] Transforming Logical Plan to Include Sources and MicroBatchReaders with New Data

.StreamExecution's Running Single Streaming Batch (and Transforming Logical Plan for New Data)
image::images/StreamExecution-runBatch-newBatchesPlan.png[align="center"]

`runBatch` transforms the <<logicalPlan, analyzed logical plan>> to include <<runBatch-getBatch, Sources and MicroBatchReaders with new data>> (`newBatchesPlan` with logical plans to process data that has arrived since the last batch).

For every <<spark-sql-streaming-StreamingExecutionRelation.md#, StreamingExecutionRelation>> (with a <<spark-sql-streaming-BaseStreamingSource.md#, Source or MicroBatchReader>>), `runBatch` tries to find the corresponding logical plan for processing new data.

NOTE: <<spark-sql-streaming-StreamingExecutionRelation.md#, StreamingExecutionRelation>> logical operator is used to represent a streaming source or reader in the <<logicalPlan, logical query plan>> (of a streaming query).

If the logical plan is found, `runBatch` makes the plan a child operator of `Project` (with `Aliases`) logical operator and replaces the `StreamingExecutionRelation`.

Otherwise, if not found, `runBatch` simply creates an empty streaming `LocalRelation` (for scanning data from an empty local collection).

In case the number of columns in dataframes with new data and ``StreamingExecutionRelation``'s do not match, `runBatch` throws an `AssertionError`:

```
Invalid batch: [output] != [dataPlan.output]
```

==== [[runBatch-newAttributePlan]] Transforming CurrentTimestamp and CurrentDate Expressions (Per Batch Metadata)

`runBatch` replaces all `CurrentTimestamp` and `CurrentDate` expressions in the <<runBatch-newBatchesPlan, transformed logical plan (with new data)>> with the <<spark-sql-streaming-OffsetSeqMetadata.md#batchTimestampMs, current batch timestamp>> (based on the [batch metadata](StreamExecution.md#offsetSeqMetadata)).

!!! note
    `CurrentTimestamp` and `CurrentDate` expressions correspond to `current_timestamp` and `current_date` standard function, respectively.

==== [[runBatch-triggerLogicalPlan]] Adapting Transformed Logical Plan to Sink with StreamWriteSupport

`runBatch` adapts the <<runBatch-newAttributePlan, transformed logical plan (with new data and current batch timestamp)>> for the new <<spark-sql-streaming-StreamWriteSupport.md#, StreamWriteSupport>> sinks (per the type of the <<sink, BaseStreamingSink>>).

For a <<spark-sql-streaming-StreamWriteSupport.md#, StreamWriteSupport>> (Data Source API V2), `runBatch` requests the `StreamWriteSupport` for a <<spark-sql-streaming-StreamWriteSupport.md#createStreamWriter, StreamWriter>> (for the [runId](StreamExecution.md#runId), the output schema, the <<outputMode, OutputMode>>, and the <<extraOptions, extra options>>). `runBatch` then creates a `WriteToDataSourceV2` logical operator with a new <<spark-sql-streaming-MicroBatchWriter.md#, MicroBatchWriter>> as a child operator (for the [current batch ID](StreamExecution.md#currentBatchId) and the <<spark-sql-streaming-StreamWriter.md#, StreamWriter>>).

For a <<spark-sql-streaming-Sink.md#, Sink>> (Data Source API V1), `runBatch` changes nothing.

For any other <<sink, BaseStreamingSink>> type, `runBatch` simply throws an `IllegalArgumentException`:

```text
unknown sink type for [sink]
```

==== [[runBatch-setLocalProperty]] Setting Local Properties

`runBatch` sets the <<runBatch-setLocalProperty-local-properties, local properties>>.

[[runBatch-setLocalProperty-local-properties]]
.runBatch's Local Properties
[cols="30,70",options="header",width="100%"]
|===
| Local Property
| Value

| <<BATCH_ID_KEY, streaming.sql.batchId>>
a| [currentBatchId](StreamExecution.md#currentBatchId)

| [__is_continuous_processing](StreamExecution.md#IS_CONTINUOUS_PROCESSING)
a| `false`
|===

==== [[runBatch-queryPlanning]] queryPlanning Phase -- Creating and Preparing IncrementalExecution for Execution

.StreamExecution's Query Planning (queryPlanning Phase)
image::images/StreamExecution-runBatch-queryPlanning.png[align="center"]

In *queryPlanning* [time-tracking section](ProgressReporter.md#reportTimeTaken), `runBatch` creates a new [IncrementalExecution](StreamExecution.md#lastExecution) with the following:

* <<runBatch-triggerLogicalPlan, Transformed logical plan>>

* <<outputMode, Output mode>>

* `state` <<checkpointFile, checkpoint directory>>

* [Run ID](StreamExecution.md#runId)

* [Batch ID](StreamExecution.md#currentBatchId)

* [Batch Metadata (Event-Time Watermark and Timestamp)](StreamExecution.md#offsetSeqMetadata)

In the end (of the `queryPlanning` phase), `runBatch` requests the `IncrementalExecution` to prepare the transformed logical plan for execution (i.e. execute the `executedPlan` query execution phase).

TIP: Read up on the `executedPlan` query execution phase in https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-QueryExecution.html[The Internals of Spark SQL].

==== [[runBatch-nextBatch]] nextBatch Phase -- Creating DataFrame (with IncrementalExecution for New Data)

.StreamExecution Creates DataFrame with New Data
image::images/StreamExecution-runBatch-nextBatch.png[align="center"]

`runBatch` creates a new `DataFrame` with the new <<runBatch-queryPlanning, IncrementalExecution>>.

The `DataFrame` represents the result of executing the current micro-batch of the streaming query.

==== [[runBatch-addBatch]] addBatch Phase -- Adding DataFrame With New Data to Sink

.StreamExecution Adds DataFrame With New Data to Sink
image::images/StreamExecution-runBatch-addBatch.png[align="center"]

In *addBatch* [time-tracking section](ProgressReporter.md#reportTimeTaken), `runBatch` adds the `DataFrame` with new data to the <<sink, BaseStreamingSink>>.

For a <<spark-sql-streaming-Sink.md#, Sink>> (Data Source API V1), `runBatch` simply requests the `Sink` to <<spark-sql-streaming-Sink.md#addBatch, add the DataFrame>> (with the [batch ID](StreamExecution.md#currentBatchId)).

For a <<spark-sql-streaming-StreamWriteSupport.md#, StreamWriteSupport>> (Data Source API V2), `runBatch` simply requests the `DataFrame` with new data to collect (which simply forces execution of the <<spark-sql-streaming-MicroBatchWriter.md#, MicroBatchWriter>>).

NOTE: `runBatch` uses `SQLExecution.withNewExecutionId` to execute and track all the Spark jobs under one execution id (so it is reported as one single multi-job execution, e.g. in web UI).

NOTE: `SQLExecution.withNewExecutionId` posts a `SparkListenerSQLExecutionStart` event before execution and a `SparkListenerSQLExecutionEnd` event right afterwards.

!!! tip
    Register `SparkListener` to get notified about the SQL execution events (`SparkListenerSQLExecutionStart` and `SparkListenerSQLExecutionEnd`).

==== [[runBatch-updateWatermark-commitLog]] Updating Watermark and Committing Offsets to Offset Commit Log

`runBatch` requests the <<watermarkTracker, WatermarkTracker>> to <<spark-sql-streaming-WatermarkTracker.md#updateWatermark, update event-time watermark>> (with the `executedPlan` of the <<runBatch-queryPlanning, IncrementalExecution>>).

`runBatch` requests the [Offset Commit Log](StreamExecution.md#commitLog) to <<spark-sql-streaming-HDFSMetadataLog.md#add, persisting metadata of the streaming micro-batch>> (with the current [batch ID](StreamExecution.md#currentBatchId) and <<spark-sql-streaming-WatermarkTracker.md#currentWatermark, event-time watermark>> of the <<watermarkTracker, WatermarkTracker>>).

In the end, `runBatch` <<spark-sql-streaming-StreamProgress.md#plusplus, adds>> the [available offsets](StreamExecution.md#availableOffsets) to the [committed offsets](StreamExecution.md#committedOffsets) (and updates the <<spark-sql-streaming-Offset.md#, offsets>> of every <<spark-sql-streaming-BaseStreamingSource.md#, BaseStreamingSource>> with new data in the current micro-batch).

## <span id="stop"> Stopping Stream Processing (Execution of Streaming Query)

```scala
stop(): Unit
```

`stop` sets the [state](StreamExecution.md#state) to `TERMINATED`.

When the [stream execution thread](StreamExecution.md#queryExecutionThread) is alive, `stop` requests the current `SparkContext` to `cancelJobGroup` identified by the [runId](StreamExecution.md#runId) and waits for this thread to die. Just to make sure that there are no more streaming jobs, `stop` requests the current `SparkContext` to `cancelJobGroup` identified by the [runId](StreamExecution.md#runId) again.

In the end, `stop` prints out the following INFO message to the logs:

```text
Query [prettyIdString] was stopped
```

`stop` is part of the [StreamingQuery](spark-sql-streaming-StreamingQuery.md#stop) abstraction.

=== [[isNewDataAvailable]] Checking Whether New Data Is Available (Based on Available and Committed Offsets) -- `isNewDataAvailable` Internal Method

[source, scala]
----
isNewDataAvailable: Boolean
----

`isNewDataAvailable` checks whether there is a streaming source (in the <<availableOffsets, available offsets>>) for which <<committedOffsets, committed offsets>> are different from the available offsets or not available (committed) at all.

`isNewDataAvailable` is positive (`true`) when there is at least one such streaming source.

NOTE: `isNewDataAvailable` is used when `MicroBatchExecution` is requested to <<runActivatedStream, run an activated streaming query>> and <<constructNextBatch, construct the next streaming micro-batch>>.

=== [[logicalPlan]] Analyzed Logical Plan With Unique StreamingExecutionRelation Operators -- `logicalPlan` Lazy Property

[source, scala]
----
logicalPlan: LogicalPlan
----

`logicalPlan` is part of the [StreamExecution](StreamExecution.md#logicalPlan) abstraction.

`logicalPlan` resolves (_replaces_) <<spark-sql-streaming-StreamingRelation.md#, StreamingRelation>>, <<spark-sql-streaming-StreamingRelationV2.md#, StreamingRelationV2>> logical operators to <<spark-sql-streaming-StreamingExecutionRelation.md#, StreamingExecutionRelation>> logical operators. `logicalPlan` uses the transformed logical plan to set the [uniqueSources](StreamExecution.md#uniqueSources) and <<sources, sources>> internal registries to be the <<spark-sql-streaming-StreamingExecutionRelation.md#source, BaseStreamingSources>> of all the `StreamingExecutionRelations` unique and not, respectively.

NOTE: `logicalPlan` is a Scala lazy value and so the initialization is guaranteed to happen only once at the first access (and is cached for later use afterwards).

Internally, `logicalPlan` transforms the <<analyzedPlan, analyzed logical plan>>.

For every <<spark-sql-streaming-StreamingRelation.md#, StreamingRelation>> logical operator, `logicalPlan` tries to replace it with the <<spark-sql-streaming-StreamingExecutionRelation.md#, StreamingExecutionRelation>> that was used earlier for the same `StreamingRelation` (if used multiple times in the plan) or creates a new one. While creating a new `StreamingExecutionRelation`, `logicalPlan` requests the `DataSource` to <<spark-sql-streaming-DataSource.md#createSource, create a streaming Source>> with the metadata path as `sources/uniqueID` directory in the [checkpoint root directory](StreamExecution.md#resolvedCheckpointRoot). `logicalPlan` prints out the following INFO message to the logs:

```text
Using Source [source] from DataSourceV1 named '[sourceName]' [dataSourceV1]
```

For every <<spark-sql-streaming-StreamingRelationV2.md#, StreamingRelationV2>> logical operator with a <<spark-sql-streaming-MicroBatchReadSupport.md#, MicroBatchReadSupport>> data source (which is not on the list of <<spark-sql-streaming-properties.md#spark.sql.streaming.disabledV2MicroBatchReaders, spark.sql.streaming.disabledV2MicroBatchReaders>>), `logicalPlan` tries to replace it with the <<spark-sql-streaming-StreamingExecutionRelation.md#, StreamingExecutionRelation>> that was used earlier for the same `StreamingRelationV2` (if used multiple times in the plan) or creates a new one. While creating a new `StreamingExecutionRelation`, `logicalPlan` requests the `MicroBatchReadSupport` to <<spark-sql-streaming-MicroBatchReadSupport.md#createMicroBatchReader, create a MicroBatchReader>> with the metadata path as `sources/uniqueID` directory in the [checkpoint root directory](StreamExecution.md#resolvedCheckpointRoot). `logicalPlan` prints out the following INFO message to the logs:

```text
Using MicroBatchReader [reader] from DataSourceV2 named '[sourceName]' [dataSourceV2]
```

For every other <<spark-sql-streaming-StreamingRelationV2.md#, StreamingRelationV2>> logical operator, `logicalPlan` tries to replace it with the <<spark-sql-streaming-StreamingExecutionRelation.md#, StreamingExecutionRelation>> that was used earlier for the same `StreamingRelationV2` (if used multiple times in the plan) or creates a new one. While creating a new `StreamingExecutionRelation`, `logicalPlan` requests the `StreamingRelation` for the underlying <<spark-sql-streaming-StreamingRelation.md#dataSource, DataSource>> that is in turn requested to <<spark-sql-streaming-DataSource.md#createSource, create a streaming Source>> with the metadata path as `sources/uniqueID` directory in the [checkpoint root directory](StreamExecution.md#resolvedCheckpointRoot). `logicalPlan` prints out the following INFO message to the logs:

```text
Using Source [source] from DataSourceV2 named '[sourceName]' [dataSourceV2]
```

`logicalPlan` requests the transformed analyzed logical plan for all `StreamingExecutionRelations` that are then requested for <<spark-sql-streaming-StreamingExecutionRelation.md#source, BaseStreamingSources>>, and saves them as the <<sources, sources>> internal registry.

In the end, `logicalPlan` sets the [uniqueSources](StreamExecution.md#uniqueSources) internal registry to be the unique `BaseStreamingSources` above.

`logicalPlan` throws an `AssertionError` when not executed on the [stream execution thread](StreamExecution.md#queryExecutionThread).

```text
logicalPlan must be initialized in QueryExecutionThread but the current thread was [currentThread]
```

=== [[BATCH_ID_KEY]][[streaming.sql.batchId]] `streaming.sql.batchId` Local Property

`MicroBatchExecution` defines *streaming.sql.batchId* as the name of the local property to be the current *batch* or *epoch IDs* (that Spark tasks can use)

`streaming.sql.batchId` is used when:

* `MicroBatchExecution` is requested to <<runBatch, run a single streaming micro-batch>> (and sets the property to be the current batch ID)

* `DataWritingSparkTask` is requested to run (and needs an epoch ID)

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| isCurrentBatchConstructed
a| [[isCurrentBatchConstructed]] Flag to control whether to <<runBatch, run a streaming micro-batch>> (`true`) or not (`false`)

Default: `false`

* When disabled (`false`), changed to whatever <<constructNextBatch, constructing the next streaming micro-batch>> gives back when <<runActivatedStream, running activated streaming query>>

* Disabled (`false`) after <<runBatch, running a streaming micro-batch>> (when enabled after <<constructNextBatch, constructing the next streaming micro-batch>>)

* Enabled (`true`) when <<populateStartOffsets, populating start offsets>> (when <<runActivatedStream, running an activated streaming query>>) and <<spark-sql-streaming-HDFSMetadataLog.md#getLatest, re-starting a streaming query from a checkpoint>> (using the [Offset Write-Ahead Log](StreamExecution.md#offsetLog))

* Disabled (`false`) when <<populateStartOffsets, populating start offsets>> (when <<runActivatedStream, running an activated streaming query>>) and <<spark-sql-streaming-HDFSMetadataLog.md#getLatest, re-starting a streaming query from a checkpoint>> when the latest offset checkpointed (written) to the [offset write-ahead log](StreamExecution.md#offsetLog) has been successfully processed and <<spark-sql-streaming-HDFSMetadataLog.md#getLatest, committed>> to the [Offset Commit Log](StreamExecution.md#commitLog)

| readerToDataSourceMap
a| [[readerToDataSourceMap]] (`Map[MicroBatchReader, (DataSourceV2, Map[String, String])]`)

| sources
a| [[sources]] <<spark-sql-streaming-BaseStreamingSource.md#, Streaming sources and readers>> (of the <<spark-sql-streaming-StreamingExecutionRelation.md#, StreamingExecutionRelations>> of the <<analyzedPlan, analyzed logical query plan>> of the streaming query)

Default: (empty)

`sources` is part of the [ProgressReporter](ProgressReporter.md#sources) abstraction.

* Initialized when `MicroBatchExecution` is requested for the <<logicalPlan, transformed logical query plan>>

Used when:

* <<populateStartOffsets, Populating start offsets>> (for the [available](StreamExecution.md#availableOffsets) and [committed](StreamExecution.md#committedOffsets) offsets)

* <<constructNextBatch, Constructing or skipping next streaming micro-batch>> (and persisting offsets to write-ahead log)

| watermarkTracker
a| [[watermarkTracker]] <<spark-sql-streaming-WatermarkTracker.md#, WatermarkTracker>> that is created when `MicroBatchExecution` is requested to <<populateStartOffsets, populate start offsets>> (when requested to <<runActivatedStream, run an activated streaming query>>)

|===
