# Sink

`Sink` is an [extension](#contract) of the `Table` abstraction for [streaming sinks](#implementations) that [add the batch results of a streaming query](#addBatch) in [Micro-Batch Stream Processing](micro-batch-execution/index.md).

!!! note
    `Sink` extends `Table` interface for the only purpose of making it compatible with Data Source V2. All `Table` methods simply throw an `IllegalStateException`.

## Contract

### <span id="addBatch"> Adding Batch

```scala
addBatch(
  batchId: Long,
  data: DataFrame): Unit
```

Adds a batch of data to the sink

Used when `MicroBatchExecution` stream execution engine is requested to [add a batch to a sink (addBatch phase)](micro-batch-execution/MicroBatchExecution.md#runBatch-addBatch) (while [running micro-batches of a streaming query](micro-batch-execution/MicroBatchExecution.md#runBatch))

## Implementations

* [FileStreamSink](datasources/file/FileStreamSink.md)
* [ForeachBatchSink](datasources/ForeachBatchSink.md)
* [KafkaSink](kafka/KafkaSink.md)
