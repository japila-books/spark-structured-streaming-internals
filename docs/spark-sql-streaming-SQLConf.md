== [[SQLConf]] SQLConf -- Internal Configuration Store

`SQLConf` is an *internal key-value configuration store* for parameters and hints used to configure a Spark Structured Streaming application (and Spark SQL applications in general).

The parameters and hints are accessible as <<accessor-methods, property accessor methods>>.

`SQLConf` is available as the `conf` property of the `SessionState` of a `SparkSession`.

[source, scala]
----
scala> :type spark
org.apache.spark.sql.SparkSession

scala> :type spark.sessionState.conf
org.apache.spark.sql.internal.SQLConf
----

[[accessor-methods]]
.SQLConf's Property Accessor Methods
[cols="1,1",options="header",width="100%"]
|===
| Method Name / Property
| Description

| `continuousStreamingExecutorQueueSize`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.continuous.executorQueueSize, spark.sql.streaming.continuous.executorQueueSize>>

a| [[continuousStreamingExecutorQueueSize]] Used when:

* `DataSourceV2ScanExec` leaf physical operator is requested for the input RDDs (and creates a <<spark-sql-streaming-ContinuousDataSourceRDD.adoc#, ContinuousDataSourceRDD>>)

* `ContinuousCoalesceExec` unary physical operator is requested to execute

| `continuousStreamingExecutorPollIntervalMs`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.continuous.executorPollIntervalMs, spark.sql.streaming.continuous.executorPollIntervalMs>>

a| [[continuousStreamingExecutorPollIntervalMs]] Used exclusively when `DataSourceV2ScanExec` leaf physical operator is requested for the input RDDs (and creates a <<spark-sql-streaming-ContinuousDataSourceRDD.adoc#, ContinuousDataSourceRDD>>)

| `disabledV2StreamingMicroBatchReaders`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.disabledV2MicroBatchReaders, spark.sql.streaming.disabledV2MicroBatchReaders>>

a| [[disabledV2StreamingMicroBatchReaders]] Used exclusively when `MicroBatchExecution` is requested for the <<spark-sql-streaming-MicroBatchExecution.adoc#logicalPlan, analyzed logical plan>> (of a streaming query)

| `fileSourceLogDeletion`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.fileSource.log.deletion, spark.sql.streaming.fileSource.log.deletion>>

a| [[fileSourceLogDeletion]][[FILE_SOURCE_LOG_DELETION]] Used exclusively when `FileStreamSourceLog` is requested for the <<spark-sql-streaming-FileStreamSourceLog.adoc#isDeletingExpiredLog, isDeletingExpiredLog>>

| `fileSourceLogCleanupDelay`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.fileSource.log.cleanupDelay, spark.sql.streaming.fileSource.log.cleanupDelay>>

a| [[fileSourceLogCleanupDelay]][[FILE_SOURCE_LOG_CLEANUP_DELAY]] Used exclusively when `FileStreamSourceLog` is requested for the <<spark-sql-streaming-FileStreamSourceLog.adoc#fileCleanupDelayMs, fileCleanupDelayMs>>

| `fileSourceLogCompactInterval`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.fileSource.log.compactInterval, spark.sql.streaming.fileSource.log.compactInterval>>

a| [[fileSourceLogCompactInterval]][[FILE_SOURCE_LOG_COMPACT_INTERVAL]] Used exclusively when `FileStreamSourceLog` is requested for the <<spark-sql-streaming-FileStreamSourceLog.adoc#defaultCompactInterval, default compaction interval>>

| `FLATMAPGROUPSWITHSTATE_STATE_FORMAT_VERSION`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion, spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion>>
a| [[FLATMAPGROUPSWITHSTATE_STATE_FORMAT_VERSION]] Used when:

* <<spark-sql-streaming-FlatMapGroupsWithStateStrategy.adoc#, FlatMapGroupsWithStateStrategy>> execution planning strategy is requested to plan a streaming query (and creates a <<spark-sql-streaming-FlatMapGroupsWithStateExec.adoc#, FlatMapGroupsWithStateExec>> physical operator for every <<spark-sql-streaming-FlatMapGroupsWithState.adoc#, FlatMapGroupsWithState>> logical operator)

* Among the <<spark-sql-streaming-OffsetSeqMetadata.adoc#relevantSQLConfs, checkpointed properties>>

| `minBatchesToRetain`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.minBatchesToRetain, spark.sql.streaming.minBatchesToRetain>>
a| [[minBatchesToRetain]] Used when:

* `CompactibleFileStreamLog` is <<spark-sql-streaming-CompactibleFileStreamLog.adoc#minBatchesToRetain, created>>

* `StreamExecution` is <<spark-sql-streaming-StreamExecution.adoc#minLogEntriesToMaintain, created>>

* `StateStoreConf` is <<spark-sql-streaming-StateStoreConf.adoc#minVersionsToRetain, created>>

| `SHUFFLE_PARTITIONS`

`spark.sql.shuffle.partitions`
a| [[SHUFFLE_PARTITIONS]] See https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-properties.html#spark.sql.shuffle.partitions[spark.sql.shuffle.partitions] in The Internals of Spark SQL.

| `stateStoreMinDeltasForSnapshot`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.stateStore.minDeltasForSnapshot, spark.sql.streaming.stateStore.minDeltasForSnapshot>>

a| [[stateStoreMinDeltasForSnapshot]] Used (as <<spark-sql-streaming-StateStoreConf.adoc#minDeltasForSnapshot, StateStoreConf.minDeltasForSnapshot>>) exclusively when `HDFSBackedStateStoreProvider` is requested to <<spark-sql-streaming-HDFSBackedStateStoreProvider.adoc#doSnapshot, doSnapshot>>

| `stateStoreProviderClass`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.stateStore.providerClass, spark.sql.streaming.stateStore.providerClass>>

a| [[stateStoreProviderClass]] Used when:

* `StateStoreWriter` is requested to <<spark-sql-streaming-StateStoreWriter.adoc#stateStoreCustomMetrics, stateStoreCustomMetrics>> (when `StateStoreWriter` is requested for the <<spark-sql-streaming-StateStoreWriter.adoc#metrics, metrics>> and <<spark-sql-streaming-StateStoreWriter.adoc#getProgress, getProgress>>)

* `StateStoreConf` is <<spark-sql-streaming-StateStoreConf.adoc#providerClass, created>>

| `STREAMING_AGGREGATION_STATE_FORMAT_VERSION`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.aggregation.stateFormatVersion, spark.sql.streaming.aggregation.stateFormatVersion>>
a| [[STREAMING_AGGREGATION_STATE_FORMAT_VERSION]] Used when:

* <<spark-sql-streaming-StatefulAggregationStrategy.adoc#, StatefulAggregationStrategy>> execution planning strategy is executed

* `OffsetSeqMetadata` is requested for the <<spark-sql-streaming-OffsetSeqMetadata.adoc#relevantSQLConfs, relevantSQLConfs>> and the <<spark-sql-streaming-OffsetSeqMetadata.adoc#relevantSQLConfDefaultValues, relevantSQLConfDefaultValues>>

| `STREAMING_CHECKPOINT_FILE_MANAGER_CLASS`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.checkpointFileManagerClass, spark.sql.streaming.checkpointFileManagerClass>>
a| [[STREAMING_CHECKPOINT_FILE_MANAGER_CLASS]] Used exclusively when `CheckpointFileManager` helper object is requested to <<spark-sql-streaming-CheckpointFileManager.adoc#create, create a CheckpointFileManager>>

| `streamingMetricsEnabled`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.metricsEnabled, spark.sql.streaming.metricsEnabled>>

a| [[streamingMetricsEnabled]] Used exclusively when `StreamExecution` is requested for <<spark-sql-streaming-StreamExecution.adoc#runStream, runStream>> (to control whether to register a <<spark-sql-streaming-StreamExecution.adoc#streamMetrics, metrics reporter>> for a streaming query)

| `STREAMING_MULTIPLE_WATERMARK_POLICY`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.multipleWatermarkPolicy, spark.sql.streaming.multipleWatermarkPolicy>>

a| [[STREAMING_MULTIPLE_WATERMARK_POLICY]]

| `streamingNoDataMicroBatchesEnabled`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.noDataMicroBatches.enabled, spark.sql.streaming.noDataMicroBatches.enabled>>

a| [[streamingNoDataMicroBatchesEnabled]][[STREAMING_NO_DATA_MICRO_BATCHES_ENABLED]] Used exclusively when `MicroBatchExecution` stream execution engine is requested to <<spark-sql-streaming-MicroBatchExecution.adoc#runActivatedStream, run an activated streaming query>>

| `streamingNoDataProgressEventInterval`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.noDataProgressEventInterval, spark.sql.streaming.noDataProgressEventInterval>>

a| [[streamingNoDataProgressEventInterval]] Used exclusively for <<spark-sql-streaming-ProgressReporter.adoc#noDataProgressEventInterval, ProgressReporter>>

| `streamingPollingDelay`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.pollingDelay, spark.sql.streaming.pollingDelay>>

a| [[streamingPollingDelay]][[STREAMING_POLLING_DELAY]] Used exclusively when `StreamExecution` is <<spark-sql-streaming-StreamExecution.adoc#, created>>

| `streamingProgressRetention`

<<spark-sql-streaming-properties.adoc#spark.sql.streaming.numRecentProgressUpdates, spark.sql.streaming.numRecentProgressUpdates>>

a| [[streamingProgressRetention]][[STREAMING_PROGRESS_RETENTION]] Used exclusively when `ProgressReporter` is requested to <<spark-sql-streaming-ProgressReporter.adoc#updateProgress, update progress of streaming query>> (and possibly remove an excess)

|===
