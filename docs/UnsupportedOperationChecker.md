# UnsupportedOperationChecker

`UnsupportedOperationChecker` checks whether the [logical plan of a streaming query uses supported operations only](#checkForStreaming).

`UnsupportedOperationChecker` is used only when [spark.sql.streaming.unsupportedOperationCheck](configuration-properties.md#spark.sql.streaming.unsupportedOperationCheck) configuration property is enabled.

## <span id="checkForStreaming"> Streaming Query Verification

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

### <span id="checkForStreamStreamJoinWatermark"> checkForStreamStreamJoinWatermark

```scala
checkForStreamStreamJoinWatermark(
  join: Join): Unit
```

`checkForStreamStreamJoinWatermark`...FIXME

## <span id="checkStreamingQueryGlobalWatermarkLimit"> checkStreamingQueryGlobalWatermarkLimit

```scala
checkStreamingQueryGlobalWatermarkLimit(
  plan: LogicalPlan,
  outputMode: OutputMode): Unit
```

`checkStreamingQueryGlobalWatermarkLimit` [finds stateful operators](#isStatefulOperation) in the given logical query plan with another [stateful operation that can possibly emit late rows](#isStatefulOperationPossiblyEmitLateRows) and throws an `AnalysisException`.

`checkStreamingQueryGlobalWatermarkLimit` propagates it (up the call chain) with [spark.sql.streaming.statefulOperator.checkCorrectness.enabled](configuration-properties.md#spark.sql.streaming.statefulOperator.checkCorrectness.enabled) enabled or prints out the following WARN message:

```text
Detected pattern of possible 'correctness' issue due to global watermark.
The query contains stateful operation which can emit rows older than the current watermark plus allowed late record delay,
which are "late rows" in downstream stateful operations and these rows can be discarded.
Please refer the programming guide doc for more details.
If you understand the possible risk of correctness issue and still need to run the query, you can disable this check by setting the config `spark.sql.streaming.statefulOperator.checkCorrectness.enabled` to false.
```

### <span id="isStatefulOperation"> isStatefulOperation

```scala
isStatefulOperation(
  p: LogicalPlan): Boolean
```

`isStatefulOperation` is positive (`true`) for the following logical operators:

* `Aggregate` ([Spark SQL]({{ book.spark_sql }}/logical-operators/Aggregate)) over a streaming data source
* `Join` ([Spark SQL]({{ book.spark_sql }}/logical-operators/Join)) with left and right streaming data sources
* [FlatMapGroupsWithState](logical-operators/FlatMapGroupsWithState.md) over a streaming data source
* [Deduplicate](logical-operators/Deduplicate.md) over a streaming data source

Otherwise, `isStatefulOperation` is negative (`false`).

### <span id="isStatefulOperationPossiblyEmitLateRows"> isStatefulOperationPossiblyEmitLateRows

```scala
isStatefulOperationPossiblyEmitLateRows(
  p: LogicalPlan): Boolean
```

`isStatefulOperationPossiblyEmitLateRows` is positive (`true`) for the following logical operators:

* `Aggregate` ([Spark SQL]({{ book.spark_sql }}/logical-operators/Aggregate)) over a streaming data source with [Append](OutputMode.md#Append) output mode
* `Join` ([Spark SQL]({{ book.spark_sql }}/logical-operators/Join)) with left and right streaming data sources for all but `Inner` join types
* [FlatMapGroupsWithState](logical-operators/FlatMapGroupsWithState.md) over a streaming data source with the [outputMode](logical-operators/FlatMapGroupsWithState.md#outputMode) as [Append](OutputMode.md#Append)

Otherwise, `isStatefulOperationPossiblyEmitLateRows` is negative (`false`).

### <span id="checkStreamingQueryGlobalWatermarkLimit-demo"> Demo

!!! tip
    Review `org.apache.spark.sql.catalyst.analysis.UnsupportedOperationsSuite.testGlobalWatermarkLimit`

    ```console
    SBT_MAVEN_PROFILES="-Pyarn,kubernetes,hive,hive-thriftserver,scala-2.13,hadoop-cloud" \
      sbt sql/testOnly org.apache.spark.sql.catalyst.analysis.UnsupportedOperationsSuite
    ```

```scala
import org.apache.spark.sql.catalyst.analysis.UnsupportedOperationChecker

// Streaming Aggregate with Append output mode over streaming join with non-Inner join type
val plan = ???

import org.apache.spark.sql.streaming.OutputMode
val outputMode = OutputMode.Append
UnsupportedOperationChecker.checkForStreaming(plan, outputMode)
```
