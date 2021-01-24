---
hide:
  - navigation
---

# Demo: Exploring Checkpointed State

The following demo shows the internals of the checkpointed state of a [stateful streaming query](../spark-sql-streaming-stateful-stream-processing.md).

The demo uses the state checkpoint directory that was used in [Demo: Streaming Watermark with Aggregation in Append Output Mode](watermark-aggregation-append.md).

```text
// Change the path to match your configuration
val checkpointRootLocation = "/tmp/checkpoint-watermark_demo/state"
val version = 1L

import org.apache.spark.sql.execution.streaming.state.StateStoreId
val storeId = StateStoreId(
  checkpointRootLocation,
  operatorId = 0,
  partitionId = 0)

// The key and value schemas should match the watermark demo
// .groupBy(window($"time", windowDuration.toString) as "sliding_window")
import org.apache.spark.sql.types.{TimestampType, StructField, StructType}
val keySchema = StructType(
  StructField("sliding_window",
    StructType(
      StructField("start", TimestampType, nullable = true) ::
      StructField("end", TimestampType, nullable = true) :: Nil),
    nullable = false) :: Nil)
scala> keySchema.printTreeString
root
 |-- sliding_window: struct (nullable = false)
 |    |-- start: timestamp (nullable = true)
 |    |-- end: timestamp (nullable = true)

// .agg(collect_list("batch") as "batches", collect_list("value") as "values")
import org.apache.spark.sql.types.{ArrayType, LongType}
val valueSchema = StructType(
  StructField("batches", ArrayType(LongType, true), true) ::
  StructField("values", ArrayType(LongType, true), true) :: Nil)
scala> valueSchema.printTreeString
root
 |-- batches: array (nullable = true)
 |    |-- element: long (containsNull = true)
 |-- values: array (nullable = true)
 |    |-- element: long (containsNull = true)

val indexOrdinal = None
import org.apache.spark.sql.execution.streaming.state.StateStoreConf
val storeConf = StateStoreConf(spark.sessionState.conf)
val hadoopConf = spark.sessionState.newHadoopConf()
import org.apache.spark.sql.execution.streaming.state.StateStoreProvider
val provider = StateStoreProvider.createAndInit(
  storeId, keySchema, valueSchema, indexOrdinal, storeConf, hadoopConf)

// You may want to use the following higher-level code instead
import java.util.UUID
val queryRunId = UUID.randomUUID
import org.apache.spark.sql.execution.streaming.state.StateStoreProviderId
val storeProviderId = StateStoreProviderId(storeId, queryRunId)
import org.apache.spark.sql.execution.streaming.state.StateStore
val store = StateStore.get(
  storeProviderId,
  keySchema,
  valueSchema,
  indexOrdinal,
  version,
  storeConf,
  hadoopConf)

import org.apache.spark.sql.execution.streaming.state.UnsafeRowPair
def formatRowPair(rowPair: UnsafeRowPair) = {
  s"(${rowPair.key.getLong(0)}, ${rowPair.value.getLong(0)})"
}
store.iterator.map(formatRowPair).foreach(println)

// WIP: Missing value (per window)
def formatRowPair(rowPair: UnsafeRowPair) = {
  val window = rowPair.key.getStruct(0, 2)
  import scala.concurrent.duration._
  val begin = window.getLong(0).millis.toSeconds
  val end = window.getLong(1).millis.toSeconds

  val value = rowPair.value.getStruct(0, 4)
  // input is (time, value, batch) all longs
  val t = value.getLong(1).millis.toSeconds
  val v = value.getLong(2)
  val b = value.getLong(3)
  s"(key: [$begin, $end], ($t, $v, $b))"
}
store.iterator.map(formatRowPair).foreach(println)
```
