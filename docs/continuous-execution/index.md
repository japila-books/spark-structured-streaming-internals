# Continuous Stream Processing

**Continuous Stream Processing** is a stream processing engine in Spark Structured Streaming used for execution of structured streaming queries with [Trigger.Continuous](../Trigger.md#Continuous) trigger.

Continuous Stream Processing execution engine uses the novel **Data Source API V2** (Spark SQL) and for the very first time makes stream processing truly **continuous** (not micro-batch).

!!! TIP
    Read up on [Data Source API V2]({{ book.spark_sql }}/spark-sql-data-source-api-v2.html) in [The Internals of Spark SQL]({{ book.spark_sql }}) online book.

Because of the two innovative changes Continuous Stream Processing is often referred to as *Structured Streaming V2*.

Under the covers, Continuous Stream Processing uses [ContinuousExecution](ContinuousExecution.md) stream execution engine. When requested to [run an activated streaming query](ContinuousExecution.md#runActivatedStream), `ContinuousExecution` adds [WriteToContinuousDataSourceExec](../physical-operators/WriteToContinuousDataSourceExec.md) physical operator as the top-level operator in the physical query plan of the streaming query.

```text
scala> :type sq
org.apache.spark.sql.streaming.StreamingQuery

scala> sq.explain
== Physical Plan ==
WriteToContinuousDataSource ConsoleWriter[numRows=20, truncate=false]
+- *(1) Project [timestamp#758, value#759L]
   +- *(1) ScanV2 rate[timestamp#758, value#759L]
```

From now on, you may think of a streaming query as a soon-to-be-generated [ContinuousWriteRDD](../ContinuousWriteRDD.md) - an RDD data structure that Spark developers use to describe a distributed computation.

When the streaming query is started (and the top-level `WriteToContinuousDataSourceExec` physical operator is requested to [execute](../physical-operators/WriteToContinuousDataSourceExec.md#doExecute)), it simply requests the underlying `ContinuousWriteRDD` to collect.

That collect operator is how a Spark job is run (as tasks over all partitions of the RDD) as described by the [ContinuousWriteRDD.compute](../ContinuousWriteRDD.md#compute) "protocol" (a recipe for the tasks to be scheduled to run on Spark executors).

.Creating Instance of StreamExecution
image::images/webui-spark-job-streaming-query-started.png[align="center"]

While the [tasks are computing partitions](../ContinuousWriteRDD.md#compute) (of the `ContinuousWriteRDD`), they keep running [until killed or completed](../ContinuousWriteRDD.md#compute-loop). And that's the _ingenious design trick_ of how the streaming query (as a Spark job with the distributed tasks running on executors) runs continuously and indefinitely.

When `DataStreamReader` is requested to [create a streaming query for a ContinuousReadSupport data source](../DataStreamReader.md#load), it creates...FIXME

## Demo

```text
import org.apache.spark.sql.streaming.Trigger
import scala.concurrent.duration._
val sq = spark
  .readStream
  .format("rate")
  .load
  .writeStream
  .format("console")
  .option("truncate", false)
  .trigger(Trigger.Continuous(15.seconds)) // <-- Uses ContinuousExecution for execution
  .queryName("rate2console")
  .start

scala> :type sq
org.apache.spark.sql.streaming.StreamingQuery

assert(sq.isActive)

// sq.stop
```
