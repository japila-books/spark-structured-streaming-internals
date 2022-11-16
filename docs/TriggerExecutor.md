# TriggerExecutor

`TriggerExecutor` is an [abstraction](#contract) of [trigger executors](#implementations).

## Contract

### <span id="execute"><span id="batchRunner"> Executing Batches

```scala
execute(
  batchRunner: () => Boolean): Unit
```

Executes batches (_triggers_) using a batch runner (_trigger handler_).

`batchRunner` is assumed to return `false` to indicate execution termination

Used when:

* `MicroBatchExecution` is requested to [run an activated streaming query](micro-batch-execution/MicroBatchExecution.md#runActivatedStream)

## Implementations

* [MultiBatchExecutor](#MultiBatchExecutor)
* [ProcessingTimeExecutor](ProcessingTimeExecutor.md)
* [SingleBatchExecutor](#SingleBatchExecutor)

### <span id="MultiBatchExecutor"> MultiBatchExecutor

Executes the [batch runner](#batchRunner) until it returns `false`

Handles [AvailableNowTrigger](Trigger.md#AvailableNowTrigger) in [MicroBatchExecution](micro-batch-execution/MicroBatchExecution.md)

Used when:

* `MicroBatchExecution` is requested for the [analyzed logical plan](micro-batch-execution/MicroBatchExecution.md#logicalPlan) (and [extracting unique streaming sources](micro-batch-execution/MicroBatchExecution.md#uniqueSources))

### <span id="SingleBatchExecutor"> SingleBatchExecutor

Executes the [batch runner](#batchRunner) exactly once
