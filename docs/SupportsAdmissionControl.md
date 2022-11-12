# SupportsAdmissionControl

`SupportsAdmissionControl` is an [extension](#contract) of the [SparkDataStream](SparkDataStream.md) abstraction for [data streams](#implementations) to control the rate of data ingested (_read_) in [Micro-Batch Stream Processing](micro-batch-execution/index.md).

## Contract

### <span id="getDefaultReadLimit"> Default ReadLimit

```java
ReadLimit getDefaultReadLimit()
```

Default [ReadLimit](ReadLimit.md) of this [SparkDataStream](SparkDataStream.md)

`getDefaultReadLimit` is [ReadAllAvailable](ReadLimit.md#allAvailable) by default (and is expected to be overriden by the [implementations](#implementations) if needed)

See [FileStreamSource](datasources/file/FileStreamSource.md#getDefaultReadLimit), [KafkaMicroBatchStream](kafka/KafkaMicroBatchStream.md#getDefaultReadLimit), [KafkaSource](kafka/KafkaSource.md#getDefaultReadLimit), [RatePerMicroBatchStream](datasources/rate-micro-batch/RatePerMicroBatchStream.md#getDefaultReadLimit)

Used when:

* `MicroBatchExecution` stream execution engine is requested for the [analyzed logical plan](micro-batch-execution/MicroBatchExecution.md#logicalPlan) (and initializes [uniqueSources](StreamExecution.md#uniqueSources) registry, i.e. [SparkDataStream](SparkDataStream.md)s with their `ReadLimit`s)

### <span id="latestOffset"> Latest Offset per ReadLimit

```java
Offset latestOffset(
  Offset startOffset,
  ReadLimit limit)
```

The most recent [Offset](Offset.md) available given a [ReadLimit](ReadLimit.md).
`null` to "announce" no data to process.

!!! note "MicroBatchExecution and latestOffset Phase"
    `MicroBatchExecution` stream execution engine uses [latestOffset](micro-batch-execution/MicroBatchExecution.md#latestOffset) execution phase to track the duration to request a [SparkDataStream](SparkDataStream.md) (indirectly via [AvailableNowDataStreamWrapper](AvailableNowDataStreamWrapper.md)) or [SupportsAdmissionControl](SupportsAdmissionControl.md) for [latestOffset](#latestOffset) and [reportLatestOffset](#reportLatestOffset)

Used when:

* `AvailableNowDataStreamWrapper` is requested to `prepareForTriggerAvailableNow`
* `MicroBatchExecution` stream execution engine is requested to [construct the next micro-batch](micro-batch-execution/MicroBatchExecution.md#constructNextBatch)

### <span id="reportLatestOffset"> Latest Offset Available

```java
Offset reportLatestOffset()
```

The most recent [Offset](Offset.md) available (regardless of [ReadLimit](ReadLimit.md))

Default: `null` (no offset to process)

!!! note "MicroBatchExecution and latestOffset Phase"
    `MicroBatchExecution` stream execution engine uses [latestOffset](micro-batch-execution/MicroBatchExecution.md#latestOffset) execution phase to track the duration to request a [SparkDataStream](SparkDataStream.md) (indirectly via `AvailableNowDataStreamWrapper`) or [SupportsAdmissionControl](SupportsAdmissionControl.md) for [latestOffset](#latestOffset) and [reportLatestOffset](#reportLatestOffset)

Used when:

* `AvailableNowDataStreamWrapper` is requested to [reportLatestOffset](AvailableNowDataStreamWrapper.md#reportLatestOffset)
* `MicroBatchExecution` stream execution engine is requested to [construct the next micro-batch](micro-batch-execution/MicroBatchExecution.md#constructNextBatch) (for [AvailableNowDataStreamWrapper](AvailableNowDataStreamWrapper.md) and `SupportsAdmissionControl` data streams)

## Implementations

* [SupportsTriggerAvailableNow](SupportsTriggerAvailableNow.md)
* [FileStreamSource](datasources/file/FileStreamSource.md)
