== Batch Processing Time

*Batch Processing Time* (aka *Batch Timeout Threshold*) is the processing time (_processing timestamp_) of the current streaming batch.

The following standard functions (and their Catalyst expressions) allow accessing the batch processing time in <<spark-sql-streaming-micro-batch-stream-processing.md#, Micro-Batch Stream Processing>>:

* `now`, `current_timestamp`, and `unix_timestamp` functions (`CurrentTimestamp`)

* `current_date` function (`CurrentDate`)

NOTE: `CurrentTimestamp` or `CurrentDate` expressions are not supported in <<spark-sql-streaming-continuous-stream-processing.md#, Continuous Stream Processing>>.

=== [[internals]] Internals

[GroupStateImpl](GroupStateImpl.md) is given the batch processing time when created for a [streaming query](GroupStateImpl.md#createForStreaming) (that is actually the [batch processing time](physical-operators/FlatMapGroupsWithStateExec.md#batchTimestampMs) of the [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator).

When created, `FlatMapGroupsWithStateExec` physical operator has the processing time undefined and set to the current timestamp in the <<spark-sql-streaming-IncrementalExecution.md#state, state preparation rule>> every streaming batch.

The current timestamp (and other batch-specific configurations) is given as the <<spark-sql-streaming-IncrementalExecution.md#offsetSeqMetadata, OffsetSeqMetadata>> (as part of the query planning phase) when a <<spark-sql-streaming-StreamExecution.md#, stream execution engine>> does the following:

* `MicroBatchExecution` is requested to <<spark-sql-streaming-MicroBatchExecution.md#constructNextBatch, construct a next streaming micro-batch>> in <<spark-sql-streaming-micro-batch-stream-processing.md#, Micro-Batch Stream Processing>>

* In <<spark-sql-streaming-continuous-stream-processing.md#, Continuous Stream Processing>> the base `StreamExecution` is requested to <<spark-sql-streaming-StreamExecution.md#runStream, run stream processing>> and initializes `OffsetSeqMetadata` to ``0``s.
