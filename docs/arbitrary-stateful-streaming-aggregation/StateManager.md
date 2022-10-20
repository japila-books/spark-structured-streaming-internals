# StateManager

`StateManager` is an [abstraction](#contract) of [state managers](#implementations) that act as _middlemen_ between [state stores](../stateful-stream-processing/StateStore.md) and [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md) physical operator used in [Arbitrary Stateful Streaming Aggregation](../arbitrary-stateful-streaming-aggregation/index.md).

## Contract

### <span id="getAllState"> getAllState

```scala
getAllState(
  store: StateStore): Iterator[StateData]
```

Retrieves all state data (for all keys) from the given [StateStore](../stateful-stream-processing/StateStore.md)

Used when:

* `InputProcessor` is requested to [processTimedOutState](InputProcessor.md#processTimedOutState)

### <span id="getState"> getState

```scala
getState(
  store: StateStore,
  keyRow: UnsafeRow): StateData
```

Gets the state data for the given key from the given [StateStore](../stateful-stream-processing/StateStore.md)

Used when:

* `InputProcessor` is requested to [processNewData](InputProcessor.md#processNewData) and [processNewDataWithInitialState](InputProcessor.md#processNewDataWithInitialState)

### <span id="putState"> putState

```scala
putState(
  store: StateStore,
  keyRow: UnsafeRow,
  state: Any,
  timeoutTimestamp: Long): Unit
```

Persists (_puts_) the given state (value) for the given key in the given [StateStore](../stateful-stream-processing/StateStore.md)

Used when:

* `InputProcessor` is requested to [callFunctionAndUpdateState](InputProcessor.md#callFunctionAndUpdateState) ([right after all rows have been processed](InputProcessor.md#onIteratorCompletion))

### <span id="removeState"> removeState

```scala
removeState(
  store: StateStore,
  keyRow: UnsafeRow): Unit
```

Removes the state for the given key from the given [StateStore](../stateful-stream-processing/StateStore.md)

Used when:

* `InputProcessor` is requested to [callFunctionAndUpdateState](InputProcessor.md#callFunctionAndUpdateState) ([right after all rows have been processed](InputProcessor.md#onIteratorCompletion))

### <span id="stateSchema"> stateSchema

```scala
stateSchema: StructType
```

Used when:

* `FlatMapGroupsWithStateExec` is requested to [execute](../physical-operators/FlatMapGroupsWithStateExec.md#doExecute)
* `StateManagerImplBase` is requested for the [stateDeserializerFunc](StateManagerImplBase.md#stateDeserializerFunc)

## Implementations

* [StateManagerImplBase](StateManagerImplBase.md)
