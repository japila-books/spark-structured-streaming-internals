== [[MicroBatchReadSupport]] MicroBatchReadSupport Contract -- Data Sources with MicroBatchReaders

`MicroBatchReadSupport` is the <<contract, extension>> of the `DataSourceV2` for <<implementations, data sources>> with a <<createMicroBatchReader, MicroBatchReader>> for <<spark-sql-streaming-micro-batch-stream-processing.adoc#, Micro-Batch Stream Processing>>.

[[contract]][[createMicroBatchReader]]
`MicroBatchReadSupport` defines a single `createMicroBatchReader` method to create a <<spark-sql-streaming-MicroBatchReader.adoc#, MicroBatchReader>>.

[source, java]
----
MicroBatchReader createMicroBatchReader(
  Optional<StructType> schema,
  String checkpointLocation,
  DataSourceOptions options)
----

`createMicroBatchReader` is used when:

* `MicroBatchExecution` is requested for the <<spark-sql-streaming-MicroBatchExecution.adoc#logicalPlan, analyzed logical plan>> (and creates a <<spark-sql-streaming-StreamingExecutionRelation.adoc#, StreamingExecutionRelation>> for a <<spark-sql-streaming-StreamingRelationV2.adoc#, StreamingRelationV2>> with a `MicroBatchReadSupport` data source)

* `DataStreamReader` is requested to <<spark-sql-streaming-DataStreamReader.adoc#load, create a streaming query for a MicroBatchReadSupport data source>>

[[implementations]]
.MicroBatchReadSupports
[cols="30,70",options="header",width="100%"]
|===
| MicroBatchReadSupport
| Description

| <<spark-sql-streaming-KafkaSourceProvider.adoc#, KafkaSourceProvider>>
| [[KafkaSourceProvider]] Data source provider for `kafka` format

| <<spark-sql-streaming-RateStreamProvider.adoc#, RateStreamProvider>>
| [[RateStreamProvider]] Data source provider for `rate` format

| <<spark-sql-streaming-TextSocketSourceProvider.adoc#, TextSocketSourceProvider>>
| [[TextSocketSourceProvider]] Data source provider for `socket` format

|===
