== [[UnsupportedOperationChecker]] UnsupportedOperationChecker

`UnsupportedOperationChecker` checks whether the <<checkForStreaming, logical plan of a streaming query uses supported operations only>>.

NOTE: `UnsupportedOperationChecker` is used exclusively when the internal link:spark-sql-streaming-properties.md#spark.sql.streaming.unsupportedOperationCheck[spark.sql.streaming.unsupportedOperationCheck] Spark property is enabled (which is by default).

[NOTE]
====
`UnsupportedOperationChecker` comes actually with two methods, i.e. `checkForBatch` and <<checkForStreaming, checkForStreaming>>, whose names reveal the different flavours of Spark SQL (as of 2.0), i.e. batch and streaming, respectively.

The Spark Structured Streaming gitbook is solely focused on <<checkForStreaming, checkForStreaming>> method.
====

=== [[checkForStreaming]] `checkForStreaming` Method

[source, scala]
----
checkForStreaming(
  plan: LogicalPlan,
  outputMode: OutputMode): Unit
----

`checkForStreaming` asserts that the following requirements hold:

1. <<only-one-streaming-aggregation-allowed, Only one streaming aggregation is allowed>>

1. <<streaming-aggregation-append-mode-requires-watermark, Streaming aggregation with Append output mode requires watermark>> (on the grouping expressions)

1. <<multiple-flatMapGroupsWithState, Multiple flatMapGroupsWithState operators are only allowed with Append output mode>>

`checkForStreaming`...FIXME

`checkForStreaming` finds all streaming aggregates (i.e. `Aggregate` logical operators with streaming sources).

NOTE: `Aggregate` logical operator represents <<spark-sql-streaming-Dataset-operators.md#groupBy, Dataset.groupBy>> and <<spark-sql-streaming-Dataset-operators.md#groupByKey, Dataset.groupByKey>> operators (and SQL's `GROUP BY` clause) in a logical query plan.

[[only-one-streaming-aggregation-allowed]]
`checkForStreaming` asserts that there is exactly one streaming aggregation in a streaming query.

Otherwise, `checkForStreaming` reports a `AnalysisException`:

[options="wrap"]
----
Multiple streaming aggregations are not supported with streaming DataFrames/Datasets
----

[[streaming-aggregation-append-mode-requires-watermark]]
`checkForStreaming` asserts that link:spark-sql-streaming-Dataset-withWatermark.md[watermark] was defined for a streaming aggregation with link:spark-sql-streaming-OutputMode.md#Append[Append] output mode (on at least one of the grouping expressions).

Otherwise, `checkForStreaming` reports a `AnalysisException`:

[options="wrap"]
----
Append output mode not supported when there are streaming aggregations on streaming DataFrames/DataSets without watermark
----

CAUTION: FIXME

`checkForStreaming` counts all link:spark-sql-streaming-FlatMapGroupsWithState.md[FlatMapGroupsWithState] logical operators (on streaming Datasets with `isMapGroupsWithState` flag disabled).

NOTE: <<spark-sql-streaming-FlatMapGroupsWithState.md#, FlatMapGroupsWithState>> logical operator represents <<spark-sql-streaming-KeyValueGroupedDataset.md#mapGroupsWithState, KeyValueGroupedDataset.mapGroupsWithState>> and <<spark-sql-streaming-KeyValueGroupedDataset.md#flatMapGroupsWithState, KeyValueGroupedDataset.flatMapGroupsWithState>> operators in a logical query plan.

NOTE: link:spark-sql-streaming-FlatMapGroupsWithState.md#isMapGroupsWithState[FlatMapGroupsWithState.isMapGroupsWithState] flag is disabled when...FIXME

[[multiple-flatMapGroupsWithState]]
`checkForStreaming` asserts that multiple link:spark-sql-streaming-FlatMapGroupsWithState.md[FlatMapGroupsWithState] logical operators are only used when:

* `outputMode` is link:spark-sql-streaming-OutputMode.md#Append[Append] output mode

* link:spark-sql-streaming-FlatMapGroupsWithState.md#outputMode[outputMode] of the `FlatMapGroupsWithState` logical operators is also link:spark-sql-streaming-OutputMode.md#Append[Append] output mode

CAUTION: FIXME Reference to an example in `flatMapGroupsWithState`

Otherwise, `checkForStreaming` reports a `AnalysisException`:

[options="wrap"]
----
Multiple flatMapGroupsWithStates are not supported when they are not all in append mode or the output mode is not append on a streaming DataFrames/Datasets
----

CAUTION: FIXME

NOTE: `checkForStreaming` is used exclusively when `StreamingQueryManager` is requested to link:spark-sql-streaming-StreamingQueryManager.md#createQuery[create a StreamingQueryWrapper] (for starting a streaming query), but only when the internal link:spark-sql-streaming-properties.md#spark.sql.streaming.unsupportedOperationCheck[spark.sql.streaming.unsupportedOperationCheck] Spark property is enabled (which is by default).
