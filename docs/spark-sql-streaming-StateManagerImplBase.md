== [[StateManagerImplBase]] StateManagerImplBase

`StateManagerImplBase` is the <<contract, extension>> of the <<spark-sql-streaming-StateManager.md#, StateManager contract>> for <<implementations, state managers>> of [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator with the following features:

* Use Catalyst expressions for <<stateSerializerExprs, state serialization>> and <<stateDeserializerExpr, deserialization>>

* Use <<timeoutTimestampOrdinalInRow, timeoutTimestampOrdinalInRow>> when <<shouldStoreTimestamp, shouldStoreTimestamp>> with the <<shouldStoreTimestamp, shouldStoreTimestamp>> flag on

[[contract]]
.StateManagerImplBase Contract (Abstract Methods Only)
[cols="30m,70",options="header",width="100%"]
|===
| Method
| Description

| stateDeserializerExpr
a| [[stateDeserializerExpr]]

[source, scala]
----
stateDeserializerExpr: Expression
----

*State deserializer*, i.e. a Catalyst expression to deserialize a state object from a row (`UnsafeRow`)

Used exclusively for the <<stateDeserializerFunc, stateDeserializerFunc>>

| stateSerializerExprs
a| [[stateSerializerExprs]]

[source, scala]
----
stateSerializerExprs: Seq[Expression]
----

*State serializer*, i.e. Catalyst expressions to serialize a state object to a row (`UnsafeRow`)

Used exclusively for the <<stateSerializerFunc, stateSerializerFunc>>

| timeoutTimestampOrdinalInRow
a| [[timeoutTimestampOrdinalInRow]]

[source, scala]
----
timeoutTimestampOrdinalInRow: Int
----

Position of the timeout timestamp in a state row

Used when `StateManagerImplBase` is requested to <<getTimestamp, get>> and <<setTimestamp, set timeout timestamp>>

|===

[[implementations]]
.StateManagerImplBases
[cols="30,70",options="header",width="100%"]
|===
| StateManagerImplBase
| Description

| <<spark-sql-streaming-StateManagerImplV1.md#, StateManagerImplV1>>
| [[StateManagerImplV1]] Legacy <<spark-sql-streaming-StateManager.md#, StateManager>>

| <<spark-sql-streaming-StateManagerImplV2.md#, StateManagerImplV2>>
| [[StateManagerImplV2]] Default <<spark-sql-streaming-StateManager.md#, StateManager>>

|===

=== [[creating-instance]][[shouldStoreTimestamp]] Creating StateManagerImplBase Instance

`StateManagerImplBase` takes a single `shouldStoreTimestamp` flag to be created (that is set when the <<implementations, concrete StateManagerImplBases>> are created).

NOTE: `StateManagerImplBase` is a Scala abstract class and cannot be <<creating-instance, created>> directly. It is created indirectly for the <<implementations, concrete StateManagerImplBases>>.

`StateManagerImplBase` initializes the <<internal-properties, internal properties>>.

=== [[getState]] Getting State Data for Key from StateStore -- `getState` Method

[source, scala]
----
getState(
  store: StateStore,
  keyRow: UnsafeRow): StateData
----

`getState` is part of the [StateManager](spark-sql-streaming-StateManager.md#getState) abstraction.

`getState`...FIXME

=== [[putState]] Persisting State Value for Key in StateStore -- `putState` Method

[source, scala]
----
putState(
  store: StateStore,
  key: UnsafeRow,
  state: Any,
  timestamp: Long): Unit
----

`putState` is part of the [StateManager](spark-sql-streaming-StateManager.md#putState) abstraction.

`putState`...FIXME

=== [[removeState]] Removing State for Key from StateStore -- `removeState` Method

[source, scala]
----
removeState(
  store: StateStore,
  keyRow: UnsafeRow): Unit
----

`removeState` is part of the [StateManager](spark-sql-streaming-StateManager.md#removeState) abstraction.

`removeState`...FIXME

=== [[getAllState]] Getting All State Data (for All Keys) from StateStore -- `getAllState` Method

[source, scala]
----
getAllState(store: StateStore): Iterator[StateData]
----

`getAllState` is part of the [StateManager](spark-sql-streaming-StateManager.md#getAllState) abstraction.

`getAllState`...FIXME

=== [[getStateObject]] `getStateObject` Internal Method

[source, scala]
----
getStateObject(row: UnsafeRow): Any
----

`getStateObject`...FIXME

`getStateObject` is used when...FIXME

=== [[getStateRow]] `getStateRow` Internal Method

[source, scala]
----
getStateRow(obj: Any): UnsafeRow
----

`getStateRow`...FIXME

`getStateRow` is used when...FIXME

=== [[getTimestamp]] Getting Timeout Timestamp (from State Row) -- `getTimestamp` Internal Method

[source, scala]
----
getTimestamp(stateRow: UnsafeRow): Long
----

`getTimestamp`...FIXME

`getTimestamp` is used when...FIXME

=== [[setTimestamp]] Setting Timeout Timestamp (to State Row) -- `setTimestamp` Internal Method

[source, scala]
----
setTimestamp(
  stateRow: UnsafeRow,
  timeoutTimestamps: Long): Unit
----

`setTimestamp`...FIXME

`setTimestamp` is used when...FIXME

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| stateSerializerFunc
a| [[stateSerializerFunc]] *State object serializer* (of type `Any => UnsafeRow`) to serialize a state object (for a per-group state key) to a row (`UnsafeRow`)

* The serialization expression (incl. the type) is specified as the <<stateSerializerExprs, stateSerializerExprs>>

Used exclusively in <<getStateRow, getStateRow>>

| stateDeserializerFunc
a| [[stateDeserializerFunc]] *State object deserializer* (of type `InternalRow => Any`) to deserialize a row (for a per-group state value) to a Scala value

* The deserialization expression (incl. the type) is specified as the <<stateDeserializerExpr, stateDeserializerExpr>>

Used exclusively in <<getStateObject, getStateObject>>

| stateDataForGets
a| [[stateDataForGets]] Empty `StateData` to share (_reuse_) between <<getState, getState>> calls (to avoid high use of memory with many `StateData` objects)

|===
