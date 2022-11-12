# ContinuousReadSupport

`ContinuousReadSupport` is the <<contract, extension>> of the `DataSourceV2` for <<implementations, data sources>> with a <<createContinuousReader, ContinuousReader>> for [Continuous Stream Processing](index.md).

[[contract]][[createContinuousReader]]
`ContinuousReadSupport` defines a single `createContinuousReader` method to create a [ContinuousReader](ContinuousReader.md).

```scala
ContinuousReader createContinuousReader(
  Optional<StructType> schema,
  String checkpointLocation,
  DataSourceOptions options)
```

`createContinuousReader` is used when:

* `ContinuousExecution` is requested to [run a streaming query](ContinuousExecution.md#runContinuous) (and finds [ContinuousExecutionRelations](../logical-operators/ContinuousExecutionRelation.md) in the [analyzed logical plan](ContinuousExecution.md#logicalPlan))

* `DataStreamReader` is requested to [create a streaming query for a ContinuousReadSupport data source](../DataStreamReader.md#load)

[[implementations]]
.ContinuousReadSupports
[cols="30,70",options="header",width="100%"]
|===
| ContinuousReadSupport
| Description

| [ContinuousMemoryStream](../datasources/memory/ContinuousMemoryStream.md)
| [[ContinuousMemoryStream]] Data source provider for `memory` format

| [KafkaSourceProvider](../kafka/KafkaSourceProvider.md)
| [[KafkaSourceProvider]] Data source provider for `kafka` format

| [RateStreamProvider](../datasources/rate/RateStreamProvider.md)
| [[RateStreamProvider]] Data source provider for `rate` format

| [TextSocketSourceProvider](../datasources/socket/TextSocketSourceProvider.md)
| [[TextSocketSourceProvider]] Data source provider for `socket` format

|===
