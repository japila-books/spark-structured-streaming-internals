== [[TriggerExecutor]] TriggerExecutor

`TriggerExecutor` is the <<contract, interface>> for *trigger executors* that `StreamExecution` link:spark-sql-streaming-StreamExecution.md#runStream[uses to execute a batch runner].

[[batchRunner]]
NOTE: *Batch runner* is an executable code that is executed at regular intervals. It is also called a *trigger handler*.

[[contract]]
[[execute]]
[source, scala]
----
package org.apache.spark.sql.execution.streaming

trait TriggerExecutor {
  def execute(batchRunner: () => Boolean): Unit
}
----

NOTE: `StreamExecution` reports a `IllegalStateException` when link:spark-sql-streaming-StreamExecution.md#triggerExecutor[TriggerExecutor] is different from the <<available-implementations, two built-in implementations>>: `OneTimeExecutor`
or `ProcessingTimeExecutor`.

[[available-implementations]]
.TriggerExecutor's Available Implementations
[cols="1,2",options="header",width="100%"]
|===
| TriggerExecutor
| Description

| [[OneTimeExecutor]] `OneTimeExecutor`
| Executes `batchRunner` exactly once.

| [[ProcessingTimeExecutor]] `ProcessingTimeExecutor`
a| Executes `batchRunner` at regular intervals (as defined using link:spark-sql-streaming-Trigger.md#ProcessingTime[ProcessingTime] and link:spark-sql-streaming-DataStreamWriter.md#trigger[DataStreamWriter.trigger] method).

[source, scala]
----
ProcessingTimeExecutor(
  processingTime: ProcessingTime,
  clock: Clock = new SystemClock())
----

NOTE: Processing terminates when `batchRunner` returns `false`.
|===

=== [[notifyBatchFallingBehind]] `notifyBatchFallingBehind` Method

CAUTION: FIXME
