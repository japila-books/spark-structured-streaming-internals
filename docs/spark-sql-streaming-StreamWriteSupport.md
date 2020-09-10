== [[StreamWriteSupport]] StreamWriteSupport Contract -- Writable Streaming Data Sources

`StreamWriteSupport` is the <<contract, abstraction>> of <<implementations, DataSourceV2 sinks>> that <<createStreamWriter, create StreamWriters>> for streaming write (when used in streaming queries in <<spark-sql-streaming-MicroBatchExecution.md#, MicroBatchExecution>> and <<spark-sql-streaming-ContinuousExecution.md#, ContinuousExecution>>).

[[contract]][[createStreamWriter]]
[source, java]
----
StreamWriter createStreamWriter(
  String queryId,
  StructType schema,
  OutputMode mode,
  DataSourceOptions options)
----

`createStreamWriter` creates a <<spark-sql-streaming-StreamWriter.md#, StreamWriter>> for streaming write and is used when the <<spark-sql-streaming-StreamExecution.md#queryExecutionThread, stream execution thread for a streaming query>> is <<spark-sql-streaming-StreamExecution.md#start, started>> and requests the stream execution engines to start, i.e.

* `ContinuousExecution` is requested to <<spark-sql-streaming-ContinuousExecution.md#runContinuous, runContinuous>>

* `MicroBatchExecution` is requested to <<spark-sql-streaming-MicroBatchExecution.md#runBatch, run a single streaming batch>>

[[implementations]]
.StreamWriteSupports
[cols="1,2",options="header",width="100%"]
|===
| StreamWriteSupport
| Description

| <<spark-sql-streaming-ConsoleSinkProvider.md#, ConsoleSinkProvider>>
| [[ConsoleSinkProvider]] Streaming sink for `console` data source format

| <<spark-sql-streaming-ForeachWriterProvider.md#, ForeachWriterProvider>>
| [[ForeachWriterProvider]]

| <<spark-sql-streaming-KafkaSourceProvider.md#, KafkaSourceProvider>>
| [[KafkaSourceProvider]]

| <<spark-sql-streaming-MemorySinkV2.md#, MemorySinkV2>>
| [[MemorySinkV2]]
|===
