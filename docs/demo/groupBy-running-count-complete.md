---
hide:
  - navigation
---

# Demo: Streaming Query for Running Counts (Socket Source and Complete Output Mode)

The following code shows a [streaming aggregation](../streaming-aggregation.md) (with [Dataset.groupBy](../operators/groupBy.md) operator) in [complete](../OutputMode.md#Complete) output mode that reads text lines from a socket (using socket data source) and outputs running counts of the words.

NOTE: The example is "borrowed" from http://spark.apache.org/docs/latest/structured-streaming-programming-guide.html[the official documentation of Spark]. Changes and errors are only mine.

IMPORTANT: Run `nc -lk 9999` first before running the demo.

```text
// START: Only for easier debugging
// Reduce the number of partitions
// The state is then only for one partition
// which should make monitoring easier
val numShufflePartitions = 1
import org.apache.spark.sql.internal.SQLConf.SHUFFLE_PARTITIONS
spark.sessionState.conf.setConf(SHUFFLE_PARTITIONS, numShufflePartitions)

assert(spark.sessionState.conf.numShufflePartitions == numShufflePartitions)
// END: Only for easier debugging

val lines = spark
  .readStream
  .format("socket")
  .option("host", "localhost")
  .option("port", 9999)
  .load

scala> lines.printSchema
root
 |-- value: string (nullable = true)

import org.apache.spark.sql.functions.explode
val words = lines
  .select(explode(split($"value", """\W+""")) as "word")

val counts = words.groupBy("word").count

scala> counts.printSchema
root
 |-- word: string (nullable = true)
 |-- count: long (nullable = false)

// nc -lk 9999 is supposed to be up at this point

val queryName = "running_counts"
val checkpointLocation = s"/tmp/checkpoint-$queryName"

// Delete the checkpoint location from previous executions
import java.nio.file.{Files, FileSystems}
import java.util.Comparator
import scala.collection.JavaConverters._
val path = FileSystems.getDefault.getPath(checkpointLocation)
if (Files.exists(path)) {
  Files.walk(path)
    .sorted(Comparator.reverseOrder())
    .iterator
    .asScala
    .foreach(p => p.toFile.delete)
}

import org.apache.spark.sql.streaming.OutputMode.Complete
val runningCounts = counts
  .writeStream
  .format("console")
  .option("checkpointLocation", checkpointLocation)
  .outputMode(Complete)
  .start

scala> runningCounts.explain
== Physical Plan ==
WriteToDataSourceV2 org.apache.spark.sql.execution.streaming.sources.MicroBatchWriter@205f195c
+- *(5) HashAggregate(keys=[word#72], functions=[count(1)])
   +- StateStoreSave [word#72], state info [ checkpoint = file:/tmp/checkpoint-running_counts/state, runId = f3b2e642-1790-4a17-ab61-3d894110b063, opId = 0, ver = 0, numPartitions = 1], Complete, 0, 2
      +- *(4) HashAggregate(keys=[word#72], functions=[merge_count(1)])
         +- StateStoreRestore [word#72], state info [ checkpoint = file:/tmp/checkpoint-running_counts/state, runId = f3b2e642-1790-4a17-ab61-3d894110b063, opId = 0, ver = 0, numPartitions = 1], 2
            +- *(3) HashAggregate(keys=[word#72], functions=[merge_count(1)])
               +- Exchange hashpartitioning(word#72, 1)
                  +- *(2) HashAggregate(keys=[word#72], functions=[partial_count(1)])
                     +- Generate explode(split(value#83, \W+)), false, [word#72]
                        +- *(1) Project [value#83]
                           +- *(1) ScanV2 socket[value#83] (Options: [host=localhost,port=9999])

// Type lines (words) in the terminal with nc
// Observe the counts in spark-shell

// Use web UI to monitor the state of state (no pun intended)
// StateStoreSave and StateStoreRestore operators all have state metrics
// Go to http://localhost:4040/SQL/ and click one of the Completed Queries with Job IDs

// You may also want to check out checkpointed state
// in /tmp/checkpoint-running_counts/state/0/0

// Eventually...
runningCounts.stop()
```
