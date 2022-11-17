# Offset &mdash; Read Position of Streaming Query

`Offset` is an [abstraction](#contract) of [stream positions](#implementations).

!!! note
    There are two `Offset` abstractions and new streaming data sources should use Data Source v2 API.

## Contract

### <span id="json"> JSON Representation

```java
String json()
```

JSON-encoded representation of the offset

Used when:

* `MicroBatchExecution` stream execution engine is requested to [construct the next streaming micro-batch](micro-batch-execution/MicroBatchExecution.md#constructNextBatch) and [run a streaming micro-batch](micro-batch-execution/MicroBatchExecution.md#runBatch)
* `OffsetSeq` is requested for the [textual representation](OffsetSeq.md#toString)
* `OffsetSeqLog` is requested to [serialize metadata (write metadata in serialized format)](OffsetSeqLog.md#serialize)
* `ProgressReporter` is requested to [record trigger offsets](ProgressReporter.md#recordTriggerOffsets)
* `ContinuousExecution` stream execution engine is requested to [run a streaming query in continuous mode](continuous-execution/ContinuousExecution.md#runContinuous) and [commit an epoch](continuous-execution/ContinuousExecution.md#commit)

## Implementations

* ContinuousMemoryStreamOffset
* FileStreamSourceOffset
* [KafkaSourceOffset](kafka/KafkaSourceOffset.md)
* LongOffset
* SerializedOffset
* TextSocketOffset
