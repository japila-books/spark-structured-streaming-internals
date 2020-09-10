== [[KafkaContinuousReader]] KafkaContinuousReader -- ContinuousReader for Kafka Data Source in Continuous Stream Processing

`KafkaContinuousReader` is a <<spark-sql-streaming-ContinuousReader.md#, ContinuousReader>> for <<spark-sql-streaming-kafka-data-source.md#, Kafka Data Source>> in <<spark-sql-streaming-continuous-stream-processing.md#, Continuous Stream Processing>>.

`KafkaContinuousReader` is <<creating-instance, created>> exclusively when `KafkaSourceProvider` is requested to <<spark-sql-streaming-KafkaSourceProvider.md#createContinuousReader, create a ContinuousReader>>.

[[pollTimeoutMs]]
[[kafkaConsumer.pollTimeoutMs]]
`KafkaContinuousReader` uses *kafkaConsumer.pollTimeoutMs* configuration parameter (default: `512`) for <<spark-sql-streaming-KafkaContinuousInputPartition.md#, KafkaContinuousInputPartitions>> when requested to <<planInputPartitions, planInputPartitions>>.

[[logging]]
[TIP]
====
Enable `INFO` or `WARN` logging levels for `org.apache.spark.sql.kafka010.KafkaContinuousReader` to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.kafka010.KafkaContinuousReader=INFO
```

Refer to link:spark-sql-streaming-logging.md[Logging].
====

=== [[creating-instance]] Creating KafkaContinuousReader Instance

`KafkaContinuousReader` takes the following to be created:

* [[offsetReader]] <<spark-sql-streaming-KafkaOffsetReader.md#, KafkaOffsetReader>>
* [[kafkaParams]] Kafka parameters (as `java.util.Map[String, Object]`)
* [[sourceOptions]] Source options (as `Map[String, String]`)
* [[metadataPath]] Metadata path
* [[initialOffsets]] <<spark-sql-streaming-KafkaOffsetRangeLimit.md#, Initial offsets>>
* [[failOnDataLoss]] `failOnDataLoss` flag

=== [[planInputPartitions]] Plan Input Partitions -- `planInputPartitions` Method

[source, scala]
----
planInputPartitions(): java.util.List[InputPartition[InternalRow]]
----

NOTE: `planInputPartitions` is part of the `DataSourceReader` contract in Spark SQL for the number of `InputPartitions` to use as RDD partitions (when `DataSourceV2ScanExec` physical operator is requested for the partitions of the input RDD).

`planInputPartitions`...FIXME

=== [[setStartOffset]] `setStartOffset` Method

[source, java]
----
setStartOffset(
  start: Optional[Offset]): Unit
----

NOTE: `setStartOffset` is part of the <<spark-sql-streaming-ContinuousReader.md#setStartOffset, ContinuousReader Contract>> to...FIXME.

`setStartOffset`...FIXME

=== [[deserializeOffset]] `deserializeOffset` Method

[source, java]
----
deserializeOffset(
  json: String): Offset
----

NOTE: `deserializeOffset` is part of the <<spark-sql-streaming-ContinuousReader.md#deserializeOffset, ContinuousReader Contract>> to...FIXME.

`deserializeOffset`...FIXME

=== [[mergeOffsets]] `mergeOffsets` Method

[source, java]
----
mergeOffsets(
  offsets: Array[PartitionOffset]): Offset
----

NOTE: `mergeOffsets` is part of the <<spark-sql-streaming-ContinuousReader.md#mergeOffsets, ContinuousReader Contract>> to...FIXME.

`mergeOffsets`...FIXME
