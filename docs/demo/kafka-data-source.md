---
hide:
  - navigation
---

# Demo: Streaming Aggregation with Kafka Data Source

The following example code shows a [streaming aggregation](../streaming-aggregation/index.md) (with [Dataset.groupBy](../operators/groupBy.md) operator) that reads records from Kafka (with [Kafka Data Source](../datasources/kafka/index.md)).

IMPORTANT: Start up Kafka cluster and spark-shell with `spark-sql-kafka-0-10` package before running the demo.

TIP: You may want to consider copying the following code to `append.txt` and using `:load append.txt` command in spark-shell to load it (rather than copying and pasting it).

```text
// START: Only for easier debugging
// The state is then only for one partition
// which should make monitoring easier
val numShufflePartitions = 1
import org.apache.spark.sql.internal.SQLConf.SHUFFLE_PARTITIONS
spark.sessionState.conf.setConf(SHUFFLE_PARTITIONS, numShufflePartitions)

assert(spark.sessionState.conf.numShufflePartitions == numShufflePartitions)
// END: Only for easier debugging

val records = spark
  .readStream
  .format("kafka")
  .option("subscribePattern", """topic-\d{2}""") // topics with two digits at the end
  .option("kafka.bootstrap.servers", ":9092")
  .load
scala> records.printSchema
root
 |-- key: binary (nullable = true)
 |-- value: binary (nullable = true)
 |-- topic: string (nullable = true)
 |-- partition: integer (nullable = true)
 |-- offset: long (nullable = true)
 |-- timestamp: timestamp (nullable = true)
 |-- timestampType: integer (nullable = true)

// Since the streaming query uses Append output mode
// it has to define a streaming event-time watermark (using Dataset.withWatermark operator)
// UnsupportedOperationChecker makes sure that the requirement holds
val ids = records
  .withColumn("tokens", split($"value", ","))
  .withColumn("seconds", 'tokens(0) cast "long")
  .withColumn("event_time", to_timestamp(from_unixtime('seconds))) // <-- Event time has to be a timestamp
  .withColumn("id", 'tokens(1))
  .withColumn("batch", 'tokens(2) cast "int")
  .withWatermark(eventTime = "event_time", delayThreshold = "10 seconds") // <-- define watermark (before groupBy!)
  .groupBy($"event_time") // <-- use event_time for grouping
  .agg(collect_list("batch") as "batches", collect_list("id") as "ids")
  .withColumn("event_time", to_timestamp($"event_time")) // <-- convert to human-readable date
scala> ids.printSchema
root
 |-- event_time: timestamp (nullable = true)
 |-- batches: array (nullable = true)
 |    |-- element: integer (containsNull = true)
 |-- ids: array (nullable = true)
 |    |-- element: string (containsNull = true)

assert(ids.isStreaming, "ids is a streaming query")

// ids knows nothing about the output mode or the current streaming watermark yet
// - Output mode is defined on writing side
// - streaming watermark is read from rows at runtime
// That's why StatefulOperatorStateInfo is generic (and uses the default Append for output mode)
// and no batch-specific values are printed out
// They will be available right after the first streaming batch
// Use explain on a streaming query to know the trigger-specific values
scala> ids.explain
== Physical Plan ==
ObjectHashAggregate(keys=[event_time#118-T10000ms], functions=[collect_list(batch#141, 0, 0), collect_list(id#129, 0, 0)])
+- StateStoreSave [event_time#118-T10000ms], state info [ checkpoint = <unknown>, runId = a870e6e2-b925-4104-9886-b211c0be1b73, opId = 0, ver = 0, numPartitions = 1], Append, 0, 2
   +- ObjectHashAggregate(keys=[event_time#118-T10000ms], functions=[merge_collect_list(batch#141, 0, 0), merge_collect_list(id#129, 0, 0)])
      +- StateStoreRestore [event_time#118-T10000ms], state info [ checkpoint = <unknown>, runId = a870e6e2-b925-4104-9886-b211c0be1b73, opId = 0, ver = 0, numPartitions = 1], 2
         +- ObjectHashAggregate(keys=[event_time#118-T10000ms], functions=[merge_collect_list(batch#141, 0, 0), merge_collect_list(id#129, 0, 0)])
            +- Exchange hashpartitioning(event_time#118-T10000ms, 1)
               +- ObjectHashAggregate(keys=[event_time#118-T10000ms], functions=[partial_collect_list(batch#141, 0, 0), partial_collect_list(id#129, 0, 0)])
                  +- EventTimeWatermark event_time#118: timestamp, interval 10 seconds
                     +- *(1) Project [cast(from_unixtime(cast(split(cast(value#8 as string), ,)[0] as bigint), yyyy-MM-dd HH:mm:ss, Some(Europe/Warsaw)) as timestamp) AS event_time#118, split(cast(value#8 as string), ,)[1] AS id#129, cast(split(cast(value#8 as string), ,)[2] as int) AS batch#141]
                        +- StreamingRelation kafka, [key#7, value#8, topic#9, partition#10, offset#11L, timestamp#12, timestampType#13]

val queryName = "ids-kafka"
val checkpointLocation = s"/tmp/checkpoint-$queryName"

// Delete the checkpoint location from previous executions
import java.nio.file.{Files, FileSystems}
import java.util.Comparator
import scala.collection.JavaConverters._
val path = FileSystems.getDefault.getPath(checkpointLocation)
if (Files.exists(path)) {
  Files.walk(path)
    .sorted(Comparator.reverseOrder())
    .iterator
    .asScala
    .foreach(p => p.toFile.delete)
}

// The following make for an easier demo
// Kafka cluster is supposed to be up at this point
// Make sure that a Kafka topic is available, e.g. topic-00
// Use ./bin/kafka-console-producer.sh --broker-list :9092 --topic topic-00
// And send a record, e.g. 1,1,1

// Define the output mode
// and start the query
import scala.concurrent.duration._
import org.apache.spark.sql.streaming.OutputMode.Append
import org.apache.spark.sql.streaming.Trigger
val streamingQuery = ids
  .writeStream
  .format("console")
  .option("truncate", false)
  .option("checkpointLocation", checkpointLocation)
  .queryName(queryName)
  .outputMode(Append)
  .start

val lastProgress = streamingQuery.lastProgress
scala> :type lastProgress
org.apache.spark.sql.streaming.StreamingQueryProgress

assert(lastProgress.stateOperators.length == 1, "There should be one stateful operator")

scala> println(lastProgress.stateOperators.head.prettyJson)
{
  "numRowsTotal" : 1,
  "numRowsUpdated" : 0,
  "memoryUsedBytes" : 742,
  "customMetrics" : {
    "loadedMapCacheHitCount" : 1,
    "loadedMapCacheMissCount" : 1,
    "stateOnCurrentVersionSizeBytes" : 374
  }
}

assert(lastProgress.sources.length == 1, "There should be one streaming source only")
scala> println(lastProgress.sources.head.prettyJson)
{
  "description" : "KafkaV2[SubscribePattern[topic-\\d{2}]]",
  "startOffset" : {
    "topic-00" : {
      "0" : 1
    }
  },
  "endOffset" : {
    "topic-00" : {
      "0" : 1
    }
  },
  "numInputRows" : 0,
  "inputRowsPerSecond" : 0.0,
  "processedRowsPerSecond" : 0.0
}

// Eventually...
streamingQuery.stop()
```
