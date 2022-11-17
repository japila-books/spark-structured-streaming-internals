# KafkaOffsetReaderConsumer

`KafkaOffsetReaderConsumer` is a [KafkaOffsetReader](KafkaOffsetReader.md).

`KafkaOffsetReaderConsumer` is considered old and deprecated (in favour of the new `Admin`-based [KafkaOffsetReaderAdmin](KafkaOffsetReaderAdmin.md)).

!!! note "spark.sql.streaming.kafka.useDeprecatedOffsetFetching"
    [spark.sql.streaming.kafka.useDeprecatedOffsetFetching](../configuration-properties.md#spark.sql.streaming.kafka.useDeprecatedOffsetFetching) configuration property controls what [KafkaOffsetReader](KafkaOffsetReader.md) is used.

## Creating Instance

`KafkaOffsetReaderConsumer` takes the following to be created:

* <span id="consumerStrategy"> [ConsumerStrategy](ConsumerStrategy.md)
* <span id="driverKafkaParams"> Driver Kafka Parameters
* <span id="readerOptions"> Reader Options
* <span id="driverGroupIdPrefix"> Driver GroupId Prefix

`KafkaOffsetReaderConsumer` is created when:

* `KafkaOffsetReader` is requested to [build a KafkaOffsetReader](KafkaOffsetReader.md#build) (with [spark.sql.streaming.kafka.useDeprecatedOffsetFetching](../configuration-properties.md#spark.sql.streaming.kafka.useDeprecatedOffsetFetching) property enabled)

## <span id="toString"> String Representation

```scala
toString(): String
```

`toString` is part of the `Object` ([Java]({{ java.api }}/java/lang/Object.html#toString())) abstraction.

---

`toString` requests the [ConsumerStrategy](#consumerStrategy) for the [string representation](ConsumerStrategy.md#toString).

!!! note
    [ConsumerStrategy](ConsumerStrategy.md) does not overload `toString`, but the [implementations](ConsumerStrategy.md#implementations) do (e.g., [SubscribeStrategy](SubscribeStrategy.md#toString)).

---

`toString` is used (among the other uses) when:

* `KafkaMicroBatchStream` is requested for the [string representation](KafkaMicroBatchStream.md#toString)
