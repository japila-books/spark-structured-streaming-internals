== [[MemorySinkBase]] MemorySinkBase Contract -- Base Contract for Memory Sinks

`MemorySinkBase` is the <<contract, extension>> of the <<spark-sql-streaming-BaseStreamingSink.md#, BaseStreamingSink contract>> for <<implementations, memory sinks>> that manage <<allData, all data>> in memory.

[[contract]]
.MemorySinkBase Contract
[cols="30m,70",options="header",width="100%"]
|===
| Method
| Description

| allData
a| [[allData]]

[source, scala]
----
allData: Seq[Row]
----

| dataSinceBatch
a| [[dataSinceBatch]]

[source, scala]
----
dataSinceBatch(
  sinceBatchId: Long): Seq[Row]
----

| latestBatchData
a| [[latestBatchData]]

[source, scala]
----
latestBatchData: Seq[Row]
----

| latestBatchId
a| [[latestBatchId]]

[source, scala]
----
latestBatchId: Option[Long]
----

|===

[[implementations]]
.MemorySinkBases
[cols="30,70",options="header",width="100%"]
|===
| MemorySinkBase
| Description

| <<spark-sql-streaming-MemorySink.md#, MemorySink>>
| [[MemorySink]] <<spark-sql-streaming-Sink.md#, Streaming sink>> for <<micro-batch-stream-processing.md#, Micro-Batch Stream Processing>> (based on Data Source API V1)

| <<spark-sql-streaming-MemorySinkV2.md#, MemorySinkV2>>
| [[MemorySinkV2]] <<spark-sql-streaming-StreamWriteSupport.md#, Writable streaming sink>> for <<spark-sql-streaming-continuous-stream-processing.md#, Continuous Stream Processing>> (based on Data Source API V2)

|===
