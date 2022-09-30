# Kafka Options

Options with `kafka.` prefix (e.g. [kafka.bootstrap.servers](#kafka.bootstrap.servers)) are considered configuration properties for the Kafka consumers used on the [driver](KafkaSourceProvider.md#kafkaParamsForDriver) and [executors](KafkaSourceProvider.md#kafkaParamsForExecutors).

## <span id="assign"> assign

[Topic subscription strategy](ConsumerStrategy.md#AssignStrategy) that accepts a JSON with topic names and partitions, e.g.

```text
{"topicA":[0,1],"topicB":[0,1]}
```

Exactly one topic subscription strategy is allowed (that `KafkaSourceProvider` [validates](KafkaSourceProvider.md#validateGeneralOptions) before creating `KafkaSource`).

## <span id="failOnDataLoss"> failOnDataLoss

Default: `true`

Used when:

* `KafkaSourceProvider` is requested for [failOnDataLoss](KafkaSourceProvider.md#failOnDataLoss) configuration property

## <span id="includeHeaders"><span id="INCLUDE_HEADERS"> includeHeaders

Default: `false`

## <span id="kafka.bootstrap.servers"> kafka.bootstrap.servers

**(required)** `bootstrap.servers` configuration property of the Kafka consumers used on the driver and executors

Default: `(empty)`

## <span id="kafkaConsumer.pollTimeoutMs"><span id="pollTimeoutMs"> kafkaConsumer.pollTimeoutMs

The time (in milliseconds) spent waiting in `Consumer.poll` if data is not available in the buffer.

Default: `spark.network.timeout` or `120s`

## <span id="maxOffsetsPerTrigger"> maxOffsetsPerTrigger

Number of records to fetch per trigger (to limit the number of records to fetch).

Default: `(undefined)`

Unless defined, `KafkaSource` requests [KafkaOffsetReader](KafkaSource.md#kafkaReader) for the [latest offsets](KafkaOffsetReader.md#fetchLatestOffsets).

## <span id="minPartitions"> minPartitions

Minimum number of partitions per executor (given Kafka partitions)

Default: `(undefined)`

Must be undefined (default) or greater than `0`

When undefined (default) or smaller than the number of `TopicPartitions` with records to consume from, [KafkaMicroBatchReader](KafkaMicroBatchReader.md) uses [KafkaOffsetRangeCalculator](KafkaMicroBatchReader.md#rangeCalculator) to [find the preferred executor](KafkaOffsetRangeCalculator.md#getLocation) for every `TopicPartition` (and the [available executors](KafkaMicroBatchReader.md#getSortedExecutorList)).

## <span id="startingOffsets"> startingOffsets

Starting offsets

Default: `latest`

Possible values:

* `latest`

* `earliest`

* JSON with topics, partitions and their starting offsets, e.g.

    ```json
    {"topicA":{"part":offset,"p1":-1},"topicB":{"0":-2}}
    ```

!!! TIP
    Use Scala's tripple quotes for the JSON for topics, partitions and offsets.

    ```text
    option(
      "startingOffsets",
      """{"topic1":{"0":5,"4":-1},"topic2":{"0":-2}}""")
    ```

## <span id="subscribe"> subscribe

[Topic subscription strategy](ConsumerStrategy.md#SubscribeStrategy) that accepts topic names as a comma-separated string, e.g.

```text
topic1,topic2,topic3
```

Exactly one topic subscription strategy is allowed (that `KafkaSourceProvider` [validates](KafkaSourceProvider.md#validateGeneralOptions) before creating `KafkaSource`).

## <span id="subscribepattern"> subscribepattern

[Topic subscription strategy](ConsumerStrategy.md#SubscribePatternStrategy) that uses Java's [java.util.regex.Pattern]({{ java.api }}/java/util/regex/Pattern.html) for the topic subscription regex pattern of topics to subscribe to, e.g.

```text
topic\d
```

!!! tip
    Use Scala's tripple quotes for the regular expression for topic subscription regex pattern.

    ```text
    option("subscribepattern", """topic\d""")
    ```

Exactly one topic subscription strategy is allowed (that `KafkaSourceProvider` [validates](KafkaSourceProvider.md#validateGeneralOptions) before creating `KafkaSource`).

## <span id="topic"> topic

Optional topic name to use for writing a streaming query

Default: `(empty)`

Unless defined, Kafka data source uses the topic names as defined in the `topic` field in the incoming data.
