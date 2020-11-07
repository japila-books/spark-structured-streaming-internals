# MicroBatchReader -- Data Source Readers in Micro-Batch Stream Processing (Data Source API V2)

`MicroBatchReader` is the <<contract, extension>> of Spark SQL's `DataSourceReader` abstraction for <<implementations, data source readers>> in [Micro-Batch Stream Processing](micro-batch-stream-processing.md).

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

Deserializes [offset](Offset.md) (from JSON format)

Used when...FIXME

| getEndOffset
a| [[getEndOffset]]

[source, java]
----
Offset getEndOffset()
----

End [offset](Offset.md) of this reader

Used when...FIXME

| getStartOffset
a| [[getStartOffset]]

[source, java]
----
Offset getStartOffset()
----

Start (beginning) [offset](Offset.md) of this reader

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

| [KafkaMicroBatchReader](datasources/kafka/KafkaMicroBatchReader.md)
| [[KafkaMicroBatchReader]]

| [MemoryStream](datasources/memory/MemoryStream.md)
| [[MemoryStream]]

| RateStreamMicroBatchReader
| [[RateStreamMicroBatchReader]]

| TextSocketMicroBatchReader
| [[TextSocketMicroBatchReader]]

|===
