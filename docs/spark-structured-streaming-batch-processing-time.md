# Batch Processing Time

**Batch Processing Time** (aka _Batch Timeout Threshold_) is the processing time (_processing timestamp_) of the current streaming batch.

The following standard functions (and their Catalyst expressions) allow accessing the batch processing time in [Micro-Batch Stream Processing](micro-batch-stream-processing.md):

* `now`, `current_timestamp`, and `unix_timestamp` functions (`CurrentTimestamp`)

* `current_date` function (`CurrentDate`)

!!! note
    `CurrentTimestamp` or `CurrentDate` expressions are not supported in [Continuous Stream Processing](spark-sql-streaming-continuous-stream-processing.md).

## Internals

[GroupStateImpl](GroupStateImpl.md) is given the batch processing time when created for a [streaming query](GroupStateImpl.md#createForStreaming) (that is actually the [batch processing time](physical-operators/FlatMapGroupsWithStateExec.md#batchTimestampMs) of the [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator).

When created, `FlatMapGroupsWithStateExec` physical operator has the processing time undefined and set to the current timestamp in the <<spark-sql-streaming-IncrementalExecution.md#state, state preparation rule>> every streaming batch.

The current timestamp (and other batch-specific configurations) is given as the <<spark-sql-streaming-IncrementalExecution.md#offsetSeqMetadata, OffsetSeqMetadata>> (as part of the query planning phase) when a [stream execution engine](StreamExecution.md) does the following:

* `MicroBatchExecution` is requested to <<MicroBatchExecution.md#constructNextBatch, construct a next streaming micro-batch>> in <<micro-batch-stream-processing.md#, Micro-Batch Stream Processing>>

* In <<spark-sql-streaming-continuous-stream-processing.md#, Continuous Stream Processing>> the base `StreamExecution` is requested to [run stream processing](StreamExecution.md#runStream) and initializes `OffsetSeqMetadata` to ``0``s.
