# StateStoreHandler

`StateStoreHandler` is the internal <<contract, base>> of <<extensions, state store handlers>> that manage a <<stateStore, StateStore>> (i.e. <<commit, commit>>, <<abortIfNeeded, abortIfNeeded>> and <<metrics, metrics>>).

[[stateStoreType]]
`StateStoreHandler` takes a single `StateStoreType` to be created:

* [[KeyToNumValuesType]] `KeyToNumValuesType` for <<KeyToNumValuesStore, KeyToNumValuesStore>> (alias: `keyToNumValues`)

* [[KeyWithIndexToValueType]] `KeyWithIndexToValueType` for <<KeyWithIndexToValueStore, KeyWithIndexToValueStore>> (alias: `keyWithIndexToValue`)

NOTE: `StateStoreHandler` is a Scala private abstract class and cannot be <<creating-instance, created>> directly. It is created indirectly for the <<extensions, concrete StateStoreHandlers>>.

[[contract]]
.StateStoreHandler Contract
[cols="1m,2",options="header",width="100%"]
|===
| Method
| Description

| stateStore
a| [[stateStore]]

[source, scala]
----
stateStore: StateStore
----

[StateStore](../stateful-stream-processing/StateStore.md)
|===

[[extensions]]
.StateStoreHandlers
[cols="1,2",options="header",width="100%"]
|===
| StateStoreHandler
| Description

| <<KeyToNumValuesStore.md#, KeyToNumValuesStore>>
| [[KeyToNumValuesStore]] `StateStoreHandler` of <<KeyToNumValuesType, KeyToNumValuesType>>

| <<KeyWithIndexToValueStore.md#, KeyWithIndexToValueStore>>
| [[KeyWithIndexToValueStore]]

|===

[[logging]]
[TIP]
====
Enable `ALL` logging levels for `org.apache.spark.sql.execution.streaming.state.SymmetricHashJoinStateManager.StateStoreHandler` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.execution.streaming.state.SymmetricHashJoinStateManager.StateStoreHandler=ALL
```

Refer to <<spark-sql-streaming-spark-logging.md#, Logging>>.
====

=== [[metrics]] Performance Metrics -- `metrics` Method

[source, scala]
----
metrics: StateStoreMetrics
----

`metrics` simply requests the [StateStore](#stateStore) for the [StateStoreMetrics](../stateful-stream-processing/StateStore.md#metrics).

`metrics` is used when `SymmetricHashJoinStateManager` is requested for the [metrics](SymmetricHashJoinStateManager.md#metrics).

=== [[commit]] Committing State (Changes to State Store) -- `commit` Method

[source, scala]
----
commit(): Unit
----

`commit`...FIXME

NOTE: `commit` is used when...FIXME

=== [[abortIfNeeded]] `abortIfNeeded` Method

[source, scala]
----
abortIfNeeded(): Unit
----

`abortIfNeeded`...FIXME

NOTE: `abortIfNeeded` is used when...FIXME

=== [[getStateStore]] Loading State Store (By Key and Value Schemas) -- `getStateStore` Method

[source, scala]
----
getStateStore(
  keySchema: StructType,
  valueSchema: StructType): StateStore
----

`getStateStore` creates a new <<StateStoreProviderId.md#, StateStoreProviderId>> (for the [StatefulOperatorStateInfo](SymmetricHashJoinStateManager.md#stateInfo) of the owning `SymmetricHashJoinStateManager`, the partition ID from the execution context, and the [name of the state store](SymmetricHashJoinStateManager.md#getStateStoreName) for the [JoinSide](SymmetricHashJoinStateManager.md#joinSide) and <<stateStoreType, StateStoreType>>).

`getStateStore` uses the `StateStore` utility to [look up a StateStore for the StateStoreProviderId](../stateful-stream-processing/StateStore.md#get-StateStore).

In the end, `getStateStore` prints out the following INFO message to the logs:

```
Loaded store [storeId]
```

`getStateStore` is used when [KeyToNumValuesStore](KeyToNumValuesStore.md#stateStore) and <<KeyWithIndexToValueStore.md#stateStore, KeyWithIndexToValueStore>> state store handlers are created (for [SymmetricHashJoinStateManager](SymmetricHashJoinStateManager.md)).

=== [[StateStoreType]] `StateStoreType` Contract (Sealed Trait)

`StateStoreType` is required to create a <<creating-instance, StateStoreHandler>>.

[[StateStoreType-implementations]]
.StateStoreTypes
[cols="1m,1m,2",options="header",width="100%"]
|===
| StateStoreType
| toString
| Description

| KeyToNumValuesType
| keyToNumValues
| [[KeyToNumValuesType]]

| KeyWithIndexToValueType
| keyWithIndexToValue
| [[KeyWithIndexToValueType]]
|===

NOTE: `StateStoreType` is a Scala private *sealed trait* which means that all the <<StateStoreType-implementations, implementations>> are in the same compilation unit (a single file).
