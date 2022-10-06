# OffsetSeqMetadata

`OffsetSeqMetadata` is the metadata of a streaming batch.

`OffsetSeqMetadata` is persisted in the write-ahead offset log.

## Creating Instance

`OffsetSeqMetadata` takes the following to be created:

* [Batch Watermark](#batchWatermarkMs)
* [Batch Timestamp](#batchTimestampMs)
* <span id="conf"> Configuration (default: empty)

`OffsetSeqMetadata` is created using [apply](#apply).

### <span id="batchWatermarkMs"> Batch Watermark

`OffsetSeqMetadata` is given the current batch's [event-time watermark](streaming-watermark/index.md) when [created](#creating-instance). Unless given, it is assumed `0`.

### <span id="batchTimestampMs"> Batch Timestamp

`OffsetSeqMetadata` is given the current batch's [batch timestamp](spark-structured-streaming-batch-processing-time.md) when [created](#creating-instance). Unless given, it is assumed `0`.

## <span id="apply"> Creating OffsetSeqMetadata

```scala
apply(
  batchWatermarkMs: Long,
  batchTimestampMs: Long,
  sessionConf: RuntimeConfig): OffsetSeqMetadata
```

`apply` find the [relevantSQLConfs](#relevantSQLConfs) in the given `RuntimeConfig` and creates a [OffsetSeqMetadata](#creating-instance) (with the batch watermark and timestamp, and the relevant configs found in the session config).

---

`apply` is used when:

* `MicroBatchExecution` is requested to [populateStartOffsets](micro-batch-execution/MicroBatchExecution.md#populateStartOffsets) (while restarting a streaming query with a checkpointed [offsets](OffsetSeqLog.md))
* `StreamExecution` is [created](StreamExecution.md#offsetSeqMetadata) and requested to [runStream](StreamExecution.md#runStream)

## <span id="relevantSQLConfs"><span id="relevantSQLConfDefaultValues"> Checkpointed Properties

`OffsetSeqMetadata` allows the following configuration properties to be _once-only settable_ that can only be set once and can never change after a streaming query is started.

* `spark.sql.shuffle.partitions` ([Spark SQL]({{ book.spark_sql }}/configuration-properties/#spark.sql.shuffle.partitions))
* [spark.sql.streaming.aggregation.stateFormatVersion](configuration-properties.md#STREAMING_AGGREGATION_STATE_FORMAT_VERSION)
* [spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion](configuration-properties.md#FLATMAPGROUPSWITHSTATE_STATE_FORMAT_VERSION)
* [spark.sql.streaming.join.stateFormatVersion](configuration-properties.md#STREAMING_JOIN_STATE_FORMAT_VERSION)
* [spark.sql.streaming.multipleWatermarkPolicy](configuration-properties.md#STREAMING_MULTIPLE_WATERMARK_POLICY)
* [spark.sql.streaming.statefulOperator.useStrictDistribution](configuration-properties.md#STATEFUL_OPERATOR_USE_STRICT_DISTRIBUTION)
* [spark.sql.streaming.stateStore.compression.codec](configuration-properties.md#STATE_STORE_COMPRESSION_CODEC)
* [spark.sql.streaming.stateStore.providerClass](configuration-properties.md#STATE_STORE_PROVIDER_CLASS)
* [spark.sql.streaming.stateStore.rocksdb.formatVersion](configuration-properties.md#STATE_STORE_ROCKSDB_FORMAT_VERSION)

The configuration properties are searched for while [creating an OffsetSeqMetadata](#apply).

The values of these configs are persisted into the offset log in the checkpoint position.

Once persisted in a checkpoint location, restarting a streaming query [will make the persisted values be in effect again](#setSessionConf) (overriding any current values of the properties if set).

## <span id="setSessionConf"> Updating RuntimeConfig with Metadata Properties

```scala
setSessionConf(
  metadata: OffsetSeqMetadata,
  sessionConf: RuntimeConfig): Unit
```

For any [relevant SQL property](#relevantSQLConfs) set in the given [OffsetSeqMetadata](OffsetSeqMetadata.md), `setSessionConf` overrides the value in the given `RuntimeConfig` if set. `setSessionConf` prints out the following WARN message to the logs:

```text
Updating the value of conf '[confKey]' in current session from '[sessionValue]' to '[metadataValue]'.
```

When not set in the given [OffsetSeqMetadata](OffsetSeqMetadata.md), `setSessionConf` makes the [default values](#relevantSQLConfDefaultValues) effective in the given `RuntimeConfig`. `setSessionConf` prints out the following WARN message to the logs:

```text
Conf '[confKey]' was not found in the offset log, using default value '[defaultValue]'
```

Otherwise, `setSessionConf` prints out one of the following WARN messages to the logs based on whether a relevant property is set in the given `RuntimeConfig` or not.

```text
Conf '[confKey]' was not found in the offset log. Using existing session conf value '[v]'.
```

```text
Conf '[confKey]' was not found in the offset log. No value set in session conf.
```

---

`setSessionConf` is used when:

* `MicroBatchExecution` is requested to [populateStartOffsets](micro-batch-execution/MicroBatchExecution.md#populateStartOffsets) (while restarting a streaming query with a checkpointed [offsets](OffsetSeqLog.md))
