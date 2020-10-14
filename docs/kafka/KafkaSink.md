# KafkaSink

`KafkaSink` is a [streaming sink](../spark-sql-streaming-Sink.md) that [KafkaSourceProvider](KafkaSourceProvider.md) registers as the `kafka` format.

```text
// start spark-shell or a Spark application with spark-sql-kafka-0-10 module
// spark-shell --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0-SNAPSHOT
import org.apache.spark.sql.SparkSession
val spark: SparkSession = ...
spark.
  readStream.
  format("text").
  load("server-logs/*.out").
  as[String].
  writeStream.
  queryName("server-logs processor").
  format("kafka").  // <-- uses KafkaSink
  option("topic", "topic1").
  option("checkpointLocation", "/tmp/kafka-sink-checkpoint"). // <-- mandatory
  start

// in another terminal
$ echo hello > server-logs/hello.out

// in the terminal with Spark
FIXME
```

## Creating Instance

`KafkaSink` takes the following when created:

* [[sqlContext]] `SQLContext`
* [[executorKafkaParams]] Kafka parameters (used on executor) as a map of `(String, Object)` pairs
* [[topic]] Optional topic name

=== [[addBatch]] `addBatch` Method

[source, scala]
----
addBatch(batchId: Long, data: DataFrame): Unit
----

Internally, `addBatch` requests `KafkaWriter` to write the input `data` to the <<topic, topic>> (if defined) or a topic in <<executorKafkaParams, executorKafkaParams>>.

NOTE: `addBatch` is a part of spark-sql-streaming-Sink.md#addBatch[Sink Contract] to "add" a batch of data to the sink.
