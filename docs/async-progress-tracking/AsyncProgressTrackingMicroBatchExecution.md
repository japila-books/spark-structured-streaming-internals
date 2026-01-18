# AsyncProgressTrackingMicroBatchExecution

`AsyncProgressTrackingMicroBatchExecution` is a custom [MicroBatchExecution](../micro-batch-execution/MicroBatchExecution.md) stream execution engine for [Micro-Batch Stream Processing](../micro-batch-execution/index.md) with [Async Progress Tracking](index.md) enabled.

## Creating Instance

`AsyncProgressTrackingMicroBatchExecution` takes the following to be created:

* <span id=""sparkSession"> [SparkSession]({{ book.spark_sql }}/SparkSession)
* <span id=""trigger"> [Trigger](../Trigger.md)
* <span id=""triggerClock"> `Clock`
* <span id=""extraOptions"> Extra Options (`Map[String, String]`)
* <span id=""plan"> [WriteToStream](../logical-operators/WriteToStream.md)

`AsyncProgressTrackingMicroBatchExecution` is created when:

* `StreamingQueryManager` is requested to [create a streaming query](../StreamingQueryManager.md#createQuery) with [asyncProgressTrackingEnabled](#asyncProgressTrackingEnabled) flag enabled

## <span id="ASYNC_PROGRESS_TRACKING_ENABLED"> asyncProgressTrackingEnabled { #asyncProgressTrackingEnabled }

When enabled, `asyncProgressTrackingEnabled` flag indicates that [StreamingQueryManager](../StreamingQueryManager.md) is supposed to use `AsyncProgressTrackingMicroBatchExecution` (not [MicroBatchExecution](../micro-batch-execution/MicroBatchExecution.md)) when requested to [create a streaming query](../StreamingQueryManager.md#createQuery).

Default: `false`
