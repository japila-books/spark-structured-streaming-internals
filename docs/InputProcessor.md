# InputProcessor

`InputProcessor` is a helper class that is used to update state (in the [state store](#store)) of a single partition of a [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator.

## Creating Instance

`InputProcessor` takes the following to be created:

* [StateStore](#store)

`InputProcessor` is created when [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator is executed (for [storeUpdateFunction](StateStoreOps.md#storeUpdateFunction) while processing rows per partition with a corresponding per-partition state store).

## <span id="store"> StateStore

`InputProcessor` is given a [StateStore](StateStore.md) when [created](#creating-instance).

The `StateStore` manages the per-group state (and is used when processing [new data](#processNewData) and [timed-out state data](#processTimedOutState), and in the ["all rows processed" callback](#onIteratorCompletion)).

## <span id="processNewData"> Processing New Data

```scala
processNewData(
  dataIter: Iterator[InternalRow]): Iterator[InternalRow]
```

`processNewData` creates a grouped iterator of (of pairs of) per-group state keys and the row values from the given data iterator (`dataIter`) with the [grouping attributes](physical-operators/FlatMapGroupsWithStateExec.md#groupingAttributes) and the output schema of the [child operator](physical-operators/FlatMapGroupsWithStateExec.md#child) (of the parent `FlatMapGroupsWithStateExec` physical operator).

For every per-group state key (in the grouped iterator), `processNewData` requests the [StateManager](physical-operators/FlatMapGroupsWithStateExec.md#stateManager) (of the parent `FlatMapGroupsWithStateExec` physical operator) to [get the state](spark-sql-streaming-StateManager.md#getState) (from the [StateStore](#store)) and [callFunctionAndUpdateState](#callFunctionAndUpdateState) (with the `hasTimedOut` flag off).

`processNewData` is used when [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator is executed.

## <span id="processTimedOutState"> Processing Timed-Out State Data

```scala
processTimedOutState(): Iterator[InternalRow]
```

`processTimedOutState` does nothing and simply returns an empty iterator for [GroupStateTimeout.NoTimeout](physical-operators/FlatMapGroupsWithStateExec.md#isTimeoutEnabled).

With [timeout enabled](physical-operators/FlatMapGroupsWithStateExec.md#isTimeoutEnabled), `processTimedOutState` gets the current timeout threshold per [GroupStateTimeout](physical-operators/FlatMapGroupsWithStateExec.md#timeoutConf):

* [batchTimestampMs](physical-operators/FlatMapGroupsWithStateExec.md#batchTimestampMs) for [ProcessingTimeTimeout](spark-sql-streaming-GroupStateTimeout.md#ProcessingTimeTimeout)

* [eventTimeWatermark](physical-operators/FlatMapGroupsWithStateExec.md#eventTimeWatermark) for [EventTimeTimeout](spark-sql-streaming-GroupStateTimeout.md#EventTimeTimeout)

`processTimedOutState` creates an iterator of timed-out state data by requesting the [StateManager](physical-operators/FlatMapGroupsWithStateExec.md#stateManager) for [all the available state data](spark-sql-streaming-StateManager.md#getAllState) (in the [StateStore](#store)) and takes only the state data with timeout defined and below the current timeout threshold.

In the end, for every timed-out state data, `processTimedOutState` [callFunctionAndUpdateState](#callFunctionAndUpdateState) (with the `hasTimedOut` flag on).

`processTimedOutState` is used when [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator is executed.

## <span id="callFunctionAndUpdateState"> callFunctionAndUpdateState Internal Method

```scala
callFunctionAndUpdateState(
  stateData: StateData,
  valueRowIter: Iterator[InternalRow],
  hasTimedOut: Boolean): Iterator[InternalRow]
```

`callFunctionAndUpdateState` is used when `InputProcessor` is requested to process [new data](#processNewData) and [timed-out state data](#processTimedOutState) with the given `hasTimedOut` flag is off and on, respectively.

`callFunctionAndUpdateState` creates a key object by requesting the given `StateData` for the `UnsafeRow` of the key (_keyRow_) and converts it to an object (using the internal [state key converter](#getKeyObj)).

`callFunctionAndUpdateState` creates value objects by taking every value row (from the given `valueRowIter` iterator) and converts them to objects (using the internal [state value converter](#getValueObj)).

`callFunctionAndUpdateState` creates a new [GroupStateImpl](GroupStateImpl.md#createForStreaming) with the following:

* The current state value (of the given `StateData`) that could possibly be `null`

* The [batchTimestampMs](physical-operators/FlatMapGroupsWithStateExec.md#batchTimestampMs) of the parent `FlatMapGroupsWithStateExec` operator (that could possibly be [-1](GroupStateImpl.md#NO_TIMESTAMP))

* The [event-time watermark](physical-operators/FlatMapGroupsWithStateExec.md#eventTimeWatermark) of the parent `FlatMapGroupsWithStateExec` operator (that could possibly be [-1](GroupStateImpl.md#NO_TIMESTAMP))

* The [GroupStateTimeout](physical-operators/FlatMapGroupsWithStateExec.md#timeoutConf) of the parent `FlatMapGroupsWithStateExec` operator

* The [watermarkPresent](physical-operators/FlatMapGroupsWithStateExec.md#watermarkPresent) flag of the parent `FlatMapGroupsWithStateExec` operator

* The given `hasTimedOut` flag

`callFunctionAndUpdateState` then executes the [user-defined state function](physical-operators/FlatMapGroupsWithStateExec.md#func) (of the parent `FlatMapGroupsWithStateExec` operator) on the key object, value objects, and the newly-created `GroupStateImpl`.

For every output value from the user-defined state function, `callFunctionAndUpdateState` updates [numOutputRows](#numOutputRows) performance metric and wraps the values to an internal row (using the internal [output value converter](#getOutputRow)).

In the end, `callFunctionAndUpdateState` returns a `Iterator[InternalRow]` which calls the [completion function](#onIteratorCompletion) right after rows have been processed (so the iterator is considered fully consumed).

### <span id="onIteratorCompletion"> "All Rows Processed" Callback

```scala
onIteratorCompletion: Unit
```

`onIteratorCompletion` branches off per whether the `GroupStateImpl` has been marked [removed](GroupStateImpl.md#hasRemoved) and no [timeout timestamp](GroupStateImpl.md#getTimeoutTimestamp) is specified or not.

When the `GroupStateImpl` has been marked [removed](GroupStateImpl.md#hasRemoved) and no [timeout timestamp](GroupStateImpl.md#getTimeoutTimestamp) is specified, `onIteratorCompletion` does the following:

. Requests the [StateManager](physical-operators/FlatMapGroupsWithStateExec.md#stateManager) (of the parent `FlatMapGroupsWithStateExec` operator) to [remove the state](spark-sql-streaming-StateManager.md#removeState) (from the [StateStore](#store) for the key row of the given `StateData`)

. Increments the [numUpdatedStateRows](#numUpdatedStateRows) performance metric

Otherwise, when the `GroupStateImpl` has not been marked [removed](GroupStateImpl.md#hasRemoved) or the [timeout timestamp](GroupStateImpl.md#getTimeoutTimestamp) is specified, `onIteratorCompletion` checks whether the timeout timestamp has changed by comparing the timeout timestamps of the [GroupStateImpl](GroupStateImpl.md#getTimeoutTimestamp) and the given `StateData`.

(only when the `GroupStateImpl` has been [updated](GroupStateImpl.md#hasUpdated), [removed](GroupStateImpl.md#hasRemoved) or the timeout timestamp changed) `onIteratorCompletion` does the following:

. Requests the [StateManager](physical-operators/FlatMapGroupsWithStateExec.md#stateManager) (of the parent `FlatMapGroupsWithStateExec` operator) to [persist the state](spark-sql-streaming-StateManager.md#putState) (in the [StateStore](#store) with the key row, updated state object, and the timeout timestamp of the given `StateData`)

. Increments the [numUpdatedStateRows](#numUpdatedStateRows) performance metrics

`onIteratorCompletion` is used when `InputProcessor` is requested to [callFunctionAndUpdateState](#callFunctionAndUpdateState) (right after rows have been processed)

## Converters

### <span id="getOutputRow"> Output Value Converter

An **output value converter** (of type `Any => InternalRow`) to wrap a given output value (from the user-defined state function) to a row

* The data type of the row is specified as the data type of the [output object attribute](physical-operators/FlatMapGroupsWithStateExec.md#outputObjAttr) when the parent `FlatMapGroupsWithStateExec` operator is created

Used when `InputProcessor` is requested to [callFunctionAndUpdateState](#callFunctionAndUpdateState).

### <span id="getKeyObj"> State Key Converter

A **state key converter** (of type `InternalRow => Any`) to deserialize a given row (for a per-group state key) to the current state value

* The deserialization expression for keys is specified as the [key deserializer expression](physical-operators/FlatMapGroupsWithStateExec.md#keyDeserializer) when the parent `FlatMapGroupsWithStateExec` operator is created

* The data type of state keys is specified as the [grouping attributes](physical-operators/FlatMapGroupsWithStateExec.md#groupingAttributes) when the parent `FlatMapGroupsWithStateExec` operator is created

Used when `InputProcessor` is requested to [callFunctionAndUpdateState](#callFunctionAndUpdateState).

### <span id="getValueObj"> State Value Converter

A **state value converter** (of type `InternalRow => Any`) to deserialize a given row (for a per-group state value) to a Scala value

* The deserialization expression for values is specified as the [value deserializer expression](physical-operators/FlatMapGroupsWithStateExec.md#valueDeserializer) when the parent `FlatMapGroupsWithStateExec` operator is created

* The data type of state values is specified as the [data attributes](physical-operators/FlatMapGroupsWithStateExec.md#dataAttributes) when the parent `FlatMapGroupsWithStateExec` operator is created

Used when `InputProcessor` is requested to [callFunctionAndUpdateState](#callFunctionAndUpdateState).
