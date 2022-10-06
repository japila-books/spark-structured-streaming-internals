---
hide:
  - navigation
---

# Demo: Streaming Aggregation (with Kafka Data Source)

This demo shows a [streaming aggregation](../streaming-aggregation/index.md) (with [Dataset.groupBy](../operators/groupBy.md) operator) that reads records from Kafka (using [Kafka Data Source](../datasources/kafka/index.md)).

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

Since the streaming query uses [Append](../OutputMode.md#Append) output mode, it has to define a streaming event-time watermark (using [Dataset.withWatermark](../operators/withWatermark.md) operator).

[UnsupportedOperationChecker](../UnsupportedOperationChecker.md) makes sure that the requirement holds.

Define a streaming aggregation query (using [groupBy](../operators/groupBy.md) high-level operator).

```scala
val ids = events
  .withColumn("tokens", split($"value", ","))
  .withColumn("seconds", 'tokens(0) cast "long")
  .withColumn("event_time", 'seconds cast "timestamp") // <-- Event time has to be a timestamp
  .withColumn("id", 'tokens(1))
  .withColumn("batch", 'tokens(2) cast "int")
  .withWatermark(eventTime = "event_time", delayThreshold = "10 seconds") // <-- define watermark (before groupBy!)
  .groupBy($"event_time") // <-- use event_time for grouping
  .agg(collect_list("batch") as "batches", collect_list("id") as "ids")
  .withColumn("event_time", to_timestamp($"event_time")) // <-- convert to human-readable date
```

```text
scala> ids.printSchema
root
 |-- event_time: timestamp (nullable = true)
 |-- batches: array (nullable = false)
 |    |-- element: integer (containsNull = false)
 |-- ids: array (nullable = false)
 |    |-- element: string (containsNull = false)
```

`ids` streaming query knows nothing about the [output mode](../OutputMode.md) or the current streaming watermark yet:

- Output mode is defined on writing side
- streaming watermark is read from rows at runtime

That's why [StatefulOperatorStateInfo](../StatefulOperatorStateInfo.md) is generic (and uses the default [Append](../OutputMode.md#Append) for output mode). And no batch-specific values are printed out. They will be available right after the first streaming batch.

## Explain Streaming Query

Use [explain](../operators/explain.md) operator on a streaming query to know the trigger-specific values.

```text
scala> ids.explain
== Physical Plan ==
ObjectHashAggregate(keys=[event_time#145-T10000ms], functions=[collect_list(batch#168, 0, 0), collect_list(id#156, 0, 0)])
+- StateStoreSave [event_time#145-T10000ms], state info [ checkpoint = <unknown>, runId = 4ef726e3-5249-47d4-b215-41702d02efdc, opId = 0, ver = 0, numPartitions = 1], Append, 0, 2
   +- ObjectHashAggregate(keys=[event_time#145-T10000ms], functions=[merge_collect_list(batch#168, 0, 0), merge_collect_list(id#156, 0, 0)])
      +- StateStoreRestore [event_time#145-T10000ms], state info [ checkpoint = <unknown>, runId = 4ef726e3-5249-47d4-b215-41702d02efdc, opId = 0, ver = 0, numPartitions = 1], 2
         +- ObjectHashAggregate(keys=[event_time#145-T10000ms], functions=[merge_collect_list(batch#168, 0, 0), merge_collect_list(id#156, 0, 0)])
            +- Exchange hashpartitioning(event_time#145-T10000ms, 1), ENSURE_REQUIREMENTS, [id=#135]
               +- ObjectHashAggregate(keys=[event_time#145-T10000ms], functions=[partial_collect_list(batch#168, 0, 0), partial_collect_list(id#156, 0, 0)])
                  +- EventTimeWatermark event_time#145: timestamp, 10 seconds
                     +- *(1) Project [cast(cast(tokens#126[0] as bigint) as timestamp) AS event_time#145, tokens#126[1] AS id#156, cast(tokens#126[2] as int) AS batch#168]
                        +- *(1) Project [split(cast(value#113 as string), ,, -1) AS tokens#126]
                           +- StreamingRelation kafka, [key#112, value#113, topic#114, partition#115, offset#116L, timestamp#117, timestampType#118]
```

## Start Streaming Query

```scala
import java.time.Clock
val timeOffset = Clock.systemUTC.instant.getEpochSecond
val queryName = s"Demo: Streaming Aggregation ($timeOffset)"
val checkpointLocation = s"/tmp/demo-checkpoint-$timeOffset"
```

```scala
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

## Send Event

```console
echo "1,1,1" | kcat -P -b :9092 -t demo.streaming-aggregation
```

## Monitor Query

```scala
val lastProgress = sq.lastProgress
```

```scala
assert(lastProgress.isInstanceOf[org.apache.spark.sql.streaming.StreamingQueryProgress])
assert(lastProgress.stateOperators.length == 1, "There should be one stateful operator")
```

```text
scala> println(lastProgress.stateOperators.head.prettyJson)
{
  "operatorName" : "stateStoreSave",
  "numRowsTotal" : 0,
  "numRowsUpdated" : 0,
  "allUpdatesTimeMs" : 74,
  "numRowsRemoved" : 0,
  "allRemovalsTimeMs" : 3,
  "commitTimeMs" : 48,
  "memoryUsedBytes" : 240,
  "numRowsDroppedByWatermark" : 0,
  "numShufflePartitions" : 1,
  "numStateStoreInstances" : 1,
  "customMetrics" : {
    "loadedMapCacheHitCount" : 0,
    "loadedMapCacheMissCount" : 0,
    "stateOnCurrentVersionSizeBytes" : 96
  }
}
```

```scala
assert(lastProgress.sources.length == 1, "There should be one streaming source only")
```

```text
scala> println(lastProgress.sources.head.prettyJson)
{
  "description" : "KafkaV2[Subscribe[demo.streaming-aggregation]]",
  "startOffset" : {
    "demo.streaming-aggregation" : {
      "0" : 1
    }
  },
  "endOffset" : {
    "demo.streaming-aggregation" : {
      "0" : 1
    }
  },
  "latestOffset" : {
    "demo.streaming-aggregation" : {
      "0" : 1
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
