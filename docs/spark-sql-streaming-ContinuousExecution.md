== [[ContinuousExecution]] ContinuousExecution -- Stream Execution Engine of Continuous Stream Processing

`ContinuousExecution` is the <<spark-sql-streaming-StreamExecution.md#, stream execution engine>> of <<spark-sql-streaming-continuous-stream-processing.md#, Continuous Stream Processing>>.

`ContinuousExecution` is <<creating-instance, created>> when `StreamingQueryManager` is requested to <<spark-sql-streaming-StreamingQueryManager.md#createQuery, create a streaming query>> with a <<sink, StreamWriteSupport sink>> and a <<trigger, ContinuousTrigger>> (when `DataStreamWriter` is requested to [start an execution of the streaming query](DataStreamWriter.md#start)).

`ContinuousExecution` can only run streaming queries with <<spark-sql-streaming-StreamingRelationV2.md#, StreamingRelationV2>> with <<spark-sql-streaming-ContinuousReadSupport.md#, ContinuousReadSupport>> data source.

[[sources]]
`ContinuousExecution` supports one <<continuousSources, ContinuousReader>> only in a <<logicalPlan, streaming query>> (and asserts it when <<addOffset, addOffset>> and <<commit, committing an epoch>>). When requested for available <<spark-sql-streaming-ProgressReporter.md#sources, streaming sources>>, `ContinuousExecution` simply gives the <<continuousSources, single ContinuousReader>>.

[source, scala]
----
import org.apache.spark.sql.streaming.Trigger
import scala.concurrent.duration._
val sq = spark
  .readStream
  .format("rate")
  .load
  .writeStream
  .format("console")
  .option("truncate", false)
  .trigger(Trigger.Continuous(1.minute)) // <-- Gives ContinuousExecution
  .queryName("rate2console")
  .start

import org.apache.spark.sql.streaming.StreamingQuery
assert(sq.isInstanceOf[StreamingQuery])

// The following gives access to the internals
// And to ContinuousExecution
import org.apache.spark.sql.execution.streaming.StreamingQueryWrapper
val engine = sq.asInstanceOf[StreamingQueryWrapper].streamingQuery
import org.apache.spark.sql.execution.streaming.StreamExecution
assert(engine.isInstanceOf[StreamExecution])

import org.apache.spark.sql.execution.streaming.continuous.ContinuousExecution
val continuousEngine = engine.asInstanceOf[ContinuousExecution]
assert(continuousEngine.trigger == Trigger.Continuous(1.minute))
----

When <<creating-instance, created>> (for a streaming query), `ContinuousExecution` is given the <<analyzedPlan, analyzed logical plan>>. The analyzed logical plan is immediately transformed to include a <<spark-sql-streaming-ContinuousExecutionRelation.md#, ContinuousExecutionRelation>> for every <<spark-sql-streaming-StreamingRelationV2.md#, StreamingRelationV2>> with <<spark-sql-streaming-ContinuousReadSupport.md#, ContinuousReadSupport>> data source (and is the <<logicalPlan, logical plan>> internally).

NOTE: `ContinuousExecution` uses the same instance of `ContinuousExecutionRelation` for the same instances of <<spark-sql-streaming-StreamingRelationV2.md#, StreamingRelationV2>> with <<spark-sql-streaming-ContinuousReadSupport.md#, ContinuousReadSupport>> data source.

When requested to <<runContinuous, run the streaming query>>, `ContinuousExecution` collects <<spark-sql-streaming-ContinuousReadSupport.md#, ContinuousReadSupport>> data sources (inside <<spark-sql-streaming-ContinuousExecutionRelation.md#, ContinuousExecutionRelation>>) from the <<logicalPlan, analyzed logical plan>> and requests each and every `ContinuousReadSupport` to <<spark-sql-streaming-ContinuousReadSupport.md#createContinuousReader, create a ContinuousReader>> (that are stored in <<continuousSources, continuousSources>> internal registry).

[[EPOCH_COORDINATOR_ID_KEY]]
`ContinuousExecution` uses *__epoch_coordinator_id* local property for...FIXME

[[START_EPOCH_KEY]]
`ContinuousExecution` uses *__continuous_start_epoch* local property for...FIXME

[[EPOCH_INTERVAL_KEY]]
`ContinuousExecution` uses *__continuous_epoch_interval* local property for...FIXME

[[logging]]
[TIP]
====
Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.continuous.ContinuousExecution` to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.execution.streaming.continuous.ContinuousExecution=ALL
```

