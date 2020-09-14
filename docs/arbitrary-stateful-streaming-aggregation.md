# Arbitrary Stateful Streaming Aggregation

**Arbitrary Stateful Streaming Aggregation** is a [streaming aggregation query](spark-sql-streaming-aggregation.md) that uses the following [KeyValueGroupedDataset](KeyValueGroupedDataset.md) operators:

* [mapGroupsWithState](KeyValueGroupedDataset.md#mapGroupsWithState) for implicit state logic

* [flatMapGroupsWithState](KeyValueGroupedDataset.md#flatMapGroupsWithState) for explicit state logic

`KeyValueGroupedDataset` represents a grouped dataset as a result of [Dataset.groupByKey](operators/groupByKey.md) operator.

`mapGroupsWithState` and `flatMapGroupsWithState` operators use [GroupState](GroupState.md) as **group streaming aggregation state** that is created separately for every **aggregation key** with an **aggregation state value** (of a user-defined type).

`mapGroupsWithState` and `flatMapGroupsWithState` operators use [GroupStateTimeout](spark-sql-streaming-GroupStateTimeout.md) as an **aggregation state timeout** that defines when a [GroupState](GroupState.md) is considered **timed-out** (_expired_).

## Demos

Use the following demos and complete applications to learn more:

* [Demo: Internals of FlatMapGroupsWithStateExec Physical Operator](demo/spark-sql-streaming-demo-FlatMapGroupsWithStateExec.md)

* [Demo: Arbitrary Stateful Streaming Aggregation with KeyValueGroupedDataset.flatMapGroupsWithState Operator](demo/arbitrary-stateful-streaming-aggregation-flatMapGroupsWithState.md)

* [groupByKey Streaming Aggregation in Update Mode](demo/groupByKey-count-Update.md)

* [FlatMapGroupsWithStateApp](https://github.com/jaceklaskowski/spark-structured-streaming-book/blob/mkdocs-material/examples/src/main/scala/pl/japila/spark/FlatMapGroupsWithStateApp.scala)

## <span id="metrics"> Performance Metrics

Arbitrary Stateful Streaming Aggregation uses **performance metrics** (of the [StateStoreWriter](spark-sql-streaming-StateStoreWriter.md) through [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator).

## <span id="internals"> Internals

One of the most important internal execution components of Arbitrary Stateful Streaming Aggregation is [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator.

When executed, `FlatMapGroupsWithStateExec` first validates a selected [GroupStateTimeout](spark-sql-streaming-GroupStateTimeout.md):

* For [ProcessingTimeTimeout](spark-sql-streaming-GroupStateTimeout.md#ProcessingTimeTimeout), [batch timeout threshold](physical-operators/FlatMapGroupsWithStateExec.md#batchTimestampMs) has to be defined

* For [EventTimeTimeout](spark-sql-streaming-GroupStateTimeout.md#EventTimeTimeout), [event-time watermark](physical-operators/FlatMapGroupsWithStateExec.md#eventTimeWatermark) has to be defined and the [input schema has the watermark attribute](spark-sql-streaming-WatermarkSupport.md#watermarkExpression)

!!! note
    FIXME When are the above requirements met?

`FlatMapGroupsWithStateExec` physical operator then [mapPartitionsWithStateStore](spark-sql-streaming-StateStoreOps.md#mapPartitionsWithStateStore) with a custom `storeUpdateFunction` of the following signature:

```scala
(StateStore, Iterator[T]) => Iterator[U]
```

While generating the recipe, `FlatMapGroupsWithStateExec` uses [StateStoreOps](spark-sql-streaming-StateStoreOps.md) extension method object to register a listener that is executed on a task completion. The listener makes sure that a given [StateStore](spark-sql-streaming-StateStore.md) has all state changes either [committed](spark-sql-streaming-StateStore.md#hasCommitted) or [aborted](spark-sql-streaming-StateStore.md#abort).

In the end, `FlatMapGroupsWithStateExec` creates a new [StateStoreRDD](spark-sql-streaming-StateStoreRDD.md) and adds it to the RDD lineage.

`StateStoreRDD` is used to properly distribute tasks across executors (per [preferred locations](spark-sql-streaming-StateStoreRDD.md#getPreferredLocations)) with help of [StateStoreCoordinator](spark-sql-streaming-StateStoreCoordinator.md) (that runs on the driver).

`StateStoreRDD` uses `StateStore` helper to [look up a StateStore](spark-sql-streaming-StateStore.md#get-StateStore) by [StateStoreProviderId](spark-sql-streaming-StateStoreProviderId.md) and store version.

`FlatMapGroupsWithStateExec` physical operator uses [state managers](spark-sql-streaming-StateManager.md) that are different than [state managers](spark-sql-streaming-StreamingAggregationStateManager.md) for [Streaming Aggregation](spark-sql-streaming-aggregation.md). [StateStore](spark-sql-streaming-StateStore.md) abstraction is the same as in [Streaming Aggregation](spark-sql-streaming-aggregation.md).

One of the important execution steps is when `InputProcessor` (of [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator) is requested to [callFunctionAndUpdateState](InputProcessor.md#callFunctionAndUpdateState). That executes the **user-defined state function** on a per-group state key object, value objects, and a [GroupStateImpl](GroupStateImpl.md).
