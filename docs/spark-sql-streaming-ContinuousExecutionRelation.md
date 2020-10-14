== [[ContinuousExecutionRelation]] ContinuousExecutionRelation Leaf Logical Operator

`ContinuousExecutionRelation` is a `MultiInstanceRelation` leaf logical operator.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-LogicalPlan-LeafNode.html[Leaf Logical Operators] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

`ContinuousExecutionRelation` is <<creating-instance, created>> (to represent <<spark-sql-streaming-StreamingRelationV2.md#, StreamingRelationV2>> with [ContinuousReadSupport](ContinuousReadSupport.md) data source) when `ContinuousExecution` is <<ContinuousExecution.md#, created>> (and requested for the <<ContinuousExecution.md#logicalPlan, logical plan>>).

[[creating-instance]]
`ContinuousExecutionRelation` takes the following to be created:

* [[source]] [ContinuousReadSupport](ContinuousReadSupport.md) source
* [[extraOptions]] Options (`Map[String, String]`)
* [[output]] Output attributes (`Seq[Attribute]`)
* [[session]] `SparkSession`
