# SupportsTriggerAvailableNow

`SupportsTriggerAvailableNow` is an [extension](#contract) of the [SupportsAdmissionControl](SupportsAdmissionControl.md) abstraction for [streaming sources](#implementations) to support [Trigger.AvailableNow](Trigger.md#AvailableNow) mode.

## Contract

### <span id="prepareForTriggerAvailableNow"> prepareForTriggerAvailableNow

```java
void prepareForTriggerAvailableNow()
```

Lets a streaming source to prepare for the [default ReadLimit](SupportsAdmissionControl.md#getDefaultReadLimit) (in [Trigger.AvailableNow](Trigger.md#AvailableNow) mode)

Used when:

* `MicroBatchExecution` is requested for the [logicalPlan](micro-batch-execution/MicroBatchExecution.md#logicalPlan) (and the [uniqueSources](micro-batch-execution/MicroBatchExecution.md#uniqueSources) for [MultiBatchExecutor](TriggerExecutor.md#MultiBatchExecutor))

## Implementations

* [AvailableNowDataStreamWrapper](AvailableNowDataStreamWrapper.md)
* [FileStreamSource](datasources/file/FileStreamSource.md)
* [KafkaMicroBatchStream](kafka/KafkaMicroBatchStream.md)
* [KafkaSource](kafka/KafkaSource.md)
* [RatePerMicroBatchStream](datasources/rate-micro-batch/RatePerMicroBatchStream.md)
