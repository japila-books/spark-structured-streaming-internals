---
hide:
  - navigation
---

# Demo: Streaming Watermark

This demo shows the internals of [streaming watermark](../streaming-watermark/index.md) with [Kafka Data Source](../datasources/kafka/index.md).

!!! note
    Please start a Kafka cluster and `spark-shell` as described in [Demo: Kafka Data Source](kafka-data-source.md).

## Streaming Query with Watermark

```scala
import java.time.Clock
val timeOffset = Clock.systemUTC.instant.getEpochSecond
```

```scala
val queryName = s"Demo: Streaming Watermark ($timeOffset)"
val checkpointLocation = s"/tmp/demo-checkpoint-$timeOffset"
```

```scala
import org.apache.spark.sql.streaming.Trigger
import scala.concurrent.duration._
val eventTimeCol = (($"tokens"(2) cast "long") + lit(timeOffset)) cast "timestamp" as "event_time"
val sq = spark
  .readStream
  .format("kafka")
  .option("subscribe", "demo.streaming-watermark")
  .option("kafka.bootstrap.servers", ":9092")
  .load
  .select($"value" cast "string")
  .select(split($"value", ",") as "tokens")
  .select(
    $"tokens"(0) as "id" cast "long",
    $"tokens"(1) as "name",
    eventTimeCol)
  .withWatermark(eventTime = "event_time", delayThreshold = "5 seconds")
  .writeStream
  .format("console")
  .queryName(queryName)
  .trigger(Trigger.ProcessingTime(1.seconds))
  .option("checkpointLocation", checkpointLocation)
  .option("truncate", false)
  .start
```

## Send Events

```console
echo "0,zero,0" | kcat -P -b :9092 -t demo.streaming-watermark
```

```console
echo "10,ten,10" | kcat -P -b :9092 -t demo.streaming-watermark
```
