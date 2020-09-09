== [[InputProcessor]] InputProcessor Helper Class of FlatMapGroupsWithStateExec Physical Operator

`InputProcessor` is a helper class to manage state in the <<store, state store>> for every partition of a [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator.

`InputProcessor` is <<creating-instance, created>> when [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator is executed (and uses `InputProcessor` in the `storeUpdateFunction` while processing rows per partition with a corresponding per-partition state store).

[[creating-instance]][[store]]
`InputProcessor` takes a single <<spark-sql-streaming-StateStore.adoc#, StateStore>> to be created. The `StateStore` manages the per-group state (and is used when processing <<processNewData, new data>> and <<processTimedOutState, timed-out state data>>, and in the ["all rows processed" callback](#onIteratorCompletion)).

=== [[processNewData]] Processing New Data (Creating Iterator of New Data Processed) -- `processNewData` Method

[source, scala]
----
processNewData(dataIter: Iterator[InternalRow]): Iterator[InternalRow]
----

`processNewData` creates a grouped iterator of (of pairs of) per-group state keys and the row values from the given data iterator (`dataIter`) with the [grouping attributes](physical-operators/FlatMapGroupsWithStateExec.md#groupingAttributes) and the output schema of the [child operator](physical-operators/FlatMapGroupsWithStateExec.md#child) (of the parent `FlatMapGroupsWithStateExec` physical operator).

For every per-group state key (in the grouped iterator), `processNewData` requests the [StateManager](physical-operators/FlatMapGroupsWithStateExec.md#stateManager) (of the parent `FlatMapGroupsWithStateExec` physical operator) to <<spark-sql-streaming-StateManager.adoc#getState, get the state>> (from the <<spark-sql-streaming-StateStore.adoc#, StateStore>>) and <<callFunctionAndUpdateState, callFunctionAndUpdateState>> (with the `hasTimedOut` flag off, i.e. `false`).

`processNewData` is used when [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator is executed.

=== [[processTimedOutState]] Processing Timed-Out State Data (Creating Iterator of Timed-Out State Data) -- `processTimedOutState` Method

[source, scala]
----
processTimedOutState(): Iterator[InternalRow]
----

`processTimedOutState` does nothing and simply returns an empty iterator for [GroupStateTimeout.NoTimeout](physical-operators/FlatMapGroupsWithStateExec.md#isTimeoutEnabled).

With [timeout enabled](physical-operators/FlatMapGroupsWithStateExec.md#isTimeoutEnabled), `processTimedOutState` gets the current timeout threshold per [GroupStateTimeout](physical-operators/FlatMapGroupsWithStateExec.md#timeoutConf):

* [batchTimestampMs](physical-operators/FlatMapGroupsWithStateExec.md#batchTimestampMs) for <<spark-sql-streaming-GroupStateTimeout.adoc#ProcessingTimeTimeout, ProcessingTimeTimeout>>

* [eventTimeWatermark](physical-operators/FlatMapGroupsWithStateExec.md#eventTimeWatermark) for <<spark-sql-streaming-GroupStateTimeout.adoc#EventTimeTimeout, EventTimeTimeout>>

`processTimedOutState` creates an iterator of timed-out state data by requesting the [StateManager](physical-operators/FlatMapGroupsWithStateExec.md#stateManager) for <<spark-sql-streaming-StateManager.adoc#getAllState, all the available state data>> (in the <<store, StateStore>>) and takes only the state data with timeout defined and below the current timeout threshold.

In the end, for every timed-out state data, `processTimedOutState` <<callFunctionAndUpdateState, callFunctionAndUpdateState>> (with the `hasTimedOut` flag enabled).

`processTimedOutState` is used when [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator is executed.

=== [[callFunctionAndUpdateState]] `callFunctionAndUpdateState` Internal Method

[source, scala]
----
callFunctionAndUpdateState(
  stateData: StateData,
  valueRowIter: Iterator[InternalRow],
  hasTimedOut: Boolean): Iterator[InternalRow]
----

[NOTE]
====
`callFunctionAndUpdateState` is used when `InputProcessor` is requested to process <<processNewData, new data>> and <<processTimedOutState, timed-out state data>>.

When <<processNewData, processing new data>>, `hasTimedOut` flag is off (`false`).

When <<processTimedOutState, processing timed-out state data>>, `hasTimedOut` flag is on (`true`).
====

`callFunctionAndUpdateState` creates a key object by requesting the given `StateData` for the `UnsafeRow` of the key (_keyRow_) and converts it to an object (using the internal <<getKeyObj, state key converter>>).

`callFunctionAndUpdateState` creates value objects by taking every value row (from the given `valueRowIter` iterator) and converts them to objects (using the internal <<getValueObj, state value converter>>).

`callFunctionAndUpdateState` creates a new [GroupStateImpl](GroupStateImpl.md#createForStreaming) with the following:

* The current state value (of the given `StateData`) that could possibly be `null`

* The [batchTimestampMs](physical-operators/FlatMapGroupsWithStateExec.md#batchTimestampMs) of the parent `FlatMapGroupsWithStateExec` operator (that could possibly be [-1](GroupStateImpl.md#NO_TIMESTAMP))

* The [event-time watermark](physical-operators/FlatMapGroupsWithStateExec.md#eventTimeWatermark) of the parent `FlatMapGroupsWithStateExec` operator (that could possibly be [-1](GroupStateImpl.md#NO_TIMESTAMP))

* The [GroupStateTimeout](physical-operators/FlatMapGroupsWithStateExec.md#timeoutConf) of the parent `FlatMapGroupsWithStateExec` operator

* The [watermarkPresent](physical-operators/FlatMapGroupsWithStateExec.md#watermarkPresent) flag of the parent `FlatMapGroupsWithStateExec` operator

* The given `hasTimedOut` flag

`callFunctionAndUpdateState` then executes the [user-defined state function](physical-operators/FlatMapGroupsWithStateExec.md#func) (of the parent `FlatMapGroupsWithStateExec` operator) on the key object, value objects, and the newly-created `GroupStateImpl`.

For every output value from the user-defined state function, `callFunctionAndUpdateState` updates <<numOutputRows, numOutputRows>> performance metric and wraps the values to an internal row (using the internal <<getOutputRow, output value converter>>).

In the end, `callFunctionAndUpdateState` returns a `Iterator[InternalRow]` which calls the <<onIteratorCompletion, completion function>> right after rows have been processed (so the iterator is considered fully consumed).

==== [[onIteratorCompletion]] "All Rows Processed" Callback -- `onIteratorCompletion` Internal Method

[source, scala]
----
onIteratorCompletion: Unit
----

`onIteratorCompletion` branches off per whether the `GroupStateImpl` has been marked [removed](GroupStateImpl.md#hasRemoved) and no [timeout timestamp](GroupStateImpl.md#getTimeoutTimestamp) is specified or not.

When the `GroupStateImpl` has been marked [removed](GroupStateImpl.md#hasRemoved) and no [timeout timestamp](GroupStateImpl.md#getTimeoutTimestamp) is specified, `onIteratorCompletion` does the following:

. Requests the [StateManager](physical-operators/FlatMapGroupsWithStateExec.md#stateManager) (of the parent `FlatMapGroupsWithStateExec` operator) to <<spark-sql-streaming-StateManager.adoc#removeState, remove the state>> (from the <<store, StateStore>> for the key row of the given `StateData`)

. Increments the <<numUpdatedStateRows, numUpdatedStateRows>> performance metric

Otherwise, when the `GroupStateImpl` has not been marked [removed](GroupStateImpl.md#hasRemoved) or the [timeout timestamp](GroupStateImpl.md#getTimeoutTimestamp) is specified, `onIteratorCompletion` checks whether the timeout timestamp has changed by comparing the timeout timestamps of the [GroupStateImpl](GroupStateImpl.md#getTimeoutTimestamp) and the given `StateData`.

(only when the `GroupStateImpl` has been [updated](GroupStateImpl.md#hasUpdated), [removed](GroupStateImpl.md#hasRemoved) or the timeout timestamp changed) `onIteratorCompletion` does the following:

. Requests the [StateManager](physical-operators/FlatMapGroupsWithStateExec.md#stateManager) (of the parent `FlatMapGroupsWithStateExec` operator) to <<spark-sql-streaming-StateManager.adoc#putState, persist the state>> (in the <<store, StateStore>> with the key row, updated state object, and the timeout timestamp of the given `StateData`)

. Increments the <<numUpdatedStateRows, numUpdatedStateRows>> performance metrics

NOTE: `onIteratorCompletion` is used exclusively when `InputProcessor` is requested to <<callFunctionAndUpdateState, callFunctionAndUpdateState>> (right after rows have been processed)

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| getKeyObj
a| [[getKeyObj]] A *state key converter* (of type `InternalRow => Any`) to deserialize a given row (for a per-group state key) to the current state value

* The deserialization expression for keys is specified as the [key deserializer expression](physical-operators/FlatMapGroupsWithStateExec.md#keyDeserializer) when the parent `FlatMapGroupsWithStateExec` operator is created

* The data type of state keys is specified as the [grouping attributes](physical-operators/FlatMapGroupsWithStateExec.md#groupingAttributes) when the parent `FlatMapGroupsWithStateExec` operator is created

Used exclusively when `InputProcessor` is requested to <<callFunctionAndUpdateState, callFunctionAndUpdateState>>.

| getOutputRow
a| [[getOutputRow]] A *output value converter* (of type `Any => InternalRow`) to wrap a given output value (from the user-defined state function) to a row

* The data type of the row is specified as the data type of the [output object attribute](physical-operators/FlatMapGroupsWithStateExec.md#outputObjAttr) when the parent `FlatMapGroupsWithStateExec` operator is created

Used exclusively when `InputProcessor` is requested to <<callFunctionAndUpdateState, callFunctionAndUpdateState>>.

| getValueObj
a| [[getValueObj]] A *state value converter* (of type `InternalRow => Any`) to deserialize a given row (for a per-group state value) to a Scala value

* The deserialization expression for values is specified as the [value deserializer expression](physical-operators/FlatMapGroupsWithStateExec.md#valueDeserializer) when the parent `FlatMapGroupsWithStateExec` operator is created

* The data type of state values is specified as the [data attributes](physical-operators/FlatMapGroupsWithStateExec.md#dataAttributes) when the parent `FlatMapGroupsWithStateExec` operator is created

Used exclusively when `InputProcessor` is requested to <<callFunctionAndUpdateState, callFunctionAndUpdateState>>.

| numOutputRows
a| [[numOutputRows]] `numOutputRows` performance metric

|===
