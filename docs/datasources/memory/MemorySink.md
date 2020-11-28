# MemorySink

`MemorySink` is a [streaming sink](../../Sink.md) that <<addBatch, stores batches (records) in memory>>.

`MemorySink` is intended only for testing or demos.

`MemorySink` is used for `memory` format and requires a query name (by `queryName` method or `queryName` option).

NOTE: `MemorySink` was introduced in the https://github.com/apache/spark/pull/12119[pull request for [SPARK-14288\][SQL\] Memory Sink for streaming].

Use `toDebugString` to see the batches.

Its aim is to allow users to test streaming applications in the Spark shell or other local tests.

You can set `checkpointLocation` using `option` method or it will be set to [spark.sql.streaming.checkpointLocation](../../configuration-properties.md#spark.sql.streaming.checkpointLocation) property.

If `spark.sql.streaming.checkpointLocation` is set, the code uses `$location/$queryName` directory.

Finally, when no `spark.sql.streaming.checkpointLocation` is set, a temporary directory `memory.stream` under `java.io.tmpdir` is used with `offsets` subdirectory inside.

NOTE: The directory is cleaned up at shutdown using `ShutdownHookManager.registerShutdownDeleteDir`.

It creates `MemorySink` instance based on the schema of the DataFrame it operates on.

It creates a new DataFrame using `MemoryPlan` with `MemorySink` instance created earlier and registers it as a temporary table (using spark-sql-dataframe.md#registerTempTable[DataFrame.registerTempTable] method).

NOTE: At this point you can query the table as if it were a regular non-streaming table using spark-sql-sqlcontext.md#sql[sql] method.

A new StreamingQuery.md[StreamingQuery] is started (using [StreamingQueryManager.startQuery](../../StreamingQueryManager.md#startQuery)) and returned.

[[logging]]
[TIP]
====
Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.MemorySink` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.execution.streaming.MemorySink=ALL
```

Refer to <<spark-sql-streaming-spark-logging.md#, Logging>>.
====

=== [[creating-instance]] Creating MemorySink Instance

`MemorySink` takes the following to be created:

* [[schema]] Output schema
* [[outputMode]] [OutputMode](../../OutputMode.md)

`MemorySink` initializes the <<batches, batches>> internal property.

=== [[batches]] In-Memory Buffer of Streaming Batches -- `batches` Internal Property

[source, scala]
----
batches: ArrayBuffer[AddedData]
----

`batches` holds data from streaming batches that have been <<addBatch, added>> (_written_) to this sink.

For [Append](../../OutputMode.md#Append) and [Update](../../OutputMode.md#Update) output modes, `batches` holds rows from all batches.

For [Complete](../../OutputMode.md#Complete) output mode, `batches` holds rows from the last batch only.

`batches` can be cleared (_emptied_) using <<clear, clear>>.

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

=== [[clear]] Clearing Up Internal Batch Buffer -- `clear` Method

[source, scala]
----
clear(): Unit
----

`clear` simply removes (_clears_) all data from the <<batches, batches>> internal registry.

NOTE: `clear` is used exclusively in tests.
