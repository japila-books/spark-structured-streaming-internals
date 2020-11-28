# StreamingAggregationStateManagerBaseImpl

`StreamingAggregationStateManagerBaseImpl` is the base implementation of the [StreamingAggregationStateManager](StreamingAggregationStateManager.md) contract for [state managers for streaming aggregations](#implementations).

[[keyProjector]]
`StreamingAggregationStateManagerBaseImpl` uses `UnsafeProjection` to <<getKey, getKey>>.

[[implementations]]
.StreamingAggregationStateManagerBaseImpls
[cols="1,2",options="header",width="100%"]
|===
| StreamingAggregationStateManagerBaseImpl
| Description

| [StreamingAggregationStateManagerImplV1](StreamingAggregationStateManagerImplV1.md)
| [[StreamingAggregationStateManagerImplV1]] Legacy [StreamingAggregationStateManager](StreamingAggregationStateManager.md) (used when [spark.sql.streaming.aggregation.stateFormatVersion](configuration-properties.md#spark.sql.streaming.aggregation.stateFormatVersion) configuration property is `1`)

| [StreamingAggregationStateManagerImplV2](StreamingAggregationStateManagerImplV2.md)
| [[StreamingAggregationStateManagerImplV2]] Default [StreamingAggregationStateManager](StreamingAggregationStateManager.md) (used when [spark.sql.streaming.aggregation.stateFormatVersion](configuration-properties.md#spark.sql.streaming.aggregation.stateFormatVersion) configuration property is `2`)
|===

[[creating-instance]]
`StreamingAggregationStateManagerBaseImpl` takes the following to be created:

* [[keyExpressions]] Catalyst expressions for the keys (`Seq[Attribute]`)
* [[inputRowAttributes]] Catalyst expressions for the input rows (`Seq[Attribute]`)

NOTE: `StreamingAggregationStateManagerBaseImpl` is a Scala abstract class and cannot be <<creating-instance, created>> directly. It is created indirectly for the <<implementations, concrete StreamingAggregationStateManagerBaseImpls>>.

## <span id="commit"> Committing (Changes to) State Store

```scala
commit(
  store: StateStore): Long
```

`commit` is part of the [StreamingAggregationStateManager](StreamingAggregationStateManager.md#commit) abstraction.

`commit` simply requests the [state store](spark-sql-streaming-StateStore.md) to [commit state changes](spark-sql-streaming-StateStore.md#commit).

## <span id="remove"> Removing Key From State Store

```scala
remove(
  store: StateStore,
  key: UnsafeRow): Unit
```

`remove` is part of the [StreamingAggregationStateManager](StreamingAggregationStateManager.md#remove) abstraction.

`remove`...FIXME

## <span id="getKey"> getKey

```scala
getKey(
  row: UnsafeRow): UnsafeRow
```

`getKey` is part of the [StreamingAggregationStateManager](StreamingAggregationStateManager.md#getKey) abstraction.

`getKey`...FIXME

## <span id="keys"> Getting All Keys in State Store

```scala
keys(
  store: StateStore): Iterator[UnsafeRow]
```

`keys` is part of the [StreamingAggregationStateManager](StreamingAggregationStateManager.md#keys) abstraction.

`keys`...FIXME
