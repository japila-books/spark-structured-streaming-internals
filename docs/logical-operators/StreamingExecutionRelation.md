# StreamingExecutionRelation Leaf Logical Operator

`StreamingExecutionRelation` is a leaf logical operator ([Spark SQL]({{ book.spark_sql }}/logical-operators/LeafNode)) that represents a [streaming source](../Source.md) in the logical query plan of a streaming query.

The main use of `StreamingExecutionRelation` logical operator is to be a "placeholder" in a logical query plan that will be replaced with the real relation (with new data that has arrived since the last batch) or an empty `LocalRelation` when `StreamExecution` is requested to [transforming logical plan to include the Sources and MicroBatchReaders with new data](../MicroBatchExecution.md#runBatch-newBatchesPlan).

!!! note
    Right after `StreamExecution` [has started running streaming batches](../MicroBatchExecution.md#runStream-initializing-sources) it initializes the streaming sources by transforming the analyzed logical plan of the streaming query so that every [StreamingRelation](StreamingRelation.md) logical operator is replaced by the corresponding `StreamingExecutionRelation`.

![StreamingExecutionRelation Represents Streaming Source At Execution](../images/StreamingExecutionRelation.png)

!!! note
    `StreamingExecutionRelation` is also resolved (_planned_) to a [StreamingRelationExec](../physical-operators/StreamingRelationExec.md) physical operator in [StreamingRelationStrategy](../StreamingRelationStrategy.md) execution planning strategy only when [explaining](../operators/explain.md) a streaming `Dataset`.

## Creating Instance

`StreamingExecutionRelation` takes the following to be created:

* <span id="source"> `BaseStreamingSource`
* <span id="output"> Output Attributes (`Seq[Attribute]`)
* <span id="session"> `SparkSession`

`StreamingExecutionRelation` is created when:

* `MicroBatchExecution` stream execution engine is requested for the [analyzed logical query plan](../MicroBatchExecution.md#logicalPlan) (for every [StreamingRelation](StreamingRelation.md))
