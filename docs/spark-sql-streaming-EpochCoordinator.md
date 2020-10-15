# EpochCoordinator RPC Endpoint

`EpochCoordinator` is a `ThreadSafeRpcEndpoint` that tracks offsets and epochs (_coordinates epochs_) by handling <<messages, messages>> (in <<receive, fire-and-forget one-way>> and <<receiveAndReply, request-response two-way>> modes) from...FIXME

`EpochCoordinator` is <<creating-instance, created>> (using <<create, create>> factory method) when `ContinuousExecution` is requested to <<ContinuousExecution.md#runContinuous, run a streaming query in continuous mode>>.

[[messages]]
[[EpochCoordinatorMessage]]
.EpochCoordinator RPC Endpoint's Messages
[cols="30m,70",options="header",width="100%"]
|===
| Message
| Description

a| `CommitPartitionEpoch`

* [[CommitPartitionEpoch-partitionId]] Partition ID
* [[CommitPartitionEpoch-epoch]] Epoch
* [[CommitPartitionEpoch-message]] DataSource API V2's `WriterCommitMessage`

| [[CommitPartitionEpoch]] Sent out (in one-way asynchronous mode) exclusively when `ContinuousWriteRDD` is requested to <<spark-sql-streaming-ContinuousWriteRDD.md#compute, compute a partition>> (after all rows were written down to a streaming sink)

| GetCurrentEpoch
| [[GetCurrentEpoch]] Sent out (in request-response synchronous mode) exclusively when `EpochMarkerGenerator` thread is requested to <<spark-sql-streaming-ContinuousQueuedDataReader-EpochMarkerGenerator.md#run, run>>

| IncrementAndGetEpoch
| [[IncrementAndGetEpoch]] Sent out (in request-response synchronous mode) exclusively when `ContinuousExecution` is requested to <<ContinuousExecution.md#runContinuous, run a streaming query in continuous mode>> (and start a separate epoch update thread)

a| `ReportPartitionOffset`

* [[ReportPartitionOffset-partitionId]] Partition ID
* [[ReportPartitionOffset-epoch]] Epoch
* [[ReportPartitionOffset-offset]] <<spark-sql-streaming-PartitionOffset.md#, PartitionOffset>>

| [[ReportPartitionOffset]] Sent out (in one-way asynchronous mode) exclusively when `ContinuousQueuedDataReader` is requested for the <<spark-sql-streaming-ContinuousQueuedDataReader.md#next, next row>> to be read in the current epoch, and the epoch is done

a| `SetReaderPartitions`

* [[SetReaderPartitions-numPartitions]] Number of partitions

