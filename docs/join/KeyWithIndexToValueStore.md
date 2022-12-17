# KeyWithIndexToValueStore

`KeyWithIndexToValueStore` is a [StateStoreHandler](StateStoreHandler.md) (of [KeyWithIndexToValueType](StateStoreHandler.md#KeyWithIndexToValueType)) for [SymmetricHashJoinStateManager](SymmetricHashJoinStateManager.md#keyWithIndexToValue) to manage a <<stateStore, join state>>.

.KeyToNumValuesStore, KeyWithIndexToValueStore and Stream-Stream Join
image::images/KeyToNumValuesStore-KeyWithIndexToValueStore.png[align="center"]

[[stateStore]]
As a [StateStoreHandler](StateStoreHandler.md), `KeyWithIndexToValueStore` manages a [state store](../stateful-stream-processing/StateStore.md) (that is [loaded](StateStoreHandler.md#getStateStore)) for keys and values per the <<keyWithIndexSchema, keys with index>> and [input values](SymmetricHashJoinStateManager.md#inputValueAttributes) schemas, respectively.

[[keyWithIndexSchema]]
`KeyWithIndexToValueStore` uses a schema (for the <<stateStore, state store>>) that is the [key schema](SymmetricHashJoinStateManager.md#keySchema) (of the parent `SymmetricHashJoinStateManager`) with an extra field `index` of type `long`.

[[logging]]
[TIP]
====
Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.state.SymmetricHashJoinStateManager$KeyWithIndexToValueStore` to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.execution.streaming.state.SymmetricHashJoinStateManager$KeyWithIndexToValueStore=ALL
```

Refer to <<spark-sql-streaming-spark-logging.md#, Logging>>.
====

=== [[get]] Looking Up State Row For Given Key and Index -- `get` Method

[source, scala]
----
get(
  key: UnsafeRow,
  valueIndex: Long): UnsafeRow
----

`get` simply requests the internal [state store](#stateStore) to [look up](../stateful-stream-processing/StateStore.md#get) the value for the given <<keyWithIndexRow, key and valueIndex>>.

`get` is used when `SymmetricHashJoinStateManager` is requested to [removeByValueCondition](SymmetricHashJoinStateManager.md#removeByValueCondition).

=== [[getAll]] Retrieving (Given Number of) Values for Key -- `getAll` Method

[source, scala]
----
getAll(
  key: UnsafeRow,
  numValues: Long): Iterator[KeyWithIndexAndValue]
----

`getAll`...FIXME

`getAll` is used when `SymmetricHashJoinStateManager` is requested to [get values for a given key](SymmetricHashJoinStateManager.md#get) and [removeByKeyCondition](SymmetricHashJoinStateManager.md#removeByKeyCondition).

=== [[put]] Storing State Row For Given Key and Index -- `put` Method

[source, scala]
----
put(
  key: UnsafeRow,
  valueIndex: Long,
  value: UnsafeRow): Unit
----

`put`...FIXME

`put` is used when `SymmetricHashJoinStateManager` is requested to [append a new value to a given key](SymmetricHashJoinStateManager.md#append) and [removeByKeyCondition](SymmetricHashJoinStateManager.md#removeByKeyCondition).

=== [[remove]] `remove` Method

[source, scala]
----
remove(
  key: UnsafeRow,
  valueIndex: Long): Unit
----

`remove`...FIXME

`remove` is used when `SymmetricHashJoinStateManager` is requested to [removeByKeyCondition](SymmetricHashJoinStateManager.md#removeByKeyCondition) and [removeByValueCondition](SymmetricHashJoinStateManager.md#removeByValueCondition).

=== [[keyWithIndexRow]] `keyWithIndexRow` Internal Method

[source, scala]
----
keyWithIndexRow(
  key: UnsafeRow,
  valueIndex: Long): UnsafeRow
----

`keyWithIndexRow` uses the <<keyWithIndexRowGenerator, keyWithIndexRowGenerator>> to generate an `UnsafeRow` for the `key` and sets the `valueIndex` at the <<indexOrdinalInKeyWithIndexRow, indexOrdinalInKeyWithIndexRow>> position.

NOTE: `keyWithIndexRow` is used when `KeyWithIndexToValueStore` is requested to <<get, get>>, <<getAll, getAll>>, <<put, put>>, <<remove, remove>> and <<removeAllValues, removeAllValues>>.

=== [[removeAllValues]] `removeAllValues` Method

[source, scala]
----
removeAllValues(
  key: UnsafeRow,
  numValues: Long): Unit
----

`removeAllValues`...FIXME

NOTE: `removeAllValues` does not _seem_ to be used at all.

=== [[iterator]] `iterator` Method

[source, scala]
----
iterator: Iterator[KeyWithIndexAndValue]
----

`iterator`...FIXME

NOTE: `iterator` does not _seem_ to be used at all.

## Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| indexOrdinalInKeyWithIndexRow
a| [[indexOrdinalInKeyWithIndexRow]] Position of the index in the key row (which corresponds to the number of the [key attributes](SymmetricHashJoinStateManager.md#keyAttributes))

Used exclusively in the <<keyWithIndexRow, keyWithIndexRow>>

| keyWithIndexExprs
a| [[keyWithIndexExprs]] [keyAttributes](SymmetricHashJoinStateManager.md#keyAttributes) with `Literal(1L)` expression appended

Used exclusively for the <<keyWithIndexRowGenerator, keyWithIndexRowGenerator>> projection

| keyWithIndexRowGenerator
a| [[keyWithIndexRowGenerator]] `UnsafeProjection` for the <<keyWithIndexExprs, keyWithIndexExprs>> bound to the [keyAttributes](SymmetricHashJoinStateManager.md#keyAttributes)

Used exclusively in <<keyWithIndexRow, keyWithIndexRow>>

|===
