# Kafka Options

Options with `kafka.` prefix (e.g. [kafka.bootstrap.servers](#kafka.bootstrap.servers)) are considered configuration properties for the Kafka consumers used on the [driver](KafkaSourceProvider.md#kafkaParamsForDriver) and [executors](KafkaSourceProvider.md#kafkaParamsForExecutors).

Kafka options are defined as part of [KafkaSourceProvider](KafkaSourceProvider.md).

## <span id="assign"><span id="ASSIGN"> assign

[Topic subscription strategy](ConsumerStrategy.md#AssignStrategy) that accepts a JSON with topic names and partitions, e.g.

```text
{"topicA":[0,1],"topicB":[0,1]}
```

Exactly one topic subscription strategy is allowed (that `KafkaSourceProvider` [validates](KafkaSourceProvider.md#validateGeneralOptions) before creating `KafkaSource`).

## <span id="endingOffsets"><span id="ENDING_OFFSETS_OPTION_KEY"> endingOffsets

## <span id="endingTimestamp"><span id="ENDING_TIMESTAMP_OPTION_KEY"> endingTimestamp

## <span id="endingOffsetsByTimestamp"><span id="ENDING_OFFSETS_BY_TIMESTAMP_OPTION_KEY"> endingOffsetsByTimestamp

## <span id="failOnDataLoss"><span id="FAIL_ON_DATA_LOSS_OPTION_KEY"> failOnDataLoss

Default: `true`

Used when:

* `KafkaSourceProvider` is requested for [failOnDataLoss](KafkaSourceProvider.md#failOnDataLoss)

## <span id="fetchOffset.numRetries"><span id="FETCH_OFFSET_NUM_RETRY"> fetchOffset.numRetries

## <span id="fetchOffset.retryIntervalMs"><span id="FETCH_OFFSET_RETRY_INTERVAL_MS"> fetchOffset.retryIntervalMs

## <span id="groupIdPrefix"><span id="GROUP_ID_PREFIX"> groupIdPrefix

## <span id="includeHeaders"><span id="INCLUDE_HEADERS"> includeHeaders

Default: `false`

## <span id="kafka.bootstrap.servers"> kafka.bootstrap.servers

**(required)** `bootstrap.servers` configuration property of the Kafka consumers used on the driver and executors

Default: `(empty)`

## <span id="kafkaConsumer.pollTimeoutMs"><span id="pollTimeoutMs"><span id="CONSUMER_POLL_TIMEOUT"> kafkaConsumer.pollTimeoutMs

The time (in milliseconds) spent waiting in `Consumer.poll` if data is not available in the buffer.

Default: `spark.network.timeout` or `120s`

## <span id="maxOffsetsPerTrigger"><span id="MAX_OFFSET_PER_TRIGGER"> maxOffsetsPerTrigger

Number of records to fetch per trigger (to limit the number of records to fetch).

Default: `(undefined)`

Unless defined, `KafkaSource` requests [KafkaOffsetReader](KafkaSource.md#kafkaReader) for the [latest offsets](KafkaOffsetReader.md#fetchLatestOffsets).

## <span id="maxTriggerDelay"><span id="MAX_TRIGGER_DELAY"> maxTriggerDelay

Default: `15m`

[Ignored in batch queries](KafkaSourceProvider.md#validateBatchOptions)

Used when:

* `KafkaMicroBatchStream` is requested for [maxTriggerDelayMs](KafkaMicroBatchStream.md#maxTriggerDelayMs)
* `KafkaSource` is requested for [maxTriggerDelayMs](KafkaSource.md#maxTriggerDelayMs)

## <span id="minOffsetsPerTrigger"><span id="MIN_OFFSET_PER_TRIGGER"> minOffsetsPerTrigger

## <span id="minPartitions"><span id="MIN_PARTITIONS_OPTION_KEY"> minPartitions

Minimum number of partitions per executor (given Kafka partitions)

Default: `(undefined)`

Must be undefined (default) or greater than `0`

When undefined (default) or smaller than the number of `TopicPartitions` with records to consume from, [KafkaMicroBatchReader](KafkaMicroBatchReader.md) uses [KafkaOffsetRangeCalculator](KafkaMicroBatchReader.md#rangeCalculator) to [find the preferred executor](KafkaOffsetRangeCalculator.md#getLocation) for every `TopicPartition` (and the [available executors](KafkaMicroBatchReader.md#getSortedExecutorList)).

## <span id="startingOffsets"><span id="STARTING_OFFSETS_OPTION_KEY"> startingOffsets

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

## <span id="startingOffsetsByTimestamp"><span id="STARTING_OFFSETS_BY_TIMESTAMP_OPTION_KEY"> startingOffsetsByTimestamp

## <span id="startingTimestamp"><span id="STARTING_TIMESTAMP_OPTION_KEY"> startingTimestamp

## <span id="subscribe"><span id="SUBSCRIBE"> subscribe

[Topic subscription strategy](ConsumerStrategy.md#SubscribeStrategy) that accepts topic names as a comma-separated string, e.g.

```text
topic1,topic2,topic3
```

Exactly one topic subscription strategy is allowed (that `KafkaSourceProvider` [validates](KafkaSourceProvider.md#validateGeneralOptions) before creating `KafkaSource`).

## <span id="subscribePattern"><span id="SUBSCRIBE_PATTERN"> subscribePattern

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

## <span id="topic"><span id="TOPIC_OPTION_KEY"> topic

Optional topic name to use for writing the result of a streaming query to

Default: `(empty)`

Unless defined, Kafka data source uses the topic names as defined in the `topic` field in the dataset
