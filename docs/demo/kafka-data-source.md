---
hide:
  - navigation
---

# Demo: Kafka Data Source

This demo shows how to use [Kafka Data Source](../datasources/kafka/index.md) in a streaming query.

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

Run `spark-shell` with [spark-sql-kafka-0-10](../datasources/kafka/index.md#spark-submit) external module.

## Define Source

```scala
val events = spark
  .readStream
  .format("kafka")
  .option("subscribe", "demo.kafka-data-source")
  .option("kafka.bootstrap.servers", ":9092")
  .load
```

```text
scala> events.printSchema
root
 |-- key: binary (nullable = true)
 |-- value: binary (nullable = true)
 |-- topic: string (nullable = true)
 |-- partition: integer (nullable = true)
 |-- offset: long (nullable = true)
 |-- timestamp: timestamp (nullable = true)
 |-- timestampType: integer (nullable = true)
```

```scala
val kvs = events.
  select(
    $"key" cast "string",
    $"value" cast "string")
```

```text
scala> kvs.printSchema
root
 |-- key: string (nullable = true)
 |-- value: string (nullable = true)
```

```text
scala> kvs.explain
== Physical Plan ==
*(1) Project [cast(key#277 as string) AS key#295, cast(value#278 as string) AS value#296]
+- StreamingRelation kafka, [key#277, value#278, topic#279, partition#280, offset#281L, timestamp#282, timestampType#283]
```

## Start Streaming Query

```scala
import java.time.Clock
val timeOffset = Clock.systemUTC.instant.getEpochSecond
val queryName = s"Demo: Kafka Data Source ($timeOffset)"
val checkpointLocation = s"/tmp/demo-checkpoint-$timeOffset"
```

```scala
import scala.concurrent.duration._
import org.apache.spark.sql.streaming.Trigger
val sq = kvs
  .writeStream
  .format("console")
  .option("checkpointLocation", checkpointLocation)
  .option("truncate", false)
  .queryName(queryName)
  .trigger(Trigger.ProcessingTime(1.seconds))
  .start
```

The batch 0 is immediately printed out to the console.

```text
-------------------------------------------
Batch: 0
-------------------------------------------
+---+-----+
|key|value|
+---+-----+
+---+-----+
```

## Send Event

```console
echo "k1:v1" | kcat -P -b :9092 -K : -t demo.kafka-data-source
```

`spark-shell` should print out the event as Batch 1.

```text
-------------------------------------------
Batch: 1
-------------------------------------------
+---+-----+
|key|value|
+---+-----+
|k1 |v1   |
+---+-----+
```

## Cleanup

```scala
sq.stop()
```

Exit `spark-shell` and stop the Kafka cluster (Confluent Platform).
