# WriteToDataSourceV2Exec Physical Operator

`WriteToDataSourceV2Exec` is a `V2TableWriteExec` ([Spark SQL]({{ book.spark_sql }}/physical-operators/V2TableWriteExec)) that represents [WriteToDataSourceV2](../logical-operators/WriteToDataSourceV2.md) logical operator at execution time.

## Creating Instance

`WriteToDataSourceV2Exec` takes the following to be created:

* <span id="batchWrite"> `BatchWrite` ([Spark SQL]({{ book.spark_sql }}/connector/BatchWrite))
* <span id="refreshCache"> Refresh Cache Function (`() => Unit`)
* <span id="query"> Physical Query Plan ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan))
* <span id="writeMetrics"> Write `CustomMetric`s ([Spark SQL]({{ book.spark_sql }}/connector/CustomMetric))

`WriteToDataSourceV2Exec` is created when:

* `DataSourceV2Strategy` ([Spark SQL]({{ book.spark_sql }}/execution-planning-strategies/DataSourceV2Strategy)) execution planning strategy is requested to plan a logical query plan (that is a [WriteToDataSourceV2](../logical-operators/WriteToDataSourceV2.md) logical operator)

## <span id="run"> Executing Physical Operator

```scala
run(): Seq[InternalRow]
```

`run` is part of the `V2CommandExec` ([Spark SQL]({{ book.spark_sql }}/physical-operators/V2TableWriteExec#run)) abstraction.

---

`run` writes rows out ([Spark SQL]({{ book.spark_sql }}/physical-operators/V2TableWriteExec#writeWithV2)) using the [BatchWrite](#batchWrite) and then refreshes the cache (using the [refresh cache function](#refreshCache)).

In the end, `run` returns the rows written out.

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.execution.datasources.v2.WriteToDataSourceV2Exec` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.execution.datasources.v2.WriteToDataSourceV2Exec=ALL
```

Refer to [Logging](../spark-logging.md).
