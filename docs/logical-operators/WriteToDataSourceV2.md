# WriteToDataSourceV2 Logical Operator

`WriteToDataSourceV2` is a unary logical operator ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan#UnaryNode)) that represents [WriteToMicroBatchDataSource](WriteToMicroBatchDataSource.md) unary logical operator at logical optimization time for writing data in Spark Structured Streaming.

!!! note "Deprecated"
    `WriteToDataSourceV2` is deprecated since Spark SQL 2.4.0 (in favour of `AppendData` logical operator and alike).

## Creating Instance

`WriteToDataSourceV2` takes the following to be created:

* <span id="relation"> `DataSourceV2Relation` ([Spark SQL]({{ book.spark_sql }}/logical-operators/DataSourceV2Relation))
* <span id="batchWrite"> `BatchWrite` ([Spark SQL]({{ book.spark_sql }}/connector/BatchWrite))
* <span id="query"> Logical Query Plan ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan))
* <span id="customMetrics"> Write `CustomMetric`s ([Spark SQL]({{ book.spark_sql }}/connector/CustomMetric))

`WriteToDataSourceV2` is created when:

* `V2Writes` ([Spark SQL]({{ book.spark_sql }}/logical-optimizations/V2Writes)) logical optimization is requested to optimize a logical query (with a [WriteToMicroBatchDataSource](WriteToMicroBatchDataSource.md) unary logical operator)

## Query Planning

`WriteToDataSourceV2` is planned as [WriteToDataSourceV2Exec](../physical-operators/WriteToDataSourceV2Exec.md) physical operator by `DataSourceV2Strategy` ([Spark SQL]({{ book.spark_sql }}/execution-planning-strategies/DataSourceV2Strategy)) execution planning strategy.
