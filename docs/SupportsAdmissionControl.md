# SupportsAdmissionControl

`SupportsAdmissionControl` is an [extension](#contract) of the [SparkDataStream](SparkDataStream.md) abstraction for [streaming sources](#implementations) that want to control the rate of data ingested in [Micro-Batch Stream Processing](micro-batch-execution/index.md).

## Contract

### <span id="getDefaultReadLimit"> Default ReadLimit

```java
ReadLimit getDefaultReadLimit()
```

Default: `ReadLimit.allAvailable`

Used when:

* `AvailableNowDataStreamWrapper` is requested to `getDefaultReadLimit`
* `MicroBatchExecution` stream execution engine is requested for the [analyzed logical plan](micro-batch-execution/MicroBatchExecution.md#logicalPlan) (and initializes the [uniqueSources](micro-batch-execution/MicroBatchExecution.md#uniqueSources))

### <span id="latestOffset"> Latest Offset

```java
Offset latestOffset(
  Offset startOffset,
  ReadLimit limit)
```

Used when `MicroBatchExecution` stream execution engine is requested for the [next micro-batch](micro-batch-execution/MicroBatchExecution.md#constructNextBatch)

## Implementations

* [SupportsTriggerAvailableNow](SupportsTriggerAvailableNow.md)
