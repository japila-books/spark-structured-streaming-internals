# KafkaSourceProvider

`KafkaSourceProvider` is a `DataSourceRegister` that registers **kafka** data source alias.

!!! tip "The Internals of Spark SQL"
    Read up on [DataSourceRegister](https://jaceklaskowski.github.io/mastering-spark-sql-book/spark-sql-DataSourceRegister) in [The Internals of Spark SQL](https://jaceklaskowski.github.io/mastering-spark-sql-book/) book.

`KafkaSourceProvider` supports [micro-batch stream processing](../micro-batch-stream-processing.md) (through [MicroBatchReadSupport](../spark-sql-streaming-MicroBatchReadSupport.md)) and uses a [specialized KafkaMicroBatchReader](#createMicroBatchReader).

## Properties of Kafka Consumers on Executors

ConsumerConfig's Key | Value
---------------------|----------
 KEY_DESERIALIZER_CLASS_CONFIG | ByteArrayDeserializer
 VALUE_DESERIALIZER_CLASS_CONFIG | ByteArrayDeserializer
 AUTO_OFFSET_RESET_CONFIG | none
 GROUP_ID_CONFIG | [uniqueGroupId](#uniqueGroupId)-executor
 ENABLE_AUTO_COMMIT_CONFIG | false
 RECEIVE_BUFFER_CONFIG | 65536

## Required Options

`KafkaSourceProvider` requires the following options (that you can set using `option` method of [DataStreamReader](../spark-sql-streaming-DataStreamReader.md) or [DataStreamWriter](../DataStreamWriter.md)):

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

`createSink` is part of the [StreamSinkProvider](../StreamSinkProvider.md) abstraction.

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

`createSource` is part of the [StreamSourceProvider](../StreamSourceProvider.md#createSource) abstraction.

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

[AssignStrategy](ConsumerStrategy.md#AssignStrategy) with Kafka [TopicPartitions]({{ kafka.doc }}/org/apache/kafka/common/TopicPartition.html)

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

[SubscribePatternStrategy](ConsumerStrategy.md#SubscribePatternStrategy) with topic subscription regex pattern (that uses a Java [java.util.regex.Pattern]({{ java.doc }}/java/util/regex/Pattern.html) for the pattern), e.g.

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

`sourceSchema` gives the <<shortName, short name>> (i.e. `kafka`) and the spark-sql-streaming-KafkaOffsetReader.md#kafkaSchema[fixed schema].

Internally, `sourceSchema` <<validateStreamOptions, validates Kafka options>> and makes sure that the optional input `schema` is indeed undefined.

When the input `schema` is defined, `sourceSchema` reports a `IllegalArgumentException`.

```text
Kafka source has a fixed schema and cannot be set with a custom one
```

`sourceSchema` is part of the [StreamSourceProvider](../StreamSourceProvider.md#sourceSchema) abstraction.

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

* *latest* becomes <<spark-sql-streaming-KafkaOffsetRangeLimit.md#LatestOffsetRangeLimit, LatestOffsetRangeLimit>>

* *earliest* becomes <<spark-sql-streaming-KafkaOffsetRangeLimit.md#EarliestOffsetRangeLimit, EarliestOffsetRangeLimit>>

* A JSON-formatted text becomes <<spark-sql-streaming-KafkaOffsetRangeLimit.md#SpecificOffsetRangeLimit, SpecificOffsetRangeLimit>>

* When the given `offsetOptionKey` is not found, `getKafkaOffsetRangeLimit` returns the given `defaultOffsets`

`getKafkaOffsetRangeLimit` is used when `KafkaSourceProvider` is requested to <<createSource, createSource>>, <<createMicroBatchReader, createMicroBatchReader>>, <<createContinuousReader, createContinuousReader>>, <<createRelation, createRelation>>, and <<validateBatchOptions, validateBatchOptions>>.

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

`validateBatchOptions` is used when `KafkaSourceProvider` is requested to [createSource](#createSource).

## Configuration Properties

### <span id="failOnDataLoss"> failOnDataLoss

```scala
failOnDataLoss(
  caseInsensitiveParams: Map[String, String]): Boolean
```

`failOnDataLoss` looks up the `failOnDataLoss` configuration property (in the `caseInsensitiveParams`) or defaults to `true`.

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

### <span id="kafkaParamsForExecutors"> kafkaParamsForExecutors

```scala
kafkaParamsForExecutors(
  specifiedKafkaParams: Map[String, String],
  uniqueGroupId: String): Map[String, Object]
```

`kafkaParamsForExecutors` sets the Kafka properties for executors.

While setting the properties, `kafkaParamsForExecutors` prints out the following DEBUG message to the logs:

```text
executor: Set [key] to [value], earlier value: [value]
```

`kafkaParamsForExecutors` is used when:

* `KafkaSourceProvider` is requested to <<createSource, createSource>> (for a <<spark-sql-streaming-KafkaSource.md#, KafkaSource>>), <<createMicroBatchReader, createMicroBatchReader>> (for a <<spark-sql-streaming-KafkaMicroBatchReader.md#, KafkaMicroBatchReader>>), and <<createContinuousReader, createContinuousReader>> (for a <<spark-sql-streaming-KafkaContinuousReader.md#, KafkaContinuousReader>>)
* `KafkaRelation` is requested to [buildScan](KafkaRelation.md#buildScan) (for a `KafkaSourceRDD`)

### <span id="kafkaParamsForProducer"> Kafka Producer Parameters

```scala
kafkaParamsForProducer(
  params: CaseInsensitiveMap[String]): ju.Map[String, Object]
```

`kafkaParamsForProducer`...FIXME

`kafkaParamsForProducer` is used when:

* `KafkaSourceProvider` is requested for a [streaming sink](#createSink) or [relation](#createRelation)
* `KafkaTable` is requested for a [WriteBuilder](KafkaTable.md#newWriteBuilder)

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.kafka010.KafkaSourceProvider` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.kafka010.KafkaSourceProvider=ALL
```

Refer to [Logging](../spark-logging.md).