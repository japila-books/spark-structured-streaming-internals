# Trigger

`Trigger` is an abstraction of [policies](#implementations) that indicate how often a [StreamingQuery](StreamingQuery.md) should be executed (_triggered_) and possibly emit a new data.

`Trigger` is used to determine a [TriggerExecutor](TriggerExecutor.md) in [MicroBatchExecution](micro-batch-execution/MicroBatchExecution.md#triggerExecutor) and [ContinuousExecution](continuous-execution/ContinuousExecution.md#triggerExecutor).

Trigger  | TriggerExecutor | Factory Method
---------|-----------------|---------------
 [AvailableNowTrigger](#AvailableNowTrigger) | [MultiBatchExecutor](TriggerExecutor.md#MultiBatchExecutor) | [Trigger.AvailableNow](#AvailableNow)
 [ContinuousTrigger](#ContinuousTrigger) | [ProcessingTimeExecutor](TriggerExecutor.md#ProcessingTimeExecutor) | [Trigger.Continuous](#Continuous)
 [OneTimeTrigger](#OneTimeTrigger) | [SingleBatchExecutor](TriggerExecutor.md#SingleBatchExecutor) | [Trigger.Once](#Once)
 [ProcessingTimeTrigger](#ProcessingTimeTrigger) | [ProcessingTimeExecutor](TriggerExecutor.md#ProcessingTimeExecutor) | [Trigger.ProcessingTime](#ProcessingTime)

## Implementations

### <span id="AvailableNowTrigger"> AvailableNowTrigger

Processes all available data in multiple batches then terminates the query

!!! note "SPARK-36533"
    `AvailableNowTrigger` is a new feature in [3.3.0]({{ spark.commit }}/e33cdfb317498b04e077c4d6356fc3848cd78da0) tracked under [SPARK-36533](https://issues.apache.org/jira/browse/SPARK-36533).

### <span id="ContinuousTrigger"> ContinuousTrigger

Continuously processes streaming data, asynchronously checkpointing at the specified interval

### <span id="OneTimeTrigger"> OneTimeTrigger

Processes all available data in one batch then terminates the query

### <span id="ProcessingTimeTrigger"> ProcessingTimeTrigger

## Static Methods

`Trigger` is also a factory object with static methods to create the [policies](#implementations).

```scala
import org.apache.spark.sql.streaming.Trigger
```

### <span id="AvailableNow"> AvailableNow

```java
Trigger AvailableNow()
```

Creates an [AvailableNowTrigger](#AvailableNowTrigger)

### <span id="Continuous"> Continuous

```java
Trigger Continuous(
  Duration interval)
Trigger Continuous(
  long intervalMs)
Trigger Continuous(
  long interval,
  TimeUnit timeUnit)
Trigger Continuous(
  Duration interval)
```

Creates a [ContinuousTrigger](#ContinuousTrigger)

### <span id="Once"> Once

```java
Trigger Once()
```

Creates a [OneTimeTrigger](#OneTimeTrigger)

### <span id="ProcessingTime"> ProcessingTime

```java
Trigger ProcessingTime(
  Duration interval)
Trigger ProcessingTime(
  long intervalMs)
Trigger ProcessingTime(
  long interval,
  TimeUnit timeUnit)
Trigger ProcessingTime(
  String interval)
```

Creates a [ProcessingTimeTrigger](#ProcessingTimeTrigger)

## <span id="DataStreamWriter"> DataStreamWriter

A `Trigger` of a streaming query is defined using [DataStreamWriter.trigger](DataStreamWriter.md#trigger).

## Demo: Trigger.Once

```text
import org.apache.spark.sql.streaming.Trigger
val query = spark.
  readStream.
  format("rate").
  load.
  writeStream.
  format("console").
  option("truncate", false).
  trigger(Trigger.Once). // <-- execute once and stop
  queryName("rate-once").
  start
assert(query.isActive == false)
println(query.lastProgress)
```

```text
{
  "id" : "2ae4b0a4-434f-4ca7-a523-4e859c07175b",
  "runId" : "24039ce5-906c-4f90-b6e7-bbb3ec38a1f5",
  "name" : "rate-once",
  "timestamp" : "2017-07-04T18:39:35.998Z",
  "numInputRows" : 0,
  "processedRowsPerSecond" : 0.0,
  "durationMs" : {
    "addBatch" : 1365,
    "getBatch" : 29,
    "getOffset" : 0,
    "queryPlanning" : 285,
    "triggerExecution" : 1742,
    "walCommit" : 40
  },
  "stateOperators" : [ ],
  "sources" : [ {
    "description" : "RateSource[rowsPerSecond=1, rampUpTimeSeconds=0, numPartitions=8]",
    "startOffset" : null,
    "endOffset" : 0,
    "numInputRows" : 0,
    "processedRowsPerSecond" : 0.0
  } ],
  "sink" : {
    "description" : "org.apache.spark.sql.execution.streaming.ConsoleSink@7dbf277"
  }
}
```

## IllegalStateException: Custom Triggers Not Supported

Although `Trigger` allows for custom implementations, `StreamExecution` [refuses such attempts](StreamExecution.md#triggerExecutor) and reports an `IllegalStateException`.

```scala
import org.apache.spark.sql.streaming.Trigger
case object MyTrigger extends Trigger
val sq = spark
  .readStream
  .format("rate")
  .load
  .writeStream
  .format("console")
  .trigger(MyTrigger) // <-- use custom trigger
  .queryName("rate-custom-trigger")
  .start
```

```text
java.lang.IllegalStateException: Unknown type of trigger: MyTrigger
  at org.apache.spark.sql.execution.streaming.MicroBatchExecution.<init>(MicroBatchExecution.scala:56)
  at org.apache.spark.sql.streaming.StreamingQueryManager.createQuery(StreamingQueryManager.scala:279)
  at org.apache.spark.sql.streaming.StreamingQueryManager.startQuery(StreamingQueryManager.scala:326)
  at org.apache.spark.sql.streaming.DataStreamWriter.startQuery(DataStreamWriter.scala:427)
  at org.apache.spark.sql.streaming.DataStreamWriter.startInternal(DataStreamWriter.scala:406)
  at org.apache.spark.sql.streaming.DataStreamWriter.start(DataStreamWriter.scala:249)
  ... 47 elided
```
