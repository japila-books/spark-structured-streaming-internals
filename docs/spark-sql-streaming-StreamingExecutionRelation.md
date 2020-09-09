== [[StreamingExecutionRelation]] StreamingExecutionRelation Leaf Logical Operator for Streaming Source At Execution

`StreamingExecutionRelation` is a leaf logical operator (i.e. `LogicalPlan`) that represents a link:spark-sql-streaming-Source.adoc[streaming source] in the logical query plan of a streaming `Dataset`.

The main use of `StreamingExecutionRelation` logical operator is to be a "placeholder" in a logical query plan that will be replaced with the real relation (with new data that has arrived since the last batch) or an empty `LocalRelation` when `StreamExecution` is requested to <<spark-sql-streaming-MicroBatchExecution.adoc#runBatch-newBatchesPlan, transforming logical plan to include the Sources and MicroBatchReaders with new data>>.

`StreamingExecutionRelation` is <<creating-instance, created>> for a link:spark-sql-streaming-StreamingRelation.adoc[StreamingRelation] in link:spark-sql-streaming-StreamExecution.adoc#analyzedPlan[analyzed logical query plan] (that is the execution representation of a streaming Dataset).

[NOTE]
====
Right after `StreamExecution` link:spark-sql-streaming-MicroBatchExecution.adoc#runStream-initializing-sources[has started running streaming batches] it initializes the streaming sources by transforming the analyzed logical plan of the streaming Dataset so that every link:spark-sql-streaming-StreamingRelation.adoc[StreamingRelation] logical operator is replaced by the corresponding `StreamingExecutionRelation`.
====

.StreamingExecutionRelation Represents Streaming Source At Execution
image::images/StreamingExecutionRelation.png[align="center"]

NOTE: `StreamingExecutionRelation` is also resolved (aka _planned_) to a link:spark-sql-streaming-StreamingRelationExec.adoc[StreamingRelationExec] physical operator in link:spark-sql-streaming-StreamingRelationStrategy.adoc[StreamingRelationStrategy] execution planning strategy only when link:spark-sql-streaming-Dataset-explain.adoc[explaining] a streaming `Dataset`.

=== [[creating-instance]] Creating StreamingExecutionRelation Instance

`StreamingExecutionRelation` takes the following when created:

* [[source]] link:spark-sql-streaming-Source.adoc[Streaming source]
* [[output]] Output attributes

=== [[apply]] Creating StreamingExecutionRelation (based on a Source) -- `apply` Object Method

[source, scala]
----
apply(source: Source): StreamingExecutionRelation
----

`apply` creates a `StreamingExecutionRelation` for the input `source` and with the attributes of the link:spark-sql-streaming-Source.adoc#schema[schema] of the `source`.

NOTE: `apply` _seems_ to be used for tests only.
