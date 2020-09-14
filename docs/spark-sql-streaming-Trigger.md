== [[Trigger]] Trigger -- How Frequently to Check Sources For New Data

`Trigger` defines how often a link:spark-sql-streaming-StreamingQuery.md[streaming query] should be executed (_triggered_) and emit a new data (which `StreamExecution` uses to link:spark-sql-streaming-StreamExecution.md#triggerExecutor[resolve a TriggerExecutor]).

[[available-implementations]]
[[available-triggers]]
[[triggers]]
.Trigger's Factory Methods
[cols="1m,2",options="header",width="100%"]
|===
| Trigger
| Creating Instance

| ContinuousTrigger
a| [[ContinuousTrigger]][[Continuous]]

[source, java]
----
Trigger Continuous(long intervalMs)
Trigger Continuous(long interval, TimeUnit timeUnit)
Trigger Continuous(Duration interval)
Trigger Continuous(String interval)
----

| OneTimeTrigger
a| [[OneTimeTrigger]][[Once]]

[source, java]
----
Trigger Once()
----

| ProcessingTime
a| [[ProcessingTime]]

[source, java]
----
Trigger ProcessingTime(Duration interval)
Trigger ProcessingTime(long intervalMs)
Trigger ProcessingTime(long interval, TimeUnit timeUnit)
Trigger ProcessingTime(String interval)
----

<<ProcessingTime-examples, Examples of ProcessingTime>>

|===

NOTE: You specify the trigger for a streaming query using ``DataStreamWriter``'s [trigger](DataStreamWriter.md#trigger) method.

[source, scala]
----
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

scala> println(query.lastProgress)
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
----

NOTE: Although `Trigger` allows for custom implementations, `StreamExecution` link:spark-sql-streaming-StreamExecution.md#triggerExecutor[refuses such attempts] and reports an `IllegalStateException`.

[source, scala]
----
import org.apache.spark.sql.streaming.Trigger
case object MyTrigger extends Trigger
scala> val sq = spark
  .readStream
  .format("rate")
  .load
  .writeStream
  .format("console")
  .trigger(MyTrigger) // <-- use custom trigger
  .queryName("rate-custom-trigger")
  .start
java.lang.IllegalStateException: Unknown type of trigger: MyTrigger
  at org.apache.spark.sql.execution.streaming.MicroBatchExecution.<init>(MicroBatchExecution.scala:60)
  at org.apache.spark.sql.streaming.StreamingQueryManager.createQuery(StreamingQueryManager.scala:275)
  at org.apache.spark.sql.streaming.StreamingQueryManager.startQuery(StreamingQueryManager.scala:316)
  at org.apache.spark.sql.streaming.DataStreamWriter.start(DataStreamWriter.scala:325)
  ... 57 elided
----

NOTE: `Trigger` was introduced in https://github.com/apache/spark/commit/855ed44ed31210d2001d7ce67c8fa99f8416edd3[the commit for [SPARK-14176\][SQL\] Add DataFrameWriter.trigger to set the stream batch period].

=== [[ProcessingTime-examples]] Examples of ProcessingTime

`ProcessingTime` is a `Trigger` that assumes that milliseconds is the minimum time unit.

You can create an instance of `ProcessingTime` using the following constructors:

* `ProcessingTime(Long)` that accepts non-negative values that represent milliseconds.
+
```
ProcessingTime(10)
```
* `ProcessingTime(interval: String)` or `ProcessingTime.create(interval: String)` that accept `CalendarInterval` instances with or without leading `interval` string.
+
```
ProcessingTime("10 milliseconds")
ProcessingTime("interval 10 milliseconds")
```
* `ProcessingTime(Duration)` that accepts `scala.concurrent.duration.Duration` instances.
+
```
ProcessingTime(10.seconds)
```
* `ProcessingTime.create(interval: Long, unit: TimeUnit)` for `Long` and `java.util.concurrent.TimeUnit` instances.
+
```
ProcessingTime.create(10, TimeUnit.SECONDS)
```
