---
hide:
  - navigation
---

# Demo: Streaming Watermark

This demo shows the internals of [streaming watermark](../streaming-watermark/index.md) with [Kafka Data Source](../datasources/kafka/index.md).

## Start Kafka Cluster

```console
./start-confluent.sh
```

```console
$ docker compose ps
NAME                COMMAND                  SERVICE             STATUS              PORTS
broker              "/etc/confluent/dock…"   broker              running             0.0.0.0:9092->9092/tcp, 0.0.0.0:9101->9101/tcp
connect             "/etc/confluent/dock…"   connect             running             0.0.0.0:8083->8083/tcp, 9092/tcp
control-center      "/etc/confluent/dock…"   control-center      running             0.0.0.0:9021->9021/tcp
rest-proxy          "/etc/confluent/dock…"   rest-proxy          running             0.0.0.0:8082->8082/tcp
schema-registry     "/etc/confluent/dock…"   schema-registry     running             0.0.0.0:8081->8081/tcp
zookeeper           "/etc/confluent/dock…"   zookeeper           running             2888/tcp, 0.0.0.0:2181->2181/tcp, 3888/tcp
```

## Start Spark Shell

Run `spark-shell` with [spark-sql-kafka-0-10](../datasources/kafka/index.md) external module.

```console
$ ./bin/spark-shell --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.3.0
...
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 3.3.0
      /_/

Using Scala version 2.12.15 (OpenJDK 64-Bit Server VM, Java 17.0.4.1)
Type in expressions to have them evaluated.
Type :help for more information.

scala>
```

## Streaming Query with Watermark

```scala
import java.time.Clock
val timeOffset = Clock.systemUTC.instant.getEpochSecond
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
  .queryName(s"Demo: Streaming Watermark ($timeOffset)")
  .trigger(Trigger.ProcessingTime(1.seconds))
  .option("checkpointLocation", s"/tmp/demo-checkpoint-$timeOffset")
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
