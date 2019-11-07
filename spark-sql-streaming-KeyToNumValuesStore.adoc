== [[KeyToNumValuesStore]] KeyToNumValuesStore -- State Store (Handler) Of Join Keys And Counts

`KeyToNumValuesStore` is a <<spark-sql-streaming-StateStoreHandler.adoc#, StateStoreHandler>> (of <<spark-sql-streaming-StateStoreHandler.adoc#KeyToNumValuesType, KeyToNumValuesType>>) for <<spark-sql-streaming-SymmetricHashJoinStateManager.adoc#keyToNumValues, SymmetricHashJoinStateManager>> to manage a <<stateStore, join state>>.

.KeyToNumValuesStore, KeyWithIndexToValueStore and Stream-Stream Join
image::images/KeyToNumValuesStore-KeyWithIndexToValueStore.png[align="center"]

[[stateStore]]
As a <<spark-sql-streaming-StateStoreHandler.adoc#, StateStoreHandler>>, `KeyToNumValuesStore` manages a <<spark-sql-streaming-StateStore.adoc#, state store>> (that is <<spark-sql-streaming-StateStoreHandler.adoc#getStateStore, loaded>>) with the join keys (per <<spark-sql-streaming-SymmetricHashJoinStateManager.adoc#keySchema, key schema>>) and their count (per <<longValueSchema, value schema>>).

[[longValueSchema]]
`KeyToNumValuesStore` uses the schema for values in the <<stateStore, state store>> with one field `value` (of type `long`) that is the number of value rows (count).

[[logging]]
[TIP]
====
Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.state.SymmetricHashJoinStateManager$KeyToNumValuesStore` to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.execution.streaming.state.SymmetricHashJoinStateManager$KeyToNumValuesStore=ALL
```

Refer to <<spark-sql-streaming-logging.adoc#, Logging>>.
====

=== [[get]] Looking Up Number Of Value Rows For Given Key (Value Count) -- `get` Method

[source, scala]
----
get(key: UnsafeRow): Long
----

`get` requests the <<stateStore, StateStore>> for the <<spark-sql-streaming-StateStore.adoc#get, value for the given key>> and returns the long value at ``0``th position (of the row found) or `0`.

NOTE: `get` is used when `SymmetricHashJoinStateManager` is requested for the <<spark-sql-streaming-SymmetricHashJoinStateManager.adoc#get, values for a given key>> and <<spark-sql-streaming-SymmetricHashJoinStateManager.adoc#append, append a new value to a given key>>.

=== [[put]] Storing Key Count For Given Key -- `put` Method

[source, scala]
----
put(
  key: UnsafeRow,
  numValues: Long): Unit
----

`put` stores the `numValues` at the ``0``th position (of the internal unsafe row) and requests the <<stateStore, StateStore>> to <<spark-sql-streaming-StateStore.adoc#put, store it with the given key>>.

`put` requires that the `numValues` count is greater than `0` (or throws an `IllegalArgumentException`).

NOTE: `put` is used when `SymmetricHashJoinStateManager` is requested for the <<spark-sql-streaming-SymmetricHashJoinStateManager.adoc#append, append a new value to a given key>> and <<spark-sql-streaming-SymmetricHashJoinStateManager.adoc#updateNumValueForCurrentKey, updateNumValueForCurrentKey>>.

=== [[iterator]] All State Keys and Values -- `iterator` Method

[source, scala]
----
iterator: Iterator[KeyAndNumValues]
----

`iterator` simply requests the <<stateStore, StateStore>> for <<spark-sql-streaming-StateStore.adoc#getRange, all state keys and values>>.

NOTE: `iterator` is used when `SymmetricHashJoinStateManager` is requested to <<spark-sql-streaming-SymmetricHashJoinStateManager.adoc#removeByKeyCondition, removeByKeyCondition>> and <<spark-sql-streaming-SymmetricHashJoinStateManager.adoc#removeByValueCondition, removeByValueCondition>>.

=== [[remove]] Removing State Key -- `remove` Method

[source, scala]
----
remove(key: UnsafeRow): Unit
----

`remove` simply requests the <<stateStore, StateStore>> to <<spark-sql-streaming-StateStore.adoc#remove, remove the given key>>.

NOTE: `remove` is used when...FIXME
