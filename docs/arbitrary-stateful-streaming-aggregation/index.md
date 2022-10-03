# Arbitrary Stateful Streaming Aggregation

**Arbitrary Stateful Streaming Aggregation** is a [streaming aggregation query](../streaming-aggregation/index.md) that uses the following high-level operators of `KeyValueGroupedDataset` ([Spark SQL]({{ book.spark_sql }}/basic-aggregation/KeyValueGroupedDataset)):

* [mapGroupsWithState](../KeyValueGroupedDataset.md#mapGroupsWithState) for implicit state logic

* [flatMapGroupsWithState](../KeyValueGroupedDataset.md#flatMapGroupsWithState) for explicit state logic

`KeyValueGroupedDataset` represents a grouped dataset as a result of [Dataset.groupByKey](../operators/groupByKey.md) operator.

`mapGroupsWithState` and `flatMapGroupsWithState` operators use [GroupState](../GroupState.md) as **group streaming aggregation state** that is created separately for every **aggregation key** with an **aggregation state value** (of a user-defined type).

`mapGroupsWithState` and `flatMapGroupsWithState` operators use [GroupStateTimeout](../GroupStateTimeout.md) as an **aggregation state timeout** that defines when a [GroupState](../GroupState.md) is considered **timed-out** (_expired_).

## Demos

Use the following demos and complete applications to learn more:

* [Demo: Internals of FlatMapGroupsWithStateExec Physical Operator](../demo/spark-sql-streaming-demo-FlatMapGroupsWithStateExec.md)

* [Demo: Arbitrary Stateful Streaming Aggregation with KeyValueGroupedDataset.flatMapGroupsWithState Operator](../demo/arbitrary-stateful-streaming-aggregation-flatMapGroupsWithState.md)

* [groupByKey Streaming Aggregation in Update Mode](../demo/groupByKey-count-Update.md)

* [FlatMapGroupsWithStateApp](https://github.com/jaceklaskowski/spark-structured-streaming-book/blob/master/examples/src/main/scala/pl/japila/spark/FlatMapGroupsWithStateApp.scala)

## <span id="metrics"> Performance Metrics

Arbitrary Stateful Streaming Aggregation uses **performance metrics** (of the [StateStoreWriter](../physical-operators/StateStoreWriter.md) through [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md) physical operator).

## <span id="internals"> Internals

One of the most important internal execution components of Arbitrary Stateful Streaming Aggregation is [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md) physical operator.

When executed, `FlatMapGroupsWithStateExec` first validates a selected [GroupStateTimeout](../GroupStateTimeout.md):

* For [ProcessingTimeTimeout](../GroupStateTimeout.md#ProcessingTimeTimeout), [batch timeout threshold](../physical-operators/FlatMapGroupsWithStateExec.md#batchTimestampMs) has to be defined

* For [EventTimeTimeout](../GroupStateTimeout.md#EventTimeTimeout), [event-time watermark](../physical-operators/FlatMapGroupsWithStateExec.md#eventTimeWatermark) has to be defined and the [input schema has the watermark attribute](../WatermarkSupport.md#watermarkExpression)

!!! note
    FIXME When are the above requirements met?

`FlatMapGroupsWithStateExec` physical operator then [mapPartitionsWithStateStore](../StateStoreOps.md#mapPartitionsWithStateStore) with a custom `storeUpdateFunction` of the following signature:

```scala
(StateStore, Iterator[T]) => Iterator[U]
```

While generating the recipe, `FlatMapGroupsWithStateExec` uses [StateStoreOps](../StateStoreOps.md) extension method object to register a listener that is executed on a task completion. The listener makes sure that a given [StateStore](../StateStore.md) has all state changes either [committed](../StateStore.md#hasCommitted) or [aborted](../StateStore.md#abort).

In the end, `FlatMapGroupsWithStateExec` creates a new [StateStoreRDD](../StateStoreRDD.md) and adds it to the RDD lineage.

`StateStoreRDD` is used to properly distribute tasks across executors (per [preferred locations](../StateStoreRDD.md#getPreferredLocations)) with help of [StateStoreCoordinator](../StateStoreCoordinator.md) (that runs on the driver).

`StateStoreRDD` uses `StateStore` helper to [look up a StateStore](../StateStore.md#get-StateStore) by [StateStoreProviderId](../spark-sql-streaming-StateStoreProviderId.md) and store version.

`FlatMapGroupsWithStateExec` physical operator uses [state managers](../spark-sql-streaming-StateManager.md) that are different than [state managers](../StreamingAggregationStateManager.md) for [Streaming Aggregation](../streaming-aggregation/index.md). [StateStore](../StateStore.md) abstraction is the same as in [Streaming Aggregation](../streaming-aggregation/index.md).

One of the important execution steps is when `InputProcessor` (of [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md) physical operator) is requested to [callFunctionAndUpdateState](../InputProcessor.md#callFunctionAndUpdateState). That executes the **user-defined state function** on a per-group state key object, value objects, and a [GroupStateImpl](../GroupStateImpl.md).
