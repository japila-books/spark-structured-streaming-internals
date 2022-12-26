# MemorySink

`MemorySink` is a `Table` ([Spark SQL]({{ book.spark_sql }}/connector/Table)) that `SupportsWrite` ([Spark SQL]({{ book.spark_sql }}/connector/SupportsWrite)) with [STREAMING_WRITE](#capabilities) capability for [Memory Data Source](index.md).

`MemorySink` is used for `memory` format and requires a [query name](#queryName).

## Creating Instance

`MemorySink` takes no arguments to be created.

`MemorySink` is created when:

* `DataStreamWriter` is requested to [start a streaming query](../../DataStreamWriter.md#startInternal) (with `memory` sink)

## <span id="queryName"> Query Name

[DataStreamWriter](../../DataStreamWriter.md#startInternal) makes sure that the query name of a streaming query with `memory` sink is specified (or throws an `AnalysisException`).

The `queryName` is used as a temporary view to query for data written into.

The `queryName` can be specified using [DataStreamWriter.queryName](../../DataStreamWriter.md#queryName) or `queryName` write option.

## <span id="batches"> Batches Registry

```scala
batches: ArrayBuffer[AddedData]
```

`MemorySink` creates an empty `ArrayBuffer` ([Scala]({{ scala.api }}/scala/collection/mutable/ArrayBuffer.html)) of [AddedData](#AddedData) when [created](#creating-instance).

`batches` is an in-memory buffer of streaming batches.

`batches` holds data from streaming batches that have been [added](#addBatch) (_written_) to this sink:

* All batches for [Append](../../OutputMode.md#Append) and [Update](../../OutputMode.md#Update) output modes
* The last batch only for [Complete](../../OutputMode.md#Complete) output mode

`batches` can be cleared (_emptied_) using [clear](#clear).

### <span id="AddedData"> AddedData

```scala
case class AddedData(
  batchId: Long,
  data: Array[Row])
```

`MemorySink` defines `AddedData` case class to store rows (_data_) per batch (_batchId_) in the [batches](#batches) registry.

The `AddedData` is used when:

* [toDebugString](#toDebugString)
* [write](#write)

## <span id="name"> Name

```scala
name(): String
```

`name` is part of the `Table` ([Spark SQL]({{ book.spark_sql }}/connector/Table#name)) abstraction.

---

`name` is `MemorySink`.

## <span id="capabilities"> Table Capabilities

```scala
capabilities(): String
```

`capabilities` is part of the `Table` ([Spark SQL]({{ book.spark_sql }}/connector/Table#capabilities)) abstraction.

---

`capabilities` is `STREAMING_WRITE` ([Spark SQL]({{ book.spark_sql }}/connector/TableCapability/#STREAMING_WRITE)).

## <span id="allData"> allData

```scala
allData: Seq[Row]
```

`allData` returns all the rows that were added to the [batches](#batches) registry.

---

`allData` is used when:

* `BasicOperators` execution planning strategy is executed (to plan a [MemoryPlan](MemoryPlan.md) to `LocalTableScanExec` physical operator)
* `MemoryPlan` is requested for the [stats](MemoryPlan.md#computeStats)

## <span id="write"> write

```scala
write(
  batchId: Long,
  needTruncate: Boolean,
  newRows: Array[Row]): Unit
```

`write`...FIXME

---

`write` is used when:

* `MemoryStreamingWrite` is requested to [commit](MemoryStreamingWrite.md#commit)

## <span id="latestBatchData"> latestBatchData

```scala
latestBatchData: Seq[Row]
```

`latestBatchData` returns the rows in the last element in the [batches](#batches) registry (that are the rows of the last batch).

---

`latestBatchData` is intended for tests.

## <span id="toDebugString"> toDebugString

```scala
toDebugString: String
```

`toDebugString`...FIXME

---

`toDebugString` is intended for tests.

## <span id="clear"> Clearing Up Batches

```scala
clear(): Unit
```

`clear` removes (_clears_) all data in the [batches](#batches) registry.

---

`clear` is intended for tests.

## Demo

### Creating MemorySink Directly

```scala
import org.apache.spark.sql.execution.streaming.sources.MemorySink
val sink = new MemorySink()
```

### Using MemorySink For Testing

```scala
val q = df
  .writeStream
  .format("memory")
  .queryName("file_data")
  .start()
  .asInstanceOf[StreamingQueryWrapper]
  .streamingQuery
q.processAllAvailable()
val memorySink = q.sink.asInstanceOf[MemorySink]
memorySink.allData
```

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.MemorySink` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.execution.streaming.MemorySink=ALL
```

Refer to [Logging](../../spark-logging.md)

<!---
## Review Me

`MemorySink` is a [streaming sink](../../Sink.md) that <<addBatch, stores batches (records) in memory>>.

NOTE: `MemorySink` was introduced in the https://github.com/apache/spark/pull/12119[pull request for [SPARK-14288\][SQL\] Memory Sink for streaming].

Its aim is to allow users to test streaming applications in the Spark shell or other local tests.

You can set `checkpointLocation` using `option` method or it will be set to [spark.sql.streaming.checkpointLocation](../../configuration-properties.md#spark.sql.streaming.checkpointLocation) property.

If `spark.sql.streaming.checkpointLocation` is set, the code uses `$location/$queryName` directory.

Finally, when no `spark.sql.streaming.checkpointLocation` is set, a temporary directory `memory.stream` under `java.io.tmpdir` is used with `offsets` subdirectory inside.

NOTE: The directory is cleaned up at shutdown using `ShutdownHookManager.registerShutdownDeleteDir`.

It creates `MemorySink` instance based on the schema of the DataFrame it operates on.

It creates a new DataFrame using `MemoryPlan` with `MemorySink` instance created earlier and registers it as a temporary table (using spark-sql-dataframe.md#registerTempTable[DataFrame.registerTempTable] method).

NOTE: At this point you can query the table as if it were a regular non-streaming table using spark-sql-sqlcontext.md#sql[sql] method.

=== [[addBatch]] Adding Batch of Data to Sink -- `addBatch` Method

[source, scala]
----
addBatch(
  batchId: Long,
  data: DataFrame): Unit
----

`addBatch` branches off based on whether the given `batchId` has already been <<addBatch-committed, committed>> or <<addBatch-not-committed, not>>.

A batch ID is considered *committed* when the given batch ID is greater than the <<latestBatchId, latest batch ID>> (if available).

`addBatch` is part of the [Sink](../../Sink.md#addBatch) abstraction.

==== [[addBatch-not-committed]] Batch Not Committed

With the `batchId` not committed, `addBatch` prints out the following DEBUG message to the logs:

```
Committing batch [batchId] to [this]
```

`addBatch` collects records from the given `data`.

NOTE: `addBatch` uses `Dataset.collect` operator to collect records.

For <<outputMode, Append>> and <<outputMode, Update>> output modes, `addBatch` adds the data (as a `AddedData`) to the <<batches, batches>> internal registry.

For <<outputMode, Complete>> output mode, `addBatch` clears the <<batches, batches>> internal registry first before adding the data (as a `AddedData`).

For any other output mode, `addBatch` reports an `IllegalArgumentException`:

```
Output mode [outputMode] is not supported by MemorySink
```

==== [[addBatch-committed]] Batch Committed

With the `batchId` committed, `addBatch` simply prints out the following DEBUG message to the logs and returns.

```
Skipping already committed batch: [batchId]
```
-->
