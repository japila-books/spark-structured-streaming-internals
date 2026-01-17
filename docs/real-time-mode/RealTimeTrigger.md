# RealTimeTrigger

`RealTimeTrigger` is a [Trigger](../Trigger.md) with a positive [batch duration](#batchDurationMs) in [MicroBatchExecution](../micro-batch-execution/MicroBatchExecution.md) stream execution engine.

`RealTimeTrigger` is executed using [ProcessingTimeExecutor](../ProcessingTimeExecutor.md) (when [MicroBatchExecution](../micro-batch-execution/MicroBatchExecution.md) is requested for a [TriggerExecutor](../micro-batch-execution/MicroBatchExecution.md#getTrigger)).

When requested to [create a streaming query](../StreamingQueryManager.md#createQuery), `StreamingQueryManager` asserts that the [batch duration](#batchDurationMs) is at least the value of [spark.sql.streaming.realTimeMode.minBatchDuration](../configuration-properties.md#STREAMING_REAL_TIME_MODE_MIN_BATCH_DURATION) configuration property.

When requested to [start a streaming query](../DataStreamWriter.md#startQuery), `DataStreamWriter` asserts that a sink is allowed.

## Creating Instance

`RealTimeTrigger` takes the following to be created:

* <span id="batchDurationMs"> Batch duration (in millis)

`RealTimeTrigger` is created using [apply](#apply) or [create](#create) utility methods.

## RealTimeTrigger.apply { #apply }

```scala
apply(): RealTimeTrigger
apply(
  batchDuration: String): RealTimeTrigger
apply(
  batchDuration: Duration): RealTimeTrigger
```

`apply` creates a [RealTimeTrigger](#creating-instance).

## RealTimeTrigger.create { #create }

```scala
create(
  batchDuration: String): RealTimeTrigger
create(
  batchDuration: Long,
  unit: TimeUnit): RealTimeTrigger
```

`create` creates a [RealTimeTrigger](#creating-instance).
