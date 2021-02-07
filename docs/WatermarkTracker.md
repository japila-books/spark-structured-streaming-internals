# WatermarkTracker

`WatermarkTracker` tracks the [event-time watermark](#globalWatermarkMs) of a streaming query (across [EventTimeWatermarkExec operators](#operatorToWatermarkMap) in a physical query plan) based on a given [MultipleWatermarkPolicy](#policy).

`WatermarkTracker` is used in [MicroBatchExecution](micro-batch-execution/MicroBatchExecution.md#watermarkTracker).

## Creating Instance

`WatermarkTracker` takes the following to be created:

* [MultipleWatermarkPolicy](#policy)

`WatermarkTracker` is created (using [apply](#apply)) when `MicroBatchExecution` is requested to [populate start offsets](micro-batch-execution/MicroBatchExecution.md#populateStartOffsets) at start or restart (from a checkpoint).

## <span id="policy"><span id="MultipleWatermarkPolicy"><span id="MaxWatermark"><span id="MinWatermark"> MultipleWatermarkPolicy

`WatermarkTracker` is given a `MultipleWatermarkPolicy` when [created](#creating-instance) that can be one of the following:

* `MaxWatermark` (alias: `min`)
* `MinWatermark` (alias: `max`)

## <span id="apply"> Creating WatermarkTracker

```scala
apply(
  conf: RuntimeConfig): WatermarkTracker
```

`apply` uses the [spark.sql.streaming.multipleWatermarkPolicy](configuration-properties.md#spark.sql.streaming.multipleWatermarkPolicy) configuration property for the global watermark policy (default: `min`) and creates a `WatermarkTracker`.

`apply` is used when `MicroBatchExecution` is requested to [populate start offsets](micro-batch-execution/MicroBatchExecution.md#populateStartOffsets) at start or restart (from a checkpoint).

## <span id="globalWatermarkMs"><span id="currentWatermark"> Global Event-Time Watermark

```scala
globalWatermarkMs: Long
```

`WatermarkTracker` uses `globalWatermarkMs` internal registry to keep track of **global event-time watermark** (based on [MultipleWatermarkPolicy](#policy) across [all EventTimeWatermarkExec operators](#operatorToWatermarkMap) in a physical query plan).

Default: `0`

`globalWatermarkMs` is used when `WatermarkTracker` is requested to [updateWatermark](#updateWatermark).

The event-time watermark can be updated in [setWatermark](#setWatermark) and [updateWatermark](#updateWatermark).

The event-time watermark is used (as `currentWatermark` method) when `MicroBatchExecution` stream execution engine is requested to [populateStartOffsets](micro-batch-execution/MicroBatchExecution.md#populateStartOffsets) and [constructNextBatch](micro-batch-execution/MicroBatchExecution.md#constructNextBatch) and [runBatch](micro-batch-execution/MicroBatchExecution.md#runBatch).

### <span id="setWatermark"> Updating Watermark (at Startup and Restart)

```scala
setWatermark(
  newWatermarkMs: Long): Unit
```

`setWatermark` sets the [global event-time watermark](#globalwatermarkms) to the given `newWatermarkMs` value.

`setWatermark` is used when `MicroBatchExecution` is requested to [populate start offsets](micro-batch-execution/MicroBatchExecution.md#populateStartOffsets) at start or restart (from a checkpoint).

### <span id="updateWatermark"> Updating Watermark (at Execution)

```scala
updateWatermark(
  executedPlan: SparkPlan): Unit
```

`updateWatermark` requests the given `SparkPlan` physical operator to collect all [EventTimeWatermarkExec](physical-operators/EventTimeWatermarkExec.md) unary physical operators.

`updateWatermark` simply exits when no `EventTimeWatermarkExec` was found.

`updateWatermark`...FIXME

`updateWatermark` is used when `MicroBatchExecution` is requested to [run a single streaming batch](micro-batch-execution/MicroBatchExecution.md#runBatch) (when requested to [run an activated streaming query](micro-batch-execution/MicroBatchExecution.md#runActivatedStream)).

### <span id="operatorToWatermarkMap"> Watermarks by EventTimeWatermarkExec Operator Registry

```scala
operatorToWatermarkMap: Map[Int, Long]
```

`WatermarkTracker` uses `operatorToWatermarkMap` internal registry to keep track of event-time watermarks of every [EventTimeWatermarkExec](physical-operators/EventTimeWatermarkExec.md) physical operator in a streaming query plan.

`operatorToWatermarkMap` is used when `WatermarkTracker` is requested to [updateWatermark](#updateWatermark).

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.WatermarkTracker` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.execution.streaming.WatermarkTracker=ALL
```

Refer to [Logging](spark-logging.md).
