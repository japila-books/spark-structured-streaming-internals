== [[GroupStateTimeout]] GroupStateTimeout -- Group State Timeout in Arbitrary Stateful Streaming Aggregation

`GroupStateTimeout` represents an *aggregation state timeout* that defines when a [GroupState](GroupState.md) can be considered *timed-out* (_expired_) in <<spark-sql-arbitrary-stateful-streaming-aggregation.adoc#, Arbitrary Stateful Streaming Aggregation>>.

`GroupStateTimeout` is used with the following <<spark-sql-streaming-KeyValueGroupedDataset.adoc#, KeyValueGroupedDataset>> operations:

* <<spark-sql-streaming-KeyValueGroupedDataset.adoc#mapGroupsWithState, mapGroupsWithState>>

* <<spark-sql-streaming-KeyValueGroupedDataset.adoc#flatMapGroupsWithState, flatMapGroupsWithState>>

[[extensions]]
.GroupStateTimeouts
[cols="30m,70",options="header",width="100%"]
|===
| GroupStateTimeout
| Description

| EventTimeTimeout
| [[EventTimeTimeout]] Timeout based on event time

Used when...FIXME

| NoTimeout
| [[NoTimeout]] No timeout

Used when...FIXME

| ProcessingTimeTimeout
a| [[ProcessingTimeTimeout]] Timeout based on processing time

[FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator requires that `batchTimestampMs` is specified when `ProcessingTimeTimeout` is used.

`batchTimestampMs` is defined when <<spark-sql-streaming-IncrementalExecution.adoc#, IncrementalExecution>> is created (with the <<spark-sql-streaming-IncrementalExecution.adoc#state, state>>). `IncrementalExecution` is given `OffsetSeqMetadata` when `StreamExecution` is requested to <<spark-sql-streaming-MicroBatchExecution.adoc#runBatch, run a streaming batch>>.

|===
