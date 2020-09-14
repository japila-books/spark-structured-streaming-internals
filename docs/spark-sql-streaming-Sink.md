== [[Sink]] Sink Contract -- Streaming Sinks for Micro-Batch Stream Processing

`Sink` is the <<contract, extension>> of the <<spark-sql-streaming-BaseStreamingSink.md#, BaseStreamingSink contract>> for <<implementations, streaming sinks>> that can <<addBatch, add batches to an output>>.

`Sink` is part of Data Source API V1 and used in <<spark-sql-streaming-micro-batch-stream-processing.md#, Micro-Batch Stream Processing>> only.

[[contract]]
.Sink Contract
[cols="30m,70",options="header",width="100%"]
|===
| Method
| Description

| addBatch
a| [[addBatch]]

[source, scala]
----
addBatch(
  batchId: Long,
  data: DataFrame): Unit
----

Adds a batch of data to the sink

Used exclusively when <<spark-sql-streaming-MicroBatchExecution.md#, MicroBatchExecution>> stream execution engine (<<spark-sql-streaming-micro-batch-stream-processing.md#, Micro-Batch Stream Processing>>) is requested to <<spark-sql-streaming-MicroBatchExecution.md#runBatch-addBatch, add a streaming batch to a sink (addBatch phase)>> while <<spark-sql-streaming-MicroBatchExecution.md#runActivatedStream, running an activated streaming query>>.

|===

[[implementations]]
.Sinks
[cols="30,70",options="header",width="100%"]
|===
| Sink
| Description

| <<spark-sql-streaming-FileStreamSink.md#, FileStreamSink>>
| [[FileStreamSink]] Used in file-based data sources (`FileFormat`)

| <<spark-sql-streaming-ForeachBatchSink.md#, ForeachBatchSink>>
| [[ForeachBatchSink]] Used for <<DataStreamWriter.md#foreachBatch, DataStreamWriter.foreachBatch>> streaming operator

| <<spark-sql-streaming-KafkaSink.md#, KafkaSink>>
| [[KafkaSink]] Used for <<spark-sql-streaming-kafka-data-source.md#, kafka>> output format

| <<spark-sql-streaming-MemorySink.md#, MemorySink>>
| [[MemorySink]] Used for `memory` output format

|===
