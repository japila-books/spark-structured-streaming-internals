# dropDuplicates Operator &mdash; Streaming Deduplication

```scala
dropDuplicates(): Dataset[T]
dropDuplicates(
  colNames: Seq[String]): Dataset[T]
dropDuplicates(
  col1: String,
  cols: String*): Dataset[T]
```

`dropDuplicates` operator drops duplicate records (given a subset of columns)

!!! note
    For a streaming Dataset, `dropDuplicates` will keep all data across triggers as intermediate state to drop duplicates rows. You can use [withWatermark](withWatermark.md) operator to limit how late the duplicate data can be and system will accordingly limit the state. In addition, too late data older than watermark will be dropped to avoid any possibility of duplicates.

## Demo

```text
// Start a streaming query
// Using old-fashioned MemoryStream (with the deprecated SQLContext)
import org.apache.spark.sql.execution.streaming.MemoryStream
import org.apache.spark.sql.SQLContext
implicit val sqlContext: SQLContext = spark.sqlContext
val source = MemoryStream[(Int, Int)]
val ids = source.toDS.toDF("time", "id").
  withColumn("time", $"time" cast "timestamp"). // <-- convert time column from Int to Timestamp
  dropDuplicates("id").
  withColumn("time", $"time" cast "long")  // <-- convert time column back from Timestamp to Int

// Conversions are only for display purposes
// Internally we need timestamps for watermark to work
// Displaying timestamps could be too much for such a simple task

scala> println(ids.queryExecution.analyzed.numberedTreeString)
00 Project [cast(time#10 as bigint) AS time#15L, id#6]
01 +- Deduplicate [id#6], true
02    +- Project [cast(time#5 as timestamp) AS time#10, id#6]
03       +- Project [_1#2 AS time#5, _2#3 AS id#6]
04          +- StreamingExecutionRelation MemoryStream[_1#2,_2#3], [_1#2, _2#3]

import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import scala.concurrent.duration._
val q = ids.
  writeStream.
  format("memory").
  queryName("dups").
  outputMode(OutputMode.Append).
  trigger(Trigger.ProcessingTime(30.seconds)).
  option("checkpointLocation", "checkpoint-dir"). // <-- use checkpointing to save state between restarts
  start

// Publish duplicate records
source.addData(1 -> 1)
source.addData(2 -> 1)
source.addData(3 -> 1)

q.processAllAvailable()

// Check out how dropDuplicates removes duplicates
// --> per single streaming batch (easy)
scala> spark.table("dups").show
+----+---+
|time| id|
+----+---+
|   1|  1|
+----+---+

source.addData(4 -> 1)
source.addData(5 -> 2)

// --> across streaming batches (harder)
scala> spark.table("dups").show
+----+---+
|time| id|
+----+---+
|   1|  1|
|   5|  2|
+----+---+

// Check out the internal state
scala> println(q.lastProgress.stateOperators(0).prettyJson)
{
  "numRowsTotal" : 2,
  "numRowsUpdated" : 1,
  "memoryUsedBytes" : 17751
}

// You could use web UI's SQL tab instead
// Use Details for Query

source.addData(6 -> 2)

scala> spark.table("dups").show
+----+---+
|time| id|
+----+---+
|   1|  1|
|   5|  2|
+----+---+

// Check out the internal state
scala> println(q.lastProgress.stateOperators(0).prettyJson)
{
  "numRowsTotal" : 2,
  "numRowsUpdated" : 0,
  "memoryUsedBytes" : 17751
}

// Restart the streaming query
q.stop

val q = ids.
  writeStream.
  format("memory").
  queryName("dups").
  outputMode(OutputMode.Complete).  // <-- memory sink supports checkpointing for Complete output mode only
  trigger(Trigger.ProcessingTime(30.seconds)).
  option("checkpointLocation", "checkpoint-dir"). // <-- use checkpointing to save state between restarts
  start

// Doh! MemorySink is fine, but Complete is only available with a streaming aggregation
// Answer it if you know why --> https://stackoverflow.com/q/45756997/1305344

// It's a high time to work on https://issues.apache.org/jira/browse/SPARK-21667
// to understand the low-level details (and the reason, it seems)

// Disabling operation checks and starting over
// ./bin/spark-shell -c spark.sql.streaming.unsupportedOperationCheck=false
// it works now --> no exception!

scala> spark.table("dups").show
+----+---+
|time| id|
+----+---+
+----+---+

source.addData(0 -> 1)
// wait till the batch is triggered
scala> spark.table("dups").show
+----+---+
|time| id|
+----+---+
|   0|  1|
+----+---+

source.addData(1 -> 1)
source.addData(2 -> 1)
// wait till the batch is triggered
scala> spark.table("dups").show
+----+---+
|time| id|
+----+---+
+----+---+

// What?! No rows?! It doesn't look as if it worked fine :(

// Use groupBy to pass the requirement of having streaming aggregation for Complete output mode
val counts = ids.groupBy("id").agg(first($"time") as "first_time")
scala> counts.explain
== Physical Plan ==
*HashAggregate(keys=[id#246], functions=[first(time#255L, false)])
+- StateStoreSave [id#246], StatefulOperatorStateInfo(<unknown>,3585583b-42d7-4547-8d62-255581c48275,0,0), Append, 0
   +- *HashAggregate(keys=[id#246], functions=[merge_first(time#255L, false)])
      +- StateStoreRestore [id#246], StatefulOperatorStateInfo(<unknown>,3585583b-42d7-4547-8d62-255581c48275,0,0)
         +- *HashAggregate(keys=[id#246], functions=[merge_first(time#255L, false)])
            +- *HashAggregate(keys=[id#246], functions=[partial_first(time#255L, false)])
               +- *Project [cast(time#250 as bigint) AS time#255L, id#246]
                  +- StreamingDeduplicate [id#246], StatefulOperatorStateInfo(<unknown>,3585583b-42d7-4547-8d62-255581c48275,1,0), 0
                     +- Exchange hashpartitioning(id#246, 200)
                        +- *Project [cast(_1#242 as timestamp) AS time#250, _2#243 AS id#246]
                           +- StreamingRelation MemoryStream[_1#242,_2#243], [_1#242, _2#243]
val q = counts.
  writeStream.
  format("memory").
  queryName("dups").
  outputMode(OutputMode.Complete).  // <-- memory sink supports checkpointing for Complete output mode only
  trigger(Trigger.ProcessingTime(30.seconds)).
  option("checkpointLocation", "checkpoint-dir"). // <-- use checkpointing to save state between restarts
  start

source.addData(0 -> 1)
source.addData(1 -> 1)
// wait till the batch is triggered
scala> spark.table("dups").show
+---+----------+
| id|first_time|
+---+----------+
|  1|         0|
+---+----------+

// Publish duplicates
// Check out how dropDuplicates removes duplicates

// Stop the streaming query
// Specify event time watermark to remove old duplicates
```
