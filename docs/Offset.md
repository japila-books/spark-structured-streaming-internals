# Offset -- Read Position of Streaming Query

`Offset` is the <<contract, base>> of <<extensions, stream positions>> that represent progress of a streaming query in <<json, json>> format.

[[contract]]
.Offset Contract (Abstract Methods Only)
[cols="30m,70",options="header",width="100%"]
|===
| Method
| Description

| json
a| [[json]]

[source, java]
----
String json()
----

Converts the offset to JSON format (JSON-encoded offset)

Used when:

* `MicroBatchExecution` stream execution engine is requested to [construct the next streaming micro-batch](micro-batch-execution/MicroBatchExecution.md#constructNextBatch) and [run a streaming micro-batch](micro-batch-execution/MicroBatchExecution.md#runBatch) (with [MicroBatchReader](micro-batch-execution/MicroBatchReader.md) sources)

* `OffsetSeq` is requested for the [textual representation](OffsetSeq.md#toString)

* `OffsetSeqLog` is requested to [serialize metadata (write metadata in serialized format)](OffsetSeqLog.md#serialize)

* `ProgressReporter` is requested to [record trigger offsets](monitoring/ProgressReporter.md#recordTriggerOffsets)

* `ContinuousExecution` stream execution engine is requested to [run a streaming query in continuous mode](continuous-execution/ContinuousExecution.md#runContinuous) and [commit an epoch](continuous-execution/ContinuousExecution.md#commit)

|===

[[extensions]]
.Offsets
[cols="30,70",options="header",width="100%"]
|===
| Offset
| Description

| ContinuousMemoryStreamOffset
| [[ContinuousMemoryStreamOffset]]

| FileStreamSourceOffset
| [[FileStreamSourceOffset]]

| [KafkaSourceOffset](datasources/kafka/KafkaSourceOffset.md)
| [[KafkaSourceOffset]]

| LongOffset
| [[LongOffset]]

| RateStreamOffset
| [[RateStreamOffset]]

| SerializedOffset
| [[SerializedOffset]] JSON-encoded offset that is used when loading an offset from an external storage, e.g. from [checkpoint](offsets-and-metadata-checkpointing.md) after restart

| TextSocketOffset
| [[TextSocketOffset]]

|===
