== [[ContinuousReadSupport]] ContinuousReadSupport Contract -- Data Sources with ContinuousReaders

`ContinuousReadSupport` is the <<contract, extension>> of the `DataSourceV2` for <<implementations, data sources>> with a <<createContinuousReader, ContinuousReader>> for <<spark-sql-streaming-continuous-stream-processing.adoc#, Continuous Stream Processing>>.

[[contract]][[createContinuousReader]]
`ContinuousReadSupport` defines a single `createContinuousReader` method to create a <<spark-sql-streaming-ContinuousReader.adoc#, ContinuousReader>>.

[source, java]
----
ContinuousReader createContinuousReader(
  Optional<StructType> schema,
  String checkpointLocation,
  DataSourceOptions options)
----

`createContinuousReader` is used when:

* `ContinuousExecution` is requested to <<spark-sql-streaming-ContinuousExecution.adoc#runContinuous, run a streaming query>> (and finds <<spark-sql-streaming-ContinuousExecutionRelation.adoc#, ContinuousExecutionRelations>> in the <<spark-sql-streaming-ContinuousExecution.adoc#logicalPlan, analyzed logical plan>>)

* `DataStreamReader` is requested to <<spark-sql-streaming-DataStreamReader.adoc#load, create a streaming query for a ContinuousReadSupport data source>>

[[implementations]]
.ContinuousReadSupports
[cols="30,70",options="header",width="100%"]
|===
| ContinuousReadSupport
| Description

| <<spark-sql-streaming-ContinuousMemoryStream.adoc#, ContinuousMemoryStream>>
| [[ContinuousMemoryStream]] Data source provider for `memory` format

| <<spark-sql-streaming-KafkaSourceProvider.adoc#, KafkaSourceProvider>>
| [[KafkaSourceProvider]] Data source provider for `kafka` format

| <<spark-sql-streaming-RateStreamProvider.adoc#, RateStreamProvider>>
| [[RateStreamProvider]] Data source provider for `rate` format

| <<spark-sql-streaming-TextSocketSourceProvider.adoc#, TextSocketSourceProvider>>
| [[TextSocketSourceProvider]] Data source provider for `socket` format

|===
