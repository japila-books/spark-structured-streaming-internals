== [[ContinuousReader]] ContinuousReader Contract -- Data Source Readers in Continuous Stream Processing

`ContinuousReader` is the <<contract, extension>> of Spark SQL's `DataSourceReader` (and <<spark-sql-streaming-BaseStreamingSource.md#, BaseStreamingSource>>) contracts for <<implementations, data source readers>> in <<spark-sql-streaming-continuous-stream-processing.md#, Continuous Stream Processing>>.

`ContinuousReader` is part of the novel Data Source API V2 in Spark SQL.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-data-source-api-v2.html[Data Source API V2] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

[[contract]]
.ContinuousReader Contract
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

Commits the specified <<spark-sql-streaming-Offset.md#, offset>>

Used exclusively when `ContinuousExecution` is requested to <<spark-sql-streaming-ContinuousExecution.md#commit, commit an epoch>>

| deserializeOffset
a| [[deserializeOffset]]

[source, java]
----
Offset deserializeOffset(String json)
----

Deserializes an <<spark-sql-streaming-Offset.md#, offset>> from JSON representation

Used when `ContinuousExecution` is requested to <<spark-sql-streaming-ContinuousExecution.md#runContinuous, run a streaming query>> and <<spark-sql-streaming-ContinuousExecution.md#commit, commit an epoch>>

| getStartOffset
a| [[getStartOffset]]

[source, java]
----
Offset getStartOffset()
----

NOTE: Used exclusively in tests.

| mergeOffsets
a| [[mergeOffsets]]

[source, java]
----
Offset mergeOffsets(PartitionOffset[] offsets)
----

Used exclusively when `ContinuousExecution` is requested to <<spark-sql-streaming-ContinuousExecution.md#addOffset, addOffset>>

| needsReconfiguration
a| [[needsReconfiguration]]

[source, java]
----
boolean needsReconfiguration()
----

Indicates that the reader needs reconfiguration (e.g. to generate new input partitions)

Used exclusively when `ContinuousExecution` is requested to <<spark-sql-streaming-ContinuousExecution.md#runContinuous, run a streaming query in continuous mode>>

| setStartOffset
a| [[setStartOffset]]

[source, java]
----
void setStartOffset(Optional<Offset> start)
----

Used exclusively when `ContinuousExecution` is requested to <<spark-sql-streaming-ContinuousExecution.md#runContinuous, run the streaming query in continuous mode>>.

|===

[[implementations]]
.ContinuousReaders
[cols="1,2",options="header",width="100%"]
|===
| ContinuousReader
| Description

| <<spark-sql-streaming-ContinuousMemoryStream.md#, ContinuousMemoryStream>>
| [[ContinuousMemoryStream]]

| <<spark-sql-streaming-KafkaContinuousReader.md#, KafkaContinuousReader>>
| [[KafkaContinuousReader]]

| <<spark-sql-streaming-RateStreamContinuousReader.md#, RateStreamContinuousReader>>
| [[RateStreamContinuousReader]]

| TextSocketContinuousReader
| [[TextSocketContinuousReader]]

|===
