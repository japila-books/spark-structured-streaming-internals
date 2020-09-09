== [[FlatMapGroupsWithStateExec]] FlatMapGroupsWithStateExec Unary Physical Operator

`FlatMapGroupsWithStateExec` is a unary physical operator that represents <<spark-sql-streaming-FlatMapGroupsWithState.adoc#, FlatMapGroupsWithState>> logical operator at execution time.

[NOTE]
====
A unary physical operator (`UnaryExecNode`) is a physical operator with a single <<child, child>> physical operator.

Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-SparkPlan.html[UnaryExecNode] (and physical operators in general) in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.
====

NOTE: <<spark-sql-streaming-FlatMapGroupsWithState.adoc#, FlatMapGroupsWithState>> unary logical operator represents <<spark-sql-streaming-KeyValueGroupedDataset.adoc#mapGroupsWithState, KeyValueGroupedDataset.mapGroupsWithState>> and <<spark-sql-streaming-KeyValueGroupedDataset.adoc#flatMapGroupsWithState, KeyValueGroupedDataset.flatMapGroupsWithState>> operators in a logical query plan.

`FlatMapGroupsWithStateExec` is <<creating-instance, created>> exclusively when <<spark-sql-streaming-FlatMapGroupsWithStateStrategy.adoc#, FlatMapGroupsWithStateStrategy>> execution planning strategy is requested to plan a <<spark-sql-streaming-FlatMapGroupsWithState.adoc#, FlatMapGroupsWithState>> logical operator for execution.

`FlatMapGroupsWithStateExec` is an `ObjectProducerExec` physical operator and so produces a <<outputObjAttr, single output object>>.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-SparkPlan-ObjectProducerExec.html[ObjectProducerExec — Physical Operators With Single Object Output] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

TIP: Check out <<spark-sql-streaming-demo-FlatMapGroupsWithStateExec.adoc#, Demo: Internals of FlatMapGroupsWithStateExec Physical Operator>>.

NOTE: `FlatMapGroupsWithStateExec` is given an <<outputMode, OutputMode>> when created, but it does not seem to be used at all. Check out the question https://stackoverflow.com/q/56921772/1305344[What's the purpose of OutputMode in flatMapGroupsWithState? How/where is it used?] on StackOverflow.

[[logging]]
[TIP]
====
Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.FlatMapGroupsWithStateExec` to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.execution.streaming.FlatMapGroupsWithStateExec=ALL
```

Refer to <<spark-sql-streaming-logging.adoc#, Logging>>.
====

=== [[creating-instance]] Creating FlatMapGroupsWithStateExec Instance

`FlatMapGroupsWithStateExec` takes the following to be created:

