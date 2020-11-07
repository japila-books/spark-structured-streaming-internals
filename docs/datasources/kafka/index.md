# Kafka Data Source

**Kafka Data Source** is the streaming data source for [Apache Kafka](https://kafka.apache.org/) in Spark Structured Streaming.

Kafka Data Source provides a <<streaming-source, streaming source>> and a <<streaming-sink, streaming sink>> for <<micro-batch-stream-processing, micro-batch>> and <<continuous-stream-processing, continuous>> stream processing.

=== [[spark-sql-kafka-0-10]] spark-sql-kafka-0-10 External Module

Kafka Data Source is part of the *spark-sql-kafka-0-10* external module that is distributed with the official distribution of Apache Spark, but it is not included in the CLASSPATH by default.

You should define `spark-sql-kafka-0-10` module as part of the build definition in your Spark project, e.g. as a `libraryDependency` in `build.sbt` for sbt:

```
libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % "{{ spark.version }}"
```

For Spark environments like `spark-submit` (and "derivatives" like `spark-shell`), you should use `--packages` command-line option:

```
./bin/spark-shell --packages org.apache.spark:spark-sql-kafka-0-10_2.12:{{ spark.version }}
```

NOTE: Replace the version of `spark-sql-kafka-0-10` module (e.g. `{{ spark.version }}` above) with one of the available versions found at https://search.maven.org/search?q=a:spark-sql-kafka-0-10_2.12[The Central Repository's Search] that matches your version of Apache Spark.

=== [[streaming-source]] Streaming Source

With <<spark-sql-kafka-0-10, spark-sql-kafka-0-10 module>> you can use *kafka* data source format for loading data (reading records) from one or more Kafka topics as a streaming Dataset.

[source, scala]
----
val records = spark
  .readStream
  .format("kafka")
  .option("subscribePattern", """topic-\d{2}""") // topics with two digits at the end
  .option("kafka.bootstrap.servers", ":9092")
  .load
----

Kafka data source supports many options for reading.

Internally, the *kafka* data source format for reading is available through [KafkaSourceProvider](KafkaSourceProvider.md) that is a [MicroBatchStream](../../MicroBatchStream.md) and [ContinuousReadSupport](../../ContinuousReadSupport.md) for <<micro-batch-stream-processing, micro-batch>> and <<continuous-stream-processing, continuous>> stream processing, respectively.

=== [[schema]] Predefined (Fixed) Schema

Kafka Data Source uses a predefined (fixed) schema.

.Kafka Data Source's Fixed Schema (in the positional order)
[cols="1m,2m",options="header",width="100%"]
|===
| Name
| Type

| key
| BinaryType

| value
| BinaryType

| topic
| StringType

| partition
| IntegerType

| offset
| LongType

| timestamp
| TimestampType

| timestampType
| IntegerType

|===

[source, scala]
----
scala> records.printSchema
root
 |-- key: binary (nullable = true)
 |-- value: binary (nullable = true)
 |-- topic: string (nullable = true)
 |-- partition: integer (nullable = true)
 |-- offset: long (nullable = true)
 |-- timestamp: timestamp (nullable = true)
 |-- timestampType: integer (nullable = true)
----

Internally, the fixed schema is defined as part of the `DataSourceReader` contract through <<spark-sql-streaming-MicroBatchReader.md#, MicroBatchReader>> and <<spark-sql-streaming-ContinuousReader.md#, ContinuousReader>> extension contracts for <<micro-batch-stream-processing, micro-batch>> and <<continuous-stream-processing, continuous>> stream processing, respectively.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-DataSourceReader.html[DataSourceReader] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

[TIP]
====
Use `Column.cast` operator to cast `BinaryType` to a `StringType` (for `key` and `value` columns).

[source, scala]
----
scala> :type records
org.apache.spark.sql.DataFrame

val values = records
  .select($"value" cast "string") // deserializing values
scala> values.printSchema
root
 |-- value: string (nullable = true)
----
====

=== [[streaming-sink]] Streaming Sink

With <<spark-sql-kafka-0-10, spark-sql-kafka-0-10 module>> you can use *kafka* data source format for writing the result of executing a streaming query (a streaming Dataset) to one or more Kafka topics.

[source, scala]
----
val sq = records
  .writeStream
  .format("kafka")
  .option("kafka.bootstrap.servers", ":9092")
  .option("topic", "kafka2console-output")
  .option("checkpointLocation", "checkpointLocation-kafka2console")
  .start
----

Internally, the **kafka** data source format for writing is available through [KafkaSourceProvider](KafkaSourceProvider.md).

## Micro-Batch Stream Processing

Kafka Data Source supports [Micro-Batch Stream Processing](../../micro-batch-stream-processing.md) using [KafkaMicroBatchReader](KafkaMicroBatchReader.md).

```text
import org.apache.spark.sql.streaming.Trigger
import scala.concurrent.duration._
val sq = spark
  .readStream
  .format("kafka")
  .option("subscribepattern", "kafka2console.*")
  .option("kafka.bootstrap.servers", ":9092")
  .load
  .withColumn("value", $"value" cast "string") // deserializing values
  .writeStream
  .format("console")
  .option("truncate", false) // format-specific option
  .option("checkpointLocation", "checkpointLocation-kafka2console") // generic query option
  .trigger(Trigger.ProcessingTime(30.seconds))
  .queryName("kafka2console-microbatch")
  .start

// In the end, stop the streaming query
sq.stop
```

Kafka Data Source can assign a single task per Kafka partition (using [KafkaOffsetRangeCalculator](KafkaOffsetRangeCalculator.md) in [Micro-Batch Stream Processing](../../micro-batch-stream-processing.md)).

Kafka Data Source can reuse a Kafka consumer (using [KafkaMicroBatchReader](KafkaMicroBatchReader.md) in [Micro-Batch Stream Processing](../../micro-batch-stream-processing.md)).

## Continuous Stream Processing

Kafka Data Source supports [Continuous Stream Processing](../../continuous-stream-processing.md) using [KafkaContinuousReader](KafkaContinuousReader.md).

```text
import org.apache.spark.sql.streaming.Trigger
import scala.concurrent.duration._
val sq = spark
  .readStream
  .format("kafka")
  .option("subscribepattern", "kafka2console.*")
  .option("kafka.bootstrap.servers", ":9092")
  .load
  .withColumn("value", $"value" cast "string") // convert bytes to string for display purposes
  .writeStream
  .format("console")
  .option("truncate", false) // format-specific option
  .option("checkpointLocation", "checkpointLocation-kafka2console") // generic query option
  .queryName("kafka2console-continuous")
  .trigger(Trigger.Continuous(10.seconds))
  .start

// In the end, stop the streaming query
sq.stop
```

## <span id="options"> Configuration Options

NOTE: Options with *kafka.* prefix (e.g. <<kafka.bootstrap.servers, kafka.bootstrap.servers>>) are considered configuration properties for the Kafka consumers used on the [driver](KafkaSourceProvider.md#kafkaParamsForDriver) and [executors](KafkaSourceProvider.md#kafkaParamsForExecutors).

.Kafka Data Source's Options (Case-Insensitive)
[cols="1m,3",options="header",width="100%"]
|===
| Option
| Description

| assign
a| [[assign]] [Topic subscription strategy](ConsumerStrategy.md#AssignStrategy) that accepts a JSON with topic names and partitions, e.g.

```
{"topicA":[0,1],"topicB":[0,1]}
```

NOTE: Exactly one topic subscription strategy is allowed (that `KafkaSourceProvider` [validates](KafkaSourceProvider.md#validateGeneralOptions) before creating `KafkaSource`).

| failOnDataLoss
a| [[failOnDataLoss]] Flag to control whether...FIXME

Default: `true`

Used when `KafkaSourceProvider` is requested for [failOnDataLoss](KafkaSourceProvider.md#failOnDataLoss) configuration property

| kafka.bootstrap.servers
a| [[kafka.bootstrap.servers]] *(required)* `bootstrap.servers` configuration property of the Kafka consumers used on the driver and executors

Default: `(empty)`

| kafkaConsumer.pollTimeoutMs
a| [[kafkaConsumer.pollTimeoutMs]][[pollTimeoutMs]] The time (in milliseconds) spent waiting in `Consumer.poll` if data is not available in the buffer.

Default: `spark.network.timeout` or `120s`

Used when...FIXME

| maxOffsetsPerTrigger
a| [[maxOffsetsPerTrigger]] Number of records to fetch per trigger (to limit the number of records to fetch).

Default: `(undefined)`

Unless defined, `KafkaSource` requests [KafkaOffsetReader](KafkaSource.md#kafkaReader) for the [latest offsets](KafkaOffsetReader.md#fetchLatestOffsets).

| minPartitions
a| [[minPartitions]] Minimum number of partitions per executor (given Kafka partitions)

Default: `(undefined)`

Must be undefined (default) or greater than `0`

When undefined (default) or smaller than the number of `TopicPartitions` with records to consume from, [KafkaMicroBatchReader](KafkaMicroBatchReader.md) uses [KafkaOffsetRangeCalculator](KafkaMicroBatchReader.md#rangeCalculator) to [find the preferred executor](KafkaOffsetRangeCalculator.md#getLocation) for every `TopicPartition` (and the [available executors](KafkaMicroBatchReader.md#getSortedExecutorList)).

| startingOffsets
a| [[startingOffsets]] Starting offsets

Default: `latest`

Possible values:

* `latest`

* `earliest`

* JSON with topics, partitions and their starting offsets, e.g.
+
```
{"topicA":{"part":offset,"p1":-1},"topicB":{"0":-2}}
```

[TIP]
====
Use Scala's tripple quotes for the JSON for topics, partitions and offsets.

[source, scala]
----
option(
  "startingOffsets",
  """{"topic1":{"0":5,"4":-1},"topic2":{"0":-2}}""")
----
====

| subscribe
a| [[subscribe]] [Topic subscription strategy](ConsumerStrategy.md#SubscribeStrategy) that accepts topic names as a comma-separated string, e.g.

```
topic1,topic2,topic3
```

NOTE: Exactly one topic subscription strategy is allowed (that `KafkaSourceProvider` [validates](KafkaSourceProvider.md#validateGeneralOptions) before creating `KafkaSource`).

| subscribepattern
a| [[subscribepattern]] [Topic subscription strategy](ConsumerStrategy.md#SubscribePatternStrategy) that uses Java's http://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html[java.util.regex.Pattern] for the topic subscription regex pattern of topics to subscribe to, e.g.

```
topic\d
```

[TIP]
====
Use Scala's tripple quotes for the regular expression for topic subscription regex pattern.

[source, scala]
----
option("subscribepattern", """topic\d""")
----
====

NOTE: Exactly one topic subscription strategy is allowed (that `KafkaSourceProvider` [validates](KafkaSourceProvider.md#validateGeneralOptions) before creating `KafkaSource`).

| topic
a| [[topic]] Optional topic name to use for writing a streaming query

Default: `(empty)`

Unless defined, Kafka data source uses the topic names as defined in the `topic` field in the incoming data.
|===

## Logical Query Plan for Reading

When `DataStreamReader` is requested to load a dataset with *kafka* data source format, it creates a DataFrame with a [StreamingRelationV2](../../logical-operators/StreamingRelationV2.md) leaf logical operator.

```text
scala> records.explain(extended = true)
== Parsed Logical Plan ==
StreamingRelationV2 org.apache.spark.sql.kafka010.KafkaSourceProvider@1a366d0, kafka, Map(maxOffsetsPerTrigger -> 1, startingOffsets -> latest, subscribepattern -> topic\d, kafka.bootstrap.servers -> :9092), [key#7, value#8, topic#9, partition#10, offset#11L, timestamp#12, timestampType#13], StreamingRelation DataSource(org.apache.spark.sql.SparkSession@39b3de87,kafka,List(),None,List(),None,Map(maxOffsetsPerTrigger -> 1, startingOffsets -> latest, subscribepattern -> topic\d, kafka.bootstrap.servers -> :9092),None), kafka, [key#0, value#1, topic#2, partition#3, offset#4L, timestamp#5, timestampType#6]
...
```

## Logical Query Plan for Writing

When `DataStreamWriter` is requested to start a streaming query with *kafka* data source format for writing, it requests the `StreamingQueryManager` to [create a streaming query](../../StreamingQueryManager.md#createQuery) that in turn creates (a [StreamingQueryWrapper](../../StreamingQueryWrapper.md) with) a <<ContinuousExecution.md#, ContinuousExecution>> or a <<MicroBatchExecution.md#, MicroBatchExecution>> for <<continuous-stream-processing, continuous>> and <<micro-batch-stream-processing, micro-batch>> stream processing, respectively.

```text
scala> sq.explain(extended = true)
== Parsed Logical Plan ==
WriteToDataSourceV2 org.apache.spark.sql.execution.streaming.sources.MicroBatchWriter@bf98b73
+- Project [key#28 AS key#7, value#29 AS value#8, topic#30 AS topic#9, partition#31 AS partition#10, offset#32L AS offset#11L, timestamp#33 AS timestamp#12, timestampType#34 AS timestampType#13]
   +- Streaming RelationV2 kafka[key#28, value#29, topic#30, partition#31, offset#32L, timestamp#33, timestampType#34] (Options: [subscribepattern=kafka2console.*,kafka.bootstrap.servers=:9092])
```

## <span id="demo"> Demo: Streaming Aggregation with Kafka Data Source

Check out [Demo: Streaming Aggregation with Kafka Data Source](../../demo/kafka-data-source.md).

!!! tip
    Use the following to publish events to Kafka.

    ```
    // 1st streaming batch
    $ cat /tmp/1
    1,1,1
    15,2,1

    $ kafkacat -P -b localhost:9092 -t topic1 -l /tmp/1

    // Alternatively (and slower due to JVM bootup)
    $ cat /tmp/1 | ./bin/kafka-console-producer.sh --topic topic1 --broker-list localhost:9092
    ```
