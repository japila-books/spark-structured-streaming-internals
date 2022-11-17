# SubscribeStrategy

`SubscribeStrategy` is a [ConsumerStrategy](ConsumerStrategy.md) that is used for [subscribe](options.md#subscribe) option.

`SubscribeStrategy` uses [KafkaConsumer.subscribe]({{ kafka.api }}/org/apache/kafka/clients/consumer/KafkaConsumer.html#subscribe(java.util.Collection)) to subscribe a [KafkaConsumer](#createConsumer) to the given [topics](#topics).

## Creating Instance

`SubscribeStrategy` takes the following to be created:

* <span id="topics"> Topic Names

`SubscribeStrategy` is created when:

* `KafkaSourceProvider` is requested for a [consumer strategy](KafkaSourceProvider.md#strategy) (for [subscribe](options.md#subscribe) option)

## <span id="createConsumer"> Creating Kafka Consumer

```scala
createConsumer(
  kafkaParams: ju.Map[String, Object]): Consumer[Array[Byte], Array[Byte]]
```

`createConsumer` is part of the [ConsumerStrategy](ConsumerStrategy.md#createConsumer) abstraction.

---

`createConsumer` creates a `KafkaConsumer` ([Apache Kafka]({{ kafka.api }}/org/apache/kafka/clients/consumer/KafkaConsumer.html)) to subscribe (using [KafkaConsumer.subscribe]({{ kafka.api }}/org/apache/kafka/clients/consumer/KafkaConsumer.html#subscribe(java.util.Collection))) to the given [topics](#topics).

## <span id="assignedTopicPartitions"> Assigned TopicPartitions

```scala
assignedTopicPartitions(
  admin: Admin): Set[TopicPartition]
```

`assignedTopicPartitions` is part of the [ConsumerStrategy](ConsumerStrategy.md#assignedTopicPartitions) abstraction.

---

`assignedTopicPartitions` [retrieveAllPartitions](ConsumerStrategy.md#retrieveAllPartitions).

## <span id="toString"> String Representation

```scala
toString(): String
```

`toString` is part of the `Object` ([Java]({{ java.api }}/java/lang/Object.html#toString())) abstraction.

---

`toString` is the following (with the comma-separated [topic names](#topics)):

```text
Subscribe[[topics]]
```

---

`toString` is used (among the other uses) when:

* `KafkaOffsetReaderConsumer` is requested for the [string representation](KafkaOffsetReaderConsumer.md#toString) (for [KafkaMicroBatchStream](KafkaMicroBatchStream.md#toString))
