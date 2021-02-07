# StreamingDataSourceV2Relation Logical Operator

`StreamingDataSourceV2Relation` is a leaf logical operator that represents [StreamingRelationV2](StreamingRelationV2.md) logical operator (with tables with a `SupportsRead` and `MICRO_BATCH_READ` or `CONTINUOUS_READ` capabilities) at execution time.

!!! tip
    Learn more about [Leaf Logical Operators]({{ book.spark_sql }}/logical-operators/LeafNode), [SupportsRead]({{ book.spark_sql }}/connector/SupportsRead) and [Table Capabilities]({{ book.spark_sql }}/connector/TableCapability) in [The Internals of Spark SQL]({{ book.spark_sql }}) online book.

## Creating Instance

`StreamingDataSourceV2Relation` takes the following to be created:

* <span id="output"> Output Attributes ([Spark SQL]({{ book.spark_sql }}/expressions/Attribute))
* <span id="scan"> `Scan` ([Spark SQL]({{ book.spark_sql }}/connector/Scan))
* <span id="stream"> [SparkDataStream](../SparkDataStream.md)
* <span id="startOffset"> Start [Offset](../Offset.md) (default: undefined)
* <span id="endOffset"> End [Offset](../Offset.md) (default: undefined)

`StreamingDataSourceV2Relation` is created when:

* `MicroBatchExecution` stream execution engine is requested for an [analyzed logical query plan](../micro-batch-execution/MicroBatchExecution.md#logicalPlan) (for [StreamingRelationV2](StreamingRelationV2.md) with a `SupportsRead` table with `MICRO_BATCH_READ` capability)

* `ContinuousExecution` stream execution engine is requested for an [analyzed logical query plan](../ContinuousExecution.md#logicalPlan) (for [StreamingRelationV2](StreamingRelationV2.md) with a `SupportsRead` table with `CONTINUOUS_READ` capability)

## <span id="computeStats"> Computing Stats

```scala
computeStats(): Statistics
```

For [Scans](#scan) with `SupportsReportStatistics`, `computeStats` requests the scan to `estimateStatistics`.

!!! tip
    Learn more about [Scan]({{ book.spark_sql }}/connector/Scan) and [SupportsReportStatistics]({{ book.spark_sql }}/connector/SupportsReportStatistics) in [The Internals of Spark SQL]({{ book.spark_sql }}) online book.

For other types of scans, `computeStats` simply assumes the default size and no row count.

`computeStats` is part of the `LeafNode` abstraction.
