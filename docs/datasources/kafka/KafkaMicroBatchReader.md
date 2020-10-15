# KafkaMicroBatchReader

`KafkaMicroBatchReader` is the [MicroBatchReader](../../spark-sql-streaming-MicroBatchReader.md) for [kafka data source](index.md) for <<micro-batch-stream-processing.md#, Micro-Batch Stream Processing>>.

`KafkaMicroBatchReader` is <<creating-instance, created>> exclusively when `KafkaSourceProvider` is requested to [create a MicroBatchReader](KafkaSourceProvider.md#createMicroBatchReader).

[[pollTimeoutMs]]
`KafkaMicroBatchReader` uses the <<options, DataSourceOptions>> to access the [kafkaConsumer.pollTimeoutMs](index.md#kafkaConsumer.pollTimeoutMs) option (default: `spark.network.timeout` or `120s`).

[[maxOffsetsPerTrigger]]
`KafkaMicroBatchReader` uses the <<options, DataSourceOptions>> to access the [maxOffsetsPerTrigger](index.md#maxOffsetsPerTrigger) option (default: `(undefined)`).

`KafkaMicroBatchReader` uses the <<executorKafkaParams, Kafka properties for executors>> to create [KafkaMicroBatchInputPartitions](KafkaMicroBatchInputPartition.md) when requested to <<planInputPartitions, planInputPartitions>>.

[[logging]]
[TIP]
====
Enable `ALL` logging level for `org.apache.spark.sql.kafka010.KafkaMicroBatchReader` to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.kafka010.KafkaMicroBatchReader=ALL
```

Refer to <<spark-sql-streaming-spark-logging.md#, Logging>>.
====

## Creating Instance

`KafkaMicroBatchReader` takes the following to be created:

* [[kafkaOffsetReader]] [KafkaOffsetReader](KafkaOffsetReader.md)
* [[executorKafkaParams]] Kafka properties for executors (`Map[String, Object]`)
* [[options]] `DataSourceOptions`
* [[metadataPath]] Metadata Path
* [[startingOffsets]] Desired starting [KafkaOffsetRangeLimit](KafkaOffsetRangeLimit.md)
* [[failOnDataLoss]] [failOnDataLoss](index.md#failOnDataLoss) option

`KafkaMicroBatchReader` initializes the <<internal-registries, internal registries and counters>>.

=== [[readSchema]] `readSchema` Method

[source, scala]
----
readSchema(): StructType
----

NOTE: `readSchema` is part of the `DataSourceReader` contract to...FIXME.

`readSchema` simply returns the [predefined fixed schema](index.md#schema).

=== [[planInputPartitions]] Plan Input Partitions -- `planInputPartitions` Method

[source, scala]
----
planInputPartitions(): java.util.List[InputPartition[InternalRow]]
----

NOTE: `planInputPartitions` is part of the `DataSourceReader` contract in Spark SQL for the number of `InputPartitions` to use as RDD partitions (when `DataSourceV2ScanExec` physical operator is requested for the partitions of the input RDD).

`planInputPartitions` first finds the new partitions (`TopicPartitions` that are in the <<endPartitionOffsets, endPartitionOffsets>> but not in the <<startPartitionOffsets, startPartitionOffsets>>) and requests the <<kafkaOffsetReader, KafkaOffsetReader>> to
[fetch their earliest offsets](KafkaOffsetReader.md#fetchEarliestOffsets).

`planInputPartitions` prints out the following INFO message to the logs:

```text
Partitions added: [newPartitionInitialOffsets]
```

`planInputPartitions` then prints out the following DEBUG message to the logs:

```
TopicPartitions: [comma-separated list of TopicPartitions]
```

`planInputPartitions` requests the <<rangeCalculator, KafkaOffsetRangeCalculator>> for <<getRanges, offset ranges>> (given the <<startPartitionOffsets, startPartitionOffsets>> and the newly-calculated `newPartitionInitialOffsets` as the `fromOffsets`, the <<endPartitionOffsets, endPartitionOffsets>> as the `untilOffsets`, and the <<getSortedExecutorList, available executors (sorted in descending order)>>).

In the end, `planInputPartitions` creates a [KafkaMicroBatchInputPartition](KafkaMicroBatchInputPartition.md) for every offset range (with the <<executorKafkaParams, Kafka properties for executors>>, the <<pollTimeoutMs, pollTimeoutMs>>, the <<failOnDataLoss, failOnDataLoss>> flag and whether to reuse a Kafka consumer among Spark tasks).

`planInputPartitions` <<reportDataLoss, reports data loss>> when...FIXME

=== [[getSortedExecutorList]] Available Executors in Spark Cluster (Sorted By Host and Executor ID in Descending Order) -- `getSortedExecutorList` Internal Method

[source, scala]
----
getSortedExecutorList(): Array[String]
----

`getSortedExecutorList` requests the `BlockManager` to request the `BlockManagerMaster` to get the peers (the other nodes in a Spark cluster), creates a `ExecutorCacheTaskLocation` for every pair of host and executor ID, and in the end, sort it in descending order.

NOTE: `getSortedExecutorList` is used exclusively when `KafkaMicroBatchReader` is requested to <<planInputPartitions, planInputPartitions>> (and calculates offset ranges).

=== [[getOrCreateInitialPartitionOffsets]] `getOrCreateInitialPartitionOffsets` Internal Method

[source, scala]
----
getOrCreateInitialPartitionOffsets(): PartitionOffsetMap
----

`getOrCreateInitialPartitionOffsets`...FIXME

NOTE: `getOrCreateInitialPartitionOffsets` is used exclusively for the <<initialPartitionOffsets, initialPartitionOffsets>> internal registry.

=== [[getStartOffset]] `getStartOffset` Method

[source, scala]
----
getStartOffset: Offset
----

NOTE: `getStartOffset` is part of the <<spark-sql-streaming-MicroBatchReader.md#getStartOffset, MicroBatchReader Contract>> to get the start (beginning) <<spark-sql-streaming-Offset.md#, offsets>>.

`getStartOffset`...FIXME

=== [[getEndOffset]] `getEndOffset` Method

[source, scala]
----
getEndOffset: Offset
----

NOTE: `getEndOffset` is part of the <<spark-sql-streaming-MicroBatchReader.md#getEndOffset, MicroBatchReader Contract>> to get the end <<spark-sql-streaming-Offset.md#, offsets>>.

`getEndOffset`...FIXME

=== [[deserializeOffset]] `deserializeOffset` Method

[source, scala]
----
deserializeOffset(json: String): Offset
----

NOTE: `deserializeOffset` is part of the <<spark-sql-streaming-MicroBatchReader.md#deserializeOffset, MicroBatchReader Contract>> to deserialize an <<spark-sql-streaming-Offset.md#, offset>> (from JSON format).

`deserializeOffset`...FIXME

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| endPartitionOffsets
a| [[endPartitionOffsets]] Ending offsets for the assigned partitions (`Map[TopicPartition, Long]`)

Used when...FIXME

| initialPartitionOffsets
a| [[initialPartitionOffsets]]

[source, scala]
----
initialPartitionOffsets: Map[TopicPartition, Long]
----

| rangeCalculator
a| [[rangeCalculator]] [KafkaOffsetRangeCalculator](KafkaOffsetRangeCalculator.md) (for the given <<options, DataSourceOptions>>)

Used exclusively when `KafkaMicroBatchReader` is requested to <<planInputPartitions, planInputPartitions>> (to calculate offset ranges)

| startPartitionOffsets
a| [[startPartitionOffsets]] Starting offsets for the assigned partitions (`Map[TopicPartition, Long]`)

Used when...FIXME

|===
