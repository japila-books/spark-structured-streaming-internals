== [[StreamingRelationV2]] StreamingRelationV2 Leaf Logical Operator

`StreamingRelationV2` is a `MultiInstanceRelation` leaf logical operator that represents <<spark-sql-streaming-MicroBatchReadSupport.md#, MicroBatchReadSupport>> or <<spark-sql-streaming-ContinuousReadSupport.md#, ContinuousReadSupport>> streaming data sources in a logical plan of a streaming query.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-LogicalPlan-LeafNode.html[Leaf logical operators] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

`StreamingRelationV2` is <<creating-instance, created>> when:

* `DataStreamReader` is requested to <<spark-sql-streaming-DataStreamReader.md#load, "load" data as a streaming DataFrame>> for <<spark-sql-streaming-MicroBatchReadSupport.md#, MicroBatchReadSupport>> and <<spark-sql-streaming-ContinuousReadSupport.md#, ContinuousReadSupport>> streaming data sources

* <<spark-sql-streaming-ContinuousMemoryStream.md#, ContinuousMemoryStream>> is created

[[isStreaming]]
`isStreaming` flag is always enabled (i.e. `true`).

[source, scala]
----
scala> :type sq
org.apache.spark.sql.DataFrame

import org.apache.spark.sql.execution.streaming.StreamingRelationV2
val relation = sq.queryExecution.logical.asInstanceOf[StreamingRelationV2]
assert(relation.isStreaming)
----

`StreamingRelationV2` is resolved (_replaced_) to the following leaf logical operators:

* <<spark-sql-streaming-ContinuousExecutionRelation.md#, ContinuousExecutionRelation>> when `ContinuousExecution` stream execution engine is requested for the <<ContinuousExecution.md#logicalPlan, analyzed logical plan>>

* [StreamingExecutionRelation](StreamingExecutionRelation.md) when `MicroBatchExecution` stream execution engine is requested for the <<MicroBatchExecution.md#logicalPlan, analyzed logical plan>>

## Creating Instance

`StreamingRelationV2` takes the following to be created:

* [[dataSource]] `DataSourceV2`
* [[sourceName]] Name of the data source
* [[extraOptions]] Options (`Map[String, String]`)
* [[output]] Output attributes (`Seq[Attribute]`)
* [[v1Relation]] Optional <<spark-sql-streaming-StreamingRelation.md#, StreamingRelation>>
* [[session]] `SparkSession`
