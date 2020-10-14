# KafkaOffsetReader

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

[[logging]]
[TIP]
====
Enable `ALL` logging level for `org.apache.spark.sql.kafka010.KafkaOffsetReader` to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.kafka010.KafkaOffsetReader=ALL
```

Refer to <<spark-sql-streaming-spark-logging.md#, Logging>>.
====

=== [[creating-instance]] Creating KafkaOffsetReader Instance

`KafkaOffsetReader` takes the following to be created:

* [[consumerStrategy]] [ConsumerStrategy](ConsumerStrategy.md)
* [[driverKafkaParams]] Kafka parameters (as name-value pairs that are used exclusively to <<createConsumer, create a Kafka consumer>>
* [[readerOptions]] Options (as name-value pairs)
* [[driverGroupIdPrefix]] Prefix of the group ID

`KafkaOffsetReader` initializes the <<internal-properties, internal properties>>.

=== [[nextGroupId]] `nextGroupId` Internal Method

[source, scala]
----
nextGroupId(): String
----

`nextGroupId` sets the <<groupId, groupId>> to be the <<driverGroupIdPrefix, driverGroupIdPrefix>>, `-` followed by the <<nextId, nextId>> (i.e. `[driverGroupIdPrefix]-[nextId]`).

In the end, `nextGroupId` increments the <<nextId, nextId>> and returns the <<groupId, groupId>>.

NOTE: `nextGroupId` is used exclusively when `KafkaOffsetReader` is requested for a <<consumer, Kafka Consumer>>.

=== [[resetConsumer]] `resetConsumer` Internal Method

[source, scala]
----
resetConsumer(): Unit
----

`resetConsumer`...FIXME

NOTE: `resetConsumer` is used when...FIXME

=== [[fetchTopicPartitions]] `fetchTopicPartitions` Method

[source, scala]
----
fetchTopicPartitions(): Set[TopicPartition]
----

CAUTION: FIXME

`fetchTopicPartitions` is used when `KafkaRelation` is requested for [getPartitionOffsets](KafkaRelation.md#getPartitionOffsets).

=== [[fetchEarliestOffsets]] Fetching Earliest Offsets -- `fetchEarliestOffsets` Method

[source, scala]
----
fetchEarliestOffsets(): Map[TopicPartition, Long]
fetchEarliestOffsets(newPartitions: Seq[TopicPartition]): Map[TopicPartition, Long]
----

CAUTION: FIXME

NOTE: `fetchEarliestOffsets` is used when `KafkaSource` [rateLimit](KafkaSource.md#rateLimit) and [generates a DataFrame for a batch](KafkaSource.md#getBatch) (when new partitions have been assigned).

=== [[fetchLatestOffsets]] Fetching Latest Offsets -- `fetchLatestOffsets` Method

[source, scala]
----
fetchLatestOffsets(): Map[TopicPartition, Long]
----

CAUTION: FIXME

NOTE: `fetchLatestOffsets` is used when `KafkaSource` [gets offsets](KafkaSource.md#getOffset) or `initialPartitionOffsets` is [initialized](KafkaSource.md#initialPartitionOffsets).

=== [[withRetriesWithoutInterrupt]] `withRetriesWithoutInterrupt` Internal Method

[source, scala]
----
withRetriesWithoutInterrupt(
  body: => Map[TopicPartition, Long]): Map[TopicPartition, Long]
----

`withRetriesWithoutInterrupt`...FIXME

NOTE: `withRetriesWithoutInterrupt` is used when...FIXME

=== [[fetchSpecificOffsets]] Fetching Offsets for Selected TopicPartitions -- `fetchSpecificOffsets` Method

[source, scala]
----
fetchSpecificOffsets(
  partitionOffsets: Map[TopicPartition, Long],
  reportDataLoss: String => Unit): KafkaSourceOffset
----

.KafkaOffsetReader's fetchSpecificOffsets
image::images/KafkaOffsetReader-fetchSpecificOffsets.png[align="center"]

`fetchSpecificOffsets` requests the <<consumer, Kafka Consumer>> to `poll(0)`.

`fetchSpecificOffsets` requests the <<consumer, Kafka Consumer>> for assigned partitions (using `Consumer.assignment()`).

`fetchSpecificOffsets` requests the <<consumer, Kafka Consumer>> to `pause(partitions)`.

You should see the following DEBUG message in the logs:

```
DEBUG KafkaOffsetReader: Partitions assigned to consumer: [partitions]. Seeking to [partitionOffsets]
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

* [KafkaContinuousReader](KafkaContinuousReader.md#stop), [KafkaMicroBatchReader](KafkaMicroBatchReader.md#stop), and [KafkaSource](KafkaSource.md#stop) are requested to stop a streaming reader or source

* `KafkaRelation` is requested to [build a distributed data scan with column pruning](KafkaRelation.md#buildScan)

=== [[runUninterruptibly]] `runUninterruptibly` Internal Method

[source, scala]
----
runUninterruptibly[T](body: => T): T
----

`runUninterruptibly`...FIXME

NOTE: `runUninterruptibly` is used when...FIXME

=== [[stopConsumer]] `stopConsumer` Internal Method

[source, scala]
----
stopConsumer(): Unit
----

`stopConsumer`...FIXME

NOTE: `stopConsumer` is used when...FIXME

=== [[toString]] Textual Representation -- `toString` Method

[source, scala]
----
toString: String
----

NOTE: `toString` is part of the ++https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#toString--++[java.lang.Object] contract for the string representation of the object.

`toString`...FIXME

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

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

| groupId
a| [[groupId]]

| kafkaReaderThread
a| [[kafkaReaderThread]] https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html[java.util.concurrent.ExecutorService]

| maxOffsetFetchAttempts
a| [[maxOffsetFetchAttempts]]

| nextId
a| [[nextId]]

Initially `0`

| offsetFetchAttemptIntervalMs
a| [[offsetFetchAttemptIntervalMs]]

|===
