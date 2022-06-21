# RatePerMicroBatchProvider

`RatePerMicroBatchProvider` is a `SimpleTableProvider` ([Spark SQL]({{ book.spark_sql }}/connector/SimpleTableProvider)).

## <span id="DataSourceRegister"><span id="shortName"> DataSourceRegister

`RatePerMicroBatchProvider` is a `DataSourceRegister` ([Spark SQL]({{ book.spark_sql }}/DataSourceRegister)) that registers `rate-micro-batch` alias.

## <span id="getTable"> Creating Table

```scala
getTable(
  options: CaseInsensitiveStringMap): Table
```

`getTable` creates a [RatePerMicroBatchTable](RatePerMicroBatchTable.md) with the [options](options.md) (given the `CaseInsensitiveStringMap`).

---

`getTable` is part of the `SimpleTableProvider` ([Spark SQL]({{ book.spark_sql }}/connector/SimpleTableProvider#getTable)) abstraction.
