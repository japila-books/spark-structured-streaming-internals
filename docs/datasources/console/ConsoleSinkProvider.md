# ConsoleSinkProvider

`ConsoleSinkProvider` is a `SimpleTableProvider` ([Spark SQL]({{ book.spark_sql }}/connector/SimpleTableProvider)) for `console` data source.

`ConsoleSinkProvider` is a `DataSourceRegister` ([Spark SQL]({{ book.spark_sql }}/DataSourceRegister)) and registers itself as the *console* data source format.

`ConsoleSinkProvider` is a `CreatableRelationProvider` ([Spark SQL]({{ book.spark_sql }}/CreatableRelationProvider)).

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
