# MicroBatchExecution

`MicroBatchExecution` is a [stream execution engine](../StreamExecution.md) for [Micro-Batch Stream Processing](index.md).

## Creating Instance

`MicroBatchExecution` takes the following to be created:

* <span id="sparkSession"> `SparkSession` ([Spark SQL]({{ book.spark_sql }}/SparkSession))
* <span id="name"> Name of the streaming query
* <span id="checkpointRoot"> Path of the Checkpoint Directory
* <span id="analyzedPlan"> Analyzed logical query plan of the streaming query ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan))
* <span id="sink"> `Table` Sink ([Spark SQL]({{ book.spark_sql }}/connector/Table))
* <span id="trigger"> [Trigger](../Trigger.md)
* <span id="triggerClock"> `Clock`
* <span id="outputMode"> [OutputMode](../OutputMode.md)
* <span id="extraOptions"> Extra Options (`Map[String, String]`)
* <span id="deleteCheckpointOnStop"> `deleteCheckpointOnStop` flag to control whether to delete the checkpoint directory on stop

`MicroBatchExecution` is created when:

* `StreamingQueryManager` is requested to [create a streaming query](../StreamingQueryManager.md#createQuery) (when `DataStreamWriter` is requested to [start an execution of the streaming query](../DataStreamWriter.md#start)) with the following:

* All [sink](#sink)s
* All [trigger](#trigger)s but [ContinuousTrigger](../Trigger.md#ContinuousTrigger)

Once created, `MicroBatchExecution` is requested to [run an activated streaming query](#runActivatedStream).

## <span id="startTrigger"> Initializing Query Progress for New Trigger

```scala
startTrigger(): Unit
```

`startTrigger` is part of the [ProgressReporter](../monitoring/ProgressReporter.md#startTrigger) abstraction.

`startTrigger`...FIXME

## <span id="sources"> Streaming Sources Registry

```scala
sources: Seq[SparkDataStream]
```

`MicroBatchExecution` uses...FIXME

Streaming sources and readers (of the [StreamingExecutionRelations](../logical-operators/StreamingExecutionRelation.md) of the [analyzed logical query plan](#analyzedPlan) of the streaming query)

Default: (empty)

`sources` is part of the [ProgressReporter](../monitoring/ProgressReporter.md#sources) abstraction.

* Initialized when `MicroBatchExecution` is requested for the [transformed logical query plan](#logicalPlan)

Used when:

* [Populating start offsets](#populateStartOffsets) (for the [available](../StreamExecution.md#availableOffsets) and [committed](../StreamExecution.md#committedOffsets) offsets)

* [Constructing or skipping next streaming micro-batch](#constructNextBatch) (and persisting offsets to write-ahead log)

## <span id="triggerExecutor"> TriggerExecutor

```scala
triggerExecutor: TriggerExecutor
```

`MicroBatchExecution` uses a [TriggerExecutor](../TriggerExecutor.md) that is how micro-batches are executed at regular intervals.

`triggerExecutor` is initialized based on the given [Trigger](#trigger) (given when creating the `MicroBatchExecution`):

* [ProcessingTimeExecutor](../TriggerExecutor.md) for [Trigger.ProcessingTime](../Trigger.md#ProcessingTime)

* [OneTimeExecutor](../TriggerExecutor.md) for [OneTimeTrigger](../Trigger.md#OneTimeTrigger) (aka [Trigger.Once](../Trigger.md#Once) trigger)

`triggerExecutor` throws an `IllegalStateException` when the [Trigger](#trigger) is not one of the [built-in implementations](../Trigger.md#available-implementations).

```text
Unknown type of trigger: [trigger]
```

`triggerExecutor` is used when:

* `StreamExecution` is requested to [run an activated streaming query](#runActivatedStream) (at regular intervals)

## <span id="runActivatedStream"> Running Activated Streaming Query

```scala
runActivatedStream(
  sparkSessionForStream: SparkSession): Unit
```

`runActivatedStream` simply requests the [TriggerExecutor](#triggerExecutor) to execute micro-batches using the [batch runner](#batchRunner) (until `MicroBatchExecution` is [terminated](../StreamExecution.md#isActive) due to a query stop or a failure).

`runActivatedStream` is part of [StreamExecution](../StreamExecution.md#runActivatedStream) abstraction.

### <span id="batchRunner"><span id="batch-runner"> TriggerExecutor's Batch Runner

The batch runner (of the [TriggerExecutor](#triggerExecutor)) is executed as long as the `MicroBatchExecution` is [active](../StreamExecution.md#isActive).

!!! note
    _trigger_ and _batch_ are considered equivalent and used interchangeably.

<span id="runActivatedStream-startTrigger">
The batch runner [initializes query progress for the new trigger](../monitoring/ProgressReporter.md#startTrigger) (aka _startTrigger_).

<span id="runActivatedStream-triggerExecution"><span id="runActivatedStream-triggerExecution-populateStartOffsets">
The batch runner starts *triggerExecution* [execution phase](../monitoring/ProgressReporter.md#reportTimeTaken) that is made up of the following steps:

1. [Populating start offsets from checkpoint](#populateStartOffsets) before the first "zero" batch (at every start or restart)

1. [Constructing or skipping the next streaming micro-batch](#constructNextBatch)

1. [Running the streaming micro-batch](#runBatch)

At the start or restart (_resume_) of a streaming query (when the [current batch ID](#currentBatchId) is uninitialized and `-1`), the batch runner [populates start offsets from checkpoint](#populateStartOffsets) and then prints out the following INFO message to the logs (using the [committedOffsets](../StreamExecution.md#committedOffsets) internal registry):

```text
Stream started from [committedOffsets]
```

The batch runner sets the human-readable description for any Spark job submitted (that streaming sources may submit to get new data) as the [batch description](../StreamExecution.md#getBatchDescriptionString).

<span id="runActivatedStream-triggerExecution-isCurrentBatchConstructed">
The batch runner [constructs the next streaming micro-batch](#constructNextBatch) (when the [isCurrentBatchConstructed](#isCurrentBatchConstructed) internal flag is off).

The batch runner [records trigger offsets](#recordTriggerOffsets) (with the [committed](../StreamExecution.md#committedOffsets) and [available](../StreamExecution.md#availableOffsets) offsets).

The batch runner updates the [current StreamingQueryStatus](../monitoring/ProgressReporter.md#currentStatus) with the [isNewDataAvailable](#isNewDataAvailable) for [isDataAvailable](../monitoring/StreamingQueryStatus.md#isDataAvailable) property.

<span id="runActivatedStream-triggerExecution-runBatch">
With the [isCurrentBatchConstructed](#isCurrentBatchConstructed) flag enabled, the batch runner [updates the status message](../monitoring/ProgressReporter.md#updateStatusMessage) to one of the following (per [isNewDataAvailable](#isNewDataAvailable)) and [runs the streaming micro-batch](#runBatch).

```text
Processing new data
```

```text
No new data but cleaning up state
```

With the [isCurrentBatchConstructed](#isCurrentBatchConstructed) flag disabled (`false`), the batch runner simply [updates the status message](../monitoring/ProgressReporter.md#updateStatusMessage) to the following:

```text
Waiting for data to arrive
```

[[runActivatedStream-triggerExecution-finishTrigger]]
The batch runner [finalizes query progress for the trigger](../monitoring/ProgressReporter.md#finishTrigger) (with a flag that indicates whether the current batch had new data).

With the [isCurrentBatchConstructed](#isCurrentBatchConstructed) flag enabled (`true`), the batch runner increments the [currentBatchId](#currentBatchId) and turns the [isCurrentBatchConstructed](#isCurrentBatchConstructed) flag off (`false`).

With the [isCurrentBatchConstructed](#isCurrentBatchConstructed) flag disabled (`false`), the batch runner simply sleeps (as long as configured using the [spark.sql.streaming.pollingDelay](../StreamExecution.md#pollingDelayMs) configuration property).

In the end, the batch runner [updates the status message](../monitoring/ProgressReporter.md#updateStatusMessage) to the following status and returns whether the `MicroBatchExecution` is [active](../StreamExecution.md#isActive) or not.

```text
Waiting for next trigger
```

## <span id="populateStartOffsets"> Populating Start Offsets From Checkpoint (Resuming from Checkpoint)

```scala
populateStartOffsets(
  sparkSessionToRunBatches: SparkSession): Unit
```

`populateStartOffsets` requests the [Offset Write-Ahead Log](../StreamExecution.md#offsetLog) for the [latest committed batch id with metadata](../HDFSMetadataLog.md#getLatest) (i.e. [OffsetSeq](../OffsetSeq.md)).

!!! note
    The batch id could not be available in the write-ahead log when a streaming query started with a new log or no batch was persisted (_added_) to the log before.

`populateStartOffsets` branches off based on whether the latest committed batch was [available](#populateStartOffsets-getLatest-available) or [not](#populateStartOffsets-getLatest-not-available).

`populateStartOffsets` is used when `MicroBatchExecution` is requested to [run an activated streaming query](#runActivatedStream) ([before the first "zero" micro-batch](#runActivatedStream-triggerExecution-populateStartOffsets)).

### <span id="populateStartOffsets-getLatest-available"> Latest Committed Batch Available

When the latest committed batch id with the metadata was available in the [Offset Write-Ahead Log](../StreamExecution.md#offsetLog), `populateStartOffsets` (re)initializes the internal state as follows:

* Sets the [current batch ID](../StreamExecution.md#currentBatchId) to the latest committed batch ID found

* Turns the [isCurrentBatchConstructed](#isCurrentBatchConstructed) internal flag on (`true`)

* Sets the [available offsets](#availableOffsets) to the offsets (from the metadata)

When the latest batch ID found is greater than `0`, `populateStartOffsets` requests the [Offset Write-Ahead Log](../StreamExecution.md#offsetLog) for the [second latest batch ID with metadata](../HDFSMetadataLog.md#get) or throws an `IllegalStateException` if not found.

```text
batch [latestBatchId - 1] doesn't exist
```

`populateStartOffsets` sets the [committed offsets](#committedOffsets) to the second latest committed offsets.

<span id="populateStartOffsets-getLatest-available-offsetSeqMetadata">
`populateStartOffsets` updates the offset metadata.

CAUTION: FIXME Describe me

`populateStartOffsets` requests the [Offset Commit Log](../StreamExecution.md#commitLog) for the [latest committed batch id with metadata](../HDFSMetadataLog.md#getLatest).

CAUTION: FIXME Describe me

When the latest committed batch id with metadata was found which is exactly the latest batch ID (found in the [Offset Commit Log](../StreamExecution.md#commitLog)), `populateStartOffsets`...FIXME

When the latest committed batch id with metadata was found, but it is not exactly the second latest batch ID (found in the [Offset Commit Log](../StreamExecution.md#commitLog)), `populateStartOffsets` prints out the following WARN message to the logs:

```text
Batch completion log latest batch id is [latestCommittedBatchId], which is not trailing batchid [latestBatchId] by one
```

When no commit log present in the [Offset Commit Log](../StreamExecution.md#commitLog), `populateStartOffsets` prints out the following INFO message to the logs:

```text
no commit log present
```

In the end, `populateStartOffsets` prints out the following DEBUG message to the logs:

```text
Resuming at batch [currentBatchId] with committed offsets [committedOffsets] and available offsets [availableOffsets]
```

### <span id="populateStartOffsets-getLatest-not-available"> No Latest Committed Batch

When the latest committed batch id with the metadata could not be found in the [Offset Write-Ahead Log](../StreamExecution.md#offsetLog), it is assumed that the streaming query is started for the very first time (or the [checkpoint location](../StreamExecution.md#checkpointRoot) has changed).

`populateStartOffsets` prints out the following INFO message to the logs:

```text
Starting new streaming query.
```

[[populateStartOffsets-currentBatchId-0]]
`populateStartOffsets` sets the [current batch ID](../StreamExecution.md#currentBatchId) to `0` and creates a new [WatermarkTracker](#watermarkTracker).

## <span id="constructNextBatch"> Constructing Or Skipping Next Streaming Micro-Batch

```scala
constructNextBatch(
  noDataBatchesEnabled: Boolean): Boolean
```

`constructNextBatch` is used when `MicroBatchExecution` is requested to [run the activated streaming query](#runActivatedStream).

!!! note
    `constructNextBatch` is only executed when the [isCurrentBatchConstructed](#isCurrentBatchConstructed) internal flag is enabled (`true`).

`constructNextBatch` performs the following steps:

1. [Requesting the latest offsets from every streaming source](#constructNextBatch-latestOffsets) (of the streaming query)

1. [Updating availableOffsets StreamProgress with the latest available offsets](#constructNextBatch-availableOffsets)

1. [Updating batch metadata with the current event-time watermark and batch timestamp](#constructNextBatch-offsetSeqMetadata)

1. [Checking whether to construct the next micro-batch or not (skip it)](#constructNextBatch-shouldConstructNextBatch)

In the end, `constructNextBatch` returns [whether the next streaming micro-batch was constructed or skipped](#constructNextBatch-shouldConstructNextBatch).

### <span id="constructNextBatch-latestOffsets"> Requesting Latest Offsets from Streaming Sources (getOffset, setOffsetRange and getEndOffset Phases)

`constructNextBatch` firstly requests every [streaming source](../StreamExecution.md#uniqueSources) for the latest offsets.

!!! note
    `constructNextBatch` checks out the latest offset in every streaming data source sequentially, i.e. one data source at a time.

![MicroBatchExecution's Getting Offsets From Streaming Sources](../images/MicroBatchExecution-constructNextBatch.png)

For every [streaming source](../Source.md) (Data Source API V1), `constructNextBatch` [updates the status message](../monitoring/ProgressReporter.md#updateStatusMessage) to the following:

```text
Getting offsets from [source]
```

### <span id="constructNextBatch-getOffset"> getOffset Phase

In **getOffset** [time-tracking section](../monitoring/ProgressReporter.md#reportTimeTaken), `constructNextBatch` requests the `Source` for the [latest offset](#getOffset).

For every [MicroBatchReader](MicroBatchReader.md) (Data Source API V2), `constructNextBatch` [updates the status message](../monitoring/ProgressReporter.md#updateStatusMessage) to the following:

```text
Getting offsets from [source]
```

### <span id="constructNextBatch-setOffsetRange"> setOffsetRange Phase

In **setOffsetRange** [time-tracking section](../monitoring/ProgressReporter.md#reportTimeTaken), `constructNextBatch` finds the available offsets of the source (in the [available offset](#availableOffsets) internal registry) and, if found, requests the `MicroBatchReader` to [deserialize the offset](MicroBatchReader.md#deserializeOffset) (from [JSON format](../Offset.md#json)). `constructNextBatch` requests the `MicroBatchReader` to [set the desired offset range](MicroBatchReader.md#setOffsetRange).

### <span id="constructNextBatch-getEndOffset"> getEndOffset Phase

In **getEndOffset** [time-tracking section](../monitoring/ProgressReporter.md#reportTimeTaken), `constructNextBatch` requests the `MicroBatchReader` for the [end offset](MicroBatchReader.md#getEndOffset).

### <span id="constructNextBatch-availableOffsets"> Updating availableOffsets StreamProgress with Latest Available Offsets

`constructNextBatch` updates the [availableOffsets StreamProgress](../StreamExecution.md#availableOffsets) with the latest reported offsets.

### <span id="constructNextBatch-offsetSeqMetadata"> Updating Batch Metadata with Current Event-Time Watermark and Batch Timestamp

`constructNextBatch` updates the [batch metadata](../StreamExecution.md#offsetSeqMetadata) with the current [event-time watermark](../WatermarkTracker.md#currentWatermark) (from the [WatermarkTracker](#watermarkTracker)) and the batch timestamp.

### <span id="constructNextBatch-shouldConstructNextBatch"> Checking Whether to Construct Next Micro-Batch or Not (Skip It)

`constructNextBatch` checks whether or not the next streaming micro-batch should be constructed (`lastExecutionRequiresAnotherBatch`).

`constructNextBatch` uses the [last IncrementalExecution](../StreamExecution.md#lastExecution) if the [last execution requires another micro-batch](../IncrementalExecution.md#shouldRunAnotherBatch) (using the [batch metadata](../StreamExecution.md#offsetSeqMetadata)) and the given `noDataBatchesEnabled` flag is enabled (`true`).

`constructNextBatch` also [checks out whether new data is available (based on available and committed offsets)](#isNewDataAvailable).

!!! note
    `shouldConstructNextBatch` local flag is enabled (`true`) when [there is new data available (based on offsets)](#isNewDataAvailable) or the [last execution requires another micro-batch](../IncrementalExecution.md#shouldRunAnotherBatch) (and the given `noDataBatchesEnabled` flag is enabled).

`constructNextBatch` prints out the following TRACE message to the logs:

```text
noDataBatchesEnabled = [noDataBatchesEnabled], lastExecutionRequiresAnotherBatch = [lastExecutionRequiresAnotherBatch], isNewDataAvailable = [isNewDataAvailable], shouldConstructNextBatch = [shouldConstructNextBatch]
```

`constructNextBatch` branches off per whether to [constructs](#constructNextBatch-shouldConstructNextBatch-enabled) or [skip](#constructNextBatch-shouldConstructNextBatch-disabled) the next batch (per `shouldConstructNextBatch` flag in the above TRACE message).

### <span id="constructNextBatch-shouldConstructNextBatch-enabled"> Constructing Next Micro-Batch

With the [shouldConstructNextBatch](#constructNextBatch-shouldConstructNextBatch) flag enabled (`true`), `constructNextBatch` [updates the status message](../monitoring/ProgressReporter.md#updateStatusMessage) to the following:

```text
Writing offsets to log
```

[[constructNextBatch-walCommit]]
In *walCommit* [time-tracking section](../monitoring/ProgressReporter.md#reportTimeTaken), `constructNextBatch` requests the [availableOffsets StreamProgress](../StreamExecution.md#availableOffsets) to [convert to OffsetSeq](../StreamProgress.md#toOffsetSeq) (with the [BaseStreamingSources](#sources) and the [current batch metadata (event-time watermark and timestamp)](../StreamExecution.md#offsetSeqMetadata)) that is in turn [added](../HDFSMetadataLog.md#add) to the [write-ahead log](../StreamExecution.md#offsetLog) for the [current batch ID](../StreamExecution.md#currentBatchId).

`constructNextBatch` prints out the following INFO message to the logs:

```text
Committed offsets for batch [currentBatchId]. Metadata [offsetSeqMetadata]
```

??? fixme
    (`if (currentBatchId != 0) ...`)

??? fixme
    (`if (minLogEntriesToMaintain < currentBatchId) ...`)

`constructNextBatch` turns the [noNewData](../StreamExecution.md#noNewData) internal flag off (`false`).

In case of a failure while [adding the available offsets](../HDFSMetadataLog.md#add) to the [write-ahead log](../StreamExecution.md#offsetLog), `constructNextBatch` throws an `AssertionError`:

```text
Concurrent update to the log. Multiple streaming jobs detected for [currentBatchId]
```

### <span id="constructNextBatch-shouldConstructNextBatch-disabled"> Skipping Next Micro-Batch

With the [shouldConstructNextBatch](#constructNextBatch-shouldConstructNextBatch) flag disabled (`false`), `constructNextBatch` turns the [noNewData](../StreamExecution.md#noNewData) flag on (`true`) and wakes up (_notifies_) all threads waiting for the [awaitProgressLockCondition](../StreamExecution.md#awaitProgressLockCondition) lock.

## <span id="runBatch"> Running Single Streaming Micro-Batch

```scala
runBatch(
  sparkSessionToRunBatch: SparkSession): Unit
```

`runBatch` prints out the following DEBUG message to the logs (with the [current batch ID](../StreamExecution.md#currentBatchId)):

```text
Running batch [currentBatchId]
```

`runBatch` then performs the following steps (aka _phases_):

1. [getBatch Phase -- Creating Logical Query Plans For Unprocessed Data From Sources and MicroBatchReaders](#runBatch-getBatch)
1. [Transforming Logical Plan to Include Sources and MicroBatchReaders with New Data](#runBatch-newBatchesPlan)
1. [Transforming CurrentTimestamp and CurrentDate Expressions (Per Batch Metadata)](#runBatch-newAttributePlan)
1. [Adapting Transformed Logical Plan to Sink with StreamWriteSupport](#runBatch-triggerLogicalPlan)
1. [Setting Local Properties](#runBatch-setLocalProperty)
1. [queryPlanning Phase -- Creating and Preparing IncrementalExecution for Execution](#runBatch-queryPlanning)
1. [nextBatch Phase -- Creating DataFrame (with IncrementalExecution for New Data)](#runBatch-nextBatch)
1. [addBatch Phase -- Adding DataFrame With New Data to Sink](#runBatch-addBatch)
1. [Updating Watermark and Committing Offsets to Offset Commit Log](#runBatch-updateWatermark-commitLog)

In the end, `runBatch` prints out the following DEBUG message to the logs (with the [current batch ID](../StreamExecution.md#currentBatchId)):

```text
Completed batch [currentBatchId]
```

!!! NOTE
    `runBatch` is used exclusively when `MicroBatchExecution` is requested to [run an activated streaming query](#runActivatedStream) (and there is new data to process).

### <span id="runBatch-getBatch"> getBatch Phase -- Creating Logical Query Plans For Unprocessed Data From Sources and MicroBatchReaders

In *getBatch* [time-tracking section](../monitoring/ProgressReporter.md#reportTimeTaken), `runBatch` goes over the [available offsets](../StreamExecution.md#availableOffsets) and processes every [Source](#runBatch-getBatch-Source) and [MicroBatchReader](#runBatch-getBatch-MicroBatchReader) (associated with the available offsets) to create logical query plans (`newData`) for data processing (per offset ranges).

!!! NOTE
    `runBatch` requests sources and readers for data per offset range sequentially, one by one.

![StreamExecution's Running Single Streaming Batch (getBatch Phase)](../images/StreamExecution-runBatch-getBatch.png)

### <span id="runBatch-getBatch-Source"> getBatch Phase and Sources

For a [Source](../Source.md) (with the available [offset](../Offset.md)s different from the [committedOffsets](../StreamExecution.md#committedOffsets) registry), `runBatch` does the following:

* Requests the [committedOffsets](../StreamExecution.md#committedOffsets) for the committed offsets for the `Source` (if available)

* Requests the `Source` for a [dataframe for the offset range](../Source.md#getBatch) (the current and available offsets)

`runBatch` prints out the following DEBUG message to the logs.

```text
Retrieving data from [source]: [current] -> [available]
```

In the end, `runBatch` returns the `Source` and the logical plan of the streaming dataset (for the offset range).

In case the `Source` returns a dataframe that is not streaming, `runBatch` throws an `AssertionError`:

```text
DataFrame returned by getBatch from [source] did not have isStreaming=true\n[logicalQueryPlan]
```

### <span id="runBatch-getBatch-MicroBatchReader"> getBatch Phase and MicroBatchReaders

For a [MicroBatchReader](MicroBatchReader.md) (with the available [offset](../Offset.md)s different from the [committedOffsets](../StreamExecution.md#committedOffsets) registry),  `runBatch` does the following:

* Requests the [committedOffsets](../StreamExecution.md#committedOffsets) for the committed offsets for the `MicroBatchReader` (if available)

* Requests the `MicroBatchReader` to [deserialize the committed offsets](MicroBatchReader.md#deserializeOffset) (if available)

* Requests the `MicroBatchReader` to [deserialize the available offsets](MicroBatchReader.md#deserializeOffset) (only for [SerializedOffset](../Offset.md#SerializedOffset)s)

* Requests the `MicroBatchReader` to [set the offset range](MicroBatchReader.md#setOffsetRange) (the current and available offsets)

`runBatch` prints out the following DEBUG message to the logs.

```text
Retrieving data from [reader]: [current] -> [availableV2]
```

`runBatch` looks up the `DataSourceV2` and the options for the `MicroBatchReader` (in the [readerToDataSourceMap](#readerToDataSourceMap) internal registry).

In the end, `runBatch` requests the `MicroBatchReader` for the [read schema](MicroBatchReader.md#readSchema) and creates a [StreamingDataSourceV2Relation](../logical-operators/StreamingDataSourceV2Relation.md) logical operator (with the read schema, the `DataSourceV2`, options, and the `MicroBatchReader`).

### <span id="runBatch-newBatchesPlan"> Transforming Logical Plan to Include Sources and MicroBatchReaders with New Data

![StreamExecution's Running Single Streaming Batch (and Transforming Logical Plan for New Data)](../images/StreamExecution-runBatch-newBatchesPlan.png)

`runBatch` transforms the [analyzed logical plan](#logicalPlan) to include [Sources and MicroBatchReaders with new data](#runBatch-getBatch) (`newBatchesPlan` with logical plans to process data that has arrived since the last batch).

For every [StreamingExecutionRelation](../logical-operators/StreamingExecutionRelation.md), `runBatch` tries to find the corresponding logical plan for processing new data.

If the logical plan is found, `runBatch` makes the plan a child operator of `Project` (with `Aliases`) logical operator and replaces the `StreamingExecutionRelation`.

Otherwise, if not found, `runBatch` simply creates an empty streaming `LocalRelation` (for scanning data from an empty local collection).

In case the number of columns in dataframes with new data and ``StreamingExecutionRelation``'s do not match, `runBatch` throws an `AssertionError`:

```text
Invalid batch: [output] != [dataPlan.output]
```

### <span id="runBatch-newAttributePlan"> Transforming CurrentTimestamp and CurrentDate Expressions (Per Batch Metadata)

`runBatch` replaces all `CurrentTimestamp` and `CurrentDate` expressions in the [transformed logical plan (with new data)](#runBatch-newBatchesPlan) with the [current batch timestamp](../OffsetSeqMetadata.md#batchTimestampMs) (based on the [batch metadata](../StreamExecution.md#offsetSeqMetadata)).

!!! note
    `CurrentTimestamp` and `CurrentDate` expressions correspond to `current_timestamp` and `current_date` standard function, respectively.

### <span id="runBatch-triggerLogicalPlan"> Adapting Transformed Logical Plan to Sink with StreamWriteSupport

`runBatch`...FIXME

For a [Sink](../Sink.md) (Data Source API V1), `runBatch` changes nothing.

For any other [BaseStreamingSink](#sink) type, `runBatch` simply throws an `IllegalArgumentException`:

```text
unknown sink type for [sink]
```

### <span id="runBatch-setLocalProperty"> Setting Local Properties

`runBatch` sets the local properties.

Local Property | Value
---------------|------
[streaming.sql.batchId](#BATCH_ID_KEY) | [currentBatchId](../StreamExecution.md#currentBatchId)
[__is_continuous_processing](../StreamExecution.md#IS_CONTINUOUS_PROCESSING) | `false`

### <span id="runBatch-queryPlanning"> queryPlanning Phase -- Creating and Preparing IncrementalExecution for Execution

![StreamExecution's Query Planning (queryPlanning Phase)](../images/StreamExecution-runBatch-queryPlanning.png)

In *queryPlanning* [time-tracking section](../monitoring/ProgressReporter.md#reportTimeTaken), `runBatch` creates a new [IncrementalExecution](../StreamExecution.md#lastExecution) with the following:

* [Transformed logical plan](#runBatch-triggerLogicalPlan)

* [Output mode](#outputMode)

* `state` [checkpoint directory](#checkpointFile)

* [Run ID](../StreamExecution.md#runId)

* [Batch ID](../StreamExecution.md#currentBatchId)

* [Batch Metadata (Event-Time Watermark and Timestamp)](../StreamExecution.md#offsetSeqMetadata)

In the end (of the `queryPlanning` phase), `runBatch` requests the `IncrementalExecution` to prepare the transformed logical plan for execution (i.e. execute the `executedPlan` query execution phase).

!!! TIP
    Read up on the `executedPlan` query execution phase in https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-QueryExecution.html[The Internals of Spark SQL].

### <span id="runBatch-nextBatch"> nextBatch Phase &mdash; Creating DataFrame (with IncrementalExecution for New Data)

![StreamExecution Creates DataFrame with New Data](../images/StreamExecution-runBatch-nextBatch.png)

`runBatch` creates a new `DataFrame` with the new [IncrementalExecution](#runBatch-queryPlanning).

The `DataFrame` represents the result of executing the current micro-batch of the streaming query.

### <span id="runBatch-addBatch"> addBatch Phase &mdash; Adding DataFrame With New Data to Sink

![StreamExecution Adds DataFrame With New Data to Sink](../images/StreamExecution-runBatch-addBatch.png)

In **addBatch** [time-tracking section](../monitoring/ProgressReporter.md#reportTimeTaken), `runBatch` adds the `DataFrame` with new data to the [BaseStreamingSink](#sink).

For a [Sink](../Sink.md) (Data Source API V1), `runBatch` simply requests the `Sink` to [add the DataFrame](../Sink.md#addBatch) (with the [batch ID](../StreamExecution.md#currentBatchId)).

`runBatch` uses `SQLExecution.withNewExecutionId` to execute and track all the Spark jobs under one execution id (so it is reported as one single multi-job execution, e.g. in web UI).

!!! note
    `SQLExecution.withNewExecutionId` posts a `SparkListenerSQLExecutionStart` event before execution and a `SparkListenerSQLExecutionEnd` event right afterwards.

!!! tip
    Register `SparkListener` to get notified about the SQL execution events (`SparkListenerSQLExecutionStart` and `SparkListenerSQLExecutionEnd`).

### <span id="runBatch-updateWatermark-commitLog"> Updating Watermark and Committing Offsets to Offset Commit Log

`runBatch` requests the [WatermarkTracker](#watermarkTracker) to [update event-time watermark](../WatermarkTracker.md#updateWatermark) (with the `executedPlan` of the [IncrementalExecution](#runBatch-queryPlanning)).

`runBatch` requests the [Offset Commit Log](../StreamExecution.md#commitLog) to [persisting metadata of the streaming micro-batch](../HDFSMetadataLog.md#add) (with the current [batch ID](../StreamExecution.md#currentBatchId) and [event-time watermark](../WatermarkTracker.md#currentWatermark) of the [WatermarkTracker](#watermarkTracker)).

In the end, `runBatch` [adds](../StreamProgress.md#plusplus) the [available offsets](../StreamExecution.md#availableOffsets) to the [committed offsets](../StreamExecution.md#committedOffsets) (and updates the [offset](../Offset.md)s of every source with new data in the current micro-batch).

## <span id="stop"> Stopping Stream Processing (Execution of Streaming Query)

```scala
stop(): Unit
```

`stop` sets the [state](../StreamExecution.md#state) to `TERMINATED`.

When the [stream execution thread](../StreamExecution.md#queryExecutionThread) is alive, `stop` requests the current `SparkContext` to `cancelJobGroup` identified by the [runId](../StreamExecution.md#runId) and waits for this thread to die. Just to make sure that there are no more streaming jobs, `stop` requests the current `SparkContext` to `cancelJobGroup` identified by the [runId](../StreamExecution.md#runId) again.

In the end, `stop` prints out the following INFO message to the logs:

```text
Query [prettyIdString] was stopped
```

`stop` is part of the [StreamingQuery](../StreamingQuery.md#stop) abstraction.

## <span id="isNewDataAvailable"> Checking Whether New Data Is Available

```scala
isNewDataAvailable: Boolean
```

`isNewDataAvailable` returns whether or not there are streaming sources (in the [available offsets](#availableOffsets)) for which [committed offsets](#committedOffsets) are different from the available offsets or not available (committed) at all.

`isNewDataAvailable` is `true` when there is at least one such streaming source.

`isNewDataAvailable` is used when:

* `MicroBatchExecution` is requested to [run an activated streaming query](#runActivatedStream) and [construct the next streaming micro-batch](#constructNextBatch)

## <span id="logicalPlan"> Analyzed Logical Plan

```scala
logicalPlan: LogicalPlan
```

`logicalPlan` is part of the [StreamExecution](../StreamExecution.md#logicalPlan) abstraction.

`logicalPlan` resolves (_replaces_) [StreamingRelation](../logical-operators/StreamingRelation.md), [StreamingRelationV2](../logical-operators/StreamingRelationV2.md) logical operators to [StreamingExecutionRelation](../logical-operators/StreamingExecutionRelation.md) logical operators. `logicalPlan` uses the transformed logical plan to set the [uniqueSources](../StreamExecution.md#uniqueSources) and [sources](#sources) internal registries to be the [BaseStreamingSources](../logical-operators/StreamingExecutionRelation.md#source) of all the `StreamingExecutionRelations` unique and not, respectively.

??? note "Lazy Value"
    `logicalPlan` is a Scala **lazy value** to guarantee that the code to initialize it is executed once only (when accessed for the first time) and cached afterwards.

Internally, `logicalPlan` transforms the [analyzed logical plan](#analyzedPlan).

For every [StreamingRelation](../logical-operators/StreamingRelation.md) logical operator, `logicalPlan` tries to replace it with the [StreamingExecutionRelation](../logical-operators/StreamingExecutionRelation.md) that was used earlier for the same `StreamingRelation` (if used multiple times in the plan) or creates a new one. While creating a new `StreamingExecutionRelation`, `logicalPlan` requests the `DataSource` to [create a streaming Source](../DataSource.md#createSource) with the metadata path as `sources/uniqueID` directory in the [checkpoint root directory](../StreamExecution.md#resolvedCheckpointRoot). `logicalPlan` prints out the following INFO message to the logs:

```text
Using Source [source] from DataSourceV1 named '[sourceName]' [dataSourceV1]
```

For every [StreamingRelationV2](../logical-operators/StreamingRelationV2.md) logical operator with a [MicroBatchStream](../MicroBatchStream.md) data source (which is not on the list of [spark.sql.streaming.disabledV2MicroBatchReaders](../configuration-properties.md#spark.sql.streaming.disabledV2MicroBatchReaders)), `logicalPlan` tries to replace it with the [StreamingExecutionRelation](../logical-operators/StreamingExecutionRelation.md) that was used earlier for the same `StreamingRelationV2` (if used multiple times in the plan) or creates a new one. While creating a new `StreamingExecutionRelation`, `logicalPlan` requests the `MicroBatchStream` to [create a MicroBatchStream](../MicroBatchStream.md#createMicroBatchReader) with the metadata path as `sources/uniqueID` directory in the [checkpoint root directory](../StreamExecution.md#resolvedCheckpointRoot). `logicalPlan` prints out the following INFO message to the logs:

```text
Using MicroBatchReader [reader] from DataSourceV2 named '[sourceName]' [dataSourceV2]
```

For every other [StreamingRelationV2](../logical-operators/StreamingRelationV2.md) leaf logical operator, `logicalPlan` tries to replace it with the [StreamingExecutionRelation](../logical-operators/StreamingExecutionRelation.md) that was used earlier for the same `StreamingRelationV2` (if used multiple times in the plan) or creates a new one. While creating a new `StreamingExecutionRelation`, `logicalPlan` requests the `StreamingRelation` for the underlying [DataSource](../logical-operators/StreamingRelation.md#dataSource) that is in turn requested to [create a streaming Source](../DataSource.md#createSource) with the metadata path as `sources/uniqueID` directory in the [checkpoint root directory](../StreamExecution.md#resolvedCheckpointRoot). `logicalPlan` prints out the following INFO message to the logs:

```text
Using Source [source] from DataSourceV2 named '[sourceName]' [dataSourceV2]
```

`logicalPlan` requests the transformed analyzed logical plan for all `StreamingExecutionRelations` that are then requested for [BaseStreamingSources](../logical-operators/StreamingExecutionRelation.md#source), and saves them as the [sources](#sources) internal registry.

In the end, `logicalPlan` sets the [uniqueSources](../StreamExecution.md#uniqueSources) internal registry to be the unique `BaseStreamingSources` above.

`logicalPlan` throws an `AssertionError` when not executed on the [stream execution thread](../StreamExecution.md#queryExecutionThread).

```text
logicalPlan must be initialized in QueryExecutionThread but the current thread was [currentThread]
```

## <span id="BATCH_ID_KEY"><span id="streaming.sql.batchId"> streaming.sql.batchId Local Property

`MicroBatchExecution` defines **streaming.sql.batchId** as the name of the local property to be the current **batch** or **epoch IDs** (that Spark tasks can use at execution time).

`streaming.sql.batchId` is used when:

* `MicroBatchExecution` is requested to [run a single streaming micro-batch](#runBatch) (and sets the property to be the current batch ID)
* `DataWritingSparkTask` is requested to run (and needs an epoch ID)

## <span id="watermarkTracker"> WatermarkTracker

[WatermarkTracker](../WatermarkTracker.md) that is created when `MicroBatchExecution` is requested to [populate start offsets](#populateStartOffsets) (when requested to [run an activated streaming query](#runActivatedStream))

## <span id="isCurrentBatchConstructed"> isCurrentBatchConstructed Flag

```scala
isCurrentBatchConstructed: Boolean
```

`MicroBatchExecution` uses `isCurrentBatchConstructed` internal flag to control whether or not to [run a streaming micro-batch](#runBatch).

Default: `false`

When `false`, changed to whatever [constructing the next streaming micro-batch](#constructNextBatch) gives back when [running activated streaming query](#runActivatedStream)

Disabled (`false`) after [running a streaming micro-batch](#runBatch) (when enabled after [constructing the next streaming micro-batch](#constructNextBatch))

Enabled (`true`) when [populating start offsets](#populateStartOffsets) (when [running an activated streaming query](#runActivatedStream)) and [re-starting a streaming query from a checkpoint](../HDFSMetadataLog.md#getLatest) (using the [Offset Write-Ahead Log](../StreamExecution.md#offsetLog))

Disabled (`false`) when [populating start offsets](#populateStartOffsets) (when [running an activated streaming query](#runActivatedStream)) and [re-starting a streaming query from a checkpoint](../HDFSMetadataLog.md#getLatest) when the latest offset checkpointed (written) to the [offset write-ahead log](../StreamExecution.md#offsetLog) has been successfully processed and [committed](../HDFSMetadataLog.md#getLatest) to the [Offset Commit Log](../StreamExecution.md#commitLog)

## Demo

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

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.MicroBatchExecution` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.execution.streaming.MicroBatchExecution=ALL
```

Refer to [Logging](../spark-logging.md).
