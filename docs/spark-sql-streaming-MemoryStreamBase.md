== [[MemoryStreamBase]] MemoryStreamBase Contract -- Base Contract for Memory Sources

`MemoryStreamBase` is the <<contract, base>> of the <<spark-sql-streaming-BaseStreamingSource.md#, BaseStreamingSource contract>> for <<implementations, memory sources>> that can <<addData, add data>>.

[[contract]]
.MemoryStreamBase Contract
[cols="30m,70",options="header",width="100%"]
|===
| Method
| Description

| addData
a| [[addData]]

[source, scala]
----
addData(
  data: TraversableOnce[A]): Offset
----

| logicalPlan
a| [[logicalPlan]]

[source, scala]
----
logicalPlan: LogicalPlan
----

|===

[[implementations]]
.MemoryStreamBases
[cols="30,70",options="header",width="100%"]
|===
| MemoryStreamBase
| Description

| <<spark-sql-streaming-ContinuousMemoryStream.md#, ContinuousMemoryStream>>
| [[ContinuousMemoryStream]]

| <<spark-sql-streaming-MemoryStream.md#, MemoryStream>>
| [[MemoryStream]] <<spark-sql-streaming-MicroBatchReader.md#, MicroBatchReader>> for <<micro-batch-stream-processing.md#, Micro-Batch Stream Processing>>

|===

=== [[creating-instance]] Creating MemoryStreamBase Instance

`MemoryStreamBase` takes the following to be created:

* [[sqlContext]] `SQLContext`

NOTE: `MemoryStreamBase` is a Scala abstract class and cannot be <<creating-instance, created>> directly. It is created indirectly for the <<implementations, concrete MemoryStreamBases>>.

=== [[toDS]] Creating Streaming Dataset -- `toDS` Method

[source, scala]
----
toDS(): Dataset[A]
----

`toDS` simply creates a `Dataset` (for the <<sqlContext, sqlContext>> and the <<logicalPlan, logicalPlan>>)

=== [[toDF]] Creating Streaming DataFrame -- `toDF` Method

[source, scala]
----
toDF(): DataFrame
----

`toDF` simply creates a `Dataset` of rows (for the <<sqlContext, sqlContext>> and the <<logicalPlan, logicalPlan>>)

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| attributes
a| [[attributes]] Schema attributes of the <<encoder, encoder>> (`Seq[AttributeReference]`)

Used when...FIXME

| encoder
a| [[encoder]] Spark SQL's `ExpressionEncoder` for the data

Used when...FIXME

|===
