# Arbitrary Stateful Streaming Aggregation

**Arbitrary Stateful Streaming Aggregation** is a [streaming aggregation query](spark-sql-streaming-aggregation.md) that uses the following [KeyValueGroupedDataset](KeyValueGroupedDataset.md) operators:

* [mapGroupsWithState](KeyValueGroupedDataset.md#mapGroupsWithState) for implicit state logic

* [flatMapGroupsWithState](KeyValueGroupedDataset.md#flatMapGroupsWithState) for explicit state logic

`KeyValueGroupedDataset` represents a grouped dataset as a result of [Dataset.groupByKey](spark-sql-streaming-Dataset-operators.md#groupByKey) operator.

`mapGroupsWithState` and `flatMapGroupsWithState` operators use [GroupState](GroupState.md) as **group streaming aggregation state** that is created separately for every **aggregation key** with an **aggregation state value** (of a user-defined type).

`mapGroupsWithState` and `flatMapGroupsWithState` operators use <<spark-sql-streaming-GroupStateTimeout.md#, GroupStateTimeout>> as an *aggregation state timeout* that defines when a [GroupState](GroupState.md) can be considered *timed-out* (_expired_).

=== [[demos]] Demos

Use the following demos and complete applications to learn more:

* <<spark-sql-streaming-demo-FlatMapGroupsWithStateExec.md#, Demo: Internals of FlatMapGroupsWithStateExec Physical Operator>>

* [Demo: Arbitrary Stateful Streaming Aggregation with KeyValueGroupedDataset.flatMapGroupsWithState Operator](demo/arbitrary-stateful-streaming-aggregation-flatMapGroupsWithState.md)

* <<spark-sql-streaming-demo-groupByKey-count-Update.md#, groupByKey Streaming Aggregation in Update Mode>>

* https://github.com/jaceklaskowski/spark-structured-streaming-book/blob/v{{ book.version }}/examples/src/main/scala/pl/japila/spark/FlatMapGroupsWithStateApp.scala[FlatMapGroupsWithStateApp]

=== [[metrics]] Performance Metrics

Arbitrary Stateful Streaming Aggregation uses *performance metrics* (of the <<spark-sql-streaming-StateStoreWriter.md#, StateStoreWriter>> through [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator).

=== [[internals]] Internals

One of the most important internal execution components of Arbitrary Stateful Streaming Aggregation is [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator.

When executed, `FlatMapGroupsWithStateExec` first validates a selected <<spark-sql-streaming-GroupStateTimeout.md#, GroupStateTimeout>>:

* For <<spark-sql-streaming-GroupStateTimeout.md#ProcessingTimeTimeout, ProcessingTimeTimeout>>, [batch timeout threshold](physical-operators/FlatMapGroupsWithStateExec.md#batchTimestampMs) has to be defined

* For <<spark-sql-streaming-GroupStateTimeout.md#EventTimeTimeout, EventTimeTimeout>>, [event-time watermark](physical-operators/FlatMapGroupsWithStateExec.md#eventTimeWatermark) has to be defined and the <<spark-sql-streaming-WatermarkSupport.md#watermarkExpression, input schema has the watermark attribute>>

NOTE: FIXME When are the above requirements met?

`FlatMapGroupsWithStateExec` physical operator then <<spark-sql-streaming-StateStoreOps.md#mapPartitionsWithStateStore, mapPartitionsWithStateStore>> with a custom `storeUpdateFunction` of the following signature:

[source, scala]
----
(StateStore, Iterator[T]) => Iterator[U]
----

While generating the recipe, `FlatMapGroupsWithStateExec` uses <<spark-sql-streaming-StateStoreOps.md#, StateStoreOps>> extension method object to register a listener that is executed on a task completion. The listener makes sure that a given <<spark-sql-streaming-StateStore.md#, StateStore>> has all state changes either <<spark-sql-streaming-StateStore.md#hasCommitted, committed>> or <<spark-sql-streaming-StateStore.md#abort, aborted>>.

In the end, `FlatMapGroupsWithStateExec` creates a new <<spark-sql-streaming-StateStoreRDD.md#, StateStoreRDD>> and adds it to the RDD lineage.

`StateStoreRDD` is used to properly distribute tasks across executors (per <<spark-sql-streaming-StateStoreRDD.md#getPreferredLocations, preferred locations>>) with help of <<spark-sql-streaming-StateStoreCoordinator.md#, StateStoreCoordinator>> (that runs on the driver).

`StateStoreRDD` uses `StateStore` helper to <<spark-sql-streaming-StateStore.md#get-StateStore, look up a StateStore>> by <<spark-sql-streaming-StateStoreProviderId.md#, StateStoreProviderId>> and store version.

`FlatMapGroupsWithStateExec` physical operator uses <<spark-sql-streaming-StateManager.md#, state managers>> that are different than <<spark-sql-streaming-StreamingAggregationStateManager.md#, state managers>> for <<spark-sql-streaming-aggregation.md#, Streaming Aggregation>>. <<spark-sql-streaming-StateStore.md#, StateStore>> abstraction is the same as in <<spark-sql-streaming-aggregation.md#, Streaming Aggregation>>.

One of the important execution steps is when `InputProcessor` (of [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator) is requested to [callFunctionAndUpdateState](InputProcessor.md#callFunctionAndUpdateState). That executes the **user-defined state function** on a per-group state key object, value objects, and a [GroupStateImpl](GroupStateImpl.md).
