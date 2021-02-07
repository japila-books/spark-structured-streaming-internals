# ContinuousReader &mdash; Data Source Readers in Continuous Stream Processing

`ContinuousReader` is the <<contract, extension>> of Spark SQL's `DataSourceReader` abstraction for <<implementations, data source readers>> in [Continuous Stream Processing](index.md).

`ContinuousReader` is part of the novel Data Source API V2 in Spark SQL.

!!! TIP
    Read up on [Data Source API V2]({{ book.spark_sql }}/spark-sql-data-source-api-v2.html) in [The Internals of Spark SQL]({{ book.spark_sql }}) online book.

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

Commits the specified [offset](../Offset.md)

Used exclusively when `ContinuousExecution` is requested to [commit an epoch](ContinuousExecution.md#commit)

| deserializeOffset
a| [[deserializeOffset]]

[source, java]
----
Offset deserializeOffset(String json)
----

Deserializes an [offset](../Offset.md) from JSON representation

Used when `ContinuousExecution` is requested to [run a streaming query](ContinuousExecution.md#runContinuous) and [commit an epoch](ContinuousExecution.md#commit)

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

Used exclusively when `ContinuousExecution` is requested to [addOffset](ContinuousExecution.md#addOffset)

| needsReconfiguration
a| [[needsReconfiguration]]

[source, java]
----
boolean needsReconfiguration()
----

Indicates that the reader needs reconfiguration (e.g. to generate new input partitions)

Used exclusively when `ContinuousExecution` is requested to [run a streaming query in continuous mode](ContinuousExecution.md#runContinuous)

| setStartOffset
a| [[setStartOffset]]

[source, java]
----
void setStartOffset(Optional<Offset> start)
----

Used exclusively when `ContinuousExecution` is requested to [run the streaming query in continuous mode](ContinuousExecution.md#runContinuous).

|===

[[implementations]]
.ContinuousReaders
[cols="1,2",options="header",width="100%"]
|===
| ContinuousReader
| Description

| [ContinuousMemoryStream](../datasources/memory/ContinuousMemoryStream.md)
| [[ContinuousMemoryStream]]

| [KafkaContinuousReader](../datasources/kafka/KafkaContinuousReader.md)
| [[KafkaContinuousReader]]

| [RateStreamContinuousReader](../RateStreamContinuousReader.md)
| [[RateStreamContinuousReader]]

| TextSocketContinuousReader
| [[TextSocketContinuousReader]]

|===
