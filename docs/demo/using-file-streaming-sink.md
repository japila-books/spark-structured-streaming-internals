# Demo: Using File Streaming Sink

This demo shows a streaming query that writes out to [FileStreamSink](../datasources/file/FileStreamSink.md).

## Prerequisites

A sample streaming query reads data using `socket` data source. Start `nc`.

```shell
nc -lk 9999
```

## Configure Logging

Enable [logging](../datasources/file/FileStreamSink.md#logging) for `FileStreamSink`.

## Start Streaming Query

Use `spark-shell` for fast interactive prototyping.

Describe the source.

```scala
val lines = spark
  .readStream
  .format("socket")
  .option("host", "localhost")
  .option("port", "9999")
  .load()
```

Describe the sink and start the streaming query.

```scala
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import concurrent.duration._
val interval = 15.seconds
val trigger = Trigger.ProcessingTime(interval)
val queryName = s"micro-batch every $interval"
val sq = lines
  .writeStream
  .format("text")
  .option("checkpointLocation", "/tmp/checkpointLocation")
  .option("path", "/tmp/socket-file")
  .trigger(trigger)
  .outputMode(OutputMode.Append) // only Append supported
  .queryName(queryName)
  .start
```

Use web UI to monitor the query (http://localhost:4040).

## Stop Query

```scala
spark.streams.active.foreach(_.stop)
```
