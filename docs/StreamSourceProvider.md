# StreamSourceProvider

`StreamSourceProvider` is an [abstraction](#contract) of [data source providers](#implementations) that can [create a streaming source](#createSource) for a data format or system.

`StreamSourceProvider` is part of Data Source API V1 for [Micro-Batch Stream Processing](micro-batch-stream-processing.md).

## Contract

### <span id="createSource"> Creating Streaming Source

```scala
createSource(
  sqlContext: SQLContext,
  metadataPath: String,
  schema: Option[StructType],
  providerName: String,
  parameters: Map[String, String]): Source
```

Creates a [Streaming Source](Source.md)

`metadataPath` is the value of the optional user-specified `checkpointLocation` option or resolved by [StreamingQueryManager](StreamingQueryManager.md#createQuery).

Used when:

* `DataSource` is requested to [create a streaming source](DataSource.md#createSource) (when `MicroBatchExecution` is requested to [initialize the analyzed logical plan](MicroBatchExecution.md#logicalPlan))

### <span id="sourceSchema"> Source Schema

```scala
sourceSchema(
  sqlContext: SQLContext,
  schema: Option[StructType],
  providerName: String,
  parameters: Map[String, String]): (String, StructType)
```

Name and schema of the [Streaming Source](Source.md)

Used when:

* `DataSource` is requested for [metadata of a streaming source](DataSource.md#sourceSchema) (when `MicroBatchExecution` is requested to [initialize the analyzed logical plan](MicroBatchExecution.md#logicalPlan))

## Implementations

* [KafkaSourceProvider](datasources/kafka/KafkaSourceProvider.md)
