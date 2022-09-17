# WriteToMicroBatchDataSource Logical Operator

`WriteToMicroBatchDataSource` is a unary logical operator ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan#UnaryNode)) that is a top-level operator in the [analyzed logical query plan](../micro-batch-execution/MicroBatchExecution.md#logicalPlan) of a streaming query with a [sink](../StreamExecution.md#sink) that `SupportsWrite` ([Spark SQL]({{ book.spark_sql }}/connector/SupportsWrite)).

## Creating Instance

`WriteToMicroBatchDataSource` takes the following to be created:

* <span id="relation"> `DataSourceV2Relation` ([Spark SQL]({{ book.spark_sql }}/logical-operators/DataSourceV2Relation)) leaf logical operator
* <span id="table"> Table with `SupportsWrite` ([Spark SQL]({{ book.spark_sql }}/connector/SupportsWrite))
* <span id="query"> Query `LogicalPlan` ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan))
* <span id="queryId"> Query ID
* <span id="writeOptions"> Write Options (`Map[String, String]`)
* <span id="outputMode"> [OutputMode](../OutputMode.md)
* <span id="batchId"> Batch ID

`WriteToMicroBatchDataSource` is created when:

* `MicroBatchExecution` is requested for the [analyzed logical query plan](../micro-batch-execution/MicroBatchExecution.md#logicalPlan) (with a [sink](../StreamExecution.md#sink) that `SupportsWrite` ([Spark SQL]({{ book.spark_sql }}/connector/SupportsWrite)))

## Query Planning

`WriteToMicroBatchDataSource` is planned as `WriteToDataSourceV2` ([Spark SQL]({{ book.spark_sql }}/physical-operators/WriteToDataSourceV2Exec)) physical operator by `V2Writes` ([Spark SQL]({{ book.spark_sql }}/logical-optimizations/V2Writes)) logical optimization.
