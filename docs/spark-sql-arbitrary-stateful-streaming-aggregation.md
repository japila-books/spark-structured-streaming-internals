== Arbitrary Stateful Streaming Aggregation

*Arbitrary Stateful Streaming Aggregation* is a <<spark-sql-streaming-aggregation.adoc#, streaming aggregation query>> that uses the following <<spark-sql-streaming-KeyValueGroupedDataset.adoc#, KeyValueGroupedDataset>> operators:

* <<spark-sql-streaming-KeyValueGroupedDataset.adoc#mapGroupsWithState, mapGroupsWithState>> for implicit state logic

* <<spark-sql-streaming-KeyValueGroupedDataset.adoc#flatMapGroupsWithState, flatMapGroupsWithState>> for explicit state logic

`KeyValueGroupedDataset` represents a grouped dataset as a result of <<spark-sql-streaming-Dataset-operators.adoc#groupByKey, Dataset.groupByKey>> operator.

`mapGroupsWithState` and `flatMapGroupsWithState` operators use [GroupState](GroupState.md) as **group streaming aggregation state** that is created separately for every *aggregation key* with an *aggregation state value* (of a user-defined type).

`mapGroupsWithState` and `flatMapGroupsWithState` operators use <<spark-sql-streaming-GroupStateTimeout.adoc#, GroupStateTimeout>> as an *aggregation state timeout* that defines when a [GroupState](GroupState.md) can be considered *timed-out* (_expired_).

=== [[demos]] Demos

Use the following demos and complete applications to learn more:

* <<spark-sql-streaming-demo-FlatMapGroupsWithStateExec.adoc#, Demo: Internals of FlatMapGroupsWithStateExec Physical Operator>>

* <<spark-sql-streaming-demo-arbitrary-stateful-streaming-aggregation-flatMapGroupsWithState.adoc#, Demo: Arbitrary Stateful Streaming Aggregation with KeyValueGroupedDataset.flatMapGroupsWithState Operator>>

* <<spark-sql-streaming-demo-groupByKey-count-Update.adoc#, groupByKey Streaming Aggregation in Update Mode>>

* https://github.com/jaceklaskowski/spark-structured-streaming-book/blob/v{{ book.version }}/examples/src/main/scala/pl/japila/spark/FlatMapGroupsWithStateApp.scala[FlatMapGroupsWithStateApp]

=== [[metrics]] Performance Metrics

Arbitrary Stateful Streaming Aggregation uses *performance metrics* (of the <<spark-sql-streaming-StateStoreWriter.adoc#, StateStoreWriter>> through [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator).

=== [[internals]] Internals

One of the most important internal execution components of Arbitrary Stateful Streaming Aggregation is [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator.

When executed, `FlatMapGroupsWithStateExec` first validates a selected <<spark-sql-streaming-GroupStateTimeout.adoc#, GroupStateTimeout>>:

* For <<spark-sql-streaming-GroupStateTimeout.adoc#ProcessingTimeTimeout, ProcessingTimeTimeout>>, [batch timeout threshold](physical-operators/FlatMapGroupsWithStateExec.md#batchTimestampMs) has to be defined

* For <<spark-sql-streaming-GroupStateTimeout.adoc#EventTimeTimeout, EventTimeTimeout>>, [event-time watermark](physical-operators/FlatMapGroupsWithStateExec.md#eventTimeWatermark) has to be defined and the <<spark-sql-streaming-WatermarkSupport.adoc#watermarkExpression, input schema has the watermark attribute>>

NOTE: FIXME When are the above requirements met?

`FlatMapGroupsWithStateExec` physical operator then <<spark-sql-streaming-StateStoreOps.adoc#mapPartitionsWithStateStore, mapPartitionsWithStateStore>> with a custom `storeUpdateFunction` of the following signature:

[source, scala]
----
(StateStore, Iterator[T]) => Iterator[U]
----

While generating the recipe, `FlatMapGroupsWithStateExec` uses <<spark-sql-streaming-StateStoreOps.adoc#, StateStoreOps>> extension method object to register a listener that is executed on a task completion. The listener makes sure that a given <<spark-sql-streaming-StateStore.adoc#, StateStore>> has all state changes either <<spark-sql-streaming-StateStore.adoc#hasCommitted, committed>> or <<spark-sql-streaming-StateStore.adoc#abort, aborted>>.

In the end, `FlatMapGroupsWithStateExec` creates a new <<spark-sql-streaming-StateStoreRDD.adoc#, StateStoreRDD>> and adds it to the RDD lineage.

`StateStoreRDD` is used to properly distribute tasks across executors (per <<spark-sql-streaming-StateStoreRDD.adoc#getPreferredLocations, preferred locations>>) with help of <<spark-sql-streaming-StateStoreCoordinator.adoc#, StateStoreCoordinator>> (that runs on the driver).

`StateStoreRDD` uses `StateStore` helper to <<spark-sql-streaming-StateStore.adoc#get-StateStore, look up a StateStore>> by <<spark-sql-streaming-StateStoreProviderId.adoc#, StateStoreProviderId>> and store version.

`FlatMapGroupsWithStateExec` physical operator uses <<spark-sql-streaming-StateManager.adoc#, state managers>> that are different than <<spark-sql-streaming-StreamingAggregationStateManager.adoc#, state managers>> for <<spark-sql-streaming-aggregation.adoc#, Streaming Aggregation>>. <<spark-sql-streaming-StateStore.adoc#, StateStore>> abstraction is the same as in <<spark-sql-streaming-aggregation.adoc#, Streaming Aggregation>>.

One of the important execution steps is when `InputProcessor` (of [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator) is requested to [callFunctionAndUpdateState](InputProcessor.md#callFunctionAndUpdateState). That executes the **user-defined state function** on a per-group state key object, value objects, and a [GroupStateImpl](GroupStateImpl.md).
