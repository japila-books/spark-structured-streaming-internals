---
hide:
  - navigation
---

# Demo: Streaming Aggregation (with Kafka Data Source)

This demo shows a streaming query with a [streaming aggregation](../streaming-aggregation/index.md) (with [Dataset.groupBy](../operators/groupBy.md) operator) that processes data from Kafka (using [Kafka Data Source](../datasources/kafka/index.md)).

!!! note
    Please start a Kafka cluster and `spark-shell` as described in [Demo: Kafka Data Source](kafka-data-source.md).

## Reset numShufflePartitions

This step makes debugging easier since the state is only for one partition (and so it should make monitoring easier).

```scala
val numShufflePartitions = 1
import org.apache.spark.sql.internal.SQLConf.SHUFFLE_PARTITIONS
spark.sessionState.conf.setConf(SHUFFLE_PARTITIONS, numShufflePartitions)

assert(spark.sessionState.conf.numShufflePartitions == numShufflePartitions)
```

## Define Kafka Source

```scala
val events = spark
  .readStream
  .format("kafka")
  .option("subscribe", "demo.streaming-aggregation")
  .option("kafka.bootstrap.servers", ":9092")
  .load
```

## Define Streaming Aggregation Query

Define a streaming aggregation query (using [groupBy](../operators/groupBy.md) high-level operator).