| [[SetReaderPartitions]] Sent out (in request-response synchronous mode) exclusively when `DataSourceV2ScanExec` leaf physical operator is requested for the input RDDs (for a <<spark-sql-streaming-ContinuousReader.md#, ContinuousReader>> and is about to create a <<spark-sql-streaming-ContinuousDataSourceRDD.md#, ContinuousDataSourceRDD>>)

The <<SetReaderPartitions-numPartitions, number of partitions>> is exactly the number of `InputPartitions` from the `ContinuousReader`.

a| `SetWriterPartitions`

* [[SetWriterPartitions-numPartitions]] Number of partitions

| [[SetWriterPartitions]] Sent out (in request-response synchronous mode) exclusively when `WriteToContinuousDataSourceExec` leaf physical operator is requested to <<spark-sql-streaming-WriteToContinuousDataSourceExec.md#doExecute, execute and generate a recipe for a distributed computation (as an RDD[InternalRow])>> (and requests a <<spark-sql-streaming-ContinuousWriteRDD.md#, ContinuousWriteRDD>> to collect that simply never finishes...and that's the _trick_ of continuous mode)

a| `StopContinuousExecutionWrites`
| [[StopContinuousExecutionWrites]] Sent out (in request-response synchronous mode) exclusively when `ContinuousExecution` is requested to <<ContinuousExecution.md#runContinuous, run a streaming query in continuous mode>> (and it finishes successfully or not)

|===

[[logging]]
[TIP]
====
Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.continuous.EpochCoordinatorRef*` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.execution.streaming.continuous.EpochCoordinatorRef*=ALL
```

Refer to <<spark-sql-streaming-spark-logging.md#, Logging>>.
====

=== [[receive]] Receiving Messages (Fire-And-Forget One-Way Mode) -- `receive` Method

[source, scala]
----
receive: PartialFunction[Any, Unit]
----

NOTE: `receive` is part of the `RpcEndpoint` Contract in Apache Spark to receive messages in fire-and-forget one-way mode.

`receive` handles the following messages:

* <<CommitPartitionEpoch, CommitPartitionEpoch>>
* <<ReportPartitionOffset, ReportPartitionOffset>>

With the <<queryWritesStopped, queryWritesStopped>> turned on, `receive` simply _swallows_ messages and does nothing.

=== [[receiveAndReply]] Receiving Messages (Request-Response Two-Way Mode) -- `receiveAndReply` Method

[source, scala]
----
receiveAndReply(context: RpcCallContext): PartialFunction[Any, Unit]
----

NOTE: `receiveAndReply` is part of the `RpcEndpoint` Contract in Apache Spark to receive and reply to messages in request-response two-way mode.

`receiveAndReply` handles the following messages:

* <<GetCurrentEpoch, GetCurrentEpoch>>
* <<IncrementAndGetEpoch, IncrementAndGetEpoch>>
* <<SetReaderPartitions, SetReaderPartitions>>
* <<SetWriterPartitions, SetWriterPartitions>>
* <<StopContinuousExecutionWrites, StopContinuousExecutionWrites>>

==== [[resolveCommitsAtEpoch]] `resolveCommitsAtEpoch` Internal Method

[source, scala]
----
resolveCommitsAtEpoch(epoch: Long): Unit
----

`resolveCommitsAtEpoch`...FIXME

NOTE: `resolveCommitsAtEpoch` is used exclusively when `EpochCoordinator` is requested to handle <<CommitPartitionEpoch, CommitPartitionEpoch>> and <<ReportPartitionOffset, ReportPartitionOffset>> messages.

==== [[commitEpoch]] `commitEpoch` Internal Method

[source, scala]
----
commitEpoch(
  epoch: Long,
  messages: Iterable[WriterCommitMessage]): Unit
----

`commitEpoch`...FIXME

NOTE: `commitEpoch` is used exclusively when `EpochCoordinator` is requested to <<resolveCommitsAtEpoch, resolveCommitsAtEpoch>>.

## Creating Instance

`EpochCoordinator` takes the following to be created:

* [[reader]] <<spark-sql-streaming-ContinuousReader.md#, ContinuousReader>>
* [[query]] <<ContinuousExecution.md#, ContinuousExecution>>
* [[startEpoch]] Start epoch
* [[session]] `SparkSession`
* [[rpcEnv]] `RpcEnv`

`EpochCoordinator` initializes the <<internal-properties, internal properties>>.

=== [[create]] Registering EpochCoordinator RPC Endpoint -- `create` Factory Method

[source, scala]
----
create(
  writer: StreamWriter,
  reader: ContinuousReader,
  query: ContinuousExecution,
  epochCoordinatorId: String,
  startEpoch: Long,
  session: SparkSession,
  env: SparkEnv): RpcEndpointRef
----

`create` simply <<creating-instance, creates a new EpochCoordinator>> and requests the `RpcEnv` to register a RPC endpoint as *EpochCoordinator-[id]* (where `id` is the given `epochCoordinatorId`).

`create` prints out the following INFO message to the logs:

```
Registered EpochCoordinator endpoint
```

NOTE: `create` is used exclusively when `ContinuousExecution` is requested to <<ContinuousExecution.md#runContinuous, run a streaming query in continuous mode>>.

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| queryWritesStopped
| [[queryWritesStopped]] Flag that indicates whether to drop messages (`true`) or not (`false`) when requested to <<receiveAndReply, handle one synchronously>>

Default: `false`

Turned on (`true`) when requested to <<StopContinuousExecutionWrites, handle a synchronous StopContinuousExecutionWrites message>>
|===
