== [[StateStoreWriter]] StateStoreWriter Contract -- Stateful Physical Operators That Write to State Store

`StateStoreWriter` is the extension of the <<spark-sql-streaming-StatefulOperator.md#, StatefulOperator Contract>> for <<implementations, physical operators>> that write to a state store and collect the <<metrics, write metrics>> for <<getProgress, execution progress reporting>>.

[[implementations]]
.StateStoreWriters
[cols="30,70",options="header",width="100%"]
|===
| StateStoreWriter
| Description

| [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md)
| [[FlatMapGroupsWithStateExec]]

| <<spark-sql-streaming-StateStoreSaveExec.md#, StateStoreSaveExec>>
| [[StateStoreSaveExec]]

| <<spark-sql-streaming-StreamingDeduplicateExec.md#, StreamingDeduplicateExec>>
| [[StreamingDeduplicateExec]]

| <<spark-sql-streaming-StreamingGlobalLimitExec.md#, StreamingGlobalLimitExec>>
| [[StreamingGlobalLimitExec]]

| <<spark-sql-streaming-StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>>
| [[StreamingSymmetricHashJoinExec]]

|===

=== [[metrics]] Performance Metrics (SQLMetrics)

[cols="30,70",options="header",width="100%"]
|===
| Name (in web UI)
| Description

| number of output rows
| [[numOutputRows]]

| number of total state rows
| [[numTotalStateRows]]

| number of updated state rows
| [[numUpdatedStateRows]]

| total time to update rows
| [[allUpdatesTimeMs]]

| total time to remove rows
| [[allRemovalsTimeMs]]

| time to commit changes
| [[commitTimeMs]]

| memory used by state
| [[stateMemory]]

|===

=== [[setStoreMetrics]] Setting StateStore-Specific Metrics for Stateful Physical Operator -- `setStoreMetrics` Method

[source, scala]
----
setStoreMetrics(store: StateStore): Unit
----

`setStoreMetrics` requests the specified <<spark-sql-streaming-StateStore.md#, StateStore>> for the <<spark-sql-streaming-StateStore.md#metrics, metrics>> and records the following metrics of a physical operator:

* <<numTotalStateRows, numTotalStateRows>> as the <<spark-sql-streaming-StateStoreMetrics.md#numKeys, number of keys>>

* <<stateMemory, stateMemory>> as the <<spark-sql-streaming-StateStoreMetrics.md#memoryUsedBytes, memory used (in bytes)>>

`setStoreMetrics` records the <<spark-sql-streaming-StateStoreMetrics.md#customMetrics, custom metrics>>.

[NOTE]
====
`setStoreMetrics` is used when the following physical operators are executed:

* [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md)
* <<spark-sql-streaming-StateStoreSaveExec.md#, StateStoreSaveExec>>
* <<spark-sql-streaming-StreamingDeduplicateExec.md#, StreamingDeduplicateExec>>
* <<spark-sql-streaming-StreamingGlobalLimitExec.md#, StreamingGlobalLimitExec>>
====

=== [[getProgress]] `getProgress` Method

[source, scala]
----
getProgress(): StateOperatorProgress
----

`getProgress`...FIXME

`getProgress` is used when `ProgressReporter` is requested to [extractStateOperatorMetrics](ProgressReporter.md#extractStateOperatorMetrics) (when `MicroBatchExecution` is requested to [run the activated streaming query](spark-sql-streaming-MicroBatchExecution.md#runActivatedStream)).

## <span id="shouldRunAnotherBatch"> Checking Out Whether Last Batch Execution Requires Another Non-Data Batch or Not

```scala
shouldRunAnotherBatch(
  newMetadata: OffsetSeqMetadata): Boolean
```

`shouldRunAnotherBatch` is negative (`false`) by default (to indicate that another non-data batch is not required given the <<spark-sql-streaming-OffsetSeqMetadata.md#, OffsetSeqMetadata>> with the event-time watermark and the batch timestamp).

`shouldRunAnotherBatch` is used when `IncrementalExecution` is requested to <<spark-sql-streaming-IncrementalExecution.md#shouldRunAnotherBatch, check out whether the last batch execution requires another batch>> (when `MicroBatchExecution` is requested to <<spark-sql-streaming-MicroBatchExecution.md#runActivatedStream, run the activated streaming query>>).

=== [[stateStoreCustomMetrics]] `stateStoreCustomMetrics` Internal Method

[source, scala]
----
stateStoreCustomMetrics: Map[String, SQLMetric]
----

`stateStoreCustomMetrics`...FIXME

NOTE: `stateStoreCustomMetrics` is used when `StateStoreWriter` is requested for the <<metrics, metrics>> and <<getProgress, getProgress>>.

=== [[timeTakenMs]] `timeTakenMs` Method

[source, scala]
----
timeTakenMs(body: => Unit): Long
----

`timeTakenMs`...FIXME

NOTE: `timeTakenMs` is used when...FIXME
