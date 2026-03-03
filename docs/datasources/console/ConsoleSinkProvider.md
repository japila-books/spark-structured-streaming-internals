# ConsoleSinkProvider

`ConsoleSinkProvider` is a `SimpleTableProvider` ([Spark SQL]({{ book.spark_sql }}/connector/SimpleTableProvider/)) for `console` data source (based on the modern [Connector API]({{ book.spark_sql }}/connector/)).

`ConsoleSinkProvider` is a `DataSourceRegister` ([Spark SQL]({{ book.spark_sql }}/DataSourceRegister/)) and registers itself as the **console** data source format.

`ConsoleSinkProvider` is a `CreatableRelationProvider` ([Spark SQL]({{ book.spark_sql }}/CreatableRelationProvider/)) (based on the legacy [DataSource V1 API]({{ book.spark_sql }}/CreatableRelationProvider/)).

## Demo

```scala
import org.apache.spark.sql.streaming.Trigger
val q = spark
  .readStream
  .format("rate")
  .load
  .writeStream
  .format("console") // <-- requests ConsoleSinkProvider for a sink
  .trigger(Trigger.Once)
  .start
```

```text
scala> println(q.lastProgress.sink)
{
  "description" : "org.apache.spark.sql.execution.streaming.ConsoleSinkProvider@2392cfb1"
}
```

## Short Name { #shortName }

??? note "DataSourceRegister"

    ```scala
    shortName(): String
    ```

    `shortName` is part of the `DataSourceRegister` ([Spark SQL]({{ book.spark_sql }}/DataSourceRegister/#shortName)) abstraction.

`shortName` returns **console**.

## Get Table { #getTable }

??? note "SimpleTableProvider"

    ```scala
    getTable(
      options: CaseInsensitiveStringMap): Table
    ```

    `getTable` is part of the `SimpleTableProvider` ([Spark SQL]({{ book.spark_sql }}/connector/SimpleTableProvider/#getTable)) abstraction.

`getTable` returns the only available [ConsoleTable](ConsoleTable.md) object.

## Create Relation { #createRelation }

??? note "CreatableRelationProvider"

    ```scala
    createRelation(
      sqlContext: SQLContext,
      mode: SaveMode,
      parameters: Map[String, String],
      data: DataFrame): BaseRelation
    ```

    `createRelation` is part of the `CreatableRelationProvider` ([Spark SQL]({{ book.spark_sql }}/CreatableRelationProvider/#createRelation)) abstraction.

`createRelation`...FIXME
