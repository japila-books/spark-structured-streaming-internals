# RatePerMicroBatchProvider

`RatePerMicroBatchProvider` is a `SimpleTableProvider` ([Spark SQL]({{ book.spark_sql }}/connector/SimpleTableProvider)) registered under [rate-micro-batch](#shortName) alias.

## <span id="DataSourceRegister"><span id="shortName"> DataSourceRegister

`RatePerMicroBatchProvider` is a `DataSourceRegister` ([Spark SQL]({{ book.spark_sql }}/DataSourceRegister)) that registers `rate-micro-batch` alias.

## Create Table { #getTable }

??? note "SimpleTableProvider"

    ```scala
    getTable(
      options: CaseInsensitiveStringMap): Table
    ```

    `getTable` is part of the `SimpleTableProvider` ([Spark SQL]({{ book.spark_sql }}/connector/SimpleTableProvider#getTable)) abstraction.

`getTable` creates a [RatePerMicroBatchTable](RatePerMicroBatchTable.md) with the [options](options.md) (given the `CaseInsensitiveStringMap`).
