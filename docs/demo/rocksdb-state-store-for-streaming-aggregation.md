---
hide:
  - navigation
---

# Demo: RocksDB State Store for Streaming Aggregation

This demo shows [RocksDB State Store](../rocksdb/index.md) used to keep state of a [streaming aggregation](../streaming-aggregation/index.md).

!!! note "FIXME"
    1. Use [Memory Data Source](../datasources/memory/index.md) as the source
    1. Logging does not work

## Configure RocksDB State Store

Configure [RocksDBStateStoreProvider](../rocksdb/RocksDBStateStoreProvider.md) to be the [StateStoreProvider](../stateful-stream-processing/StateStoreProvider.md) in `spark-shell`.

```shell
./bin/spark-shell \
  --conf spark.sql.streaming.stateStore.providerClass=org.apache.spark.sql.execution.streaming.state.RocksDBStateStoreProvider
```

## Send Records

```scala
val events = spark
  .readStream
  .format("rate")
  .load
  .withColumnRenamed("timestamp", "event_time")
  .withColumn("gid", $"value" % 5) // create 5 groups
```

## Define Windowed Streaming Aggregation

The streaming query uses [Update](../OutputMode.md#Update) output mode with a [streaming watermark](../watermark/index.md).

```scala
val windowed = events
  .withWatermark(eventTime = "event_time", delayThreshold = "10 seconds")
  .groupBy(
    $"gid",
    window(
      timeColumn = $"event_time",
      windowDuration = "5 seconds"))
  .agg(
    collect_list("value") as "vs")
```

```scala
windowed.printSchema
```

```text
root
 |-- gid: long (nullable = true)
 |-- window: struct (nullable = false)
 |    |-- start: timestamp (nullable = true)
 |    |-- end: timestamp (nullable = true)
 |-- vs: array (nullable = false)
 |    |-- element: long (containsNull = false)
```

## Start Streaming Query

```scala
import java.time.Clock
val timeOffset = Clock.systemUTC.instant.getEpochSecond
val queryName = s"Demo: RocksDB for Streaming Aggregation ($timeOffset)"
val checkpointLocation = s"/tmp/demo-checkpoint-$timeOffset"

import scala.concurrent.duration._
import org.apache.spark.sql.streaming.OutputMode.Update
import org.apache.spark.sql.streaming.Trigger
val sq = windowed
  .writeStream
  .format("console")
  .option("checkpointLocation", checkpointLocation)
  .option("truncate", false)
  .outputMode(Update)
  .queryName(queryName)
  .trigger(Trigger.ProcessingTime(5.seconds))
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

## Cleanup

```scala
spark.streams.active.foreach(_.stop)
```
