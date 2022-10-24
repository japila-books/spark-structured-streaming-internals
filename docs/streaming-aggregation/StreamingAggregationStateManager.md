# StreamingAggregationStateManager

`StreamingAggregationStateManager` is an [abstraction](#contract) of [state managers](#implementations) for the physical operators used in [Streaming Aggregation](index.md) (e.g., [StateStoreSaveExec](../physical-operators/StateStoreSaveExec.md) and [StateStoreRestoreExec](../physical-operators/StateStoreRestoreExec.md)).

## Contract

### <span id="commit"> Committing State Changes

```scala
commit(
  store: StateStore): Long
```

Commits all state changes (_updates_) to the given [StateStore](../stateful-stream-processing/StateStore.md) and returns a new version

See [StreamingAggregationStateManagerBaseImpl](StreamingAggregationStateManagerBaseImpl.md#commit)

Used when:

* `StateStoreSaveExec` physical operator is [executed](../physical-operators/StateStoreSaveExec.md#doExecute)

### <span id="get"> Retrieving Value for Key (from ReadStateStore)

```scala
get(
  store: ReadStateStore,
  key: UnsafeRow): UnsafeRow
```

Retrieves (_gets_) the current value for a given non-`null` key from [ReadStateStore](../stateful-stream-processing/ReadStateStore.md)

See [StreamingAggregationStateManagerImplV2](StreamingAggregationStateManagerImplV2.md#get)

Used when:

* `StateStoreRestoreExec` physical operator is [executed](../physical-operators/StateStoreRestoreExec.md#doExecute)

### <span id="getKey"> Extracting Key

```scala
getKey(
  row: UnsafeRow): UnsafeRow
```

Extracts the columns of a key from the given `row`

See [StreamingAggregationStateManagerBaseImpl](StreamingAggregationStateManagerBaseImpl.md#getKey)

Used when:

* `StateStoreRestoreExec` physical operator is [executed](../physical-operators/StateStoreRestoreExec.md#doExecute)

### <span id="iterator"> All Aggregates by Grouping Keys

```scala
iterator(
  store: ReadStateStore): Iterator[UnsafeRowPair] // (1)!
```

1. `UnsafeRowPair(var key: UnsafeRow = null, var value: UnsafeRow = null)`

Lazy collection (`Iterator`) of all the aggregates by grouping keys (state pairs) in the given [ReadStateStore](../stateful-stream-processing/ReadStateStore.md)

See [StreamingAggregationStateManagerImplV2](StreamingAggregationStateManagerImplV2.md#iterator)

Used when:

* `StateStoreSaveExec` physical operator is [executed](../physical-operators/StateStoreSaveExec.md#doExecute) (for [Append](../physical-operators/StateStoreSaveExec.md#doExecute-Append) output mode)

### <span id="put"> Storing New Value for Key

```scala
put(
  store: StateStore,
  row: UnsafeRow): Unit
```

Stores (_puts_) a new value for a non-`null` key to the [StateStore](../stateful-stream-processing/StateStore.md).
The key and the value are part of the given `row`.
The key is extracted using [getKey](#getKey).

See [StreamingAggregationStateManagerImplV2](StreamingAggregationStateManagerImplV2.md#put)

Used when:

* `StateStoreSaveExec` physical operator is [executed](../physical-operators/StateStoreSaveExec.md#doExecute)

### <span id="remove"> Removing Key

```scala
remove(
  store: StateStore,
  key: UnsafeRow): Unit
```

Removes a non-`null` key from the [StateStore](../stateful-stream-processing/StateStore.md)

See [StreamingAggregationStateManagerBaseImpl](StreamingAggregationStateManagerBaseImpl.md#remove)

Used when:

* `WatermarkSupport` physical operator is requested to [removeKeysOlderThanWatermark](../physical-operators/WatermarkSupport.md#removeKeysOlderThanWatermark)
* `StateStoreSaveExec` physical operator is [executed](../physical-operators/StateStoreSaveExec.md#doExecute)

## Implementations

* [StreamingAggregationStateManagerBaseImpl](StreamingAggregationStateManagerBaseImpl.md)

??? note "Sealed Trait"
    `StreamingAggregationStateManager` is a Scala **sealed trait** which means that all of the implementations are in the same compilation unit (a single file).

    Learn more in the [Scala Language Specification]({{ scala.spec }}/05-classes-and-objects.html#sealed).

## <span id="createStateManager"> Creating StreamingAggregationStateManager

```scala
createStateManager(
  keyExpressions: Seq[Attribute],
  inputRowAttributes: Seq[Attribute],
  stateFormatVersion: Int): StreamingAggregationStateManager
```

`createStateManager` creates a `StreamingAggregationStateManager` based on the given `stateFormatVersion` (based on [spark.sql.streaming.aggregation.stateFormatVersion](../configuration-properties.md#spark.sql.streaming.aggregation.stateFormatVersion)).

stateFormatVersion | StreamingAggregationStateManager
-------------------|----------
 1 | `StreamingAggregationStateManagerImplV1`
 2 | [StreamingAggregationStateManagerImplV2](StreamingAggregationStateManagerImplV2.md)

---

`createStateManager` is used when:

* `StateStoreRestoreExec` physical operator is [created](../physical-operators/StateStoreRestoreExec.md#stateManager)
* `StateStoreSaveExec` physical operator is [created](../physical-operators/StateStoreSaveExec.md#stateManager)
