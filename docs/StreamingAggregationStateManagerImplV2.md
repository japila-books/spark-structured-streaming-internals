# StreamingAggregationStateManagerImplV2

`StreamingAggregationStateManagerImplV2` is the default [state manager for streaming aggregations](StreamingAggregationStateManagerBaseImpl.md).

!!! note
    The version of a state manager is controlled using [spark.sql.streaming.aggregation.stateFormatVersion](configuration-properties.md#spark.sql.streaming.aggregation.stateFormatVersion) internal configuration property.

## Creating Instance

`StreamingAggregationStateManagerImplV2` (like the parent [StreamingAggregationStateManagerBaseImpl](StreamingAggregationStateManagerBaseImpl.md#creating-instance)) takes the following to be created:

* [[keyExpressions]] Catalyst expressions for the keys (`Seq[Attribute]`)
* [[inputRowAttributes]] Catalyst expressions for the input rows (`Seq[Attribute]`)

`StreamingAggregationStateManagerImplV2` is created when `StreamingAggregationStateManager` is requested for a [new StreamingAggregationStateManager](StreamingAggregationStateManager.md#createStateManager).

## <span id="put"> Storing Row in State Store

```scala
put(
  store: StateStore,
  row: UnsafeRow): Unit
```

`put` is part of the [StreamingAggregationStateManager](StreamingAggregationStateManager.md#put) abstraction.

`put`...FIXME

## <span id="get"> Getting Saved State for Non-Null Key from State Store

```scala
get(
  store: StateStore,
  key: UnsafeRow): UnsafeRow
```

`get` is part of the [StreamingAggregationStateManager](StreamingAggregationStateManager.md#get) abstraction.

`get` requests the given [StateStore](StateStore.md) for the current state value for the given key.

`get` returns `null` if the key could not be found in the state store. Otherwise, `get` [restoreOriginalRow](#restoreOriginalRow) (for the key and the saved state).

=== [[restoreOriginalRow]] `restoreOriginalRow` Internal Method

[source, scala]
----
restoreOriginalRow(key: UnsafeRow, value: UnsafeRow): UnsafeRow
restoreOriginalRow(rowPair: UnsafeRowPair): UnsafeRow
----

`restoreOriginalRow`...FIXME

NOTE: `restoreOriginalRow` is used when `StreamingAggregationStateManagerImplV2` is requested to <<get, get the saved state for a given non-null key from a state store>>, <<iterator, iterator>> and <<values, values>>.

## <span id="getStateValueSchema"> getStateValueSchema

```scala
getStateValueSchema: StructType
```

`getStateValueSchema` is part of the [StreamingAggregationStateManager](StreamingAggregationStateManager.md#getStateValueSchema) abstraction.

`getStateValueSchema` simply requests the [valueExpressions](#valueExpressions) for the schema.

## <span id="iterator"> iterator

```scala
iterator(
  store: StateStore): Iterator[UnsafeRowPair]
```

`iterator` is part of the [StreamingAggregationStateManager](StreamingAggregationStateManager.md#iterator) abstraction.

`iterator` simply requests the input [state store](StateStore.md) for the [iterator](StateStore.md#iterator) that is mapped to an iterator of `UnsafeRowPairs` with the key (of the input `UnsafeRowPair`) and the value as a [restored original row](#restoreOriginalRow).

!!! note
    [scala.collection.Iterator]({{ scala.api }}/scala/collection/Iterator.html) is a data structure that allows to iterate over a sequence of elements that are usually fetched lazily (i.e. no elements are fetched from the underlying store until processed).

## <span id="values"> values

```scala
values(
  store: StateStore): Iterator[UnsafeRow]
```

`values` is part of the [StreamingAggregationStateManager](StreamingAggregationStateManager.md#values) abstraction.

`values`...FIXME

## Internal Properties

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
