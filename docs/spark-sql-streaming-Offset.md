== [[Offset]] Offset -- Read Position of Streaming Query

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

* `MicroBatchExecution` stream execution engine is requested to <<MicroBatchExecution.md#constructNextBatch, construct the next streaming micro-batch>> and <<MicroBatchExecution.md#runBatch, run a streaming micro-batch>> (with <<spark-sql-streaming-MicroBatchReader.md#, MicroBatchReader>> sources)

* `OffsetSeq` is requested for the [textual representation](OffsetSeq.md#toString)

* `OffsetSeqLog` is requested to <<spark-sql-streaming-OffsetSeqLog.md#serialize, serialize metadata (write metadata in serialized format)>>

* `ProgressReporter` is requested to [record trigger offsets](monitoring/ProgressReporter.md#recordTriggerOffsets)

* `ContinuousExecution` stream execution engine is requested to <<ContinuousExecution.md#runContinuous, run a streaming query in continuous mode>> and <<ContinuousExecution.md#commit, commit an epoch>>

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
| [[SerializedOffset]] JSON-encoded offset that is used when loading an offset from an external storage, e.g. from <<spark-sql-streaming-offsets-and-metadata-checkpointing.md#, checkpoint>> after restart

| TextSocketOffset
| [[TextSocketOffset]]

|===
