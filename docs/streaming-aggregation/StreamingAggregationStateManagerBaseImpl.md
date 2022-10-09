# StreamingAggregationStateManagerBaseImpl

`StreamingAggregationStateManagerBaseImpl` is a base implementation of the [StreamingAggregationStateManager](StreamingAggregationStateManager.md) abstraction for [state managers for streaming aggregations](#implementations).

## Implementations

* `StreamingAggregationStateManagerImplV1` (legacy)
* [StreamingAggregationStateManagerImplV2](StreamingAggregationStateManagerImplV2.md)

## Creating Instance

`StreamingAggregationStateManagerBaseImpl` takes the following to be created:

* <span id="keyExpressions"> Key `Attribute`s ([Spark SQL]({{ book.spark_sql }}/expressions/Attribute))
* <span id="inputRowAttributes"> Input Row `Attribute`s ([Spark SQL]({{ book.spark_sql }}/expressions/Attribute))

!!! note "Abstract Class"
    `StreamingAggregationStateManagerBaseImpl` is an abstract class and cannot be created directly. It is created indirectly for the [concrete StreamingAggregationStateManagerBaseImpls](#implementations).

## <span id="commit"> Committing State Changes

```scala
commit(
  store: StateStore): Long
```

`commit` is part of the [StreamingAggregationStateManager](StreamingAggregationStateManager.md#commit) abstraction.

---

`commit` requests the given [StateStore](../stateful-stream-processing/StateStore.md) to [commit state changes](../stateful-stream-processing/StateStore.md#commit).

## <span id="getKey"> Extracting Key

```scala
getKey(
  row: UnsafeRow): UnsafeRow
```

`getKey` is part of the [StreamingAggregationStateManager](StreamingAggregationStateManager.md#getKey) abstraction.

---

`getKey` uses the [keyProjector](#keyProjector) to extract a key from the given `row`.

## <span id="remove"> Removing Key

```scala
remove(
  store: StateStore,
  key: UnsafeRow): Unit
```

`remove` is part of the [StreamingAggregationStateManager](StreamingAggregationStateManager.md#remove) abstraction.

---

`remove` requests the given [StateStore](../stateful-stream-processing/StateStore.md) to [remove](../stateful-stream-processing/StateStore.md#remove) the given `key`.
