# Configuration Properties

**Configuration properties** (aka **settings**) allow you to fine-tune a Spark Structured Streaming application.

??? tip "The Internals of Spark SQL"
    Learn more about [Configuration Properties]({{ book.spark_sql }}/configuration-properties/) in [The Internals of Spark SQL]({{ book.spark_sql }}).

## <span id="spark.sql.streaming.aggregation.stateFormatVersion"> aggregation.stateFormatVersion

**spark.sql.streaming.aggregation.stateFormatVersion**

**(internal)** Version of the state format

Default: `2`

Supported values:

* `1` (for the legacy [StreamingAggregationStateManagerImplV1](StreamingAggregationStateManagerBaseImpl.md#StreamingAggregationStateManagerImplV1))
* `2` (for the default [StreamingAggregationStateManagerImplV2](StreamingAggregationStateManagerBaseImpl.md#StreamingAggregationStateManagerImplV2))

[Checkpointed property](OffsetSeqMetadata.md#relevantSQLConfs) that is not supposed to be overriden after a streaming query has been started (and could later recover from a checkpoint after being restarted)

Used when:

* [StatefulAggregationStrategy](execution-planning-strategies/StatefulAggregationStrategy.md) execution planning strategy is executed (and plans a streaming query with an aggregate that simply boils down to creating a [StateStoreRestoreExec](physical-operators/StateStoreRestoreExec.md) with the proper _implementation version_ of [StreamingAggregationStateManager](StreamingAggregationStateManager.md))

## <span id="spark.sql.streaming.checkpointFileManagerClass"> checkpointFileManagerClass

**spark.sql.streaming.checkpointFileManagerClass**

**(internal)** [CheckpointFileManager](CheckpointFileManager.md) to use to write checkpoint files atomically

Default: [FileContextBasedCheckpointFileManager](FileContextBasedCheckpointFileManager.md) (with [FileSystemBasedCheckpointFileManager](FileSystemBasedCheckpointFileManager.md) in case of unsupported file system used for storing metadata files)

## <span id="spark.sql.streaming.checkpointLocation"> checkpointLocation

**spark.sql.streaming.checkpointLocation**

Default checkpoint directory for storing checkpoint data

Default: `(empty)`

## <span id="spark.sql.streaming.commitProtocolClass"> commitProtocolClass

**spark.sql.streaming.commitProtocolClass**

**(internal)** `FileCommitProtocol` to use for [writing out micro-batches](datasources/file/FileStreamSink.md#addBatch) in [FileStreamSink](datasources/file/FileStreamSink.md).

Default: [org.apache.spark.sql.execution.streaming.ManifestFileCommitProtocol](datasources/file/ManifestFileCommitProtocol.md)

Use [SQLConf.streamingFileCommitProtocolClass](SQLConf.md#streamingFileCommitProtocolClass) to access the current value.

??? tip "The Internals of Apache Spark"
    Learn more on [FileCommitProtocol]({{ book.spark_core }}/FileCommitProtocol) in [The Internals of Apache Spark]({{ book.spark_core }}).

## <span id="spark.sql.streaming.continuous.executorQueueSize"> continuous.executorQueueSize

**spark.sql.streaming.continuous.executorQueueSize**

**(internal)** The size (measured in number of rows) of the queue used in continuous execution to buffer the results of a ContinuousDataReader.

Default: `1024`

## <span id="spark.sql.streaming.continuous.executorPollIntervalMs"> continuous.executorPollIntervalMs

**spark.sql.streaming.continuous.executorPollIntervalMs**

**(internal)** The interval (in millis) at which continuous execution readers will poll to check whether the epoch has advanced on the driver.

Default: `100` (ms)

## <span id="spark.sql.streaming.disabledV2MicroBatchReaders"> disabledV2MicroBatchReaders

**spark.sql.streaming.disabledV2MicroBatchReaders**

**(internal)** A comma-separated list of fully-qualified class names of data source providers for which [MicroBatchStream](MicroBatchStream.md) is disabled. Reads from these sources will fall back to the V1 Sources.

Default: `(empty)`

Use [SQLConf.disabledV2StreamingMicroBatchReaders](SQLConf.md#disabledV2StreamingMicroBatchReaders) to get the current value.

## <span id="spark.sql.streaming.fileSink.log.cleanupDelay"> fileSink.log.cleanupDelay

**spark.sql.streaming.fileSink.log.cleanupDelay**

**(internal)** How long (in millis) that a file is guaranteed to be visible for all readers.

Default: `10 minutes`

Use [SQLConf.fileSinkLogCleanupDelay](SQLConf.md#fileSinkLogCleanupDelay) to access the current value.

## <span id="spark.sql.streaming.fileSink.log.deletion"> fileSink.log.deletion

**spark.sql.streaming.fileSink.log.deletion**

**(internal)** Whether to delete the expired log files in file stream sink

Default: `true`

Use [SQLConf.fileSinkLogDeletion](SQLConf.md#fileSinkLogDeletion) to access the current value.

## <span id="spark.sql.streaming.fileSink.log.compactInterval"> fileSink.log.compactInterval

**spark.sql.streaming.fileSink.log.compactInterval**

**(internal)** Number of log files after which all the previous files are compacted into the next log file

Default: `10`

Use [SQLConf.fileSinkLogCompactInterval](SQLConf.md#fileSinkLogCompactInterval) to access the current value.

## <span id="spark.sql.streaming.fileSource.log.cleanupDelay"> fileSource.log.cleanupDelay

**spark.sql.streaming.fileSource.log.cleanupDelay**

**(internal)** How long (in millis) a file is guaranteed to be visible for all readers.

Default: `10` (minutes)

Use [SQLConf.fileSourceLogCleanupDelay](SQLConf.md#fileSourceLogCleanupDelay) to get the current value.

## <span id="spark.sql.streaming.fileSource.log.compactInterval"> fileSource.log.compactInterval

**spark.sql.streaming.fileSource.log.compactInterval**

**(internal)** Number of log files after which all the previous files are compacted into the next log file.

Default: `10`

Must be a positive value (greater than `0`)

Use [SQLConf.fileSourceLogCompactInterval](SQLConf.md#fileSourceLogCompactInterval) to get the current value.

## <span id="spark.sql.streaming.fileSource.log.deletion"> fileSource.log.deletion

**spark.sql.streaming.fileSource.log.deletion**

**(internal)** Whether to delete the expired log files in file stream source

Default: `true`

Use [SQLConf.fileSourceLogDeletion](SQLConf.md#fileSourceLogDeletion) to get the current value.

## <span id="spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion"> flatMapGroupsWithState.stateFormatVersion

**spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion**

**(internal)** State format version used to create a [StateManager](spark-sql-streaming-StateManager.md) for [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md#stateManager) physical operator

Default: `2`

Supported values:

* `1`
* `2`

[Checkpointed property](OffsetSeqMetadata.md#relevantSQLConfs) that is not supposed to be overriden after a streaming query has been started (and could later recover from a checkpoint after being restarted)

Used when:

* [FlatMapGroupsWithStateStrategy](execution-planning-strategies/FlatMapGroupsWithStateStrategy.md) execution planning strategy is requested to plan a streaming query (and creates a [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator for every [FlatMapGroupsWithState](logical-operators/FlatMapGroupsWithState.md) logical operator)

## <span id="spark.sql.streaming.maxBatchesToRetainInMemory"> maxBatchesToRetainInMemory

**spark.sql.streaming.maxBatchesToRetainInMemory**

**(internal)** The maximum number of batches which will be retained in memory to avoid loading from files.

Default: `2`

Maximum count of versions a State Store implementation should retain in memory.

The value adjusts a trade-off between memory usage vs cache miss:

* `2` covers both success and direct failure cases
* `1` covers only success case
* `0` or negative value disables cache to maximize memory size of executors

Used when `HDFSBackedStateStoreProvider` is requested to [initialize](HDFSBackedStateStoreProvider.md#init).

## <span id="spark.sql.streaming.metricsEnabled"> metricsEnabled

**spark.sql.streaming.metricsEnabled**

Enables streaming metrics

Default: `false`

Use [SQLConf.streamingMetricsEnabled](SQLConf.md#streamingMetricsEnabled) to access the current value.

## <span id="spark.sql.streaming.minBatchesToRetain"> minBatchesToRetain

**spark.sql.streaming.minBatchesToRetain**

**(internal)** Minimum number of batches that must be retained and made recoverable

 [Stream execution engines](StreamExecution.md) discard (_purge_) offsets from the `offsets` metadata log when the [current batch ID](StreamExecution.md#currentBatchId) (in [MicroBatchExecution](micro-batch-execution/MicroBatchExecution.md)) or the [epoch committed](continuous-execution/ContinuousExecution.md#commit) (in [ContinuousExecution](continuous-execution/ContinuousExecution.md)) is above the threshold.

Default: `100`

Use [SQLConf.minBatchesToRetain](SQLConf.md#minBatchesToRetain) to access the current value.

## <span id="spark.sql.streaming.multipleWatermarkPolicy"> multipleWatermarkPolicy

**spark.sql.streaming.multipleWatermarkPolicy**

**Global watermark policy** that is the policy to calculate the global watermark value when there are multiple watermark operators in a streaming query

Default: `min`

Supported values:

* `min` - chooses the minimum watermark reported across multiple operators
* `max` - chooses the maximum across multiple operators

Cannot be changed between query restarts from the same checkpoint location.

## <span id="spark.sql.streaming.noDataMicroBatches.enabled"> noDataMicroBatches.enabled

**spark.sql.streaming.noDataMicroBatches.enabled**

Controls whether the [streaming micro-batch engine](micro-batch-execution/MicroBatchExecution.md) should execute batches with no data to process for eager state management for stateful streaming queries (`true`) or not (`false`).

Default: `true`

Use [SQLConf.streamingNoDataMicroBatchesEnabled](SQLConf.md#streamingNoDataMicroBatchesEnabled) to get the current value

## <span id="spark.sql.streaming.noDataProgressEventInterval"> noDataProgressEventInterval

**spark.sql.streaming.noDataProgressEventInterval**

**(internal)** How long to wait between two progress events when there is no data (in millis) when `ProgressReporter` is requested to [finish a trigger](monitoring/ProgressReporter.md#finishTrigger)

Default: `10000L`

Use [SQLConf.streamingNoDataProgressEventInterval](SQLConf.md#streamingNoDataProgressEventInterval) to get the current value

## <span id="spark.sql.streaming.numRecentProgressUpdates"> numRecentProgressUpdates

**spark.sql.streaming.numRecentProgressUpdates**

Number of [StreamingQueryProgresses](monitoring/StreamingQueryProgress.md) to retain in [progressBuffer](monitoring/ProgressReporter.md#progressBuffer) internal registry when `ProgressReporter` is requested to [update progress of streaming query](monitoring/ProgressReporter.md#updateProgress)

Default: `100`

Use [SQLConf.streamingProgressRetention](SQLConf.md#streamingProgressRetention) to get the current value

## <span id="spark.sql.streaming.pollingDelay"> pollingDelay

**spark.sql.streaming.pollingDelay**

**(internal)** How long (in millis) to delay `StreamExecution` before [polls for new data when no data was available in a batch](micro-batch-execution/MicroBatchExecution.md#runBatches-batchRunner-no-data)

Default: `10` (milliseconds)

## <span id="spark.sql.streaming.stateStore.maintenanceInterval"> stateStore.maintenanceInterval

**spark.sql.streaming.stateStore.maintenanceInterval**

The initial delay and how often to execute StateStore's [maintenance task](StateStore.md#MaintenanceTask).

Default: `60s`

## <span id="spark.sql.streaming.stateStore.minDeltasForSnapshot"> stateStore.minDeltasForSnapshot

**spark.sql.streaming.stateStore.minDeltasForSnapshot**

**(internal)** Minimum number of state store delta files that need to be generated before `HDFSBackedStateStore` will consider generating a snapshot (consolidate the deltas into a snapshot)

Default: `10`

Use [SQLConf.stateStoreMinDeltasForSnapshot](SQLConf.md#stateStoreMinDeltasForSnapshot) to get the current value.

## <span id="spark.sql.streaming.stateStore.providerClass"> stateStore.providerClass

**spark.sql.streaming.stateStore.providerClass**

**(internal)** The fully-qualified class name of the [StateStoreProvider](spark-sql-streaming-StateStoreProvider.md) implementation that manages state data in stateful streaming queries. This class must have a zero-arg constructor.

Default: [HDFSBackedStateStoreProvider](HDFSBackedStateStoreProvider.md)

Use [SQLConf.stateStoreProviderClass](SQLConf.md#stateStoreProviderClass) to get the current value.

## <span id="spark.sql.streaming.unsupportedOperationCheck"> unsupportedOperationCheck

**spark.sql.streaming.unsupportedOperationCheck**

**(internal)** When enabled (`true`), `StreamingQueryManager` [makes sure that the logical plan of a streaming query uses supported operations only](UnsupportedOperationChecker.md#checkForStreaming)

Default: `true`
