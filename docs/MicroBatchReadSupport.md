# MicroBatchReadSupport &mdash; Data Sources with MicroBatchReaders

`MicroBatchReadSupport` is the <<contract, extension>> of the `DataSourceV2` for <<implementations, data sources>> with a <<createMicroBatchReader, MicroBatchReader>> for <<micro-batch-stream-processing.md#, Micro-Batch Stream Processing>>.

[[contract]][[createMicroBatchReader]]
`MicroBatchReadSupport` defines a single `createMicroBatchReader` method to create a <<spark-sql-streaming-MicroBatchReader.md#, MicroBatchReader>>.

[source, java]
----
MicroBatchReader createMicroBatchReader(
  Optional<StructType> schema,
  String checkpointLocation,
  DataSourceOptions options)
----

`createMicroBatchReader` is used when:

* `MicroBatchExecution` is requested for the <<MicroBatchExecution.md#logicalPlan, analyzed logical plan>> (and creates a [StreamingExecutionRelation](StreamingExecutionRelation.md) for a [StreamingRelationV2](spark-sql-streaming-StreamingRelationV2.md) with a `MicroBatchReadSupport` data source)

* `DataStreamReader` is requested to [create a streaming query for a MicroBatchReadSupport data source](DataStreamReader.md#load)

[[implementations]]
.MicroBatchReadSupports
[cols="30,70",options="header",width="100%"]
|===
| MicroBatchReadSupport
| Description

| [KafkaSourceProvider](datasources/kafka/KafkaSourceProvider.md)
| [[KafkaSourceProvider]] Data source provider for `kafka` format

| <<spark-sql-streaming-RateStreamProvider.md#, RateStreamProvider>>
| [[RateStreamProvider]] Data source provider for `rate` format

| <<spark-sql-streaming-TextSocketSourceProvider.md#, TextSocketSourceProvider>>
| [[TextSocketSourceProvider]] Data source provider for `socket` format

|===