Refer to <<spark-sql-streaming-logging.md#, Logging>>.
====

=== [[runActivatedStream]] Running Activated Streaming Query -- `runActivatedStream` Method

[source, scala]
----
runActivatedStream(sparkSessionForStream: SparkSession): Unit
----

NOTE: `runActivatedStream` is part of <<spark-sql-streaming-StreamExecution.md#runActivatedStream, StreamExecution Contract>> to run a streaming query.

`runActivatedStream` simply <<runContinuous, runs the streaming query in continuous mode>> as long as the <<spark-sql-streaming-StreamExecution.md#state, state>> is <<spark-sql-streaming-StreamExecution.md#ACTIVE, ACTIVE>>.

=== [[runContinuous]] Running Streaming Query in Continuous Mode -- `runContinuous` Internal Method

[source, scala]
----
runContinuous(sparkSessionForQuery: SparkSession): Unit
----

`runContinuous` initializes the <<continuousSources, continuousSources>> internal registry by traversing the <<logicalPlan, analyzed logical plan>> to find <<spark-sql-streaming-ContinuousExecutionRelation.md#, ContinuousExecutionRelation>> leaf logical operators and requests their <<spark-sql-streaming-ContinuousReadSupport.md#, ContinuousReadSupport data sources>> to <<spark-sql-streaming-ContinuousReadSupport.md#createContinuousReader, create a ContinuousReader>> (with the *sources* metadata directory under the <<spark-sql-streaming-StreamExecution.md#resolvedCheckpointRoot, checkpoint directory>>).

`runContinuous` initializes the <<spark-sql-streaming-StreamExecution.md#uniqueSources, uniqueSources>> internal registry to be the <<continuousSources, continuousSources>> distinct.

`runContinuous` <<getStartOffsets, gets the start offsets>> (they may or may not be available).

`runContinuous` transforms the <<logicalPlan, analyzed logical plan>>. For every <<spark-sql-streaming-ContinuousExecutionRelation.md#, ContinuousExecutionRelation>> `runContinuous` finds the corresponding <<spark-sql-streaming-ContinuousReader.md#, ContinuousReader>> (in the <<continuousSources, continuousSources>>), requests it to <<spark-sql-streaming-ContinuousReader.md#deserializeOffset, deserialize the start offsets>> (from their JSON representation), and then <<spark-sql-streaming-ContinuousReader.md#setStartOffset, setStartOffset>>. In the end, `runContinuous` creates a `StreamingDataSourceV2Relation` (with the read schema of the `ContinuousReader` and the `ContinuousReader` itself).

`runContinuous` rewires the transformed plan (with the `StreamingDataSourceV2Relation`) to use the new attributes from the source (the reader).

NOTE: `CurrentTimestamp` and `CurrentDate` expressions are not supported for continuous processing.

`runContinuous` requests the <<sink, StreamWriteSupport>> to <<spark-sql-streaming-StreamWriteSupport.md#createStreamWriter, create a StreamWriter>> (with the <<spark-sql-streaming-StreamExecution.md#runId, run ID of the streaming query>>).

`runContinuous` creates a <<spark-sql-streaming-WriteToContinuousDataSource.md#, WriteToContinuousDataSource>> (with the <<spark-sql-streaming-StreamWriter.md#, StreamWriter>> and the transformed logical query plan).

`runContinuous` finds the only <<spark-sql-streaming-ContinuousReader.md#, ContinuousReader>> (of the only `StreamingDataSourceV2Relation`) in the query plan with the `WriteToContinuousDataSource`.

[[runContinuous-queryPlanning]]
In *queryPlanning* <<spark-sql-streaming-ProgressReporter.md#reportTimeTaken, time-tracking section>>, `runContinuous` creates an <<spark-sql-streaming-IncrementalExecution.md#, IncrementalExecution>> (that becomes the <<spark-sql-streaming-StreamExecution.md#lastExecution, lastExecution>>) that is immediately executed (i.e. the entire query execution pipeline is executed up to and including _executedPlan_).

`runContinuous` sets the following local properties:

* <<spark-sql-streaming-StreamExecution.md#IS_CONTINUOUS_PROCESSING, __is_continuous_processing>> as `true`

* <<START_EPOCH_KEY, __continuous_start_epoch>> as the <<spark-sql-streaming-StreamExecution.md#currentBatchId, currentBatchId>>

