# KafkaOffsetReader

`KafkaOffsetReader` is an [abstraction](#contract) of [Kafka offset readers](#implementations).

## Contract (Subset)

### <span id="fetchEarliestOffsets"> fetchEarliestOffsets

```scala
fetchEarliestOffsets(): Map[TopicPartition, Long]
fetchEarliestOffsets(
  newPartitions: Seq[TopicPartition]): Map[TopicPartition, Long]
```

Used when:

* `KafkaContinuousStream` is requested to [initialOffset](KafkaContinuousStream.md#initialOffset), [planInputPartitions](KafkaContinuousStream.md#planInputPartitions)
* `KafkaMicroBatchStream` is requested to [getOrCreateInitialPartitionOffsets](KafkaMicroBatchStream.md#getOrCreateInitialPartitionOffsets), [rateLimit](KafkaMicroBatchStream.md#rateLimit)
* `KafkaSource` is requested for [initialPartitionOffsets](KafkaSource.md#initialPartitionOffsets), [rateLimit](KafkaSource.md#rateLimit)

### <span id="fetchGlobalTimestampBasedOffsets"> fetchGlobalTimestampBasedOffsets

```scala
fetchGlobalTimestampBasedOffsets(
  timestamp: Long,
  isStartingOffsets: Boolean,
  strategyOnNoMatchingStartingOffset: StrategyOnNoMatchStartingOffset.Value): KafkaSourceOffset
```

Used when:

* `KafkaContinuousStream` is requested to [initialOffset](KafkaContinuousStream.md#initialOffset)
* `KafkaMicroBatchStream` is requested to [getOrCreateInitialPartitionOffsets](KafkaMicroBatchStream.md#getOrCreateInitialPartitionOffsets)
* `KafkaSource` is requested for [initialPartitionOffsets](KafkaSource.md#initialPartitionOffsets)

### <span id="fetchLatestOffsets"> fetchLatestOffsets

```scala
fetchLatestOffsets(
  knownOffsets: Option[PartitionOffsetMap]): PartitionOffsetMap // (1)!
```

1. `type PartitionOffsetMap = Map[TopicPartition, Long]`

Used when:

* `KafkaContinuousStream` is requested to [initialOffset](KafkaContinuousStream.md#initialOffset), [needsReconfiguration](KafkaContinuousStream.md#needsReconfiguration)
* `KafkaMicroBatchStream` is requested to [latestOffset](KafkaMicroBatchStream.md#latestOffset), [getOrCreateInitialPartitionOffsets](KafkaMicroBatchStream.md#getOrCreateInitialPartitionOffsets), [prepareForTriggerAvailableNow](KafkaMicroBatchStream.md#prepareForTriggerAvailableNow)
* `KafkaSource` is requested for [initialPartitionOffsets](KafkaSource.md#initialPartitionOffsets), [latestOffset](KafkaSource.md#latestOffset), [prepareForTriggerAvailableNow](KafkaSource.md#prepareForTriggerAvailableNow)

### <span id="fetchPartitionOffsets"> fetchPartitionOffsets

```scala
fetchPartitionOffsets(
  offsetRangeLimit: KafkaOffsetRangeLimit,
  isStartingOffsets: Boolean): Map[TopicPartition, Long]
```

Used when:

* _never used_?!

## Implementations

* [KafkaOffsetReaderAdmin](KafkaOffsetReaderAdmin.md)
* [KafkaOffsetReaderConsumer](KafkaOffsetReaderConsumer.md)

## <span id="build"> Creating KafkaOffsetReader

```scala
build(
  consumerStrategy: ConsumerStrategy,
  driverKafkaParams: ju.Map[String, Object],
  readerOptions: CaseInsensitiveMap[String],
  driverGroupIdPrefix: String): KafkaOffsetReader
```

`build` branches off based on [spark.sql.streaming.kafka.useDeprecatedOffsetFetching](../../configuration-properties.md#spark.sql.streaming.kafka.useDeprecatedOffsetFetching) configuration property.

With `useDeprecatedKafkaOffsetFetching` enabled, `build` prints out the following DEBUG message to the logs and creates a [KafkaOffsetReaderConsumer](KafkaOffsetReaderConsumer.md).

```text
Creating old and deprecated Consumer based offset reader
```

Otherwise, `build` prints out the following DEBUG message to the logs and creates a [KafkaOffsetReaderAdmin](KafkaOffsetReaderAdmin.md).

```text
Creating new Admin based offset reader
```

---

`build` is used when:

* `KafkaBatch` is requested to [planInputPartitions](KafkaBatch.md#planInputPartitions)
* `KafkaRelation` is requested to [buildScan](KafkaRelation.md#buildScan)
* `KafkaSourceProvider` is requested to [create a Source](KafkaSourceProvider.md#createSource)
* `KafkaScan` is requested to create a [MicroBatchStream](KafkaScan.md#toMicroBatchStream) or a [ContinuousStream](KafkaScan.md#toContinuousStream)

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.kafka010.KafkaOffsetReader` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.kafka010.KafkaOffsetReader=ALL
```

Refer to [Logging](../../spark-logging.md).

<!---

## Review Me

`KafkaOffsetReader` relies on the [ConsumerStrategy](#consumerStrategy) to <<consumer, create a Kafka Consumer>>.

`KafkaOffsetReader` <<consumer, creates a Kafka Consumer>> with *group.id* (`ConsumerConfig.GROUP_ID_CONFIG`) configuration explicitly set to <<nextGroupId, nextGroupId>> (i.e. the given <<driverGroupIdPrefix, driverGroupIdPrefix>> followed by <<nextId, nextId>>).

`KafkaOffsetReader` is <<creating-instance, created>> when:

* `KafkaRelation` is requested to [build a distributed data scan with column pruning](KafkaRelation.md#buildScan)

* `KafkaSourceProvider` is requested to [create a KafkaSource](KafkaSourceProvider.md#createSource), [createMicroBatchReader](KafkaSourceProvider.md#createMicroBatchReader), and [createContinuousReader](KafkaSourceProvider.md#createContinuousReader)

[[options]]
.KafkaOffsetReader's Options
[cols="1m,3",options="header",width="100%"]
|===
| Name
| Description

| fetchOffset.numRetries
a| [[fetchOffset.numRetries]]

Default: `3`

| fetchOffset.retryIntervalMs
a| [[fetchOffset.retryIntervalMs]] How long to wait before retries

Default: `1000`

|===

[[kafkaSchema]]
`KafkaOffsetReader` defines the [predefined fixed schema](index.md#schema).

=== [[nextGroupId]] `nextGroupId` Internal Method

[source, scala]
----
nextGroupId(): String
----

`nextGroupId` sets the <<groupId, groupId>> to be the <<driverGroupIdPrefix, driverGroupIdPrefix>>, `-` followed by the <<nextId, nextId>> (i.e. `[driverGroupIdPrefix]-[nextId]`).

In the end, `nextGroupId` increments the <<nextId, nextId>> and returns the <<groupId, groupId>>.

NOTE: `nextGroupId` is used exclusively when `KafkaOffsetReader` is requested for a <<consumer, Kafka Consumer>>.

=== [[fetchSpecificOffsets]] Fetching Offsets for Selected TopicPartitions -- `fetchSpecificOffsets` Method

[source, scala]
----
fetchSpecificOffsets(
  partitionOffsets: Map[TopicPartition, Long],
  reportDataLoss: String => Unit): KafkaSourceOffset
----

![KafkaOffsetReader's fetchSpecificOffsets](../../images/KafkaOffsetReader-fetchSpecificOffsets.png)

`fetchSpecificOffsets` requests the <<consumer, Kafka Consumer>> to `poll(0)`.

`fetchSpecificOffsets` requests the <<consumer, Kafka Consumer>> for assigned partitions (using `Consumer.assignment()`).

`fetchSpecificOffsets` requests the <<consumer, Kafka Consumer>> to `pause(partitions)`.

You should see the following DEBUG message in the logs:

```text
Partitions assigned to consumer: [partitions]. Seeking to [partitionOffsets]
```

For every partition offset in the input `partitionOffsets`, `fetchSpecificOffsets` requests the <<consumer, Kafka Consumer>> to:

* `seekToEnd` for the latest (aka `-1`)
* `seekToBeginning` for the earliest (aka `-2`)
* `seek` for other offsets

In the end, `fetchSpecificOffsets` creates a collection of Kafka's `TopicPartition` and `position` (using the <<consumer, Kafka Consumer>>).

`fetchSpecificOffsets` is used when `KafkaSource` [fetches and verifies initial partition offsets](KafkaSource.md#fetchAndVerify).

=== [[createConsumer]] Creating Kafka Consumer -- `createConsumer` Internal Method

[source, scala]
----
createConsumer(): Consumer[Array[Byte], Array[Byte]]
----

`createConsumer` requests <<consumerStrategy, ConsumerStrategy>> to [create a Kafka Consumer](ConsumerStrategy.md#createConsumer) with <<driverKafkaParams, driverKafkaParams>> and <<nextGroupId, new generated group.id Kafka property>>.

NOTE: `createConsumer` is used when `KafkaOffsetReader` is <<creating-instance, created>> (and initializes <<consumer, consumer>>) and <<resetConsumer, resetConsumer>>

=== [[consumer]] Creating Kafka Consumer (Unless Already Available) -- `consumer` Method

[source, scala]
----
consumer: Consumer[Array[Byte], Array[Byte]]
----

`consumer` gives the cached <<_consumer, Kafka Consumer>> or creates one itself.

NOTE: Since `consumer` method is used (to access the internal <<_consumer, Kafka Consumer>>) in the `fetch` methods that gives the property of creating a new Kafka Consumer whenever the internal <<_consumer, Kafka Consumer>> reference become `null`, i.e. as in <<resetConsumer, resetConsumer>>.

`consumer`...FIXME

NOTE: `consumer` is used when `KafkaOffsetReader` is requested to <<fetchTopicPartitions, fetchTopicPartitions>>, <<fetchSpecificOffsets, fetchSpecificOffsets>>, <<fetchEarliestOffsets, fetchEarliestOffsets>>, and <<fetchLatestOffsets, fetchLatestOffsets>>.

=== [[close]] Closing -- `close` Method

[source, scala]
----
close(): Unit
----

`close` <<stopConsumer, stop the Kafka Consumer>> (if the <<_consumer, Kafka Consumer>> is available).

`close` requests the <<kafkaReaderThread, ExecutorService>> to shut down.

`close` is used when:

* [KafkaMicroBatchReader](KafkaMicroBatchReader.md#stop), and [KafkaSource](KafkaSource.md#stop) are requested to stop a streaming reader or source

* `KafkaRelation` is requested to [build a distributed data scan with column pruning](KafkaRelation.md#buildScan)

=== [[internal-properties]] Internal Properties

| _consumer
a| [[_consumer]] Kafka's https://kafka.apache.org/21/javadoc/org/apache/kafka/clients/consumer/Consumer.html[Consumer] (`Consumer[Array[Byte], Array[Byte]]`)

<<createConsumer, Initialized>> when `KafkaOffsetReader` is <<creating-instance, created>>.

Used when `KafkaOffsetReader`:

* <<fetchTopicPartitions, fetchTopicPartitions>>
* <<fetchSpecificOffsets, fetches offsets for selected TopicPartitions>>
* <<fetchEarliestOffsets, fetchEarliestOffsets>>
* <<fetchLatestOffsets, fetchLatestOffsets>>
* <<resetConsumer, resetConsumer>>
* <<close, is closed>>

| execContext
a| [[execContext]] https://www.scala-lang.org/api/2.12.8/scala/concurrent/ExecutionContextExecutorService.html[scala.concurrent.ExecutionContextExecutorService]

| kafkaReaderThread
a| [[kafkaReaderThread]] https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html[java.util.concurrent.ExecutorService]

-->