* [[func]] *User-defined state function* that is applied to every group (of type `(Any, Iterator[Any], LogicalGroupState[Any]) => Iterator[Any]`)
* [[keyDeserializer]] Key deserializer expression
* [[valueDeserializer]] Value deserializer expression
* [[groupingAttributes]] Grouping attributes (as used for grouping in link:spark-sql-streaming-KeyValueGroupedDataset.adoc#groupingAttributes[KeyValueGroupedDataset] for `mapGroupsWithState` or `flatMapGroupsWithState` operators)
* [[dataAttributes]] Data attributes
* [[outputObjAttr]] Output object attribute (that is the reference to the single object field this operator outputs)
* [[stateInfo]] <<spark-sql-streaming-StatefulOperatorStateInfo.adoc#, StatefulOperatorStateInfo>>
* [[stateEncoder]] State encoder (`ExpressionEncoder[Any]`)
* [[stateFormatVersion]] State format version
* [[outputMode]] <<spark-sql-streaming-OutputMode.adoc#, OutputMode>>
* [[timeoutConf]] <<spark-sql-streaming-GroupStateTimeout.adoc#, GroupStateTimeout>>
* [[batchTimestampMs]] <<spark-structured-streaming-batch-processing-time.adoc#, Batch Processing Time>>
* [[eventTimeWatermark]] <<spark-sql-streaming-watermark.adoc#, Event-time watermark>>
* [[child]] Child physical operator

`FlatMapGroupsWithStateExec` initializes the <<internal-properties, internal properties>>.

=== [[metrics]] Performance Metrics (SQLMetrics)

`FlatMapGroupsWithStateExec` uses the performance metrics of <<spark-sql-streaming-StateStoreWriter.adoc#metrics, StateStoreWriter>>.

.FlatMapGroupsWithStateExec in web UI (Details for Query)
image::images/FlatMapGroupsWithStateExec-webui-query-details.png[align="center"]

=== [[StateStoreWriter]] FlatMapGroupsWithStateExec as StateStoreWriter

`FlatMapGroupsWithStateExec` is a <<spark-sql-streaming-StateStoreWriter.adoc#, stateful physical operator that can write to a state store>>(and `MicroBatchExecution` requests <<shouldRunAnotherBatch, whether to run another batch or not>> based on the <<timeoutConf, GroupStateTimeout>>).

`FlatMapGroupsWithStateExec` uses the <<timeoutConf, GroupStateTimeout>> (and possibly the updated <<spark-sql-streaming-OffsetSeqMetadata.adoc#, metadata>>) when asked <<shouldRunAnotherBatch, whether to run another batch or not>> (when `MicroBatchExecution` is requested to <<spark-sql-streaming-MicroBatchExecution.adoc#constructNextBatch, construct the next streaming micro-batch>> when requested to <<spark-sql-streaming-MicroBatchExecution.adoc#runActivatedStream, run the activated streaming query>>).

=== [[WatermarkSupport]] FlatMapGroupsWithStateExec with Streaming Event-Time Watermark Support (WatermarkSupport)

`FlatMapGroupsWithStateExec` is a <<spark-sql-streaming-WatermarkSupport.adoc#, physical operator that supports streaming event-time watermark>>.

`FlatMapGroupsWithStateExec` is given the <<eventTimeWatermark, optional event time watermark>> when created.

The <<eventTimeWatermark, event-time watermark>> is initially undefined (`None`) when planned to for execution (in <<spark-sql-streaming-FlatMapGroupsWithStateStrategy.adoc#, FlatMapGroupsWithStateStrategy>> execution planning strategy).

[NOTE]
====
`FlatMapGroupsWithStateStrategy` converts link:spark-sql-streaming-FlatMapGroupsWithState.adoc[FlatMapGroupsWithState] unary logical operator to `FlatMapGroupsWithStateExec` physical operator with undefined <<stateInfo, StatefulOperatorStateInfo>>, <<batchTimestampMs, batchTimestampMs>>, and <<eventTimeWatermark, eventTimeWatermark>>.
====

The <<eventTimeWatermark, event-time watermark>> (with the <<stateInfo, StatefulOperatorStateInfo>> and the <<batchTimestampMs, batchTimestampMs>>) is only defined to the <<spark-sql-streaming-OffsetSeqMetadata.adoc#batchWatermarkMs, current event-time watermark>> of the given <<spark-sql-streaming-IncrementalExecution.adoc#offsetSeqMetadata, OffsetSeqMetadata>> when `IncrementalExecution` query execution pipeline is requested to apply the <<spark-sql-streaming-IncrementalExecution.adoc#state, state>> preparation rule (as part of the <<spark-sql-streaming-IncrementalExecution.adoc#preparations, preparations>> rules).

[NOTE]
====
The <<spark-sql-streaming-IncrementalExecution.adoc#preparations, preparations>> rules are executed (applied to a physical query plan) at the `executedPlan` phase of Structured Query Execution Pipeline to generate an optimized physical query plan ready for execution).

Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-QueryExecution.html[Structured Query Execution Pipeline] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.
====

`IncrementalExecution` is used as the <<spark-sql-streaming-StreamExecution.adoc#lastExecution, lastExecution>> of the available <<spark-sql-streaming-StreamExecution.adoc#extensions, streaming query execution engines>>. It is created in the *queryPlanning* phase (of the <<spark-sql-streaming-MicroBatchExecution.adoc#runBatch-queryPlanning, MicroBatchExecution>> and <<spark-sql-streaming-ContinuousExecution.adoc#runContinuous-queryPlanning, ContinuousExecution>> execution engines) based on the current <<spark-sql-streaming-StreamExecution.adoc#offsetSeqMetadata, OffsetSeqMetadata>>.

NOTE: The <<eventTimeWatermark, optional event-time watermark>> can only be defined when the <<spark-sql-streaming-IncrementalExecution.adoc#state, state>> preparation rule is executed which is at the `executedPlan` phase of Structured Query Execution Pipeline which is also part of the *queryPlanning* phase.

=== [[stateManager]] FlatMapGroupsWithStateExec and StateManager -- `stateManager` Property

[source, scala]
----
stateManager: StateManager
----

While being created, `FlatMapGroupsWithStateExec` creates a <<spark-sql-streaming-StateManager.adoc#, StateManager>> (with the <<stateEncoder, state encoder>> and the <<isTimeoutEnabled, isTimeoutEnabled>> flag).

A `StateManager` is <<spark-sql-streaming-FlatMapGroupsWithStateExecHelper.adoc#createStateManager, created>> per <<stateFormatVersion, state format version>> that is given while creating a `FlatMapGroupsWithStateExec` (to choose between the <<spark-sql-streaming-StateManagerImplBase.adoc#implementations, available implementations>>).

The <<stateFormatVersion, state format version>> is controlled by <<spark-sql-streaming-properties.adoc#spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion, spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion>> internal configuration property (default: `2`).

NOTE: <<spark-sql-streaming-StateManagerImplV2.adoc#, StateManagerImplV2>> is the default `StateManager`.

The `StateManager` is used exclusively when `FlatMapGroupsWithStateExec` physical operator is <<doExecute, executed>> (to generate a recipe for a distributed computation as an `RDD[InternalRow]`) for the following:

* <<spark-sql-streaming-StateManager.adoc#stateSchema, State schema>> (for the <<spark-sql-streaming-StateStoreRDD.adoc#valueSchema, value schema>> of a <<spark-sql-streaming-StateStoreRDD.adoc#, StateStoreRDD>>)

* <<spark-sql-streaming-StateManager.adoc#getState, State data for a key in a StateStore>> while [processing new data](InputProcessor.md#processNewData)

* <<spark-sql-streaming-StateManager.adoc#getAllState, All state data (for all keys) in a StateStore>> while [processing timed-out state data](InputProcessor.md#processTimedOutState)

* <<spark-sql-streaming-StateManager.adoc#removeState, Removing the state for a key from a StateStore>> when [all rows have been processed](InputProcessor.md#onIteratorCompletion)

* <<spark-sql-streaming-StateManager.adoc#putState, Persisting the state for a key in a StateStore>> when [all rows have been processed](InputProcessor.md#onIteratorCompletion)

=== [[keyExpressions]] `keyExpressions` Method

[source, scala]
----
keyExpressions: Seq[Attribute]
----

NOTE: `keyExpressions` is part of the <<spark-sql-streaming-WatermarkSupport.adoc#keyExpressions, WatermarkSupport Contract>> to...FIXME.

`keyExpressions` simply returns the <<groupingAttributes, grouping attributes>>.

=== [[doExecute]] Executing Physical Operator (Generating RDD[InternalRow]) -- `doExecute` Method

[source, scala]
----
doExecute(): RDD[InternalRow]
----

NOTE: `doExecute` is part of `SparkPlan` Contract to generate the runtime representation of an physical operator as a distributed computation over internal binary rows on Apache Spark (i.e. `RDD[InternalRow]`).

`doExecute` first initializes the <<spark-sql-streaming-StateStoreWriter.adoc#metrics, metrics>> (which happens on the driver).

`doExecute` then requests the <<child, child>> physical operator to execute and generate an `RDD[InternalRow]`.

`doExecute` uses <<spark-sql-streaming-StateStoreOps.adoc#, StateStoreOps>> to <<spark-sql-streaming-StateStoreOps.adoc#mapPartitionsWithStateStore, create a StateStoreRDD>> with a `storeUpdateFunction` that does the following (for a partition):

. Creates an [InputProcessor](InputProcessor.md) for a given [StateStore](spark-sql-streaming-StateStore.adoc)

. (only when the <<timeoutConf, GroupStateTimeout>> is <<spark-sql-streaming-GroupStateTimeout.adoc#EventTimeTimeout, EventTimeTimeout>>) Filters out late data based on the <<spark-sql-streaming-WatermarkSupport.adoc#watermarkPredicateForData, event-time watermark>>, i.e. rows from a given `Iterator[InternalRow]` that are older than the <<spark-sql-streaming-WatermarkSupport.adoc#watermarkPredicateForData, event-time watermark>> are excluded from the steps that follow

. Requests the `InputProcessor` to [create an iterator of a new data processed](InputProcessor.md#processNewData) from the (possibly filtered) iterator

. Requests the `InputProcessor` to [create an iterator of a timed-out state data](InputProcessor.md#processTimedOutState)

. Creates an iterator by concatenating the above iterators (with the new data processed first)

. In the end, creates a `CompletionIterator` that executes a completion function (`completionFunction`) after it has successfully iterated through all the elements (i.e. when a client has consumed all the rows). The completion method requests the given `StateStore` to <<spark-sql-streaming-StateStore.adoc#commit, commit changes>> followed by <<spark-sql-streaming-StateStoreWriter.adoc#setStoreMetrics, setting the store-specific metrics>>.

=== [[shouldRunAnotherBatch]] Checking Out Whether Last Batch Execution Requires Another Non-Data Batch or Not -- `shouldRunAnotherBatch` Method

[source, scala]
----
shouldRunAnotherBatch(newMetadata: OffsetSeqMetadata): Boolean
----

NOTE: `shouldRunAnotherBatch` is part of the <<spark-sql-streaming-StateStoreWriter.adoc#shouldRunAnotherBatch, StateStoreWriter Contract>> to indicate whether <<spark-sql-streaming-MicroBatchExecution.adoc#, MicroBatchExecution>> should run another non-data batch (based on the updated <<spark-sql-streaming-OffsetSeqMetadata.adoc#, OffsetSeqMetadata>> with the current event-time watermark and the batch timestamp).

`shouldRunAnotherBatch` uses the <<timeoutConf, GroupStateTimeout>> as follows:

* With <<spark-sql-streaming-GroupStateTimeout.adoc#EventTimeTimeout, EventTimeTimeout>>, `shouldRunAnotherBatch` is positive (`true`) only when the <<eventTimeWatermark, event-time watermark>> is defined and is older (below) the <<spark-sql-streaming-OffsetSeqMetadata.adoc#batchWatermarkMs, event-time watermark>> of the given `OffsetSeqMetadata`

* With <<spark-sql-streaming-GroupStateTimeout.adoc#NoTimeout, NoTimeout>> (and other <<spark-sql-streaming-GroupStateTimeout.adoc#extensions, GroupStateTimeouts>> if there were any), `shouldRunAnotherBatch` is always negative (`false`)

* With <<spark-sql-streaming-GroupStateTimeout.adoc#ProcessingTimeTimeout, ProcessingTimeTimeout>>, `shouldRunAnotherBatch` is always positive (`true`)

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| isTimeoutEnabled
a| [[isTimeoutEnabled]] Flag that says whether the <<timeoutConf, GroupStateTimeout>> is not <<spark-sql-streaming-GroupStateTimeout.adoc#NoTimeout, NoTimeout>>

Used when:

* `FlatMapGroupsWithStateExec` is created (and creates the internal <<stateManager, StateManager>>)

* `InputProcessor` is requested to [processTimedOutState](InputProcessor.md#processTimedOutState)

| stateAttributes
a| [[stateAttributes]]

| stateDeserializer
a| [[stateDeserializer]]

| stateSerializer
a| [[stateSerializer]]

| timestampTimeoutAttribute
a| [[timestampTimeoutAttribute]]

| watermarkPresent
a| [[watermarkPresent]] Flag that says whether the <<child, child>> physical operator has a <<spark-sql-streaming-EventTimeWatermark.adoc#delayKey, watermark attribute>> (among the output attributes).

Used exclusively when `InputProcessor` is requested to [callFunctionAndUpdateState](InputProcessor.md#callFunctionAndUpdateState)
|===
