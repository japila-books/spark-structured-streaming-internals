# SupportsTriggerAvailableNow

`SupportsTriggerAvailableNow` is an [extension](#contract) of the [SupportsAdmissionControl](SupportsAdmissionControl.md) abstraction for [streaming sources](#implementations) that support [Trigger.AvailableNow](Trigger.md#AvailableNow) mode.

## Contract

### prepareForTriggerAvailableNow { #prepareForTriggerAvailableNow }

```java
void prepareForTriggerAvailableNow()
```

Lets a streaming source to prepare for the [default ReadLimit](SupportsAdmissionControl.md#getDefaultReadLimit) (in [Trigger.AvailableNow](Trigger.md#AvailableNow) mode)

See:

* [AvailableNowDataStreamWrapper](AvailableNowDataStreamWrapper.md#prepareForTriggerAvailableNow)
* [FileStreamSource](datasources/file/FileStreamSource.md#prepareForTriggerAvailableNow)
* [KafkaMicroBatchStream](kafka/KafkaMicroBatchStream.md#prepareForTriggerAvailableNow)
* [KafkaSource](kafka/KafkaSource.md#prepareForTriggerAvailableNow)
* [MemoryStreamBaseClass](datasources/memory/MemoryStreamBaseClass.md#prepareForTriggerAvailableNow)
* [RatePerMicroBatchStream](datasources/rate-micro-batch/RatePerMicroBatchStream.md#prepareForTriggerAvailableNow)

Used when:

* `MicroBatchExecution` is requested for the [logicalPlan](micro-batch-execution/MicroBatchExecution.md#logicalPlan) (and the [uniqueSources](micro-batch-execution/MicroBatchExecution.md#uniqueSources) for [MultiBatchExecutor](TriggerExecutor.md#MultiBatchExecutor))

## Implementations

* [AvailableNowDataStreamWrapper](AvailableNowDataStreamWrapper.md)
* [FileStreamSource](datasources/file/FileStreamSource.md)
* [KafkaMicroBatchStream](kafka/KafkaMicroBatchStream.md)
* [KafkaSource](kafka/KafkaSource.md)
* [MemoryStreamBaseClass](datasources/memory/MemoryStreamBaseClass.md)
* [RatePerMicroBatchStream](datasources/rate-micro-batch/RatePerMicroBatchStream.md)
