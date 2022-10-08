# SQLConf

`SQLConf` is an **internal configuration store** for parameters and hints used to configure a Spark Structured Streaming application (and Spark SQL applications in general).

!!! tip
    Find out more on [SQLConf]({{ book.spark_sql }}/SQLConf/) in [The Internals of Spark SQL]({{ book.spark_sql }}).

## <span id="FLATMAPGROUPSWITHSTATE_STATE_FORMAT_VERSION"> FLATMAPGROUPSWITHSTATE_STATE_FORMAT_VERSION

[spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion](configuration-properties.md#spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion)

## <span id="STATEFUL_OPERATOR_USE_STRICT_DISTRIBUTION"> STATEFUL_OPERATOR_USE_STRICT_DISTRIBUTION

[spark.sql.streaming.statefulOperator.useStrictDistribution](configuration-properties.md#spark.sql.streaming.statefulOperator.useStrictDistribution)

## <span id="stateStoreProviderClass"><span id="STATE_STORE_PROVIDER_CLASS"> stateStoreProviderClass

[spark.sql.streaming.stateStore.providerClass](configuration-properties.md#spark.sql.streaming.stateStore.providerClass)

## <span id="streamingFileCommitProtocolClass"><span id="STREAMING_FILE_COMMIT_PROTOCOL_CLASS"> streamingFileCommitProtocolClass

[spark.sql.streaming.commitProtocolClass](configuration-properties.md#spark.sql.streaming.commitProtocolClass) configuration property

Used when `FileStreamSink` is requested to ["add" a batch of data](datasources/file/FileStreamSink.md#addBatch)

## <span id="streamingMetricsEnabled"><span id="STREAMING_METRICS_ENABLED"> streamingMetricsEnabled

[spark.sql.streaming.metricsEnabled](configuration-properties.md#spark.sql.streaming.metricsEnabled) configuration property

Used when:

* `StreamExecution` is requested to [runStream](StreamExecution.md#runStream)

## <span id="streamingNoDataMicroBatchesEnabled"><span id="STREAMING_NO_DATA_MICRO_BATCHES_ENABLED"> streamingNoDataMicroBatchesEnabled

[spark.sql.streaming.noDataMicroBatches.enabled](configuration-properties.md#spark.sql.streaming.noDataMicroBatches.enabled)

Used when:

* `MicroBatchExecution` stream execution engine is requested to [run an activated streaming query](micro-batch-execution/MicroBatchExecution.md#runActivatedStream)

## <span id="fileSinkLogCleanupDelay"><span id="FILE_SINK_LOG_CLEANUP_DELAY"> fileSinkLogCleanupDelay

[spark.sql.streaming.fileSink.log.cleanupDelay](configuration-properties.md#spark.sql.streaming.fileSink.log.cleanupDelay) configuration property

Used when [FileStreamSinkLog](datasources/file/FileStreamSinkLog.md#fileCleanupDelayMs) is created

## <span id="fileSinkLogDeletion"><span id="FILE_SINK_LOG_DELETION"> fileSinkLogDeletion

[spark.sql.streaming.fileSink.log.deletion](configuration-properties.md#spark.sql.streaming.fileSink.log.deletion) configuration property

Used when [FileStreamSinkLog](datasources/file/FileStreamSinkLog.md#isDeletingExpiredLog) is created

## <span id="fileSinkLogCompactInterval"><span id="FILE_SINK_LOG_COMPACT_INTERVAL"> fileSinkLogCompactInterval

[spark.sql.streaming.fileSink.log.compactInterval](configuration-properties.md#spark.sql.streaming.fileSink.log.compactInterval) configuration property

Used when [FileStreamSinkLog](datasources/file/FileStreamSinkLog.md#defaultCompactInterval) is created

## <span id="minBatchesToRetain"><span id="MIN_BATCHES_TO_RETAIN"> minBatchesToRetain

[spark.sql.streaming.minBatchesToRetain](configuration-properties.md#spark.sql.streaming.minBatchesToRetain) configuration property

Used when:

* `CompactibleFileStreamLog` is [created](datasources/file/CompactibleFileStreamLog.md#minBatchesToRetain)

* [StreamExecution](StreamExecution.md#minLogEntriesToMaintain) is created

* `StateStoreConf` is [created](stateful-stream-processing/StateStoreConf.md#minVersionsToRetain)

[[accessor-methods]]
.SQLConf's Property Accessor Methods
[cols="1,1",options="header",width="100%"]
|===
| Method Name / Property
| Description

| `continuousStreamingExecutorQueueSize`

[spark.sql.streaming.continuous.executorQueueSize](configuration-properties.md#spark.sql.streaming.continuous.executorQueueSize)

a| [[continuousStreamingExecutorQueueSize]] Used when:

* `DataSourceV2ScanExec` leaf physical operator is requested for the input RDDs (and creates a <<ContinuousDataSourceRDD.md#, ContinuousDataSourceRDD>>)

* `ContinuousCoalesceExec` unary physical operator is requested to execute

| `continuousStreamingExecutorPollIntervalMs`

[spark.sql.streaming.continuous.executorPollIntervalMs](configuration-properties.md#spark.sql.streaming.continuous.executorPollIntervalMs)

a| [[continuousStreamingExecutorPollIntervalMs]] Used exclusively when `DataSourceV2ScanExec` leaf physical operator is requested for the input RDDs (and creates a <<ContinuousDataSourceRDD.md#, ContinuousDataSourceRDD>>)

| `disabledV2StreamingMicroBatchReaders`

[spark.sql.streaming.disabledV2MicroBatchReaders](configuration-properties.md#spark.sql.streaming.disabledV2MicroBatchReaders)

a| [[disabledV2StreamingMicroBatchReaders]] Used exclusively when `MicroBatchExecution` is requested for the <<MicroBatchExecution.md#logicalPlan, analyzed logical plan>> (of a streaming query)

| `fileSourceLogDeletion`

[spark.sql.streaming.fileSource.log.deletion](configuration-properties.md#spark.sql.streaming.fileSource.log.deletion)

a| [[fileSourceLogDeletion]][[FILE_SOURCE_LOG_DELETION]] Used exclusively when `FileStreamSourceLog` is requested for the [isDeletingExpiredLog](datasources/file/FileStreamSourceLog.md#isDeletingExpiredLog)

| `fileSourceLogCleanupDelay`

[spark.sql.streaming.fileSource.log.cleanupDelay](configuration-properties.md#spark.sql.streaming.fileSource.log.cleanupDelay)

a| [[fileSourceLogCleanupDelay]][[FILE_SOURCE_LOG_CLEANUP_DELAY]] Used exclusively when `FileStreamSourceLog` is requested for the [fileCleanupDelayMs](datasources/file/FileStreamSourceLog.md#fileCleanupDelayMs)

| `fileSourceLogCompactInterval`

[spark.sql.streaming.fileSource.log.compactInterval](configuration-properties.md#spark.sql.streaming.fileSource.log.compactInterval)

a| [[fileSourceLogCompactInterval]][[FILE_SOURCE_LOG_COMPACT_INTERVAL]] Used exclusively when `FileStreamSourceLog` is requested for the [default compaction interval](datasources/file/FileStreamSourceLog.md#defaultCompactInterval)

| `stateStoreMinDeltasForSnapshot`

[spark.sql.streaming.stateStore.minDeltasForSnapshot](configuration-properties.md#spark.sql.streaming.stateStore.minDeltasForSnapshot)

a| [[stateStoreMinDeltasForSnapshot]] Used (as [StateStoreConf.minDeltasForSnapshot](stateful-stream-processing/StateStoreConf.md#minDeltasForSnapshot)) exclusively when `HDFSBackedStateStoreProvider` is requested to [doSnapshot](stateful-stream-processing/HDFSBackedStateStoreProvider.md#doSnapshot)

| `STREAMING_AGGREGATION_STATE_FORMAT_VERSION`

[spark.sql.streaming.aggregation.stateFormatVersion](configuration-properties.md#spark.sql.streaming.aggregation.stateFormatVersion)

a| [[STREAMING_AGGREGATION_STATE_FORMAT_VERSION]] Used when:

* [StatefulAggregationStrategy](execution-planning-strategies/StatefulAggregationStrategy.md) execution planning strategy is executed

* `OffsetSeqMetadata` is requested for the [relevantSQLConfs](OffsetSeqMetadata.md#relevantSQLConfs) and the [relevantSQLConfDefaultValues](OffsetSeqMetadata.md#relevantSQLConfDefaultValues)

| `STREAMING_CHECKPOINT_FILE_MANAGER_CLASS`

[spark.sql.streaming.checkpointFileManagerClass](configuration-properties.md#spark.sql.streaming.checkpointFileManagerClass)
a| [[STREAMING_CHECKPOINT_FILE_MANAGER_CLASS]] Used exclusively when `CheckpointFileManager` helper object is requested to [create a CheckpointFileManager](CheckpointFileManager.md#create)

| `streamingMetricsEnabled`

[spark.sql.streaming.metricsEnabled](configuration-properties.md#spark.sql.streaming.metricsEnabled)

a| [[streamingMetricsEnabled]] Used exclusively when `StreamExecution` is requested for [runStream](StreamExecution.md#runStream) (to control whether to register a [metrics reporter](StreamExecution.md#streamMetrics) for a streaming query)

| `STREAMING_MULTIPLE_WATERMARK_POLICY`

[spark.sql.streaming.multipleWatermarkPolicy](configuration-properties.md#spark.sql.streaming.multipleWatermarkPolicy)

a| [[STREAMING_MULTIPLE_WATERMARK_POLICY]]

| `streamingNoDataProgressEventInterval`

[spark.sql.streaming.noDataProgressEventInterval](configuration-properties.md#spark.sql.streaming.noDataProgressEventInterval)

a| [[streamingNoDataProgressEventInterval]] Used exclusively for [ProgressReporter](monitoring/ProgressReporter.md#noDataProgressEventInterval)

| `streamingPollingDelay`

[spark.sql.streaming.pollingDelay](configuration-properties.md#spark.sql.streaming.pollingDelay)

a| [[streamingPollingDelay]][[STREAMING_POLLING_DELAY]] Used exclusively when [StreamExecution](StreamExecution.md) is created

| `streamingProgressRetention`

[spark.sql.streaming.numRecentProgressUpdates](configuration-properties.md#spark.sql.streaming.numRecentProgressUpdates)

a| [[streamingProgressRetention]][[STREAMING_PROGRESS_RETENTION]] Used exclusively when `ProgressReporter` is requested to [update progress of streaming query](monitoring/ProgressReporter.md#updateProgress) (and possibly remove an excess)

|===
