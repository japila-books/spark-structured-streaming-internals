# Stateful Stream Processing

**Stateful Stream Processing** is a stream processing with state (implicit or explicit).

In Spark Structured Streaming, a streaming query is stateful when is one of the following (that makes use of [StateStores](spark-sql-streaming-StateStore.md)):

* [Streaming Aggregation](streaming-aggregation.md)

* [Arbitrary Stateful Streaming Aggregation](arbitrary-stateful-streaming-aggregation.md)

* [Stream-Stream Join](spark-sql-streaming-join.md)

* [Streaming Deduplication](spark-sql-streaming-deduplication.md)

* [Streaming Limit](spark-sql-streaming-limit.md)

## <span id="versioned-state-statestores-and-statestoreproviders"> Versioned State, StateStores and StateStoreProviders

Spark Structured Streaming uses [StateStore](spark-sql-streaming-StateStore.md)s for versioned and fault-tolerant key-value state stores.

State stores are checkpointed incrementally to avoid state loss and for increased performance.

State stores are managed by [StateStoreProvider](spark-sql-streaming-StateStoreProvider.md)s with [HDFSBackedStateStoreProvider](HDFSBackedStateStoreProvider.md) being the default and only known implementation. `HDFSBackedStateStoreProvider` uses Hadoop DFS-compliant file system for [state checkpointing and fault-tolerance](HDFSBackedStateStoreProvider.md#baseDir).

State store providers manage versioned state per [stateful operator (and partition it operates on)](spark-sql-streaming-StateStoreId.md).

The lifecycle of a `StateStoreProvider` begins when `StateStore` utility (on a Spark executor) is requested for the [StateStore by provider ID and version](spark-sql-streaming-StateStore.md#get-StateStore).

IMPORTANT: It is worth to notice that since `StateStore` and `StateStoreProvider` utilities are Scala objects that makes it possible that there can only be one instance of `StateStore` and `StateStoreProvider` on a single JVM. Scala objects are (sort of) singletons which means that there will be exactly one instance of each per JVM and that is exactly the JVM of a Spark executor. As long as the executor is up and running state versions are cached and no Hadoop DFS is used (except for the initial load).

When requested for a <<spark-sql-streaming-StateStore.md#get-StateStore, StateStore>>, `StateStore` utility is given the version of a state store to look up. The version is either the <<spark-sql-streaming-EpochTracker.md#getCurrentEpoch, current epoch>> (in <<spark-sql-streaming-continuous-stream-processing.md#, Continuous Stream Processing>>) or the [current batch ID](StatefulOperatorStateInfo.md#storeVersion) (in <<micro-batch-stream-processing.md#, Micro-Batch Stream Processing>>).

`StateStore` utility requests `StateStoreProvider` utility to <<spark-sql-streaming-StateStoreProvider.md#createAndInit, createAndInit>> that creates the `StateStoreProvider` implementation (based on <<spark-sql-streaming-properties.md#spark.sql.streaming.stateStore.providerClass, spark.sql.streaming.stateStore.providerClass>> internal configuration property) and requests it to <<spark-sql-streaming-StateStoreProvider.md#init, initialize>>.

The initialized `StateStoreProvider` is cached in <<spark-sql-streaming-StateStore.md#loadedProviders, loadedProviders>> internal lookup table (for a <<spark-sql-streaming-StateStoreId.md#, StateStoreId>>) for later lookups.

`StateStoreProvider` utility then requests the `StateStoreProvider` for the <<spark-sql-streaming-StateStoreProvider.md#getStore, state store for a specified version>>. (e.g. a [HDFSBackedStateStore](HDFSBackedStateStore.md) in case of [HDFSBackedStateStoreProvider](HDFSBackedStateStoreProvider.md#getStore)).

An instance of `StateStoreProvider` is requested to <<spark-sql-streaming-StateStoreProvider.md#doMaintenance, do its own maintenance>> or <<spark-sql-streaming-StateStoreProvider.md#close, close>> (when <<spark-sql-streaming-StateStoreProvider.md#verifyIfStoreInstanceActive, a corresponding StateStore is inactive>>) in <<spark-sql-streaming-StateStoreProvider.md#MaintenanceTask, MaintenanceTask daemon thread>> that runs periodically every <<spark-sql-streaming-properties.md#spark.sql.streaming.stateStore.maintenanceInterval, spark.sql.streaming.stateStore.maintenanceInterval>> configuration property (default: `60s`).

## <span id="IncrementalExecution"> IncrementalExecution &mdash; QueryExecution of Streaming Queries

Regardless of the query language (Dataset API or SQL), any structured query (incl. streaming queries) becomes a logical query plan.

In Spark Structured Streaming it is [IncrementalExecution](IncrementalExecution.md) that plans streaming queries for execution.

While [planning a streaming query for execution](IncrementalExecution.md#executedPlan) (aka _query planning_), `IncrementalExecution` uses the [state preparation rule](IncrementalExecution.md#state). The rule fills out the following physical operators with the execution-specific configuration (with [StatefulOperatorStateInfo](IncrementalExecution.md#nextStatefulOperationStateInfo) being the most important for stateful stream processing):

* [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md)

* [StateStoreRestoreExec](physical-operators/StateStoreRestoreExec.md)

* [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md)

* [StreamingDeduplicateExec](physical-operators/StreamingDeduplicateExec.md)

* [StreamingGlobalLimitExec](physical-operators/StreamingGlobalLimitExec.md)

* [StreamingSymmetricHashJoinExec](physical-operators/StreamingSymmetricHashJoinExec.md)

==== [[IncrementalExecution-shouldRunAnotherBatch]] Micro-Batch Stream Processing and Extra Non-Data Batch for StateStoreWriter Stateful Operators

In <<micro-batch-stream-processing.md#, Micro-Batch Stream Processing>> (with <<MicroBatchExecution.md#runActivatedStream, MicroBatchExecution>> engine), `IncrementalExecution` uses [shouldRunAnotherBatch](IncrementalExecution.md#shouldRunAnotherBatch) flag that allows [StateStoreWriters](physical-operators/StateStoreWriter.md) stateful physical operators to [indicate whether the last batch execution requires another non-data batch](physical-operators/StateStoreWriter.md#shouldRunAnotherBatch).

The following `StateStoreWriters` redefine `shouldRunAnotherBatch` flag.

[[StateStoreWriters-shouldRunAnotherBatch]]
.StateStoreWriters and shouldRunAnotherBatch Flag
[cols="30,70",options="header",width="100%"]
|===
| StateStoreWriter
| shouldRunAnotherBatch Flag

| [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md)
a| [[shouldRunAnotherBatch-FlatMapGroupsWithStateExec]] Based on [GroupStateTimeout](physical-operators/FlatMapGroupsWithStateExec.md#shouldRunAnotherBatch)

| <<StateStoreSaveExec.md#, StateStoreSaveExec>>
a| [[shouldRunAnotherBatch-StateStoreSaveExec]] Based on <<StateStoreSaveExec.md#shouldRunAnotherBatch, OutputMode and event-time watermark>>

| <<physical-operators/StreamingDeduplicateExec.md#, StreamingDeduplicateExec>>
a| [[shouldRunAnotherBatch-StreamingDeduplicateExec]] Based on <<physical-operators/StreamingDeduplicateExec.md#shouldRunAnotherBatch, event-time watermark>>

| <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>>
a| [[shouldRunAnotherBatch-StreamingSymmetricHashJoinExec]] Based on <<physical-operators/StreamingSymmetricHashJoinExec.md#shouldRunAnotherBatch, event-time watermark>>

|===

## <span id="StateStoreRDD"> StateStoreRDD

Right after [query planning](#IncrementalExecution), a stateful streaming query (a single micro-batch actually) becomes an RDD with one or more [StateStoreRDD](StateStoreRDD.md)s.

You can find the `StateStoreRDDs` of a streaming query in the RDD lineage.

```text
scala> :type streamingQuery
org.apache.spark.sql.streaming.StreamingQuery

scala> streamingQuery.explain
== Physical Plan ==
*(4) HashAggregate(keys=[window#13-T0ms, value#3L], functions=[count(1)])
+- StateStoreSave [window#13-T0ms, value#3L], state info [ checkpoint = file:/tmp/checkpoint-counts/state, runId = 1dec2d81-f2d0-45b9-8f16-39ede66e13e7, opId = 0, ver = 1, numPartitions = 1], Append, 10000, 2
   +- *(3) HashAggregate(keys=[window#13-T0ms, value#3L], functions=[merge_count(1)])
      +- StateStoreRestore [window#13-T0ms, value#3L], state info [ checkpoint = file:/tmp/checkpoint-counts/state, runId = 1dec2d81-f2d0-45b9-8f16-39ede66e13e7, opId = 0, ver = 1, numPartitions = 1], 2
         +- *(2) HashAggregate(keys=[window#13-T0ms, value#3L], functions=[merge_count(1)])
            +- Exchange hashpartitioning(window#13-T0ms, value#3L, 1)
               +- *(1) HashAggregate(keys=[window#13-T0ms, value#3L], functions=[partial_count(1)])
                  +- *(1) Project [named_struct(start, precisetimestampconversion(((((CASE WHEN (cast(CEIL((cast((precisetimestampconversion(time#2-T0ms, TimestampType, LongType) - 0) as double) / 5000000.0)) as double) = (cast((precisetimestampconversion(time#2-T0ms, TimestampType, LongType) - 0) as double) / 5000000.0)) THEN (CEIL((cast((precisetimestampconversion(time#2-T0ms, TimestampType, LongType) - 0) as double) / 5000000.0)) + 1) ELSE CEIL((cast((precisetimestampconversion(time#2-T0ms, TimestampType, LongType) - 0) as double) / 5000000.0)) END + 0) - 1) * 5000000) + 0), LongType, TimestampType), end, precisetimestampconversion(((((CASE WHEN (cast(CEIL((cast((precisetimestampconversion(time#2-T0ms, TimestampType, LongType) - 0) as double) / 5000000.0)) as double) = (cast((precisetimestampconversion(time#2-T0ms, TimestampType, LongType) - 0) as double) / 5000000.0)) THEN (CEIL((cast((precisetimestampconversion(time#2-T0ms, TimestampType, LongType) - 0) as double) / 5000000.0)) + 1) ELSE CEIL((cast((precisetimestampconversion(time#2-T0ms, TimestampType, LongType) - 0) as double) / 5000000.0)) END + 0) - 1) * 5000000) + 5000000), LongType, TimestampType)) AS window#13-T0ms, value#3L]
                     +- *(1) Filter isnotnull(time#2-T0ms)
                        +- EventTimeWatermark time#2: timestamp, interval
                           +- LocalTableScan <empty>, [time#2, value#3L]

import org.apache.spark.sql.execution.streaming.{StreamExecution, StreamingQueryWrapper}
val se = streamingQuery.asInstanceOf[StreamingQueryWrapper].streamingQuery

scala> :type se
org.apache.spark.sql.execution.streaming.StreamExecution

scala> :type se.lastExecution
org.apache.spark.sql.execution.streaming.IncrementalExecution

val rdd = se.lastExecution.toRdd
scala> rdd.toDebugString
res3: String =
(1) MapPartitionsRDD[39] at toRdd at <console>:40 []
 |  StateStoreRDD[38] at toRdd at <console>:40 [] // <-- here
 |  MapPartitionsRDD[37] at toRdd at <console>:40 []
 |  StateStoreRDD[36] at toRdd at <console>:40 [] // <-- here
 |  MapPartitionsRDD[35] at toRdd at <console>:40 []
 |  ShuffledRowRDD[17] at start at <pastie>:67 []
 +-(1) MapPartitionsRDD[16] at start at <pastie>:67 []
    |  MapPartitionsRDD[15] at start at <pastie>:67 []
    |  MapPartitionsRDD[14] at start at <pastie>:67 []
    |  MapPartitionsRDD[13] at start at <pastie>:67 []
    |  ParallelCollectionRDD[12] at start at <pastie>:67 []
```

## <span id="StateStoreCoordinator"> StateStoreCoordinator RPC Endpoint, StateStoreRDD and Preferred Locations

Since execution of a stateful streaming query happens on Spark executors whereas planning is on the driver, Spark Structured Streaming uses RPC environment for tracking locations of the state stores in use. That makes the tasks (of a structured query) to be scheduled where the state (of a partition) is.

When planned for execution, the `StateStoreRDD` is first asked for the [preferred locations of a partition](StateStoreRDD.md#getPreferredLocations) (which happens on the driver) that are later used to [compute it](StateStoreRDD.md#compute) (on Spark executors).

Spark Structured Streaming uses RPC environment to keep track of [StateStore](spark-sql-streaming-StateStore.md)s (their [StateStoreProvider](spark-sql-streaming-StateStoreProvider.md) actually) for RDD planning.

Every time [StateStoreRDD](StateStoreRDD.md) is requested for the [preferred locations of a partition](StateStoreRDD.md#getPreferredLocations), it communicates with the [StateStoreCoordinator](spark-sql-streaming-StateStoreCoordinator.md) RPC endpoint that knows the locations of the required `StateStores` (per host and executor ID).

`StateStoreRDD` uses [StateStoreProviderId](spark-sql-streaming-StateStoreProviderId.md) with [StateStoreId](spark-sql-streaming-StateStoreId.md) to uniquely identify the [state store](spark-sql-streaming-StateStore.md) to use for (_associate with_) a stateful operator and a partition.

## State Management

The state in a stateful streaming query can be implicit or explicit.
