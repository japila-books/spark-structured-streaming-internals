# MicroBatchStream

`MicroBatchStream` is an [extension](#contract) of the [SparkDataStream](SparkDataStream.md) abstraction for [streaming sources](#implementations) for [Micro-Batch Stream Processing](micro-batch-execution/index.md).

## Contract

### <span id="createReaderFactory"> Creating PartitionReaderFactory

```java
PartitionReaderFactory createReaderFactory()
```

`PartitionReaderFactory` ([Spark SQL]({{ book.spark_sql }}/connector/PartitionReaderFactory))

Used when:

* `MicroBatchScanExec` physical operator is requested for a [PartitionReaderFactory](physical-operators/MicroBatchScanExec.md#readerFactory)

### <span id="latestOffset"> Latest Offset

```java
Offset latestOffset()
```

Latest [Offset](Offset.md)

Used when:

* `MicroBatchExecution` is requested to [constructing or skipping next streaming micro-batch](micro-batch-execution/MicroBatchExecution.md#constructNextBatch)

### <span id="planInputPartitions"> Input Partitions

```java
InputPartition[] planInputPartitions(
  Offset start,
  Offset end)
```

`InputPartition`s ([Spark SQL]({{ book.spark_sql }}/connector/InputPartition))

Used when:

* `MicroBatchScanExec` physical operator is requested for [input partitions](physical-operators/MicroBatchScanExec.md#partitions)

## Implementations

* `AvailableNowMicroBatchStreamWrapper`
* [KafkaMicroBatchStream](datasources/kafka/KafkaMicroBatchStream.md)
* [MemoryStream](datasources/memory/MemoryStream.md)
* `RatePerMicroBatchStream`
* `RateStreamMicroBatchStream`
* `TextSocketMicroBatchStream`
