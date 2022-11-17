# ConsumerStrategy

`ConsumerStrategy` is an [abstraction](#contract) of [consumer strategies](#implementations) of which partitions to read records from.

## Contract

### <span id="assignedTopicPartitions"> Assigned TopicPartitions

```scala
assignedTopicPartitions(
  admin: Admin): Set[TopicPartition]
```

Assigned `TopicPartition`s ([Apache Kafka]({{ kafka.api }}/org/apache/kafka/common/TopicPartition.html))

Used when:

* `KafkaOffsetReaderAdmin` is requested to [fetchPartitionOffsets](KafkaOffsetReaderAdmin.md#fetchPartitionOffsets), [partitionsAssignedToAdmin](KafkaOffsetReaderAdmin.md#partitionsAssignedToAdmin)

### <span id="createConsumer"> Creating Kafka Consumer

```scala
createConsumer(
  kafkaParams: ju.Map[String, Object]): Consumer[Array[Byte], Array[Byte]]
```

Creates a `Consumer` ([Apache Kafka]({{ kafka.api }}/org/apache/kafka/clients/consumer/Consumer.html))

Used when:

* `KafkaOffsetReaderConsumer` is requested for a [Kafka Consumer](KafkaOffsetReaderConsumer.md#consumer)

## Implementations

??? note "Sealed Trait"
    `ConsumerStrategy` is a Scala **sealed trait** which means that all of the implementations are in the same compilation unit (a single file).

    Learn more in the [Scala Language Specification]({{ scala.spec }}/05-classes-and-objects.html#sealed).

* `AssignStrategy`
* `SubscribePatternStrategy`
* [SubscribeStrategy](SubscribeStrategy.md)