* <<EPOCH_COORDINATOR_ID_KEY, __epoch_coordinator_id>> as the <<currentEpochCoordinatorId, currentEpochCoordinatorId>>, i.e. <<spark-sql-streaming-StreamExecution.md#runId, runId>> followed by `--` with a random UUID

* <<EPOCH_INTERVAL_KEY, __continuous_epoch_interval>> as the interval of the <<spark-sql-streaming-Trigger.md#ContinuousTrigger, ContinuousTrigger>>

`runContinuous` uses the `EpochCoordinatorRef` helper to <<spark-sql-streaming-EpochCoordinatorRef.md#create, create a remote reference to the EpochCoordinator RPC endpoint>> (with the <<spark-sql-streaming-StreamWriter.md#, StreamWriter>>, the <<spark-sql-streaming-ContinuousReader.md#, ContinuousReader>>, the <<currentEpochCoordinatorId, currentEpochCoordinatorId>>, and the <<spark-sql-streaming-StreamExecution.md#currentBatchId, currentBatchId>>).

NOTE: The <<spark-sql-streaming-EpochCoordinator.md#, EpochCoordinator RPC endpoint>> runs on the driver as the single point to coordinate epochs across partition tasks.

`runContinuous` creates a daemon <<runContinuous-epoch-update-thread, epoch update thread>> and starts it immediately.

[[runContinuous-runContinuous]]
In *runContinuous* <<spark-sql-streaming-ProgressReporter.md#reportTimeTaken, time-tracking section>>, `runContinuous` requests the physical query plan (of the <<spark-sql-streaming-StreamExecution.md#lastExecution, IncrementalExecution>>) to execute (that simply requests the physical operator to `doExecute` and generate an `RDD[InternalRow]`).

NOTE: `runContinuous` is used exclusively when `ContinuousExecution` is requested to <<runActivatedStream, run an activated streaming query>>.

==== [[runContinuous-epoch-update-thread]] Epoch Update Thread

`runContinuous` creates an *epoch update thread* that...FIXME

==== [[getStartOffsets]] Getting Start Offsets From Checkpoint -- `getStartOffsets` Internal Method

[source, scala]
----
getStartOffsets(sparkSessionToRunBatches: SparkSession): OffsetSeq
----

`getStartOffsets`...FIXME

NOTE: `getStartOffsets` is used exclusively when `ContinuousExecution` is requested to <<runContinuous, run a streaming query in continuous mode>>.

=== [[commit]] Committing Epoch -- `commit` Method

[source, scala]
----
commit(epoch: Long): Unit
----

In essence, `commit` <<spark-sql-streaming-HDFSMetadataLog.md#add, adds>> the given epoch to <<spark-sql-streaming-StreamExecution.md#commitLog, commit log>> and the <<spark-sql-streaming-StreamExecution.md#committedOffsets, committedOffsets>>, and requests the <<continuousSources, ContinuousReader>> to <<spark-sql-streaming-ContinuousReader.md#commit, commit the corresponding offset>>. In the end, `commit` <<spark-sql-streaming-HDFSMetadataLog.md#purge, removes old log entries>> from the <<spark-sql-streaming-StreamExecution.md#offsetLog, offset>> and <<spark-sql-streaming-StreamExecution.md#commitLog, commit>> logs (to keep <<spark-sql-streaming-StreamExecution.md#minLogEntriesToMaintain, spark.sql.streaming.minBatchesToRetain>> entries only).

Internally, `commit` <<spark-sql-streaming-ProgressReporter.md#recordTriggerOffsets, recordTriggerOffsets>> (with the from and to offsets as the <<spark-sql-streaming-StreamExecution.md#committedOffsets, committedOffsets>> and <<spark-sql-streaming-StreamExecution.md#availableOffsets, availableOffsets>>, respectively).

At this point, `commit` may simply return when the <<spark-sql-streaming-StreamExecution.md#queryExecutionThread, stream execution thread>> is no longer alive (died).

`commit` requests the <<spark-sql-streaming-StreamExecution.md#commitLog, commit log>> to <<spark-sql-streaming-HDFSMetadataLog.md#add, store a metadata>> for the epoch.

`commit` requests the single <<continuousSources, ContinuousReader>> to <<spark-sql-streaming-ContinuousReader.md#deserializeOffset, deserialize the offset>> for the epoch (from the <<spark-sql-streaming-StreamExecution.md#offsetLog, offset write-ahead log>>).

