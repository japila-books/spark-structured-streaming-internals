== [[MicroBatchReader]] MicroBatchReader Contract -- Data Source Readers in Micro-Batch Stream Processing (Data Source API V2)

`MicroBatchReader` is the <<contract, extension>> of Spark SQL's `DataSourceReader` (and <<spark-sql-streaming-BaseStreamingSource.md#, BaseStreamingSource>>) contracts for <<implementations, data source readers>> in <<spark-sql-streaming-micro-batch-stream-processing.md#, Micro-Batch Stream Processing>>.

`MicroBatchReader` is part of the novel Data Source API V2 in Spark SQL.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-data-source-api-v2.html[Data Source API V2] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

[[contract]]
.MicroBatchReader Contract
[cols="1m,3",options="header",width="100%"]
|===
| Method
| Description

| commit
a| [[commit]]

[source, java]
----
void commit(Offset end)
----

Used when...FIXME

| deserializeOffset
a| [[deserializeOffset]]

[source, java]
----
Offset deserializeOffset(String json)
----

Deserializes <<spark-sql-streaming-Offset.md#, offset>> (from JSON format)

Used when...FIXME

| getEndOffset
a| [[getEndOffset]]

[source, java]
----
Offset getEndOffset()
----

End <<spark-sql-streaming-Offset.md#, offset>> of this reader

Used when...FIXME

| getStartOffset
a| [[getStartOffset]]

[source, java]
----
Offset getStartOffset()
----

Start (beginning) <<spark-sql-streaming-Offset.md#, offsets>> of this reader

Used when...FIXME

| setOffsetRange
a| [[setOffsetRange]]

[source, java]
----
void setOffsetRange(
  Optional<Offset> start,
  Optional<Offset> end)
----

Sets the desired offset range for input partitions created from this reader (for data scan)

Used when...FIXME

|===

[[implementations]]
.MicroBatchReaders
[cols="1,2",options="header",width="100%"]
|===
| MicroBatchReader
| Description

| <<spark-sql-streaming-KafkaMicroBatchReader.md#, KafkaMicroBatchReader>>
| [[KafkaMicroBatchReader]]

| <<spark-sql-streaming-MemoryStream.md#, MemoryStream>>
| [[MemoryStream]]

| RateStreamMicroBatchReader
| [[RateStreamMicroBatchReader]]

| TextSocketMicroBatchReader
| [[TextSocketMicroBatchReader]]

|===
