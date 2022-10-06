---
hide:
  - navigation
---

# Demo: Streaming Aggregation

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

## Load Events from Kafka

```scala
val events = spark
  .readStream
  .format("kafka")
  .option("subscribe", "demo.streaming-aggregation")
  .option("kafka.bootstrap.servers", ":9092")
  .load
  .select($"value" cast "string")
  .withColumn("tokens", split($"value", ","))
  .withColumn("id", 'tokens(0))
  .withColumn("v", 'tokens(1) cast "int")
  .withColumn("second", 'tokens(2) cast "long")
  .withColumn("event_time", 'second cast "timestamp") // <-- Event time has to be a timestamp
  .select("id", "v", "second", "event_time")
```

!!! note "FIXME Consider JSON format for values"
    JSONified values would make more sense. It'd certainly make the demo more verbose (extra JSON-specific "things") but perhaps would ease building a connection between events on the command line and their DataFrame representation.

## Define Windowed Streaming Aggregation

Define a streaming aggregation query (using [groupBy](../operators/groupBy.md) high-level operator).

The streaming query uses [Append](../OutputMode.md#Append) output mode and defines a [streaming watermark](../streaming-watermark/index.md) (using [Dataset.withWatermark](../operators/withWatermark.md) operator). Otherwise, [UnsupportedOperationChecker](../UnsupportedOperationChecker.md) would fail the query.

```scala
val windowed = events
  .withWatermark(eventTime = "event_time", delayThreshold = "10 seconds")
  .groupBy(
    $"id",
    window(
      timeColumn = $"event_time",
      windowDuration = "5 seconds"))
  .agg(
    collect_list("v") as "vs",
    collect_list("second") as "seconds")
```

```scala
windowed.printSchema
```

```text
root
 |-- id: string (nullable = true)
 |-- window: struct (nullable = false)
 |    |-- start: timestamp (nullable = true)
 |    |-- end: timestamp (nullable = true)
 |-- vs: array (nullable = false)
 |    |-- element: integer (containsNull = false)
 |-- seconds: array (nullable = false)
 |    |-- element: long (containsNull = false)
```

## Explain Streaming Query

Use [explain](../operators/explain.md) operator on a streaming query to know the trigger-specific values.

`ids` streaming query knows nothing about the [OutputMode](../OutputMode.md) or the current [streaming watermark](../streaming-watermark/index.md) yet:

* [OutputMode](../OutputMode.md) is defined on write side
* [Streaming watermark](../streaming-watermark/index.md) is read from rows at runtime

That's why [StatefulOperatorStateInfo](../StatefulOperatorStateInfo.md) is generic (and uses the default [Append](../OutputMode.md#Append) for output mode). And no batch-specific values are printed out. They will be available right after the first streaming batch.

```scala
windowed.explain
```

```text
== Physical Plan ==
ObjectHashAggregate(keys=[id#26, window#66-T10000ms], functions=[collect_list(v#30, 0, 0), collect_list(second#35L, 0, 0)])
+- StateStoreSave [id#26, window#66-T10000ms], state info [ checkpoint = <unknown>, runId = f48be620-90b4-4103-a959-def26a434ea8, opId = 0, ver = 0, numPartitions = 1], Append, 0, 2
   +- ObjectHashAggregate(keys=[id#26, window#66-T10000ms], functions=[merge_collect_list(v#30, 0, 0), merge_collect_list(second#35L, 0, 0)])
      +- StateStoreRestore [id#26, window#66-T10000ms], state info [ checkpoint = <unknown>, runId = f48be620-90b4-4103-a959-def26a434ea8, opId = 0, ver = 0, numPartitions = 1], 2
         +- ObjectHashAggregate(keys=[id#26, window#66-T10000ms], functions=[merge_collect_list(v#30, 0, 0), merge_collect_list(second#35L, 0, 0)])
            +- Exchange hashpartitioning(id#26, window#66-T10000ms, 1), ENSURE_REQUIREMENTS, [id=#75]
               +- ObjectHashAggregate(keys=[id#26, window#66-T10000ms], functions=[partial_collect_list(v#30, 0, 0), partial_collect_list(second#35L, 0, 0)])
                  +- *(2) Project [named_struct(start, precisetimestampconversion(((precisetimestampconversion(event_time#41-T10000ms, TimestampType, LongType) - (((precisetimestampconversion(event_time#41-T10000ms, TimestampType, LongType) - 0) + 5000000) % 5000000)) - 0), LongType, TimestampType), end, precisetimestampconversion((((precisetimestampconversion(event_time#41-T10000ms, TimestampType, LongType) - (((precisetimestampconversion(event_time#41-T10000ms, TimestampType, LongType) - 0) + 5000000) % 5000000)) - 0) + 5000000), LongType, TimestampType)) AS window#66-T10000ms, id#26, v#30, second#35L]
                     +- *(2) Filter isnotnull(event_time#41-T10000ms)
                        +- EventTimeWatermark event_time#41: timestamp, 10 seconds
                           +- *(1) Project [id#26, v#30, second#35L, cast(second#35L as timestamp) AS event_time#41]
                              +- *(1) Project [tokens#23[0] AS id#26, cast(tokens#23[1] as int) AS v#30, cast(tokens#23[2] as bigint) AS second#35L]
                                 +- *(1) Project [split(cast(value#8 as string), ,, -1) AS tokens#23]
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
val sq = windowed
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
+---+------+---+-------+
|id |window|vs |seconds|
+---+------+---+-------+
+---+------+---+-------+
```

### Start Diagnostic Query

```scala
import java.time.Clock
val timeOffset = Clock.systemUTC.instant.getEpochSecond
val queryName = s"Diagnostic Query ($timeOffset)"
val checkpointLocation_diag = s"/tmp/demo-checkpoint-$timeOffset"

import scala.concurrent.duration._
import org.apache.spark.sql.streaming.OutputMode.Append
import org.apache.spark.sql.streaming.Trigger
events
  .writeStream
  .format("console")
  .option("checkpointLocation", checkpointLocation_diag)
  .option("truncate", false)
  .queryName(queryName)
  .trigger(Trigger.ProcessingTime(1.seconds))
  .start
```

The query immediately prints out the following Batch 0.

```text
-------------------------------------------
Batch: 0
-------------------------------------------
+---+---+------+----------+
|id |v  |second|event_time|
+---+---+------+----------+
+---+---+------+----------+
```

## Send Events

The window duration is `5 seconds` with a delay of `10 seconds` so it really takes 15 seconds to start getting final results (_materialization_).

```console
echo "1,1,1" | kcat -P -b :9092 -t demo.streaming-aggregation
```

This will make the streaming query to print out Batch 1 to the console.

```text
-------------------------------------------
Batch: 1
-------------------------------------------
+---+------+---+-------+
|id |window|vs |seconds|
+---+------+---+-------+
+---+------+---+-------+
```

Use `6` as the event time (that is a second after `5 seconds` window duration).

```console
echo "1,2,6" | kcat -P -b :9092 -t demo.streaming-aggregation
```

There should be no final result printed out yet (just an empty Batch 2).

```text
-------------------------------------------
Batch: 2
-------------------------------------------
+---+------+---+-------+
|id |window|vs |seconds|
+---+------+---+-------+
+---+------+---+-------+
```

Use `16` as the event time (that is a second after `5 seconds` window duration and `10 seconds` delay).

```console
echo "1,3,16" | kcat -P -b :9092 -t demo.streaming-aggregation
```

That should produce the first final result (as Batch 4).

```text
-------------------------------------------
Batch: 3
-------------------------------------------
+---+------+---+-------+
|id |window|vs |seconds|
+---+------+---+-------+
+---+------+---+-------+

-------------------------------------------
Batch: 4
-------------------------------------------
+---+------------------------------------------+---+-------+
|id |window                                    |vs |seconds|
+---+------------------------------------------+---+-------+
|1  |{1970-01-01 01:00:00, 1970-01-01 01:00:05}|[1]|[1]    |
+---+------------------------------------------+---+-------+
```

## Monitor Query

```scala
val lastProgress = sq.lastProgress
```

```scala
println(lastProgress)
```

```text
{
  "id" : "8b27e38c-a73c-49db-8bfd-2d213d11b8a2",
  "runId" : "4d3887d9-33e8-43f5-b0f9-d815d06bf039",
  "name" : "Demo: Streaming Aggregation (1665063267)",
  "timestamp" : "2022-10-06T13:37:39.005Z",
  "batchId" : 5,
  "numInputRows" : 0,
  "inputRowsPerSecond" : 0.0,
  "processedRowsPerSecond" : 0.0,
  "durationMs" : {
    "latestOffset" : 3,
    "triggerExecution" : 3
  },
  "eventTime" : {
    "watermark" : "1970-01-01T00:00:06.000Z"
  },
  "stateOperators" : [ {
    "operatorName" : "stateStoreSave",
    "numRowsTotal" : 2,
    "numRowsUpdated" : 0,
    "allUpdatesTimeMs" : 6,
    "numRowsRemoved" : 1,
    "allRemovalsTimeMs" : 21,
    "commitTimeMs" : 26,
    "memoryUsedBytes" : 1504,
    "numRowsDroppedByWatermark" : 0,
    "numShufflePartitions" : 1,
    "numStateStoreInstances" : 1,
    "customMetrics" : {
      "loadedMapCacheHitCount" : 8,
      "loadedMapCacheMissCount" : 0,
      "stateOnCurrentVersionSizeBytes" : 784
    }
  } ],
  "sources" : [ {
    "description" : "KafkaV2[Subscribe[demo.streaming-aggregation]]",
    "startOffset" : {
      "demo.streaming-aggregation" : {
        "0" : 30
      }
    },
    "endOffset" : {
      "demo.streaming-aggregation" : {
        "0" : 30
      }
    },
    "latestOffset" : {
      "demo.streaming-aggregation" : {
        "0" : 30
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
  } ],
  "sink" : {
    "description" : "org.apache.spark.sql.execution.streaming.ConsoleTable$@606d6243",
    "numOutputRows" : 0
  }
}
```

```scala
assert(lastProgress.isInstanceOf[org.apache.spark.sql.streaming.StreamingQueryProgress])
assert(lastProgress.stateOperators.length == 1, "There should be one stateful operator")
```

```scala
println(lastProgress.stateOperators.head.prettyJson)
```

```text
{
  "operatorName" : "stateStoreSave",
  "numRowsTotal" : 2,
  "numRowsUpdated" : 0,
  "allUpdatesTimeMs" : 6,
  "numRowsRemoved" : 1,
  "allRemovalsTimeMs" : 21,
  "commitTimeMs" : 26,
  "memoryUsedBytes" : 1504,
  "numRowsDroppedByWatermark" : 0,
  "numShufflePartitions" : 1,
  "numStateStoreInstances" : 1,
  "customMetrics" : {
    "loadedMapCacheHitCount" : 8,
    "loadedMapCacheMissCount" : 0,
    "stateOnCurrentVersionSizeBytes" : 784
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
      "0" : 30
    }
  },
  "endOffset" : {
    "demo.streaming-aggregation" : {
      "0" : 30
    }
  },
  "latestOffset" : {
    "demo.streaming-aggregation" : {
      "0" : 30
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
spark.streams.active.foreach(_.stop)
```
