== [[MemorySinkBase]] MemorySinkBase Contract -- Base Contract for Memory Sinks

`MemorySinkBase` is the <<contract, extension>> of the <<spark-sql-streaming-BaseStreamingSink.adoc#, BaseStreamingSink contract>> for <<implementations, memory sinks>> that manage <<allData, all data>> in memory.

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

| <<spark-sql-streaming-MemorySink.adoc#, MemorySink>>
| [[MemorySink]] <<spark-sql-streaming-Sink.adoc#, Streaming sink>> for <<spark-sql-streaming-micro-batch-stream-processing.adoc#, Micro-Batch Stream Processing>> (based on Data Source API V1)

| <<spark-sql-streaming-MemorySinkV2.adoc#, MemorySinkV2>>
| [[MemorySinkV2]] <<spark-sql-streaming-StreamWriteSupport.adoc#, Writable streaming sink>> for <<spark-sql-streaming-continuous-stream-processing.adoc#, Continuous Stream Processing>> (based on Data Source API V2)

|===
