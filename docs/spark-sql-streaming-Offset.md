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

* `MicroBatchExecution` stream execution engine is requested to <<spark-sql-streaming-MicroBatchExecution.md#constructNextBatch, construct the next streaming micro-batch>> and <<spark-sql-streaming-MicroBatchExecution.md#runBatch, run a streaming micro-batch>> (with <<spark-sql-streaming-MicroBatchReader.md#, MicroBatchReader>> sources)

* `OffsetSeq` is requested for the <<spark-sql-streaming-OffsetSeq.md#toString, textual representation>>

* `OffsetSeqLog` is requested to <<spark-sql-streaming-OffsetSeqLog.md#serialize, serialize metadata (write metadata in serialized format)>>

* `ProgressReporter` is requested to <<spark-sql-streaming-ProgressReporter.md#recordTriggerOffsets, record trigger offsets>>

* `ContinuousExecution` stream execution engine is requested to <<spark-sql-streaming-ContinuousExecution.md#runContinuous, run a streaming query in continuous mode>> and <<spark-sql-streaming-ContinuousExecution.md#commit, commit an epoch>>

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

| <<spark-sql-streaming-KafkaSourceOffset.md#, KafkaSourceOffset>>
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
