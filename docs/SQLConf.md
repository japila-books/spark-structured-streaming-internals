# SQLConf &mdash; Internal Configuration Store

`SQLConf` is an **internal configuration store** for parameters and hints used to configure a Spark Structured Streaming application (and Spark SQL applications in general).

!!! tip
    Find out more on [SQLConf](https://jaceklaskowski.github.io/mastering-spark-sql-book/SQLConf/) in [The Internals of Spark SQL](https://jaceklaskowski.github.io/mastering-spark-sql-book)

## <span id="streamingFileCommitProtocolClass"><span id="STREAMING_FILE_COMMIT_PROTOCOL_CLASS"> streamingFileCommitProtocolClass

[spark.sql.streaming.commitProtocolClass](spark-sql-streaming-properties.md#spark.sql.streaming.commitProtocolClass) configuration property

Used when `FileStreamSink` is requested to ["add" a batch of data](datasources/file/FileStreamSink.md#addBatch)

## <span id="streamingMetricsEnabled"><span id="STREAMING_METRICS_ENABLED"> streamingMetricsEnabled

[spark.sql.streaming.metricsEnabled](spark-sql-streaming-properties.md#spark.sql.streaming.metricsEnabled) configuration property

Used when `StreamExecution` is requested to [runStream](StreamExecution.md#runStream)

## <span id="fileSinkLogCleanupDelay"><span id="FILE_SINK_LOG_CLEANUP_DELAY"> fileSinkLogCleanupDelay

[spark.sql.streaming.fileSink.log.cleanupDelay](spark-sql-streaming-properties.md#spark.sql.streaming.fileSink.log.cleanupDelay) configuration property

Used when [FileStreamSinkLog](datasources/file/FileStreamSinkLog.md#fileCleanupDelayMs) is created

## <span id="fileSinkLogDeletion"><span id="FILE_SINK_LOG_DELETION"> fileSinkLogDeletion

[spark.sql.streaming.fileSink.log.deletion](spark-sql-streaming-properties.md#spark.sql.streaming.fileSink.log.deletion) configuration property

Used when [FileStreamSinkLog](datasources/file/FileStreamSinkLog.md#isDeletingExpiredLog) is created

## <span id="fileSinkLogCompactInterval"><span id="FILE_SINK_LOG_COMPACT_INTERVAL"> fileSinkLogCompactInterval

[spark.sql.streaming.fileSink.log.compactInterval](spark-sql-streaming-properties.md#spark.sql.streaming.fileSink.log.compactInterval) configuration property

Used when [FileStreamSinkLog](datasources/file/FileStreamSinkLog.md#defaultCompactInterval) is created

## <span id="minBatchesToRetain"><span id="MIN_BATCHES_TO_RETAIN"> minBatchesToRetain

[spark.sql.streaming.minBatchesToRetain](spark-sql-streaming-properties.md#spark.sql.streaming.minBatchesToRetain) configuration property

Used when:

* `CompactibleFileStreamLog` is [created](datasources/file/CompactibleFileStreamLog.md#minBatchesToRetain)

* [StreamExecution](StreamExecution.md#minLogEntriesToMaintain) is created

* `StateStoreConf` is [created](StateStoreConf.md#minVersionsToRetain)

[[accessor-methods]]
.SQLConf's Property Accessor Methods
[cols="1,1",options="header",width="100%"]
|===
| Method Name / Property
| Description

| `continuousStreamingExecutorQueueSize`

<<spark-sql-streaming-properties.md#spark.sql.streaming.continuous.executorQueueSize, spark.sql.streaming.continuous.executorQueueSize>>

a| [[continuousStreamingExecutorQueueSize]] Used when:

* `DataSourceV2ScanExec` leaf physical operator is requested for the input RDDs (and creates a <<spark-sql-streaming-ContinuousDataSourceRDD.md#, ContinuousDataSourceRDD>>)

* `ContinuousCoalesceExec` unary physical operator is requested to execute

| `continuousStreamingExecutorPollIntervalMs`

<<spark-sql-streaming-properties.md#spark.sql.streaming.continuous.executorPollIntervalMs, spark.sql.streaming.continuous.executorPollIntervalMs>>

a| [[continuousStreamingExecutorPollIntervalMs]] Used exclusively when `DataSourceV2ScanExec` leaf physical operator is requested for the input RDDs (and creates a <<spark-sql-streaming-ContinuousDataSourceRDD.md#, ContinuousDataSourceRDD>>)

| `disabledV2StreamingMicroBatchReaders`

<<spark-sql-streaming-properties.md#spark.sql.streaming.disabledV2MicroBatchReaders, spark.sql.streaming.disabledV2MicroBatchReaders>>

a| [[disabledV2StreamingMicroBatchReaders]] Used exclusively when `MicroBatchExecution` is requested for the <<MicroBatchExecution.md#logicalPlan, analyzed logical plan>> (of a streaming query)

| `fileSourceLogDeletion`

<<spark-sql-streaming-properties.md#spark.sql.streaming.fileSource.log.deletion, spark.sql.streaming.fileSource.log.deletion>>

a| [[fileSourceLogDeletion]][[FILE_SOURCE_LOG_DELETION]] Used exclusively when `FileStreamSourceLog` is requested for the [isDeletingExpiredLog](datasources/file/FileStreamSourceLog.md#isDeletingExpiredLog)

| `fileSourceLogCleanupDelay`

<<spark-sql-streaming-properties.md#spark.sql.streaming.fileSource.log.cleanupDelay, spark.sql.streaming.fileSource.log.cleanupDelay>>

a| [[fileSourceLogCleanupDelay]][[FILE_SOURCE_LOG_CLEANUP_DELAY]] Used exclusively when `FileStreamSourceLog` is requested for the [fileCleanupDelayMs](datasources/file/FileStreamSourceLog.md#fileCleanupDelayMs)

| `fileSourceLogCompactInterval`

<<spark-sql-streaming-properties.md#spark.sql.streaming.fileSource.log.compactInterval, spark.sql.streaming.fileSource.log.compactInterval>>

a| [[fileSourceLogCompactInterval]][[FILE_SOURCE_LOG_COMPACT_INTERVAL]] Used exclusively when `FileStreamSourceLog` is requested for the [default compaction interval](datasources/file/FileStreamSourceLog.md#defaultCompactInterval)

| `FLATMAPGROUPSWITHSTATE_STATE_FORMAT_VERSION`

<<spark-sql-streaming-properties.md#spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion, spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion>>
a| [[FLATMAPGROUPSWITHSTATE_STATE_FORMAT_VERSION]] Used when:

* <<spark-sql-streaming-FlatMapGroupsWithStateStrategy.md#, FlatMapGroupsWithStateStrategy>> execution planning strategy is requested to plan a streaming query (and creates a [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator for every [FlatMapGroupsWithState](logical-operators/FlatMapGroupsWithState.md) logical operator)

* Among the <<spark-sql-streaming-OffsetSeqMetadata.md#relevantSQLConfs, checkpointed properties>>

| `SHUFFLE_PARTITIONS`

`spark.sql.shuffle.partitions`
a| [[SHUFFLE_PARTITIONS]] See https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-properties.html#spark.sql.shuffle.partitions[spark.sql.shuffle.partitions] in The Internals of Spark SQL.

| `stateStoreMinDeltasForSnapshot`

<<spark-sql-streaming-properties.md#spark.sql.streaming.stateStore.minDeltasForSnapshot, spark.sql.streaming.stateStore.minDeltasForSnapshot>>

a| [[stateStoreMinDeltasForSnapshot]] Used (as [StateStoreConf.minDeltasForSnapshot](StateStoreConf.md#minDeltasForSnapshot)) exclusively when `HDFSBackedStateStoreProvider` is requested to [doSnapshot](HDFSBackedStateStoreProvider.md#doSnapshot)

| `stateStoreProviderClass`

<<spark-sql-streaming-properties.md#spark.sql.streaming.stateStore.providerClass, spark.sql.streaming.stateStore.providerClass>>

a| [[stateStoreProviderClass]] Used when:

* `StateStoreWriter` is requested to [stateStoreCustomMetrics](physical-operators/StateStoreWriter.md#stateStoreCustomMetrics) (when `StateStoreWriter` is requested for the [metrics](physical-operators/StateStoreWriter.md#metrics) and [getProgress](physical-operators/StateStoreWriter.md#getProgress))

* `StateStoreConf` is [created](StateStoreConf.md#providerClass)

| `STREAMING_AGGREGATION_STATE_FORMAT_VERSION`

<<spark-sql-streaming-properties.md#spark.sql.streaming.aggregation.stateFormatVersion, spark.sql.streaming.aggregation.stateFormatVersion>>
a| [[STREAMING_AGGREGATION_STATE_FORMAT_VERSION]] Used when:

* <<spark-sql-streaming-StatefulAggregationStrategy.md#, StatefulAggregationStrategy>> execution planning strategy is executed

* `OffsetSeqMetadata` is requested for the <<spark-sql-streaming-OffsetSeqMetadata.md#relevantSQLConfs, relevantSQLConfs>> and the <<spark-sql-streaming-OffsetSeqMetadata.md#relevantSQLConfDefaultValues, relevantSQLConfDefaultValues>>

| `STREAMING_CHECKPOINT_FILE_MANAGER_CLASS`

<<spark-sql-streaming-properties.md#spark.sql.streaming.checkpointFileManagerClass, spark.sql.streaming.checkpointFileManagerClass>>
a| [[STREAMING_CHECKPOINT_FILE_MANAGER_CLASS]] Used exclusively when `CheckpointFileManager` helper object is requested to [create a CheckpointFileManager](CheckpointFileManager.md#create)

| `streamingMetricsEnabled`

<<spark-sql-streaming-properties.md#spark.sql.streaming.metricsEnabled, spark.sql.streaming.metricsEnabled>>

a| [[streamingMetricsEnabled]] Used exclusively when `StreamExecution` is requested for [runStream](StreamExecution.md#runStream) (to control whether to register a [metrics reporter](StreamExecution.md#streamMetrics) for a streaming query)

| `STREAMING_MULTIPLE_WATERMARK_POLICY`

<<spark-sql-streaming-properties.md#spark.sql.streaming.multipleWatermarkPolicy, spark.sql.streaming.multipleWatermarkPolicy>>

a| [[STREAMING_MULTIPLE_WATERMARK_POLICY]]

| `streamingNoDataMicroBatchesEnabled`

<<spark-sql-streaming-properties.md#spark.sql.streaming.noDataMicroBatches.enabled, spark.sql.streaming.noDataMicroBatches.enabled>>

a| [[streamingNoDataMicroBatchesEnabled]][[STREAMING_NO_DATA_MICRO_BATCHES_ENABLED]] Used exclusively when `MicroBatchExecution` stream execution engine is requested to <<MicroBatchExecution.md#runActivatedStream, run an activated streaming query>>

| `streamingNoDataProgressEventInterval`

<<spark-sql-streaming-properties.md#spark.sql.streaming.noDataProgressEventInterval, spark.sql.streaming.noDataProgressEventInterval>>

a| [[streamingNoDataProgressEventInterval]] Used exclusively for [ProgressReporter](monitoring/ProgressReporter.md#noDataProgressEventInterval)

| `streamingPollingDelay`

<<spark-sql-streaming-properties.md#spark.sql.streaming.pollingDelay, spark.sql.streaming.pollingDelay>>

a| [[streamingPollingDelay]][[STREAMING_POLLING_DELAY]] Used exclusively when [StreamExecution](StreamExecution.md) is created

| `streamingProgressRetention`

<<spark-sql-streaming-properties.md#spark.sql.streaming.numRecentProgressUpdates, spark.sql.streaming.numRecentProgressUpdates>>

a| [[streamingProgressRetention]][[STREAMING_PROGRESS_RETENTION]] Used exclusively when `ProgressReporter` is requested to [update progress of streaming query](monitoring/ProgressReporter.md#updateProgress) (and possibly remove an excess)

|===