The streaming query uses [Append](../OutputMode.md#Append) output mode and defines a [streaming watermark](../streaming-watermark/index.md) (using [Dataset.withWatermark](../operators/withWatermark.md) operator). Otherwise, [UnsupportedOperationChecker](../UnsupportedOperationChecker.md) would fail the query.

```scala
val ids = events
  .select($"value" cast "string")
  .withColumn("tokens", split($"value", ","))
  .withColumn("seconds", 'tokens(0) cast "long")
  .withColumn("event_time", 'seconds cast "timestamp") // <-- Event time has to be a timestamp
  .withColumn("id", 'tokens(1))
  .withColumn("batch", 'tokens(2) cast "int")
  .select("id", "batch", "seconds", "event_time")
  .withWatermark(eventTime = "event_time", delayThreshold = "10 seconds") // <-- define watermark (before groupBy!)
  .groupBy($"event_time") // group on event_time so it's included in the result
  .agg(first("seconds") as "seconds", collect_list("batch") as "batches", collect_list("id") as "ids")
```

```scala
ids.printSchema
```

```text
root
 |-- event_time: timestamp (nullable = true)
 |-- seconds: long (nullable = true)
 |-- batches: array (nullable = false)
 |    |-- element: integer (containsNull = false)
 |-- ids: array (nullable = false)
 |    |-- element: string (containsNull = false)
```

## Explain Streaming Query

Use [explain](../operators/explain.md) operator on a streaming query to know the trigger-specific values.

`ids` streaming query knows nothing about the [OutputMode](../OutputMode.md) or the current [streaming watermark](../streaming-watermark/index.md) yet:

* [OutputMode](../OutputMode.md) is defined on write side
* [Streaming watermark](../streaming-watermark/index.md) is read from rows at runtime

That's why [StatefulOperatorStateInfo](../StatefulOperatorStateInfo.md) is generic (and uses the default [Append](../OutputMode.md#Append) for output mode). And no batch-specific values are printed out. They will be available right after the first streaming batch.

=== "Scala"

    ```scala
    ids.explain
    ```

```text
== Physical Plan ==
ObjectHashAggregate(keys=[event_time#555-T10000ms], functions=[first(seconds#551L, false), collect_list(batch#566, 0, 0), collect_list(id#560, 0, 0)])
+- StateStoreSave [event_time#555-T10000ms], state info [ checkpoint = <unknown>, runId = 3fe80dc5-ebec-4ae6-bf17-82e7bdf5988c, opId = 0, ver = 0, numPartitions = 1], Append, 0, 2
   +- ObjectHashAggregate(keys=[event_time#555-T10000ms], functions=[merge_first(seconds#551L, false), merge_collect_list(batch#566, 0, 0), merge_collect_list(id#560, 0, 0)])
      +- StateStoreRestore [event_time#555-T10000ms], state info [ checkpoint = <unknown>, runId = 3fe80dc5-ebec-4ae6-bf17-82e7bdf5988c, opId = 0, ver = 0, numPartitions = 1], 2
         +- ObjectHashAggregate(keys=[event_time#555-T10000ms], functions=[merge_first(seconds#551L, false), merge_collect_list(batch#566, 0, 0), merge_collect_list(id#560, 0, 0)])
            +- Exchange hashpartitioning(event_time#555-T10000ms, 1), ENSURE_REQUIREMENTS, [id=#1047]
               +- ObjectHashAggregate(keys=[event_time#555-T10000ms], functions=[partial_first(seconds#551L, false), partial_collect_list(batch#566, 0, 0), partial_collect_list(id#560, 0, 0)])
                  +- EventTimeWatermark event_time#555: timestamp, 10 seconds
                     +- *(1) Project [tokens#548[1] AS id#560, cast(tokens#548[2] as int) AS batch#566, seconds#551L, cast(seconds#551L as timestamp) AS event_time#555]
                        +- *(1) Project [tokens#548, cast(tokens#548[0] as bigint) AS seconds#551L]
                           +- *(1) Project [split(cast(value#8 as string), ,, -1) AS tokens#548]
                              +- StreamingRelation kafka, [key#7, value#8, topic#9, partition#10, offset#11L, timestamp#12, timestampType#13]
```

## Start Streaming Query

```scala
import java.time.Clock
val timeOffset = Clock.systemUTC.instant.getEpochSecond
val queryName = s"Demo: Streaming Aggregation ($timeOffset)"
val checkpointLocation = s"/tmp/demo-checkpoint-$timeOffset"

import scala.concurrent.duration._
import org.apache.spark.sql.streaming.OutputMode.Append
import org.apache.spark.sql.streaming.Trigger
val sq = ids
  .writeStream
  .format("console")
  .option("checkpointLocation", checkpointLocation)
  .option("truncate", false)
  .outputMode(Append)
  .queryName(queryName)
  .trigger(Trigger.ProcessingTime(1.seconds))
  .start
```

The streaming query gets executed and prints out Batch 0 to the console.

```text
-------------------------------------------
Batch: 0
-------------------------------------------
+----------+-------+-------+---+
|event_time|seconds|batches|ids|
+----------+-------+-------+---+
+----------+-------+-------+---+
```

## Send Event

```console
echo "1,1,1" | kcat -P -b :9092 -t demo.streaming-aggregation
```

This will make the streaming query to print out Batch 1 to the console.

```text
-------------------------------------------
Batch: 1
-------------------------------------------
+----------+-------+-------+---+
|event_time|seconds|batches|ids|
+----------+-------+-------+---+
+----------+-------+-------+---+
```

## Monitor Query

```scala
val lastProgress = sq.lastProgress
```

```scala
assert(lastProgress.isInstanceOf[org.apache.spark.sql.streaming.StreamingQueryProgress])
assert(lastProgress.stateOperators.length == 1, "There should be one stateful operator")
```

```scala
println(lastProgress.stateOperators.head.prettyJson)
```

```text
scala> println(lastProgress.stateOperators.head.prettyJson)
{
  "operatorName" : "stateStoreSave",
  "numRowsTotal" : 1,
  "numRowsUpdated" : 0,
  "allUpdatesTimeMs" : 25,
  "numRowsRemoved" : 0,
  "allRemovalsTimeMs" : 8,
  "commitTimeMs" : 37,
  "memoryUsedBytes" : 712,
  "numRowsDroppedByWatermark" : 0,
  "numShufflePartitions" : 1,
  "numStateStoreInstances" : 1,
  "customMetrics" : {
    "loadedMapCacheHitCount" : 2,
    "loadedMapCacheMissCount" : 0,
    "stateOnCurrentVersionSizeBytes" : 408
  }
}
```

```scala
assert(lastProgress.sources.length == 1, "There should be one streaming source only")
```

```scala
println(lastProgress.sources.head.prettyJson)
```

```text
{
  "description" : "KafkaV2[Subscribe[demo.streaming-aggregation]]",
  "startOffset" : {
    "demo.streaming-aggregation" : {
      "0" : 5
    }
  },
  "endOffset" : {
    "demo.streaming-aggregation" : {
      "0" : 5
    }
  },
  "latestOffset" : {
    "demo.streaming-aggregation" : {
      "0" : 5
    }
  },
  "numInputRows" : 0,
  "inputRowsPerSecond" : 0.0,
  "processedRowsPerSecond" : 0.0,
  "metrics" : {
    "avgOffsetsBehindLatest" : "0.0",
    "maxOffsetsBehindLatest" : "0",
    "minOffsetsBehindLatest" : "0"
  }
}
```

## Cleanup

```scala
sq.stop()
```
