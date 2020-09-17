== Micro-Batch Stream Processing (Structured Streaming V1)

*Micro-Batch Stream Processing* is a stream processing model in Spark Structured Streaming that is used for streaming queries with <<spark-sql-streaming-Trigger.md#Once, Trigger.Once>> and <<spark-sql-streaming-Trigger.md#ProcessingTime, Trigger.ProcessingTime>> triggers.

Micro-batch stream processing uses <<spark-sql-streaming-MicroBatchExecution.md#, MicroBatchExecution>> stream execution engine.

Micro-batch stream processing supports <<spark-sql-streaming-MicroBatchReadSupport.md#, MicroBatchReadSupport>> data sources.

Micro-batch stream processing is often referred to as *Structured Streaming V1*.

[source, scala]
----
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

scala> sq.explain
== Physical Plan ==
WriteToDataSourceV2 org.apache.spark.sql.execution.streaming.sources.MicroBatchWriter@678e6267
+- *(1) Project [timestamp#54, value#55L]
   +- *(1) ScanV2 rate[timestamp#54, value#55L]

// sq.stop
----

=== [[execution-phases]] Execution Phases (Processing Cycle)

Once <<spark-sql-streaming-MicroBatchExecution.md#, MicroBatchExecution>> stream processing engine is requested to <<spark-sql-streaming-MicroBatchExecution.md#runActivatedStream, run an activated streaming query>>, the query execution goes through the following *execution phases* every trigger:

. [[triggerExecution]] <<spark-sql-streaming-MicroBatchExecution.md#runActivatedStream-triggerExecution, triggerExecution>>
. <<spark-sql-streaming-MicroBatchExecution.md#constructNextBatch-getOffset, getOffset>> for [Sources](Source.md) or <<spark-sql-streaming-MicroBatchExecution.md#constructNextBatch-setOffsetRange, setOffsetRange>> for <<spark-sql-streaming-MicroBatchReader.md#, MicroBatchReaders>>
. <<spark-sql-streaming-MicroBatchExecution.md#constructNextBatch-getEndOffset, getEndOffset>>
. <<spark-sql-streaming-MicroBatchExecution.md#constructNextBatch-walCommit, walCommit>>
. <<spark-sql-streaming-MicroBatchExecution.md#runBatch-getBatch, getBatch>>
. <<spark-sql-streaming-MicroBatchExecution.md#runBatch-queryPlanning, queryPlanning>>
. <<spark-sql-streaming-MicroBatchExecution.md#runBatch-addBatch, addBatch>>

Execution phases with execution times are available using <<spark-sql-streaming-StreamingQuery.md#lastProgress, StreamingQueryProgress>> under `durationMs`.

[source, scala]
----
scala> :type sq
org.apache.spark.sql.streaming.StreamingQuery
sq.lastProgress.durationMs.get("walCommit")
----

TIP: Enable INFO logging level for <<spark-sql-streaming-StreamExecution.md#logging, StreamExecution>> logger to be notified about durations.

```
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

=== [[monitoring]] Monitoring (using StreamingQueryListener and Logs)

`MicroBatchExecution` <<spark-sql-streaming-ProgressReporter.md#postEvent, posts events>> to announce when a streaming query is started and stopped as well as after every micro-batch. <<spark-sql-streaming-StreamingQueryListener.md#, StreamingQueryListener>> interface can be used to intercept the events and act accordingly.

After <<triggerExecution, triggerExecution phase>> `MicroBatchExecution` is requested to <<spark-sql-streaming-ProgressReporter.md#finishTrigger, finish up a streaming batch (trigger) and generate a StreamingQueryProgress>> (with execution statistics).

`MicroBatchExecution` prints out the following DEBUG message to the logs:

```
Execution stats: [executionStats]
```

`MicroBatchExecution` <<spark-sql-streaming-ProgressReporter.md#updateProgress, posts a QueryProgressEvent with the StreamingQueryProgress>> and prints out the following INFO message to the logs:

```
Streaming query made progress: [newProgress]
```
