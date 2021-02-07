# MemorySinkBase

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

| [MemorySink](MemorySink.md)
| [[MemorySink]] [Streaming sink](../../Sink.md) for [Micro-Batch Stream Processing](../../micro-batch-execution/index.md) (based on Data Source API V1)

| [MemorySinkV2](MemorySinkV2.md)
| [[MemorySinkV2]] Streaming sink for [Continuous Stream Processing](../../continuous-stream-processing.md) (based on Data Source API V2)

|===
