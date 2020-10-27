# ContinuousReader &mdash; Data Source Readers in Continuous Stream Processing

`ContinuousReader` is the <<contract, extension>> of Spark SQL's `DataSourceReader` abstraction for <<implementations, data source readers>> in [Continuous Stream Processing](spark-sql-streaming-continuous-stream-processing.md).

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

Commits the specified [offset](Offset.md)

Used exclusively when `ContinuousExecution` is requested to <<ContinuousExecution.md#commit, commit an epoch>>

| deserializeOffset
a| [[deserializeOffset]]

[source, java]
----
Offset deserializeOffset(String json)
----

Deserializes an [offset](Offset.md) from JSON representation

Used when `ContinuousExecution` is requested to <<ContinuousExecution.md#runContinuous, run a streaming query>> and <<ContinuousExecution.md#commit, commit an epoch>>

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

Used exclusively when `ContinuousExecution` is requested to <<ContinuousExecution.md#addOffset, addOffset>>

| needsReconfiguration
a| [[needsReconfiguration]]

[source, java]
----
boolean needsReconfiguration()
----

Indicates that the reader needs reconfiguration (e.g. to generate new input partitions)

Used exclusively when `ContinuousExecution` is requested to <<ContinuousExecution.md#runContinuous, run a streaming query in continuous mode>>

| setStartOffset
a| [[setStartOffset]]

[source, java]
----
void setStartOffset(Optional<Offset> start)
----

Used exclusively when `ContinuousExecution` is requested to <<ContinuousExecution.md#runContinuous, run the streaming query in continuous mode>>.

|===

[[implementations]]
.ContinuousReaders
[cols="1,2",options="header",width="100%"]
|===
| ContinuousReader
| Description

| [ContinuousMemoryStream](spark-sql-streaming-ContinuousMemoryStream.md)
| [[ContinuousMemoryStream]]

| [KafkaContinuousReader](datasources/kafka/KafkaContinuousReader.md)
| [[KafkaContinuousReader]]

| [RateStreamContinuousReader](spark-sql-streaming-RateStreamContinuousReader.md)
| [[RateStreamContinuousReader]]

| TextSocketContinuousReader
| [[TextSocketContinuousReader]]

|===
