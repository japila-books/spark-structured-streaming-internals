== [[BaseStreamingSource]] BaseStreamingSource Contract -- Base of Streaming Readers and Sources

`BaseStreamingSource` is the <<contract, abstraction>> of <<extensions, streaming readers and sources>> that can be <<stop, stopped>>.

The main purpose of `BaseStreamingSource` is to share a common abstraction between the former Data Source API V1 (<<Source, Source API>>) and the modern Data Source API V2 (until Spark Structured Streaming migrates to the Data Source API V2 fully).

[[contract]]
.BaseStreamingSource Contract
[cols="30m,70",options="header",width="100%"]
|===
| Method
| Description

| stop
a| [[stop]]

[source, java]
----
void stop()
----

Stops the streaming source or reader (and frees up any resources it may have allocated)

Used when:

* `StreamExecution` is requested to [stop streaming sources and readers](StreamExecution.md#stopSources)

* `DataStreamReader` is requested to <<spark-sql-streaming-DataStreamReader.md#load, load data from a MicroBatchReadSupport data source>> (for read schema)

|===

[[extensions]]
.BaseStreamingSources (Extensions Only)
[cols="30,70",options="header",width="100%"]
|===
| BaseStreamingSource
| Description

| <<spark-sql-streaming-ContinuousReader.md#, ContinuousReader>>
| [[ContinuousReader]] Data source readers in <<spark-sql-streaming-continuous-stream-processing.md#, Continuous Stream Processing>> (based on Data Source API V2)

| <<spark-sql-streaming-MemoryStreamBase.md#, MemoryStreamBase>>
| [[MemoryStreamBase]] Base implementation of <<spark-sql-streaming-ContinuousMemoryStream.md#, ContinuousMemoryStream>> (for <<spark-sql-streaming-continuous-stream-processing.md#, Continuous Stream Processing>>) and <<spark-sql-streaming-MemoryStream.md#, MemoryStream>> (for <<micro-batch-stream-processing.md#, Micro-Batch Stream Processing>>)

| <<spark-sql-streaming-MicroBatchReader.md#, MicroBatchReader>>
| [[MicroBatchReader]] Data source readers in <<micro-batch-stream-processing.md#, Micro-Batch Stream Processing>> (based on Data Source API V2)

| [Source](Source.md)
| [[Source]] Streaming sources for <<micro-batch-stream-processing.md#, Micro-Batch Stream Processing>> (based on Data Source API V1)

|===
