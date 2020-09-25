== [[OffsetSeqMetadata]] OffsetSeqMetadata -- Metadata of Streaming Batch

`OffsetSeqMetadata` holds the metadata for the current streaming batch:

* [[batchWatermarkMs]] <<spark-sql-streaming-watermark.md#, Event-time watermark>> threshold

* [[batchTimestampMs]] <<spark-structured-streaming-batch-processing-time.md#, Batch timestamp>> (in millis)

* [[conf]] *Streaming configuration* with `spark.sql.shuffle.partitions` and spark-sql-streaming-properties.md#spark.sql.streaming.stateStore.providerClass[spark.sql.streaming.stateStore.providerClass] Spark properties

NOTE: `OffsetSeqMetadata` is used mainly when `IncrementalExecution` is spark-sql-streaming-IncrementalExecution.md#creating-instance[created].

[[relevantSQLConfs]]
`OffsetSeqMetadata` considers some configuration properties as *relevantSQLConfs*:

* <<SQLConf.md#SHUFFLE_PARTITIONS, SHUFFLE_PARTITIONS>>
* <<SQLConf.md#STATE_STORE_PROVIDER_CLASS, STATE_STORE_PROVIDER_CLASS>>
* <<SQLConf.md#STREAMING_MULTIPLE_WATERMARK_POLICY, STREAMING_MULTIPLE_WATERMARK_POLICY>>
* <<SQLConf.md#FLATMAPGROUPSWITHSTATE_STATE_FORMAT_VERSION, FLATMAPGROUPSWITHSTATE_STATE_FORMAT_VERSION>>
* <<SQLConf.md#STREAMING_AGGREGATION_STATE_FORMAT_VERSION, STREAMING_AGGREGATION_STATE_FORMAT_VERSION>>

`relevantSQLConfs` are used when `OffsetSeqMetadata` is <<apply, created>> and is requested to <<setSessionConf, setSessionConf>>.

=== [[apply]] Creating OffsetSeqMetadata -- `apply` Factory Method

[source, scala]
----
apply(
  batchWatermarkMs: Long,
  batchTimestampMs: Long,
  sessionConf: RuntimeConfig): OffsetSeqMetadata
----

`apply`...FIXME

NOTE: `apply` is used when...FIXME

=== [[setSessionConf]] `setSessionConf` Method

[source, scala]
----
setSessionConf(metadata: OffsetSeqMetadata, sessionConf: RuntimeConfig): Unit
----

`setSessionConf`...FIXME

NOTE: `setSessionConf` is used when...FIXME
