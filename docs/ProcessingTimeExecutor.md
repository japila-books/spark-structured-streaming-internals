# ProcessingTimeExecutor

`ProcessingTimeExecutor` is a [TriggerExecutor](TriggerExecutor.md) that [executes a trigger handler](#execute) every [ProcessingTimeTrigger](#processingTimeTrigger) (until the handler returns `false`).

`ProcessingTimeExecutor` is created for the following [Trigger](Trigger.md)s:

* [Trigger.ProcessingTime](Trigger.md#ProcessingTimeTrigger) in [Micro-Batch Stream Processing](micro-batch-execution/index.md)
* [Trigger.Continuous](Trigger.md#ContinuousTrigger) in [Continuous Stream Processing](continuous-execution/index.md)

!!! note "Continuous Stream Processing"
    `ProcessingTimeExecutor` is the only [TriggerExecutor](TriggerExecutor.md) supported in [Continuous Stream Processing](continuous-execution/index.md).

## Creating Instance

`ProcessingTimeExecutor` takes the following to be created:

* <span id="processingTimeTrigger"> [ProcessingTimeTrigger](Trigger.md#ProcessingTimeTrigger)
* <span id="clock"> `Clock`

`ProcessingTimeExecutor` is created when:

* `MicroBatchExecution` is [created](micro-batch-execution/MicroBatchExecution.md#triggerExecutor) (with a [ProcessingTimeTrigger](Trigger.md#ProcessingTimeTrigger))
* `ContinuousExecution` is [created](continuous-execution/ContinuousExecution.md#triggerExecutor) (with a [ContinuousTrigger](Trigger.md#ContinuousTrigger))

## <span id="execute"> Executing Trigger

```scala
execute(
  triggerHandler: () => Boolean): Unit
```

`execute` is part of the [TriggerExecutor](TriggerExecutor.md#execute) abstraction.

---

`execute` [calculates the next batch's start time](#nextBatchTime).

`execute` executes the given `triggerHandler` every [processingTimeTrigger](#processingTimeTrigger) until the given `triggerHandler` returns `false` (to signal execution termination).

### <span id="nextBatchTime"> Next Batch's Start Time

```scala
nextBatchTime(
  now: Long): Long
```

`nextBatchTime` returns the start time (in millis) of the next batch interval given the current `now` time.
