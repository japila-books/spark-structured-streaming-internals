# KafkaSourceProvider

`KafkaSourceProvider` is the entry point (_provider_) to the built-in Kafka support in Spark Structured Streaming (and Spark SQL).

`KafkaSourceProvider` is a `DataSourceRegister` ([Spark SQL]({{ book.spark_sql }}/DataSourceRegister)) that registers itself under the [kafka](#shortName) alias.

`KafkaSourceProvider` supports [micro-batch stream processing](../../micro-batch-execution/index.md) (through [MicroBatchStream](../../MicroBatchStream.md)) and uses a [specialized KafkaMicroBatchReader](#createMicroBatchReader).

## <span id="shortName"> Short Name (Alias)

```scala
shortName(): String
```

`shortName` is part of the [DataSourceRegister]({{ book.spark_sql }}/DataSourceRegister#shortName) abstraction.

---

`shortName` is `kafka`.

## <span id="kafkaParamsForExecutors"> Kafka Consumer Properties on Executors

```scala
kafkaParamsForExecutors(
  specifiedKafkaParams: Map[String, String],
  uniqueGroupId: String): Map[String, Object]
```

`kafkaParamsForExecutors` sets the Kafka properties for the Kafka `Consumers` on executors.

---

`kafkaParamsForExecutors` creates a `KafkaConfigUpdater` for `executor` module (with the given `specifiedKafkaParams`).

`kafkaParamsForExecutors` sets (_overrides_) the following Kafka properties explicitly (in the `KafkaConfigUpdater`).

ConsumerConfig's Key | Value | Note
---------------------|-------|------
 `key.deserializer` | `ByteArrayDeserializer` |
 `value.deserializer` | `ByteArrayDeserializer` |
 `auto.offset.reset` | `none` |
 `group.id` | `[uniqueGroupId]-executor` | `setIfUnset`
 `enable.auto.commit` | `false` |
 `receive.buffer.bytes` | `65536` | `setIfUnset`

In the end, `kafkaParamsForExecutors` requests the `KafkaConfigUpdater` to `build` a Kafka configuration.

---

`kafkaParamsForExecutors` is used when:

* `KafkaSourceProvider` is requested to [createSource](#createSource) (for a [KafkaSource](KafkaSource.md))
* `KafkaScan` is requested to [toMicroBatchStream](KafkaScan.md#toMicroBatchStream) (to create a [KafkaMicroBatchStream](KafkaMicroBatchStream.md)), and [toContinuousStream](KafkaScan.md#toContinuousStream) (for a [KafkaContinuousStream](KafkaContinuousStream.md))
* `KafkaBatch` is requested to [planInputPartitions](KafkaBatch.md#planInputPartitions) (for [KafkaBatchInputPartition](KafkaBatchInputPartition.md)s)
* `KafkaRelation` is requested to [buildScan](KafkaRelation.md#buildScan) (for a [KafkaSourceRDD](KafkaSourceRDD.md))

## <span id="batchUniqueGroupId"> Unique Group ID for Batch Queries

```scala
batchUniqueGroupId(
  params: CaseInsensitiveMap[String]): String
```

`batchUniqueGroupId` takes [GROUP_ID_PREFIX](options.md#GROUP_ID_PREFIX), if specified, or defaults to `spark-kafka-relation` prefix to build the following group ID:

```text
[groupIdPrefix]-[randomUUID]
```

---

`batchUniqueGroupId` is used when:

* `KafkaBatch` is requested to [planInputPartitions](KafkaBatch.md#planInputPartitions)
* `KafkaRelation` is requested to [build a Scan](KafkaRelation.md#buildScan)

## <span id="streamingUniqueGroupId"> Unique Group ID for Streaming Queries

```scala
streamingUniqueGroupId(
  params: CaseInsensitiveMap[String],
  metadataPath: String): String
```

`streamingUniqueGroupId` takes [GROUP_ID_PREFIX](options.md#GROUP_ID_PREFIX), if specified, or defaults to `spark-kafka-source` prefix to build the following group ID:

```text
[groupIdPrefix]-[randomUUID]-[metadataPath.hashCode]
```

---

`streamingUniqueGroupId` is used when:

* `KafkaSourceProvider` is requested to [create a Source](#createSource)
* `KafkaScan` is requested to [toMicroBatchStream](KafkaScan.md#toMicroBatchStream), [toContinuousStream](KafkaScan.md#toContinuousStream)

## Required Options

`KafkaSourceProvider` requires the following options (that you can set using `option` method of [DataStreamReader](../../DataStreamReader.md) or [DataStreamWriter](../../DataStreamWriter.md)):

* Exactly one of the following options: [subscribe](index.md#subscribe), [subscribePattern](index.md#subscribePattern) or [assign](index.md#assign)

* [kafka.bootstrap.servers](index.md#kafka.bootstrap.servers)

!!! tip
    Refer to [Kafka Data Source's Options](index.md#options) for the supported configuration options.

## <span id="getTable"> Creating KafkaTable

```scala
getTable(
  options: CaseInsensitiveStringMap): KafkaTable
```

`getTable` creates a [KafkaTable](KafkaTable.md) with the value of `includeheaders` option (default: `false`).

`getTable` is part of the `SimpleTableProvider` abstraction (Spark SQL).

## <span id="createSink"> Creating Streaming Sink

```scala
createSink(
  sqlContext: SQLContext,
  parameters: Map[String, String],
  partitionColumns: Seq[String],
  outputMode: OutputMode): Sink
```

`createSink` creates a [KafkaSink](KafkaSink.md) for `topic` option (if defined) and [Kafka Producer parameters](#kafkaParamsForProducer).

`createSink` is part of the [StreamSinkProvider](../../StreamSinkProvider.md) abstraction.

## <span id="createSource"> Creating Streaming Source

```scala
createSource(
  sqlContext: SQLContext,
  metadataPath: String,
  schema: Option[StructType],
  providerName: String,
  parameters: Map[String, String]): Source
```

`createSource` [validates stream options](#validateStreamOptions).

`createSource`...FIXME

`createSource` is part of the [StreamSourceProvider](../../StreamSourceProvider.md#createSource) abstraction.

## <span id="validateGeneralOptions"> Validating Options For Batch And Streaming Queries

```scala
validateGeneralOptions(
  parameters: Map[String, String]): Unit
```

!!! note
    Parameters are case-insensitive, i.e. `OptioN` and `option` are equal.

`validateGeneralOptions` makes sure that exactly one topic subscription strategy is used in `parameters` and can be:

* `subscribe`
* `subscribepattern`
* `assign`

`validateGeneralOptions` reports an `IllegalArgumentException` when there is no subscription strategy in use or there are more than one strategies used.

`validateGeneralOptions` makes sure that the value of subscription strategies meet the requirements:

* `assign` strategy starts with `{` (the opening curly brace)
* `subscribe` strategy has at least one topic (in a comma-separated list of topics)
* `subscribepattern` strategy has the pattern defined

`validateGeneralOptions` makes sure that `group.id` has not been specified and reports an `IllegalArgumentException` otherwise.

```text
Kafka option 'group.id' is not supported as user-specified consumer groups are not used to track offsets.
```

`validateGeneralOptions` makes sure that `auto.offset.reset` has not been specified and reports an `IllegalArgumentException` otherwise.

```text
Kafka option 'auto.offset.reset' is not supported.
Instead set the source option 'startingoffsets' to 'earliest' or 'latest' to specify where to start. Structured Streaming manages which offsets are consumed internally, rather than relying on the kafkaConsumer to do it. This will ensure that no data is missed when new topics/partitions are dynamically subscribed. Note that 'startingoffsets' only applies when a new Streaming query is started, and
that resuming will always pick up from where the query left off. See the docs for more details.
```

`validateGeneralOptions` makes sure that the following options have not been specified and reports an `IllegalArgumentException` otherwise:

* `kafka.key.deserializer`
* `kafka.value.deserializer`
* `kafka.enable.auto.commit`
* `kafka.interceptor.classes`

In the end, `validateGeneralOptions` makes sure that `kafka.bootstrap.servers` option was specified and reports an `IllegalArgumentException` otherwise.

```text
Option 'kafka.bootstrap.servers' must be specified for configuring Kafka consumer
```

`validateGeneralOptions` is used when `KafkaSourceProvider` validates options for [streaming](#validateStreamOptions) and [batch](#validateBatchOptions) queries.

## <span id="strategy"> Creating ConsumerStrategy

```scala
strategy(
  caseInsensitiveParams: Map[String, String]): ConsumerStrategy
```

`strategy` converts a key (in `caseInsensitiveParams`) to a [ConsumerStrategy](ConsumerStrategy.md).

Key      | ConsumerStrategy
---------|----------
 `assign` | [AssignStrategy](#AssignStrategy)
 `subscribe` | [SubscribeStrategy](#SubscribeStrategy)
 `subscribepattern` | [SubscribePatternStrategy](#SubscribePatternStrategy)

`strategy` is used when...FIXME

### <span id="AssignStrategy"> AssignStrategy

[AssignStrategy](ConsumerStrategy.md#AssignStrategy) with Kafka [TopicPartitions]({{ kafka.api }}/org/apache/kafka/common/TopicPartition.html)

`strategy` uses `JsonUtils.partitions` method to parse a JSON with topic names and partitions, e.g.

```text
{"topicA":[0,1],"topicB":[0,1]}
```

The topic names and partitions are mapped directly to Kafka's `TopicPartition` objects.

### <span id="SubscribeStrategy"> SubscribeStrategy

[SubscribeStrategy](ConsumerStrategy.md#SubscribeStrategy) with topic names

`strategy` extracts topic names from a comma-separated string, e.g.

```text
topic1,topic2,topic3
```

### <span id="SubscribePatternStrategy"> SubscribePatternStrategy

[SubscribePatternStrategy](ConsumerStrategy.md#SubscribePatternStrategy) with topic subscription regex pattern (that uses a Java [java.util.regex.Pattern]({{ java.api }}/java/util/regex/Pattern.html) for the pattern), e.g.

```text
topic\d
```

## <span id="sourceSchema"> Name and Schema of Streaming Source

```scala
sourceSchema(
  sqlContext: SQLContext,
  schema: Option[StructType],
  providerName: String,
  parameters: Map[String, String]): (String, StructType)
```

`sourceSchema` gives the [short name](#shortName) (i.e. `kafka`) and the [fixed schema](KafkaOffsetReader.md#kafkaSchema).

Internally, `sourceSchema` [validates Kafka options](#validateStreamOptions) and makes sure that the optional input `schema` is indeed undefined.

When the input `schema` is defined, `sourceSchema` reports a `IllegalArgumentException`.

```text
Kafka source has a fixed schema and cannot be set with a custom one
```

`sourceSchema` is part of the [StreamSourceProvider](../../StreamSourceProvider.md#sourceSchema) abstraction.

## <span id="validateStreamOptions"> Validating Kafka Options for Streaming Queries

```scala
validateStreamOptions(
  caseInsensitiveParams: Map[String, String]): Unit
```

`validateStreamOptions` makes sure that `endingoffsets` option is not used. Otherwise, `validateStreamOptions` reports a `IllegalArgumentException`.

```text
ending offset not valid in streaming queries
```

`validateStreamOptions` [validates the general options](#validateGeneralOptions).

`validateStreamOptions` is used when `KafkaSourceProvider` is requested for the [schema for Kafka source](#sourceSchema) and to [create a KafkaSource](#createSource).

## <span id="getKafkaOffsetRangeLimit"> Converting Configuration Options to KafkaOffsetRangeLimit

```scala
getKafkaOffsetRangeLimit(
  params: Map[String, String],
  offsetOptionKey: String,
  defaultOffsets: KafkaOffsetRangeLimit): KafkaOffsetRangeLimit
```

`getKafkaOffsetRangeLimit` finds the given `offsetOptionKey` in the `params` and does the following conversion:

* *latest* becomes [LatestOffsetRangeLimit](KafkaOffsetRangeLimit.md#LatestOffsetRangeLimit)

* *earliest* becomes [EarliestOffsetRangeLimit](KafkaOffsetRangeLimit.md#EarliestOffsetRangeLimit)

* A JSON-formatted text becomes [SpecificOffsetRangeLimit](KafkaOffsetRangeLimit.md#SpecificOffsetRangeLimit)

* When the given `offsetOptionKey` is not found, `getKafkaOffsetRangeLimit` returns the given `defaultOffsets`

`getKafkaOffsetRangeLimit` is used when:

* `KafkaSourceProvider` is requested to [createSource](#createSource), [createMicroBatchReader](#createMicroBatchReader), [createContinuousReader](#createContinuousReader), [createRelation](#createRelation), and [validateBatchOptions](#validateBatchOptions)

## <span id="createRelation"> Creating Fake BaseRelation

```scala
createRelation(
  sqlContext: SQLContext,
  parameters: Map[String, String]): BaseRelation
```

`createRelation`...FIXME

`createRelation` is part of the `RelationProvider` abstraction (Spark SQL).

## <span id="validateBatchOptions"> Validating Configuration Options for Batch Processing

```scala
validateBatchOptions(
  caseInsensitiveParams: Map[String, String]): Unit
```

`validateBatchOptions`...FIXME

---

`validateBatchOptions` is used when:

* `KafkaSourceProvider` is requested to [createSource](#createSource)

## <span id="failOnDataLoss"> failOnDataLoss

```scala
failOnDataLoss(
  caseInsensitiveParams: Map[String, String]): Boolean
```

`failOnDataLoss` looks up the [failOnDataLoss](options.md#failOnDataLoss) configuration property (in the given `caseInsensitiveParams`) or defaults to `true`.

---

`failOnDataLoss` is used when:

* `KafkaSourceProvider` is requested for the following:
    * [Source](#createSource) (and creates a [KafkaSource](KafkaSource.md#failOnDataLoss))
    * [BaseRelation](#createRelation) (and creates a [KafkaRelation](KafkaRelation.md#failOnDataLoss))
* `KafkaScan` is requested for the following:
    * [Batch](KafkaScan.md#toBatch) (and creates a [KafkaBatch](KafkaBatch.md#failOnDataLoss))
    * [MicroBatchStream](KafkaScan.md#toMicroBatchStream) (and creates a [KafkaMicroBatchStream](KafkaMicroBatchStream.md#failOnDataLoss))
    * [ContinuousStream](KafkaScan.md#toContinuousStream) (and creates a [KafkaContinuousStream](KafkaContinuousStream.md#failOnDataLoss))

## Utilities

### <span id="kafkaParamsForDriver"> kafkaParamsForDriver

```scala
kafkaParamsForDriver(
  specifiedKafkaParams: Map[String, String]): Map[String, Object]
```

`kafkaParamsForDriver`...FIXME

`kafkaParamsForDriver` is used when:

* `KafkaBatch` is requested to [planInputPartitions](KafkaBatch.md#planInputPartitions)
* `KafkaRelation` is requested to [buildScan](KafkaRelation.md#buildScan)
* `KafkaSourceProvider` is requested for a [streaming source](#createSource)
* `KafkaScan` is requested for a [MicroBatchStream](KafkaScan.md#toMicroBatchStream) and [ContinuousStream](KafkaScan.md#toContinuousStream)

### <span id="kafkaParamsForProducer"> Kafka Producer Parameters

```scala
kafkaParamsForProducer(
  params: CaseInsensitiveMap[String]): ju.Map[String, Object]
```

`kafkaParamsForProducer` [converts](#convertToSpecifiedParams) the given `params`.

`kafkaParamsForProducer` creates a `KafkaConfigUpdater` for `executor` module (with the converted params) and defines the two serializer-specific options to use `ByteArraySerializer`:

* `key.serializer`
* `value.serializer`

In the end, `kafkaParamsForProducer` requests the `KafkaConfigUpdater` to `build` a Kafka configuration (`Map[String, Object]`).

---

`kafkaParamsForProducer` ensures that neither `kafka.key.serializer` nor `kafka.value.serializer` are specified or throws an `IllegalArgumentException`.

```text
Kafka option 'key.serializer' is not supported as keys are serialized with ByteArraySerializer.
```

```text
Kafka option 'value.serializer' is not supported as values are serialized with ByteArraySerializer.
```

---

`kafkaParamsForProducer` is used when:

* `KafkaSourceProvider` is requested for a [streaming sink](#createSink) or [relation](#createRelation)
* `KafkaTable` is requested for a [WriteBuilder](KafkaTable.md#newWriteBuilder)

## <span id="convertToSpecifiedParams"> convertToSpecifiedParams

```scala
convertToSpecifiedParams(
  parameters: Map[String, String]): Map[String, String]
```

`convertToSpecifiedParams` finds `kafka.`-prefixed keys in the given `parameters` to drop the `kafka.` prefix and create a new parameters with a Kafka-specific configuration.

---

`convertToSpecifiedParams` is used when:

* `KafkaSourceProvider` is requested to [createSource](#createSource), [createRelation](#createRelation), [kafkaParamsForProducer](#kafkaParamsForProducer)
* `KafkaScan` is requested to [toBatch](KafkaScan.md#toBatch), [toMicroBatchStream](KafkaScan.md#toMicroBatchStream), [toContinuousStream](KafkaScan.md#toContinuousStream)

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.kafka010.KafkaSourceProvider` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.kafka010.KafkaSourceProvider=ALL
```

Refer to [Logging](../../spark-logging.md).