`commit` adds the single <<continuousSources, ContinuousReader>> and the offset (for the epoch) to the <<spark-sql-streaming-StreamExecution.md#committedOffsets, committedOffsets>> registry.

`commit` requests the single <<continuousSources, ContinuousReader>> to <<spark-sql-streaming-ContinuousReader.md#commit, commit the offset>>.

`commit` requests the <<spark-sql-streaming-StreamExecution.md#offsetLog, offset>> and <<spark-sql-streaming-StreamExecution.md#commitLog, commit>> logs to <<spark-sql-streaming-HDFSMetadataLog.md#purge, remove log entries>> to keep <<spark-sql-streaming-StreamExecution.md#minLogEntriesToMaintain, spark.sql.streaming.minBatchesToRetain>> only.

`commit` then acquires the <<spark-sql-streaming-StreamExecution.md#awaitProgressLock, awaitProgressLock>>, wakes up all threads waiting for the <<spark-sql-streaming-StreamExecution.md#awaitProgressLockCondition, awaitProgressLockCondition>> and in the end releases the <<spark-sql-streaming-StreamExecution.md#awaitProgressLock, awaitProgressLock>>.

NOTE: `commit` supports only one continuous source (registered in the <<continuousSources, continuousSources>> internal registry).

`commit` asserts that the given epoch is available in the <<spark-sql-streaming-StreamExecution.md#offsetLog, offsetLog>> internal registry (i.e. the offset for the given epoch has been reported before).

NOTE: `commit` is used exclusively when `EpochCoordinator` is requested to <<spark-sql-streaming-EpochCoordinator.md#commitEpoch, commitEpoch>>.

=== [[addOffset]] `addOffset` Method

[source, scala]
----
addOffset(
  epoch: Long,
  reader: ContinuousReader,
  partitionOffsets: Seq[PartitionOffset]): Unit
----

In essense, `addOffset` requests the given <<spark-sql-streaming-ContinuousReader.md#, ContinuousReader>> to <<spark-sql-streaming-ContinuousReader.md#mergeOffsets, mergeOffsets>> (with the given `PartitionOffsets`) and then requests the <<spark-sql-streaming-StreamExecution.md#offsetLog, OffsetSeqLog>> to <<spark-sql-streaming-HDFSMetadataLog.md#add, register the offset with the given epoch>>.

.ContinuousExecution.addOffset
image::images/ContinuousExecution-addOffset.png[align="center"]

Internally, `addOffset` requests the given <<spark-sql-streaming-ContinuousReader.md#, ContinuousReader>> to <<spark-sql-streaming-ContinuousReader.md#mergeOffsets, mergeOffsets>> (with the given `PartitionOffsets`) and to get the current "global" offset back.

`addOffset` then requests the <<spark-sql-streaming-StreamExecution.md#offsetLog, OffsetSeqLog>> to <<spark-sql-streaming-HDFSMetadataLog.md#add, add>> the current "global" offset for the given `epoch`.

`addOffset` requests the <<spark-sql-streaming-StreamExecution.md#offsetLog, OffsetSeqLog>> for the <<spark-sql-streaming-HDFSMetadataLog.md#get, offset at the previous epoch>>.

If the offsets at the current and previous epochs are the same, `addOffset` turns the <<spark-sql-streaming-StreamExecution.md#noNewData, noNewData>> internal flag on.

`addOffset` then acquires the <<spark-sql-streaming-StreamExecution.md#awaitProgressLock, awaitProgressLock>>, wakes up all threads waiting for the <<spark-sql-streaming-StreamExecution.md#awaitProgressLockCondition, awaitProgressLockCondition>> and in the end releases the <<spark-sql-streaming-StreamExecution.md#awaitProgressLock, awaitProgressLock>>.

NOTE: `addOffset` supports exactly one <<continuousSources, continuous source>>.

NOTE: `addOffset` is used exclusively when `EpochCoordinator` is requested to <<spark-sql-streaming-EpochCoordinator.md#ReportPartitionOffset, handle a ReportPartitionOffset message>>.

=== [[logicalPlan]] Analyzed Logical Plan of Streaming Query -- `logicalPlan` Property

[source, scala]
----
logicalPlan: LogicalPlan
----

NOTE: `logicalPlan` is part of <<spark-sql-streaming-StreamExecution.md#logicalPlan, StreamExecution Contract>> that is the analyzed logical plan of the streaming query.

