# Micro-Batch Stream Processing

**Micro-Batch Stream Processing** is a stream processing model in Spark Structured Streaming that is used for streaming queries with [Trigger.Once](../Trigger.md#Once) and [Trigger.ProcessingTime](../Trigger.md#ProcessingTime) triggers.

Micro-batch stream processing uses [MicroBatchExecution](MicroBatchExecution.md) stream execution engine.

Micro-batch stream processing supports [MicroBatchStream](../MicroBatchStream.md) data sources.

Micro-batch stream processing is often referred to as *Structured Streaming V1*.

## Execution Phases

When [MicroBatchExecution](MicroBatchExecution.md) stream processing engine is requested to [run an activated streaming query](MicroBatchExecution.md#runActivatedStream), the query execution goes through the following **execution phases** every trigger (_micro-batch_):

1. [triggerExecution](MicroBatchExecution.md#runActivatedStream-triggerExecution)
1. [getOffset](MicroBatchExecution.md#constructNextBatch-getOffset) for [Source](../Source.md)s or [setOffsetRange](#MicroBatchExecution.md#constructNextBatch-setOffsetRange) for...FIXME
1. [getEndOffset](MicroBatchExecution.md#constructNextBatch-getEndOffset)
1. [walCommit](MicroBatchExecution.md#constructNextBatch-walCommit)
1. [getBatch](MicroBatchExecution.md#runBatch-getBatch)
1. [queryPlanning](MicroBatchExecution.md#runBatch-queryPlanning)
1. [addBatch](MicroBatchExecution.md#runBatch-addBatch)

Execution phases with execution times are available using [StreamingQueryProgress](../StreamingQuery.md#lastProgress) under `durationMs`.

```text
scala> :type sq
org.apache.spark.sql.streaming.StreamingQuery
sq.lastProgress.durationMs.get("walCommit")
```

!!! tip
    Enable INFO logging level for [StreamExecution](../StreamExecution.md#logging) logger to be notified about durations.

```text
17/08/11 09:04:17 INFO StreamExecution: Streaming query made progress: {
  "id" : "ec8f8228-90f6-4e1f-8ad2-80222affed63",
  "runId" : "f605c134-cfb0-4378-88c1-159d8a7c232e",
  "name" : "rates-to-console",
  "timestamp" : "2017-08-11T07:04:17.373Z",
  "batchId" : 0,
  "numInputRows" : 0,
  "processedRowsPerSecond" : 0.0,
  "durationMs" : {          // <-- Durations (in millis)
    "addBatch" : 38,
    "getBatch" : 1,
    "getOffset" : 0,
    "queryPlanning" : 1,
    "triggerExecution" : 62,
    "walCommit" : 19
  },
```

## Monitoring

`MicroBatchExecution` [posts events](../ProgressReporter.md#postEvent) to announce when a streaming query is started and stopped as well as after every micro-batch. [StreamingQueryListener](../monitoring/StreamingQueryListener.md) interface can be used to intercept the events and act accordingly.

After `triggerExecution` phase `MicroBatchExecution` is requested to [finish up a streaming batch (trigger) and generate a StreamingQueryProgress](../ProgressReporter.md#finishTrigger) (with execution statistics).

`MicroBatchExecution` prints out the following DEBUG message to the logs:

```text
Execution stats: [executionStats]
```

`MicroBatchExecution` [posts a QueryProgressEvent with the StreamingQueryProgress](../ProgressReporter.md#updateProgress) and prints out the following INFO message to the logs:

```text
Streaming query made progress: [newProgress]
```

## Demo

```scala
import org.apache.spark.sql.streaming.Trigger
import scala.concurrent.duration._
val sq = spark
  .readStream
  .format("rate")
  .load
  .writeStream
  .format("console")
  .option("truncate", false)
  .trigger(Trigger.ProcessingTime(1.minute)) // <-- Uses MicroBatchExecution for execution
  .queryName("rate2console")
  .start
assert(sq.isActive)
```

```text
scala> sq.explain
== Physical Plan ==
WriteToDataSourceV2 org.apache.spark.sql.execution.streaming.sources.MicroBatchWrite@42363db7
+- *(1) Project [timestamp#54, value#55L]
   +- *(1) ScanV2 rate[timestamp#54, value#55L]
```

```scala
sq.stop
```
