== [[StateManager]] StateManager Contract -- State Managers for Arbitrary Stateful Streaming Aggregation

`StateManager` is the <<contract, abstraction>> of <<implementations, state managers>> that act as _middlemen_ between <<spark-sql-streaming-StateStore.adoc#, state stores>> and the <<spark-sql-streaming-FlatMapGroupsWithStateExec.adoc#, FlatMapGroupsWithStateExec>> physical operator used in <<spark-sql-arbitrary-stateful-streaming-aggregation.adoc#, Arbitrary Stateful Streaming Aggregation>>.

[[contract]]
.StateManager Contract
[cols="30m,70",options="header",width="100%"]
|===
| Method
| Description

| getAllState
a| [[getAllState]]

[source, scala]
----
getAllState(store: StateStore): Iterator[StateData]
----

Retrieves all state data (for all keys) from the <<spark-sql-streaming-StateStore.adoc#, StateStore>>

Used exclusively when `InputProcessor` is requested to <<spark-sql-streaming-InputProcessor.adoc#processTimedOutState, processTimedOutState>>

| getState
a| [[getState]]

[source, scala]
----
getState(
  store: StateStore,
  keyRow: UnsafeRow): StateData
----

Gets the state data for the key from the <<spark-sql-streaming-StateStore.adoc#, StateStore>>

Used exclusively when `InputProcessor` is requested to <<spark-sql-streaming-InputProcessor.adoc#processNewData, processNewData>>

| putState
a| [[putState]]

[source, scala]
----
putState(
  store: StateStore,
  keyRow: UnsafeRow,
  state: Any,
  timeoutTimestamp: Long): Unit
----

Persists (_puts_) the state value for the key in the <<spark-sql-streaming-StateStore.adoc#, StateStore>>

Used exclusively when `InputProcessor` is requested to <<spark-sql-streaming-InputProcessor.adoc#callFunctionAndUpdateState, callFunctionAndUpdateState>> (<<spark-sql-streaming-InputProcessor.adoc#onIteratorCompletion, right after all rows have been processed>>)

| removeState
a| [[removeState]]

[source, scala]
----
removeState(
  store: StateStore,
  keyRow: UnsafeRow): Unit
----

Removes the state for the key from the <<spark-sql-streaming-StateStore.adoc#, StateStore>>

Used exclusively when `InputProcessor` is requested to <<spark-sql-streaming-InputProcessor.adoc#callFunctionAndUpdateState, callFunctionAndUpdateState>> (<<spark-sql-streaming-InputProcessor.adoc#onIteratorCompletion, right after all rows have been processed>>)

| stateSchema
a| [[stateSchema]]

[source, scala]
----
stateSchema: StructType
----

*State schema*

NOTE: <<spark-sql-streaming-StateStoreOps.adoc#mapPartitionsWithStateStore, It looks like>> (in <<spark-sql-streaming-FlatMapGroupsWithStateExec.adoc#stateManager, StateManager>> of the <<spark-sql-streaming-FlatMapGroupsWithStateExec.adoc#, FlatMapGroupsWithStateExec>> physical operator) `stateSchema` is used for the schema of state value objects (not state keys as they are described by the grouping attributes instead).

Used when:

* `FlatMapGroupsWithStateExec` physical operator is requested to <<spark-sql-streaming-FlatMapGroupsWithStateExec.adoc#doExecute, execute and generate a recipe for a distributed computation (as an RDD[InternalRow])>>

* `StateManagerImplBase` is requested for the <<spark-sql-streaming-StateManagerImplBase.adoc#stateDeserializerFunc, stateDeserializerFunc>>

|===

[[implementations]]
NOTE: <<spark-sql-streaming-StateManagerImplBase.adoc#, StateManagerImplBase>> is the one and only known direct implementation of the <<contract, StateManager Contract>> in Spark Structured Streaming.

NOTE: `StateManager` is a Scala *sealed trait* which means that all the <<implementations, implementations>> are in the same compilation unit (a single file).
