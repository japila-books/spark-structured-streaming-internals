# Configuration Properties

**Configuration properties** (aka **settings**) allow you to fine-tune a Spark Structured Streaming application.

??? tip "The Internals of Spark SQL"
    Learn more about [Configuration Properties]({{ book.spark_sql }}/spark-sql-properties/) in [The Internals of Spark SQL]({{ book.spark_sql }}).

## <span id="spark.sql.streaming.commitProtocolClass"> spark.sql.streaming.commitProtocolClass

**(internal)** `FileCommitProtocol` to use for [writing out micro-batches](datasources/file/FileStreamSink.md#addBatch) in [FileStreamSink](datasources/file/FileStreamSink.md).

Default: [org.apache.spark.sql.execution.streaming.ManifestFileCommitProtocol](datasources/file/ManifestFileCommitProtocol.md)

Use [SQLConf.streamingFileCommitProtocolClass](SQLConf.md#streamingFileCommitProtocolClass) to access the current value.

??? tip "The Internals of Apache Spark"
    Learn more on [FileCommitProtocol]({{ book.spark_core }}/FileCommitProtocol) in [The Internals of Apache Spark]({{ book.spark_core }}).

## <span id="spark.sql.streaming.metricsEnabled"> spark.sql.streaming.metricsEnabled

Enables streaming metrics

Default: `false`

Use [SQLConf.streamingMetricsEnabled](SQLConf.md#streamingMetricsEnabled) to access the current value.

## <span id="spark.sql.streaming.fileSink.log.cleanupDelay"> spark.sql.streaming.fileSink.log.cleanupDelay

**(internal)** How long (in millis) that a file is guaranteed to be visible for all readers.

Default: `10 minutes`

Use [SQLConf.fileSinkLogCleanupDelay](SQLConf.md#fileSinkLogCleanupDelay) to access the current value.

## <span id="spark.sql.streaming.fileSink.log.deletion"> spark.sql.streaming.fileSink.log.deletion

**(internal)** Whether to delete the expired log files in file stream sink

Default: `true`

Use [SQLConf.fileSinkLogDeletion](SQLConf.md#fileSinkLogDeletion) to access the current value.

## <span id="spark.sql.streaming.fileSink.log.compactInterval"> spark.sql.streaming.fileSink.log.compactInterval

**(internal)** Number of log files after which all the previous files are compacted into the next log file

Default: `10`

Use [SQLConf.fileSinkLogCompactInterval](SQLConf.md#fileSinkLogCompactInterval) to access the current value.

## <span id="spark.sql.streaming.minBatchesToRetain"> spark.sql.streaming.minBatchesToRetain

**(internal)** Minimum number of batches that must be retained and made recoverable

Default: `100`

Use [SQLConf.minBatchesToRetain](SQLConf.md#minBatchesToRetain) to access the current value.

[[properties]]
.Structured Streaming's Properties
[cols="1m,2",options="header",width="100%"]
|===
| Name
| Description

| spark.sql.streaming.aggregation.stateFormatVersion
a| [[spark.sql.streaming.aggregation.stateFormatVersion]] *(internal)* Version of the state format

Default: `2`

Supported values:

* [[spark.sql.streaming.aggregation.stateFormatVersion-legacyVersion]] `1` (for the legacy <<spark-sql-streaming-StreamingAggregationStateManagerBaseImpl.md#StreamingAggregationStateManagerImplV1, StreamingAggregationStateManagerImplV1>>)

* [[spark.sql.streaming.aggregation.stateFormatVersion-default]] `2` (for the default <<spark-sql-streaming-StreamingAggregationStateManagerBaseImpl.md#StreamingAggregationStateManagerImplV2, StreamingAggregationStateManagerImplV2>>)

Used when <<spark-sql-streaming-StatefulAggregationStrategy.md#, StatefulAggregationStrategy>> execution planning strategy is executed (and plans a streaming query with an aggregate that simply boils down to creating a <<spark-sql-streaming-StateStoreRestoreExec.md#, StateStoreRestoreExec>> with the proper _implementation version_ of <<spark-sql-streaming-StreamingAggregationStateManager.md#, StreamingAggregationStateManager>>)

Among the <<spark-sql-streaming-OffsetSeqMetadata.md#relevantSQLConfs, checkpointed properties>> that are not supposed to be overriden after a streaming query has once been started (and could later recover from a checkpoint after being restarted)

| spark.sql.streaming.checkpointFileManagerClass
a| [[spark.sql.streaming.checkpointFileManagerClass]] *(internal)* [CheckpointFileManager](CheckpointFileManager.md) to use to write checkpoint files atomically

Default: [FileContextBasedCheckpointFileManager](FileContextBasedCheckpointFileManager.md) (with [FileSystemBasedCheckpointFileManager](FileSystemBasedCheckpointFileManager.md) in case of unsupported file system used for storing metadata files)

| spark.sql.streaming.checkpointLocation
a| [[spark.sql.streaming.checkpointLocation]] Default checkpoint directory for storing checkpoint data

Default: `(empty)`

| spark.sql.streaming.continuous.executorQueueSize
a| [[spark.sql.streaming.continuous.executorQueueSize]] *(internal)* The size (measured in number of rows) of the queue used in continuous execution to buffer the results of a ContinuousDataReader.

Default: `1024`

| spark.sql.streaming.continuous.executorPollIntervalMs
a| [[spark.sql.streaming.continuous.executorPollIntervalMs]] *(internal)* The interval (in millis) at which continuous execution readers will poll to check whether the epoch has advanced on the driver.

Default: `100` (ms)

| spark.sql.streaming.disabledV2MicroBatchReaders
a| [[spark.sql.streaming.disabledV2MicroBatchReaders]] *(internal)* A comma-separated list of fully-qualified class names of data source providers for which [MicroBatchStream](MicroBatchStream.md) is disabled. Reads from these sources will fall back to the V1 Sources.

Default: `(empty)`

Use <<SQLConf.md#disabledV2StreamingMicroBatchReaders, SQLConf.disabledV2StreamingMicroBatchReaders>> to get the current value.

| spark.sql.streaming.fileSource.log.cleanupDelay
a| [[spark.sql.streaming.fileSource.log.cleanupDelay]] *(internal)* How long (in millis) a file is guaranteed to be visible for all readers.

Default: `10` (minutes)

Use <<SQLConf.md#fileSourceLogCleanupDelay, SQLConf.fileSourceLogCleanupDelay>> to get the current value.

| spark.sql.streaming.fileSource.log.compactInterval
a| [[spark.sql.streaming.fileSource.log.compactInterval]] *(internal)* Number of log files after which all the previous files are compacted into the next log file.

Default: `10`

Must be a positive value (greater than `0`)

Use <<SQLConf.md#fileSourceLogCompactInterval, SQLConf.fileSourceLogCompactInterval>> to get the current value.

| spark.sql.streaming.fileSource.log.deletion
a| [[spark.sql.streaming.fileSource.log.deletion]] *(internal)* Whether to delete the expired log files in file stream source

Default: `true`

Use <<SQLConf.md#fileSourceLogDeletion, SQLConf.fileSourceLogDeletion>> to get the current value.

| spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion
a| [[spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion]] *(internal)* State format version used to create a <<spark-sql-streaming-StateManager.md#, StateManager>> for [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md#stateManager) physical operator

Default: `2`

Supported values:

* `1`
* `2`

Among the <<spark-sql-streaming-OffsetSeqMetadata.md#relevantSQLConfs, checkpointed properties>> that are not supposed to be overriden after a streaming query has once been started (and could later recover from a checkpoint after being restarted)

| spark.sql.streaming.maxBatchesToRetainInMemory
a| [[spark.sql.streaming.maxBatchesToRetainInMemory]] *(internal)* The maximum number of batches which will be retained in memory to avoid loading from files.

Default: `2`

Maximum count of versions a State Store implementation should retain in memory.

The value adjusts a trade-off between memory usage vs cache miss:

* `2` covers both success and direct failure cases
* `1` covers only success case
* `0` or negative value disables cache to maximize memory size of executors

Used when `HDFSBackedStateStoreProvider` is requested to [initialize](HDFSBackedStateStoreProvider.md#init).

| spark.sql.streaming.multipleWatermarkPolicy
a| [[spark.sql.streaming.multipleWatermarkPolicy]] *Global watermark policy* that is the policy to calculate the global watermark value when there are multiple watermark operators in a streaming query

Default: `min`

Supported values:

* `min` - chooses the minimum watermark reported across multiple operators

* `max` - chooses the maximum across multiple operators

Cannot be changed between query restarts from the same checkpoint location.

| spark.sql.streaming.noDataMicroBatches.enabled
a| [[spark.sql.streaming.noDataMicroBatches.enabled]] Flag to control whether the <<MicroBatchExecution.md#, streaming micro-batch engine>> should execute batches with no data to process for eager state management for stateful streaming queries (`true`) or not (`false`).

Default: `true`

Use <<SQLConf.md#streamingNoDataMicroBatchesEnabled, SQLConf.streamingNoDataMicroBatchesEnabled>> to get the current value

| spark.sql.streaming.noDataProgressEventInterval
a| [[spark.sql.streaming.noDataProgressEventInterval]] *(internal)* How long to wait between two progress events when there is no data (in millis) when `ProgressReporter` is requested to [finish a trigger](monitoring/ProgressReporter.md#finishTrigger)

Default: `10000L`

Use <<SQLConf.md#streamingNoDataProgressEventInterval, SQLConf.streamingNoDataProgressEventInterval>> to get the current value

| spark.sql.streaming.numRecentProgressUpdates
a| [[spark.sql.streaming.numRecentProgressUpdates]] Number of [StreamingQueryProgresses](monitoring/StreamingQueryProgress.md) to retain in [progressBuffer](monitoring/ProgressReporter.md#progressBuffer) internal registry when `ProgressReporter` is requested to [update progress of streaming query](monitoring/ProgressReporter.md#updateProgress)

Default: `100`

Use <<SQLConf.md#streamingProgressRetention, SQLConf.streamingProgressRetention>> to get the current value

| spark.sql.streaming.pollingDelay
a| [[spark.sql.streaming.pollingDelay]] *(internal)* How long (in millis) to delay `StreamExecution` before MicroBatchExecution.md#runBatches-batchRunner-no-data[polls for new data when no data was available in a batch]

Default: `10` (milliseconds)

| spark.sql.streaming.stateStore.maintenanceInterval
a| [[spark.sql.streaming.stateStore.maintenanceInterval]] The initial delay and how often to execute StateStore's spark-sql-streaming-StateStore.md#MaintenanceTask[maintenance task].

Default: `60s`

| spark.sql.streaming.stateStore.minDeltasForSnapshot
a| [[spark.sql.streaming.stateStore.minDeltasForSnapshot]] *(internal)* Minimum number of state store delta files that need to be generated before HDFSBackedStateStore will consider generating a snapshot (consolidate the deltas into a snapshot)

Default: `10`

Use <<SQLConf.md#stateStoreMinDeltasForSnapshot, SQLConf.stateStoreMinDeltasForSnapshot>> to get the current value.

| spark.sql.streaming.stateStore.providerClass
a| [[spark.sql.streaming.stateStore.providerClass]] *(internal)* The fully-qualified class name of the <<spark-sql-streaming-StateStoreProvider.md#, StateStoreProvider>> implementation that manages state data in stateful streaming queries. This class must have a zero-arg constructor.

Default: [HDFSBackedStateStoreProvider](HDFSBackedStateStoreProvider.md)

Use <<SQLConf.md#stateStoreProviderClass, SQLConf.stateStoreProviderClass>> to get the current value.

| spark.sql.streaming.unsupportedOperationCheck
a| [[spark.sql.streaming.unsupportedOperationCheck]] *(internal)* When enabled (`true`), `StreamingQueryManager` spark-sql-streaming-UnsupportedOperationChecker.md#checkForStreaming[makes sure that the logical plan of a streaming query uses supported operations only].

Default: `true`

|===
