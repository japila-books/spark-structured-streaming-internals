== [[StateStoreHandler]] StateStoreHandler Internal Contract

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

<<spark-sql-streaming-StateStore.adoc#, StateStore>>
|===

[[extensions]]
.StateStoreHandlers
[cols="1,2",options="header",width="100%"]
|===
| StateStoreHandler
| Description

| <<spark-sql-streaming-KeyToNumValuesStore.adoc#, KeyToNumValuesStore>>
| [[KeyToNumValuesStore]] `StateStoreHandler` of <<KeyToNumValuesType, KeyToNumValuesType>>

| <<spark-sql-streaming-KeyWithIndexToValueStore.adoc#, KeyWithIndexToValueStore>>
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

Refer to <<spark-sql-streaming-logging.adoc#, Logging>>.
====

=== [[metrics]] Performance Metrics -- `metrics` Method

[source, scala]
----
metrics: StateStoreMetrics
----

`metrics` simply requests the <<stateStore, StateStore>> for the <<spark-sql-streaming-StateStore.adoc#metrics, StateStoreMetrics>>.

NOTE: `metrics` is used exclusively when `SymmetricHashJoinStateManager` is requested for the <<spark-sql-streaming-SymmetricHashJoinStateManager.adoc#metrics, metrics>>.

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

`getStateStore` creates a new <<spark-sql-streaming-StateStoreProviderId.adoc#, StateStoreProviderId>> (for the <<spark-sql-streaming-SymmetricHashJoinStateManager.adoc#stateInfo, StatefulOperatorStateInfo>> of the owning `SymmetricHashJoinStateManager`, the partition ID from the execution context, and the <<spark-sql-streaming-SymmetricHashJoinStateManager.adoc#getStateStoreName, name of the state store>> for the <<spark-sql-streaming-SymmetricHashJoinStateManager.adoc#joinSide, JoinSide>> and <<stateStoreType, StateStoreType>>).

`getStateStore` uses the `StateStore` utility to <<spark-sql-streaming-StateStore.adoc#get-StateStore, look up a StateStore for the StateStoreProviderId>>.

In the end, `getStateStore` prints out the following INFO message to the logs:

```
Loaded store [storeId]
```

NOTE: `getStateStore` is used when <<spark-sql-streaming-KeyToNumValuesStore.adoc#stateStore, KeyToNumValuesStore>> and <<spark-sql-streaming-KeyWithIndexToValueStore.adoc#stateStore, KeyWithIndexToValueStore>> state store handlers are created (for <<spark-sql-streaming-SymmetricHashJoinStateManager.adoc#, SymmetricHashJoinStateManager>>).

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
