# StreamingRelationV2 Leaf Logical Operator

`StreamingRelationV2` is a `MultiInstanceRelation` leaf logical operator that represents `SupportsRead` streaming tables (with `MICRO_BATCH_READ` or `CONTINUOUS_READ` capabilities) in a logical plan of a streaming query.

!!! tip
    Learn more about [Leaf logical operators]({{ book.spark_sql }}/logical-operators/LeafNode) in [The Internals of Spark SQL]({{ book.spark_sql }}) online book.

## Creating Instance

`StreamingRelationV2` takes the following to be created:

* <span id="source"> `TableProvider` ([Spark SQL]({{ book.spark_sql }}/connector/TableProvider))
* <span id="sourceName"> Source Name
* <span id="table"> `Table` ([Spark SQL]({{ book.spark_sql }}/connector/Table))
* <span id="extraOptions"> Extra Options
* <span id="output"> Output Attributes
* <span id="v1Relation"> [StreamingRelation](StreamingRelation.md)
* <span id="session"> `SparkSession` ([Spark SQL]({{ book.spark_sql }}/SparkSession))

`StreamingRelationV2` is created when:

* `MemoryStreamBase` is requested for a [logical query plan](../datasources/memory/MemoryStreamBase.md#logicalPlan)
* `DataStreamReader` is reqested to [load data](../DataStreamReader.md#load) (for a `SupportsRead` table with `MICRO_BATCH_READ` or `CONTINUOUS_READ` capabilities)

## Logical Resolution

`StreamingRelationV2` is resolved to the following leaf logical operators:

* [StreamingDataSourceV2Relation](StreamingDataSourceV2Relation.md) or [StreamingExecutionRelation](StreamingExecutionRelation.md) when `MicroBatchExecution` stream execution engine is requested for an [analyzed logical plan](../MicroBatchExecution.md#logicalPlan)
* [StreamingDataSourceV2Relation](StreamingDataSourceV2Relation.md) when `ContinuousExecution` stream execution engine is created (and initializes an [analyzed logical plan](../ContinuousExecution.md#logicalPlan))
