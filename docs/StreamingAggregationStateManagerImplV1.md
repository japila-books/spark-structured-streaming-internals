# StreamingAggregationStateManagerImplV1

`StreamingAggregationStateManagerImplV1` is the legacy [state manager for streaming aggregations](StreamingAggregationStateManagerBaseImpl.md).

!!! note
    The version of a state manager is controlled using [spark.sql.streaming.aggregation.stateFormatVersion](spark-sql-streaming-properties.md#spark.sql.streaming.aggregation.stateFormatVersion) internal configuration property.

## <span id="put"> Storing Row in State Store

```scala
put(
  store: StateStore,
  row: UnsafeRow): Unit
```

`put` is part of the [StreamingAggregationStateManager](StreamingAggregationStateManager.md#put) abstraction.

`put`...FIXME

## Creating Instance

`StreamingAggregationStateManagerImplV1` takes the following when created:

* [[keyExpressions]] Attribute expressions for keys (`Seq[Attribute]`)
* [[inputRowAttributes]] Attribute expressions of input rows (`Seq[Attribute]`)

`StreamingAggregationStateManagerImplV1` is created when `StreamingAggregationStateManager` is requested for a [new StreamingAggregationStateManager](StreamingAggregationStateManager.md#createStateManager).
