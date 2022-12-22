# UnsupportedOperationChecker

`UnsupportedOperationChecker` checks whether the [logical plan of a streaming query uses supported operations only](#checkForStreaming).

`UnsupportedOperationChecker` is used when the internal [spark.sql.streaming.unsupportedOperationCheck](configuration-properties.md#spark.sql.streaming.unsupportedOperationCheck) Spark property is enabled.

## <span id="checkForStreaming"> checkForStreaming Method

```scala
checkForStreaming(
  plan: LogicalPlan,
  outputMode: OutputMode): Unit
```

`checkForStreaming` asserts that the following requirements hold:

1. [Only one streaming aggregation is allowed](#only-one-streaming-aggregation-allowed)

1. [Streaming aggregation with Append output mode requires watermark](#streaming-aggregation-append-mode-requires-watermark) (on the grouping expressions)

1. [Multiple flatMapGroupsWithState operators are only allowed with Append output mode](#multiple-flatMapGroupsWithState)

---

`checkForStreaming` is used when:

* `StreamingQueryManager` is requested to [create a StreamingQueryWrapper](StreamingQueryManager.md#createQuery) (for starting a streaming query), but only when the internal [spark.sql.streaming.unsupportedOperationCheck](configuration-properties.md#spark.sql.streaming.unsupportedOperationCheck) configuration property is enabled.

### Only One Streaming Aggregation Is Allowed

`checkForStreaming` finds all streaming aggregates (i.e., `Aggregate` logical operators with streaming sources).

`checkForStreaming` asserts that there is exactly one streaming aggregation in a streaming query.

Otherwise, `checkForStreaming` reports a `AnalysisException`:

```text
Multiple streaming aggregations are not supported with streaming DataFrames/Datasets
```

### <span id="streaming-aggregation-append-mode-requires-watermark"> Streaming Aggregation With Append Output Mode Requires Watermark

`checkForStreaming` asserts that [watermark](operators/withWatermark.md) was defined for a streaming aggregation with [Append](OutputMode.md#Append) output mode (on at least one of the grouping expressions).

Otherwise, `checkForStreaming` reports a `AnalysisException`:

```text
Append output mode not supported when there are streaming aggregations on streaming DataFrames/DataSets without watermark
```

CAUTION: FIXME

### <span id="multiple-flatMapGroupsWithState"> Multiple flatMapGroupsWithState Operators Are Only Allowed With Append Output Mode

`checkForStreaming` counts all [FlatMapGroupsWithState](logical-operators/FlatMapGroupsWithState.md) logical operators (on streaming Datasets with `isMapGroupsWithState` flag disabled).

`checkForStreaming` asserts that multiple [FlatMapGroupsWithState](logical-operators/FlatMapGroupsWithState.md) logical operators are only used when:

* `outputMode` is [Append](OutputMode.md#Append) output mode
* [outputMode](logical-operators/FlatMapGroupsWithState.md#outputMode) of the `FlatMapGroupsWithState` logical operators is also [Append](OutputMode.md#Append) output mode

Otherwise, `checkForStreaming` reports a `AnalysisException`:

```text
Multiple flatMapGroupsWithStates are not supported when they are not all in append mode or the output mode is not append on a streaming DataFrames/Datasets
```

## <span id="checkForStreamStreamJoinWatermark"> checkForStreamStreamJoinWatermark

```scala
checkForStreamStreamJoinWatermark(
  join: Join): Unit
```

`checkForStreamStreamJoinWatermark`...FIXME
