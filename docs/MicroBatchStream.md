# MicroBatchStream

`MicroBatchStream` is an [extension](#contract) of the [SparkDataStream](SparkDataStream.md) abstraction for [streaming sources](#implementations) for [Micro-Batch Stream Processing](micro-batch-stream-processing.md).

## Contract

### <span id="createReaderFactory"> Creating PartitionReaderFactory

```java
PartitionReaderFactory createReaderFactory()
```

Used when `MicroBatchScanExec` physical operator is requested for a [PartitionReaderFactory](physical-operators/MicroBatchScanExec.md#readerFactory)

### <span id="latestOffset"> Latest Offset

```java
Offset latestOffset()
```

Used when `MicroBatchExecution` is requested to [constructNextBatch](MicroBatchExecution.md#constructNextBatch)

### <span id="planInputPartitions"> Input Partitions

```java
InputPartition[] planInputPartitions(
  Offset start,
  Offset end)
```

Used when `MicroBatchScanExec` physical operator is requested for [partitions](physical-operators/MicroBatchScanExec.md#partitions)

## Implementations

* KafkaMicroBatchStream
* MemoryStream
* RateStreamMicroBatchStream
* TextSocketMicroBatchStream
