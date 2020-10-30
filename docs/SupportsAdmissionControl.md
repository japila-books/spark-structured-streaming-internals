# SupportsAdmissionControl

`SupportsAdmissionControl` is an [extension](#contract) of the [SparkDataStream](SparkDataStream.md) abstraction for [streaming sources](#implementations) that want to control the rate of data ingested in [Micro-Batch Stream Processing](micro-batch-stream-processing.md).

## Contract

### <span id="getDefaultReadLimit"> Default ReadLimit

```java
ReadLimit getDefaultReadLimit()
```

Default: `ReadLimit.allAvailable`

Used when `MicroBatchExecution` stream execution engine is requested for the [analyzed logical plan](MicroBatchExecution.md#logicalPlan) (of the streaming query)

### <span id="latestOffset"> Latest Offset

```java
Offset latestOffset(
  Offset startOffset,
  ReadLimit limit)
```

Used when `MicroBatchExecution` stream execution engine is requested for the [next micro-batch](MicroBatchExecution.md#constructNextBatch)

## Implementations

* [FileStreamSource](datasources/file/FileStreamSource.md)
* [KafkaMicroBatchStream](datasources/kafka/KafkaMicroBatchStream.md)
* [KafkaSource](datasources/kafka/KafkaSource.md)
