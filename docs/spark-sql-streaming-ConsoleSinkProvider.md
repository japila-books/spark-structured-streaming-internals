== [[ConsoleSinkProvider]] ConsoleSinkProvider

`ConsoleSinkProvider` is a `DataSourceV2` with <<spark-sql-streaming-StreamWriteSupport.md#, StreamWriteSupport>> for *console* data source format.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-DataSourceV2.html[DataSourceV2 Contract] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

`ConsoleSinkProvider` is a <<spark-sql-DataSourceRegister.md#, DataSourceRegister>> and registers itself as the *console* data source format.

[source, scala]
----
import org.apache.spark.sql.streaming.Trigger
val q = spark
  .readStream
  .format("rate")
  .load
  .writeStream
  .format("console") // <-- requests ConsoleSinkProvider for a sink
  .trigger(Trigger.Once)
  .start
scala> println(q.lastProgress.sink)
{
  "description" : "org.apache.spark.sql.execution.streaming.ConsoleSinkProvider@2392cfb1"
}
----

[[createStreamWriter]]
When requested for a <<spark-sql-streaming-StreamWriteSupport.md#createStreamWriter, StreamWriter>>, `ConsoleSinkProvider` simply creates a <<spark-sql-streaming-ConsoleWriter.md#, ConsoleWriter>> (with the given schema and options).

[[CreatableRelationProvider]]
`ConsoleSinkProvider` is a <<createRelation, CreatableRelationProvider>>.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-CreatableRelationProvider.html[CreatableRelationProvider] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

=== [[createRelation]] `createRelation` Method

[source, scala]
----
createRelation(
  sqlContext: SQLContext,
  mode: SaveMode,
  parameters: Map[String, String],
  data: DataFrame): BaseRelation
----

NOTE: `createRelation` is part of the `CreatableRelationProvider` Contract to support writing a structured query (a DataFrame) per save mode.

`createRelation`...FIXME