`logicalPlan` resolves <<spark-sql-streaming-StreamingRelationV2.md#, StreamingRelationV2>> leaf logical operators (with a <<spark-sql-streaming-ContinuousReadSupport.md#, ContinuousReadSupport>> source) to <<spark-sql-streaming-ContinuousExecutionRelation.md#, ContinuousExecutionRelation>> leaf logical operators.

Internally, `logicalPlan` transforms the <<analyzedPlan, analyzed logical plan>> as follows:

. For every <<spark-sql-streaming-StreamingRelationV2.md#, StreamingRelationV2>> leaf logical operator with a <<spark-sql-streaming-ContinuousReadSupport.md#, ContinuousReadSupport>> source, `logicalPlan` looks it up for the corresponding <<spark-sql-streaming-ContinuousExecutionRelation.md#, ContinuousExecutionRelation>> (if available in the internal lookup registry) or creates a `ContinuousExecutionRelation` (with the `ContinuousReadSupport` source, the options and the output attributes of the `StreamingRelationV2` operator)

. For any other `StreamingRelationV2`, `logicalPlan` throws an `UnsupportedOperationException`:
+
```
Data source [name] does not support continuous processing.
```

=== [[creating-instance]] Creating ContinuousExecution Instance

`ContinuousExecution` takes the following when created:

* [[sparkSession]] `SparkSession`
* [[name]] The name of the structured query
* [[checkpointRoot]] Path to the checkpoint directory (aka _metadata directory_)
* [[analyzedPlan]] Analyzed logical query plan (`LogicalPlan`)
* [[sink]] <<spark-sql-streaming-StreamWriteSupport.md#, StreamWriteSupport>>
* [[trigger]] <<spark-sql-streaming-Trigger.md#, Trigger>>
* [[triggerClock]] `Clock`
* [[outputMode]] <<spark-sql-streaming-OutputMode.md#, Output mode>>
* [[extraOptions]] Options (`Map[String, String]`)
* [[deleteCheckpointOnStop]] `deleteCheckpointOnStop` flag to control whether to delete the checkpoint directory on stop

`ContinuousExecution` initializes the <<internal-properties, internal properties>>.

=== [[stop]] Stopping Stream Processing (Execution of Streaming Query) -- `stop` Method

[source, scala]
----
stop(): Unit
----

NOTE: `stop` is part of the <<spark-sql-streaming-StreamingQuery.md#stop, StreamingQuery Contract>> to stop a streaming query.

`stop` transitions the streaming query to `TERMINATED` state.

If the <<spark-sql-streaming-StreamExecution.md#queryExecutionThread, queryExecutionThread>> is alive (i.e. it has been started and has not yet died), `stop` interrupts it and waits for this thread to die.

In the end, `stop` prints out the following INFO message to the logs:

```
Query [prettyIdString] was stopped
```

NOTE: <<spark-sql-streaming-StreamExecution.md#prettyIdString, prettyIdString>> is in the format of `queryName [id = [id], runId = [runId]]`.

=== [[awaitEpoch]] `awaitEpoch` Internal Method

[source, scala]
----
awaitEpoch(epoch: Long): Unit
----

`awaitEpoch`...FIXME

NOTE: `awaitEpoch` seems to be used exclusively in tests.

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| continuousSources
a| [[continuousSources]]

[source, scala]
----
continuousSources: Seq[ContinuousReader]
----

Registry of <<spark-sql-streaming-ContinuousReader.md#, ContinuousReaders>> (in the <<logicalPlan, analyzed logical plan of the streaming query>>)

As asserted in <<commit, commit>> and <<addOffset, addOffset>> there could only be exactly one `ContinuousReaders` registered.

Used when `ContinuousExecution` is requested to <<commit, commit>>, <<getStartOffsets, getStartOffsets>>, and <<runContinuous, runContinuous>>

Use <<sources, sources>> to access the current value

| currentEpochCoordinatorId
| [[currentEpochCoordinatorId]] FIXME

Used when...FIXME

| triggerExecutor
a| [[triggerExecutor]] <<spark-sql-streaming-TriggerExecutor.md#, TriggerExecutor>> for the <<trigger, Trigger>>:

* `ProcessingTimeExecutor` for <<spark-sql-streaming-Trigger.md#ContinuousTrigger, ContinuousTrigger>>

Used when...FIXME

NOTE: `StreamExecution` throws an `IllegalStateException` when the <<trigger, Trigger>> is not a <<spark-sql-streaming-Trigger.md#ContinuousTrigger, ContinuousTrigger>>.
|===
