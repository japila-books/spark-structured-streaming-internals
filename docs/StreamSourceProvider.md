== [[StreamSourceProvider]] StreamSourceProvider Contract -- Streaming Source Providers for Micro-Batch Stream Processing (Data Source API V1)

`StreamSourceProvider` is the <<contract, contract>> of <<implementations, data source providers>> that can <<createSource, create a streaming source>> for a format (e.g. text file) or system (e.g. Apache Kafka).

`StreamSourceProvider` is part of Data Source API V1 and used in <<spark-sql-streaming-micro-batch-stream-processing.md#, Micro-Batch Stream Processing>> only.

[[contract]]
.StreamSourceProvider Contract
[cols="30m,70",options="header",width="100%"]
|===
| Method
| Description

| createSource
a| [[createSource]]

[source, scala]
----
createSource(
  sqlContext: SQLContext,
  metadataPath: String,
  schema: Option[StructType],
  providerName: String,
  parameters: Map[String, String]): Source
----

Creates a [streaming source](Source.md)

NOTE: `metadataPath` is the value of the optional user-specified `checkpointLocation` option or resolved by spark-sql-streaming-StreamingQueryManager.md#createQuery[StreamingQueryManager].

Used exclusively when `DataSource` is requested to <<spark-sql-streaming-DataSource.md#createSource, create a streaming source>> (when `MicroBatchExecution` is requested to <<spark-sql-streaming-MicroBatchExecution.md#logicalPlan, initialize the analyzed logical plan>>)

| sourceSchema
a| [[sourceSchema]]

[source, scala]
----
sourceSchema(
  sqlContext: SQLContext,
  schema: Option[StructType],
  providerName: String,
  parameters: Map[String, String]): (String, StructType)
----

The name and schema of the [streaming source](Source.md)

Used exclusively when `DataSource` is requested for <<spark-sql-streaming-DataSource.md#sourceSchema, metadata of a streaming source>> (when `MicroBatchExecution` is requested to <<spark-sql-streaming-MicroBatchExecution.md#logicalPlan, initialize the analyzed logical plan>>)

|===

[[implementations]]
NOTE: <<spark-sql-streaming-KafkaSourceProvider.md#, KafkaSourceProvider>> is the only known `StreamSourceProvider` in Spark Structured Streaming.
