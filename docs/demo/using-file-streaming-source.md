---
hide:
  - navigation
---

# Demo: Using File Streaming Source

This demo shows a streaming query that reads files using [FileStreamSource](../datasources/file/FileStreamSource.md).

## Prerequisites

Make sure that the source directory is available before starting the query.

```shell
mkdir /tmp/text-logs
```

## Configure Logging

Enable [logging](../datasources/file/FileStreamSource.md#logging) for `FileStreamSource`.

## Start Streaming Query

Use `spark-shell` for fast interactive prototyping.

Describe a source to load data from.

```scala
val lines = spark
  .readStream
  .format("text")
  .option("maxFilesPerTrigger", 1)
  .load("/tmp/text-logs")
```

Show the schema.

```text
scala> lines.printSchema
root
 |-- value: string (nullable = true)
```

Describe the sink (`console`) and start the streaming query.

```scala
import org.apache.spark.sql.streaming.Trigger
import concurrent.duration._
val interval = 15.seconds
val trigger = Trigger.ProcessingTime(interval)
val queryName = s"one file every micro-batch (every $interval)"
val sq = lines
  .writeStream
  .format("console")
  .option("checkpointLocation", "/tmp/checkpointLocation")
  .trigger(trigger)
  .queryName(queryName)
  .start
```

Use web UI to monitor the query (http://localhost:4040).

## Stop Query

```scala
spark.streams.active.foreach(_.stop)
```
