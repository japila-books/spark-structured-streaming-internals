# StreamingRelation Leaf Logical Operator for Streaming Source

`StreamingRelation` is a leaf logical operator (i.e. `LogicalPlan`) that represents a [streaming source](Source.md) in a logical plan.

`StreamingRelation` is <<creating-instance, created>> when `DataStreamReader` is requested to spark-sql-streaming-DataStreamReader.md#load[load data from a streaming source] and creates a streaming `Dataset`.

.StreamingRelation Represents Streaming Source
image::images/StreamingRelation.png[align="center"]

[source, scala]
----
val rate = spark.
  readStream.     // <-- creates a DataStreamReader
  format("rate").
  load("hello")   // <-- creates a StreamingRelation
scala> println(rate.queryExecution.logical.numberedTreeString)
00 StreamingRelation DataSource(org.apache.spark.sql.SparkSession@4e5dcc50,rate,List(),None,List(),None,Map(path -> hello),None), rate, [timestamp#0, value#1L]
----

[[isStreaming]]
`isStreaming` flag is always enabled (i.e. `true`).

[source, scala]
----
import org.apache.spark.sql.execution.streaming.StreamingRelation
val relation = rate.queryExecution.logical.asInstanceOf[StreamingRelation]
scala> relation.isStreaming
res1: Boolean = true
----

[[toString]]
`toString` gives the <<sourceName, source name>>.

[source, scala]
----
scala> println(relation)
rate
----

NOTE: `StreamingRelation` is [resolved](StreamExecution.md#logicalPlan) (aka _planned_) to spark-sql-streaming-StreamingExecutionRelation.md[StreamingExecutionRelation] (right after `StreamExecution` [starts running batches](StreamExecution.md#runStream)).

=== [[apply]] Creating StreamingRelation for DataSource -- `apply` Object Method

[source, scala]
----
apply(dataSource: DataSource): StreamingRelation
----

`apply` creates a `StreamingRelation` for the given <<spark-sql-streaming-DataSource.md#, DataSource>> (that represents a streaming source).

NOTE: `apply` is used exclusively when `DataStreamReader` is requested for a <<spark-sql-streaming-DataStreamReader.md#load, streaming DataFrame>>.

=== [[creating-instance]] Creating StreamingRelation Instance

`StreamingRelation` takes the following when created:

* [[dataSource]] spark-sql-streaming-DataSource.md[DataSource]
* [[sourceName]] Short name of the streaming source
* [[output]] Output attributes of the schema of the streaming source
