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
  .load
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
  .trigger(trigger)
  .outputMode(OutputMode.Append) // only Append supported
  .queryName(queryName)
  .start(path = "/tmp/socket-file")
```

Use web UI to monitor the query (http://localhost:4040).

## Deep Dive into Internals

```scala
import org.apache.spark.sql.streaming.StreamingQuery
assert(sq.isInstanceOf[StreamingQuery])
```

```text
scala> sq.explain(extended = true)
== Parsed Logical Plan ==
StreamingDataSourceV2Relation [value#0], org.apache.spark.sql.execution.streaming.sources.TextSocketTable$$anon$1@aa58ed0, TextSocketV2[host: localhost, port: 9999], -1, 0

== Analyzed Logical Plan ==
value: string
StreamingDataSourceV2Relation [value#0], org.apache.spark.sql.execution.streaming.sources.TextSocketTable$$anon$1@aa58ed0, TextSocketV2[host: localhost, port: 9999], -1, 0

== Optimized Logical Plan ==
StreamingDataSourceV2Relation [value#0], org.apache.spark.sql.execution.streaming.sources.TextSocketTable$$anon$1@aa58ed0, TextSocketV2[host: localhost, port: 9999], -1, 0

== Physical Plan ==
*(1) Project [value#0]
+- MicroBatchScan[value#0] class org.apache.spark.sql.execution.streaming.sources.TextSocketTable$$anon$1
```

```text
scala> println(sq.lastProgress)
{
  "id" : "f4dc1b6c-6bc7-423a-9bfe-49db2a440bda",
  "runId" : "1a05533a-4db0-486d-8c44-7d4a8e49a7bc",
  "name" : "micro-batch every 15 seconds",
  "timestamp" : "2020-10-17T09:47:30.003Z",
  "batchId" : 2,
  "numInputRows" : 0,
  "inputRowsPerSecond" : 0.0,
  "processedRowsPerSecond" : 0.0,
  "durationMs" : {
    "latestOffset" : 0,
    "triggerExecution" : 0
  },
  "stateOperators" : [ ],
  "sources" : [ {
    "description" : "TextSocketV2[host: localhost, port: 9999]",
    "startOffset" : 0,
    "endOffset" : 0,
    "numInputRows" : 0,
    "inputRowsPerSecond" : 0.0,
    "processedRowsPerSecond" : 0.0
  } ],
  "sink" : {
    "description" : "FileSink[/tmp/socket-file]",
    "numOutputRows" : -1
  }
}
```

```text
import org.apache.spark.sql.execution.debug._
scala> sq.debugCodegen()
Found 1 WholeStageCodegen subtrees.
== Subtree 1 / 1 (maxMethodCodeSize:120; maxConstantPoolSize:100(0.15% used); numInnerClasses:0) ==
*(1) Project [value#0]
+- MicroBatchScan[value#0] class org.apache.spark.sql.execution.streaming.sources.TextSocketTable$$anon$1

Generated code:
/* 001 */ public Object generate(Object[] references) {
/* 002 */   return new GeneratedIteratorForCodegenStage1(references);
/* 003 */ }
/* 004 */
/* 005 */ // codegenStageId=1
/* 006 */ final class GeneratedIteratorForCodegenStage1 extends org.apache.spark.sql.execution.BufferedRowIterator {
/* 007 */   private Object[] references;
...
```

### MicroBatchExecution

Access [MicroBatchExecution](../MicroBatchExecution.md).

```scala
import org.apache.spark.sql.execution.streaming.StreamingQueryWrapper
val streamEngine = sq.asInstanceOf[StreamingQueryWrapper].streamingQuery

import org.apache.spark.sql.execution.streaming.StreamExecution
assert(streamEngine.isInstanceOf[StreamExecution])

import org.apache.spark.sql.execution.streaming.MicroBatchExecution
val microBatchEngine = streamEngine.asInstanceOf[MicroBatchExecution]
```

### IncrementalExecution

Access [IncrementalExecution](../IncrementalExecution.md).

```scala
val qe = microBatchEngine.lastExecution
import org.apache.spark.sql.execution.streaming.IncrementalExecution
assert(qe.isInstanceOf[IncrementalExecution])
assert(qe != null, "No physical plan. Waiting for data.")
```

### FileStreamSink

A streaming query (as a [StreamExecution](../StreamExecution.md)) is associated with [one sink](../StreamExecution.md#sink). That's a [FileStreamSink](../datasources/file/FileStreamSink.md) in this demo.

```scala
import org.apache.spark.sql.execution.streaming.FileStreamSink
val sink = microBatchEngine.sink.asInstanceOf[FileStreamSink]
assert(sink.isInstanceOf[FileStreamSink])
```

```text
scala> println(sink)
FileSink[/tmp/socket-file]
```

## Stop Query

```scala
spark.streams.active.foreach(_.stop)
```
