# ContinuousReadSupport

`ContinuousReadSupport` is the <<contract, extension>> of the `DataSourceV2` for <<implementations, data sources>> with a <<createContinuousReader, ContinuousReader>> for [Continuous Stream Processing](continuous-stream-processing.md).

[[contract]][[createContinuousReader]]
`ContinuousReadSupport` defines a single `createContinuousReader` method to create a <<spark-sql-streaming-ContinuousReader.md#, ContinuousReader>>.

[source, java]
----
ContinuousReader createContinuousReader(
  Optional<StructType> schema,
  String checkpointLocation,
  DataSourceOptions options)
----

`createContinuousReader` is used when:

* `ContinuousExecution` is requested to <<ContinuousExecution.md#runContinuous, run a streaming query>> (and finds [ContinuousExecutionRelations](ContinuousExecutionRelation.md) in the [analyzed logical plan](ContinuousExecution.md#logicalPlan))

* `DataStreamReader` is requested to [create a streaming query for a ContinuousReadSupport data source](DataStreamReader.md#load)

[[implementations]]
.ContinuousReadSupports
[cols="30,70",options="header",width="100%"]
|===
| ContinuousReadSupport
| Description

| [ContinuousMemoryStream](datasources/memory/ContinuousMemoryStream.md)
| [[ContinuousMemoryStream]] Data source provider for `memory` format

| [KafkaSourceProvider](datasources/kafka/KafkaSourceProvider.md)
| [[KafkaSourceProvider]] Data source provider for `kafka` format

| <<spark-sql-streaming-RateStreamProvider.md#, RateStreamProvider>>
| [[RateStreamProvider]] Data source provider for `rate` format

| <<spark-sql-streaming-TextSocketSourceProvider.md#, TextSocketSourceProvider>>
| [[TextSocketSourceProvider]] Data source provider for `socket` format

|===
