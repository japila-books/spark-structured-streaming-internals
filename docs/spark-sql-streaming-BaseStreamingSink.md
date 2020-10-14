== [[BaseStreamingSink]] BaseStreamingSink Contract -- Base of Streaming Writers and Sinks

`BaseStreamingSink` is the abstraction of <<extensions, streaming writers and sinks>> with the only purpose of sharing a common abstraction between the former Data Source API V1 (<<Sink, Sink API>>) and the modern Data Source API V2 (until Spark Structured Streaming migrates to the Data Source API V2 fully).

`BaseStreamingSink` defines no methods.

[[extensions]]
.BaseStreamingSinks (Extensions Only)
[cols="30,70",options="header",width="100%"]
|===
| BaseStreamingSink
| Description

| [MemorySinkBase](spark-sql-streaming-MemorySinkBase.md)
| [[MemorySinkBase]] Base contract for data sinks in <<spark-sql-streaming-memory-data-source.md#, memory data source>>

| [Sink](Sink.md)
| [[Sink]] Streaming sinks for <<micro-batch-stream-processing.md#, Micro-Batch Stream Processing>> (based on Data Source API V1)

| [StreamWriteSupport](spark-sql-streaming-StreamWriteSupport.md)
| [[StreamWriteSupport]] Data source writers (based on Data Source API V2)

|===
