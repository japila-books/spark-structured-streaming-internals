# KafkaRelation

`KafkaRelation` is a `BaseRelation` ([Spark SQL]({{ book.spark_sql }}/BaseRelation)).

`KafkaRelation` is a `TableScan` ([Spark SQL]({{ book.spark_sql }}/TableScan)).

`KafkaRelation` is <<creating-instance, created>> exclusively when `KafkaSourceProvider` is requested to [create a BaseRelation](KafkaSourceProvider.md#createRelation).

[[options]]
.KafkaRelation's Options
[cols="1m,3",options="header",width="100%"]
|===
| Name
| Description

| kafkaConsumer.pollTimeoutMs
a| [[kafkaConsumer.pollTimeoutMs]][[pollTimeoutMs]]

Default: `spark.network.timeout` configuration if set or `120s`

|===

[[logging]]
[TIP]
====
Enable `ALL` logging levels for `org.apache.spark.sql.kafka010.KafkaRelation` to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.kafka010.KafkaRelation=ALL
```

Refer to <<spark-sql-streaming-spark-logging.md#, Logging>>.
====

=== [[creating-instance]] Creating KafkaRelation Instance

`KafkaRelation` takes the following when created:

* [[sqlContext]] `SQLContext`
* [[strategy]] [ConsumerStrategy](ConsumerStrategy.md)
* [[sourceOptions]] `Source` options (`Map[String, String]`)
* [[specifiedKafkaParams]] User-defined Kafka parameters (`Map[String, String]`)
* [[failOnDataLoss]] `failOnDataLoss` flag
* [[startingOffsets]] [Starting offsets](KafkaOffsetRangeLimit.md)
* [[endingOffsets]] [Ending offsets](KafkaOffsetRangeLimit.md)

=== [[getPartitionOffsets]] `getPartitionOffsets` Internal Method

[source, scala]
----
getPartitionOffsets(
  kafkaReader: KafkaOffsetReader,
  kafkaOffsets: KafkaOffsetRangeLimit): Map[TopicPartition, Long]
----

CAUTION: FIXME

NOTE: `getPartitionOffsets` is used exclusively when `KafkaRelation` <<buildScan, builds RDD of rows (from the tuples)>>.

=== [[buildScan]] Building Distributed Data Scan with Column Pruning -- `buildScan` Method

[source, scala]
----
buildScan(): RDD[Row]
----

NOTE: `buildScan` is part of the `TableScan` ([Spark SQL]({{ book.spark_sql }}/TableScan/#buildScan)) contract to build a distributed data scan with column pruning.

`buildScan` generates a unique group ID of the format *spark-kafka-relation-[randomUUID]* (to make sure that a streaming query creates a new consumer group).

`buildScan` creates a [KafkaOffsetReader](KafkaOffsetReader.md) with the following:

* The given <<strategy, ConsumerStrategy>> and the <<sourceOptions, source options>>

* [Kafka parameters for the driver](KafkaSourceProvider.md#kafkaParamsForDriver) based on the given <<specifiedKafkaParams, specifiedKafkaParams>>

* *spark-kafka-relation-[randomUUID]-driver* for the `driverGroupIdPrefix`

`buildScan` uses the `KafkaOffsetReader` to <<getPartitionOffsets, getPartitionOffsets>> for the starting and ending offsets (based on the given <<startingOffsets, KafkaOffsetRangeLimit>> and the <<endingOffsets, KafkaOffsetRangeLimit>>, respectively). `buildScan` requests the `KafkaOffsetReader` to [close](KafkaOffsetReader.md#close) afterwards.

`buildScan` creates offset ranges (that are a collection of `KafkaSourceRDDOffsetRanges` with a Kafka `TopicPartition`, beginning and ending offsets and undefined preferred location).

`buildScan` prints out the following INFO message to the logs:

```
Generating RDD of offset ranges: [offsetRanges]
```

`buildScan` creates a [KafkaSourceRDD](KafkaSourceRDD.md) with the following:

* [Kafka parameters for executors](KafkaSourceProvider.md#kafkaParamsForExecutors) based on the given <<specifiedKafkaParams, specifiedKafkaParams>> and the unique group ID (`spark-kafka-relation-[randomUUID]`)

* The offset ranges created

* <<pollTimeoutMs, pollTimeoutMs>> configuration

* The given <<failOnDataLoss, failOnDataLoss>> flag

* `reuseKafkaConsumer` flag off (`false`)

`buildScan` requests the `KafkaSourceRDD` to `map` Kafka `ConsumerRecords` to `InternalRows`.

In the end, `buildScan` requests the <<sqlContext, SQLContext>> to create a `DataFrame` (with the name *kafka* and the predefined <<schema, schema>>) that is immediately converted to a `RDD[InternalRow]`.

`buildScan` throws a `IllegalStateException` when...FIXME

```
different topic partitions for starting offsets topics[[fromTopics]] and ending offsets topics[[untilTopics]]
```

`buildScan` throws a `IllegalStateException` when...FIXME

```
[tp] doesn't have a from offset
```
