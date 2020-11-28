# OffsetSeqMetadata &mdash; Metadata of Streaming Batch

`OffsetSeqMetadata` holds the metadata for the current streaming batch:

* [[batchWatermarkMs]] [Event-time watermark](streaming-watermark.md) threshold

* [[batchTimestampMs]] [Batch timestamp](spark-structured-streaming-batch-processing-time.md) (in millis)

* [[conf]] **Streaming configuration** with `spark.sql.shuffle.partitions` and [spark.sql.streaming.stateStore.providerClass](configuration-properties.md#spark.sql.streaming.stateStore.providerClass) configuration properties

`OffsetSeqMetadata` is used mainly when [IncrementalExecution](IncrementalExecution.md) is created.

[[relevantSQLConfs]]
`OffsetSeqMetadata` considers some configuration properties as *relevantSQLConfs*:

* [SHUFFLE_PARTITIONS](SQLConf.md#SHUFFLE_PARTITIONS)
* [STATE_STORE_PROVIDER_CLASS](SQLConf.md#STATE_STORE_PROVIDER_CLASS)
* [STREAMING_MULTIPLE_WATERMARK_POLICY](SQLConf.md#STREAMING_MULTIPLE_WATERMARK_POLICY)
* [FLATMAPGROUPSWITHSTATE_STATE_FORMAT_VERSION](SQLConf.md#FLATMAPGROUPSWITHSTATE_STATE_FORMAT_VERSION)
* [STREAMING_AGGREGATION_STATE_FORMAT_VERSION](SQLConf.md#STREAMING_AGGREGATION_STATE_FORMAT_VERSION)

`relevantSQLConfs` are used when `OffsetSeqMetadata` is [created](#apply) and is requested to [setSessionConf](#setSessionConf).

## <span id="apply"> Creating OffsetSeqMetadata

```scala
apply(
  batchWatermarkMs: Long,
  batchTimestampMs: Long,
  sessionConf: RuntimeConfig): OffsetSeqMetadata
```

`apply`...FIXME

`apply` is used when...FIXME

=== [[setSessionConf]] `setSessionConf` Method

[source, scala]
----
setSessionConf(metadata: OffsetSeqMetadata, sessionConf: RuntimeConfig): Unit
----

`setSessionConf`...FIXME

NOTE: `setSessionConf` is used when...FIXME
