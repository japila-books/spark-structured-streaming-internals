== [[StreamingRelationV2]] StreamingRelationV2 Leaf Logical Operator

`StreamingRelationV2` is a `MultiInstanceRelation` leaf logical operator that represents <<spark-sql-streaming-MicroBatchReadSupport.adoc#, MicroBatchReadSupport>> or <<spark-sql-streaming-ContinuousReadSupport.adoc#, ContinuousReadSupport>> streaming data sources in a logical plan of a streaming query.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-LogicalPlan-LeafNode.html[Leaf logical operators] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

`StreamingRelationV2` is <<creating-instance, created>> when:

* `DataStreamReader` is requested to <<spark-sql-streaming-DataStreamReader.adoc#load, "load" data as a streaming DataFrame>> for <<spark-sql-streaming-MicroBatchReadSupport.adoc#, MicroBatchReadSupport>> and <<spark-sql-streaming-ContinuousReadSupport.adoc#, ContinuousReadSupport>> streaming data sources

* <<spark-sql-streaming-ContinuousMemoryStream.adoc#, ContinuousMemoryStream>> is created

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

* <<spark-sql-streaming-ContinuousExecutionRelation.adoc#, ContinuousExecutionRelation>> when `ContinuousExecution` stream execution engine is requested for the <<spark-sql-streaming-ContinuousExecution.adoc#logicalPlan, analyzed logical plan>>

* <<spark-sql-streaming-StreamingExecutionRelation.adoc#, StreamingExecutionRelation>> when `MicroBatchExecution` stream execution engine is requested for the <<spark-sql-streaming-MicroBatchExecution.adoc#logicalPlan, analyzed logical plan>>

=== [[creating-instance]] Creating StreamingRelationV2 Instance

`StreamingRelationV2` takes the following to be created:

* [[dataSource]] `DataSourceV2`
* [[sourceName]] Name of the data source
* [[extraOptions]] Options (`Map[String, String]`)
* [[output]] Output attributes (`Seq[Attribute]`)
* [[v1Relation]] Optional <<spark-sql-streaming-StreamingRelation.adoc#, StreamingRelation>>
* [[session]] `SparkSession`
