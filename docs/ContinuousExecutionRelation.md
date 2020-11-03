# ContinuousExecutionRelation Leaf Logical Operator

`ContinuousExecutionRelation` is a `MultiInstanceRelation` leaf logical operator.

!!! tip
    Learn more about [Leaf Logical Operators]({{ book.spark_sql }}/logical-operators/LeafNode) in [The Internals of Spark SQL]({{ book.spark_sql }}) book.

## Creating Instance

`ContinuousExecutionRelation` takes the following to be created:

* [[source]] [ContinuousReadSupport](ContinuousReadSupport.md) source
* [[extraOptions]] Options (`Map[String, String]`)
* [[output]] Output attributes (`Seq[Attribute]`)
* [[session]] `SparkSession`

`ContinuousExecutionRelation` is created (to represent [StreamingRelationV2](logical-operators/StreamingRelationV2.md) with [ContinuousReadSupport](ContinuousReadSupport.md) data source) when `ContinuousExecution` is [created](ContinuousExecution.md) (and requested for the [logical plan](ContinuousExecution.md#logicalPlan)).
