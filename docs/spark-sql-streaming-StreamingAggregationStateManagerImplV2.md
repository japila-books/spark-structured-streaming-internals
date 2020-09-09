== [[StreamingAggregationStateManagerImplV2]] StreamingAggregationStateManagerImplV2 -- Default State Manager for Streaming Aggregation

`StreamingAggregationStateManagerImplV2` is the default <<spark-sql-streaming-StreamingAggregationStateManagerBaseImpl.adoc#, state manager for streaming aggregations>>.

NOTE: The version of a state manager is controlled using <<spark-sql-streaming-properties.adoc#spark.sql.streaming.aggregation.stateFormatVersion, spark.sql.streaming.aggregation.stateFormatVersion>> internal configuration property.

`StreamingAggregationStateManagerImplV2` is <<creating-instance, created>> exclusively when `StreamingAggregationStateManager` is requested for a <<spark-sql-streaming-StreamingAggregationStateManager.adoc#createStateManager, new StreamingAggregationStateManager>>.

[[creating-instance]]
`StreamingAggregationStateManagerImplV2` (like the parent <<spark-sql-streaming-StreamingAggregationStateManagerBaseImpl.adoc#creating-instance, StreamingAggregationStateManagerBaseImpl>>) takes the following to be created:

* [[keyExpressions]] Catalyst expressions for the keys (`Seq[Attribute]`)
* [[inputRowAttributes]] Catalyst expressions for the input rows (`Seq[Attribute]`)

=== [[put]] Storing Row in State Store -- `put` Method

[source, scala]
----
put(store: StateStore, row: UnsafeRow): Unit
----

NOTE: `put` is part of the <<spark-sql-streaming-StreamingAggregationStateManager.adoc#put, StreamingAggregationStateManager Contract>> to store a row in a state store.

`put`...FIXME

=== [[get]] Getting Saved State for Non-Null Key from State Store -- `get` Method

[source, scala]
----
get(store: StateStore, key: UnsafeRow): UnsafeRow
----

NOTE: `get` is part of the <<spark-sql-streaming-StreamingAggregationStateManager.adoc#get, StreamingAggregationStateManager Contract>> to get the saved state for a given non-null key from a given <<spark-sql-streaming-StateStore.adoc#, state store>>.

`get` requests the given <<spark-sql-streaming-StateStore.adoc#, StateStore>> for the current state value for the given key.

`get` returns `null` if the key could not be found in the state store. Otherwise, `get` <<restoreOriginalRow, restoreOriginalRow>> (for the key and the saved state).

=== [[restoreOriginalRow]] `restoreOriginalRow` Internal Method

[source, scala]
----
restoreOriginalRow(key: UnsafeRow, value: UnsafeRow): UnsafeRow
restoreOriginalRow(rowPair: UnsafeRowPair): UnsafeRow
----

`restoreOriginalRow`...FIXME

NOTE: `restoreOriginalRow` is used when `StreamingAggregationStateManagerImplV2` is requested to <<get, get the saved state for a given non-null key from a state store>>, <<iterator, iterator>> and <<values, values>>.

=== [[getStateValueSchema]] `getStateValueSchema` Method

[source, scala]
----
getStateValueSchema: StructType
----

NOTE: `getStateValueSchema` is part of the <<spark-sql-streaming-StreamingAggregationStateManager.adoc#getStateValueSchema, StreamingAggregationStateManager Contract>> to...FIXME.

`getStateValueSchema` simply requests the <<valueExpressions, valueExpressions>> for the schema.

=== [[iterator]] `iterator` Method

[source, scala]
----
iterator: iterator(store: StateStore): Iterator[UnsafeRowPair]
----

NOTE: `iterator` is part of the <<spark-sql-streaming-StreamingAggregationStateManager.adoc#iterator, StreamingAggregationStateManager Contract>> to...FIXME.

`iterator` simply requests the input <<spark-sql-streaming-StateStore.adoc#, state store>> for the <<spark-sql-streaming-StateStore.adoc#iterator, iterator>> that is mapped to an iterator of `UnsafeRowPairs` with the key (of the input `UnsafeRowPair`) and the value as a <<restoreOriginalRow, restored original row>>.

NOTE: https://www.scala-lang.org/api/current/scala/collection/Iterator.html[scala.collection.Iterator] is a data structure that allows to iterate over a sequence of elements that are usually fetched lazily (i.e. no elements are fetched from the underlying store until processed).

=== [[values]] `values` Method

[source, scala]
----
values(store: StateStore): Iterator[UnsafeRow]
----

NOTE: `values` is part of the <<spark-sql-streaming-StreamingAggregationStateManager.adoc#values, StreamingAggregationStateManager Contract>> to...FIXME.

`values`...FIXME

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| joiner
| [[joiner]]

| keyValueJoinedExpressions
a| [[keyValueJoinedExpressions]]

| needToProjectToRestoreValue
a| [[needToProjectToRestoreValue]]

| restoreValueProjector
a| [[restoreValueProjector]]

| valueExpressions
a| [[valueExpressions]]

| valueProjector
a| [[valueProjector]]
|===
