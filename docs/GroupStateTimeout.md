# GroupStateTimeout

`GroupStateTimeout` represents an **aggregation state timeout** that defines when a [GroupState](GroupState.md) can be considered **timed-out** (_expired_) in [Arbitrary Stateful Streaming Aggregation](arbitrary-stateful-streaming-aggregation/index.md).

`GroupStateTimeout` is used with the following `KeyValueGroupedDataset` operations:

* `mapGroupsWithState`
* `flatMapGroupsWithState`

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

`batchTimestampMs` is defined when [IncrementalExecution](IncrementalExecution.md) is created (with the [state](IncrementalExecution.md#state)). `IncrementalExecution` is given `OffsetSeqMetadata` when `StreamExecution` is requested to [run a streaming batch](micro-batch-execution/MicroBatchExecution.md#runBatch).

|===
