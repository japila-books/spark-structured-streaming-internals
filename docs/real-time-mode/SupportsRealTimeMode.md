---
title: SupportsRealTimeMode
---

# SupportsRealTimeMode Streaming Sources

`SupportsRealTimeMode` is an [abstraction](#contract) of [streaming sources](#implementations) that can be used in [Real-Time Mode](index.md).

## Contract

### mergeOffsets { #mergeOffsets }

```java
Offset mergeOffsets(
  PartitionOffset[] offsets)
```

See:

* [KafkaMicroBatchStream](../kafka/KafkaMicroBatchStream.md#mergeOffsets)
* [LowLatencyMemoryStream](../datasources/memory/LowLatencyMemoryStream.md#mergeOffsets)

Used when:

* `MicroBatchExecution` stream execution engine is requested to [markMicroBatchEnd](../micro-batch-execution/MicroBatchExecution.md#markMicroBatchEnd)

### planInputPartitions { #planInputPartitions }

```java
InputPartition[] planInputPartitions(
  Offset start)
```

[InputPartition]({{ book.spark_sql }}/connector/InputPartition)s for the start offset

See:

* [KafkaMicroBatchStream](../kafka/KafkaMicroBatchStream.md#planInputPartitions)
* [LowLatencyMemoryStream](../datasources/memory/LowLatencyMemoryStream.md#planInputPartitions)

Used when:

* `RealTimeStreamScanExec` physical operator is requested for the [inputPartitions](../physical-operators/RealTimeStreamScanExec.md#inputPartitions)

### prepareForRealTimeMode { #prepareForRealTimeMode }

```java
void prepareForRealTimeMode()
```

Informs this source that it is executed in real-time mode (with [RealTimeTrigger](RealTimeTrigger.md))

See:

* [KafkaMicroBatchStream](../kafka/KafkaMicroBatchStream.md#prepareForRealTimeMode)

Used when:

* `MicroBatchExecution` stream execution engine is requested for the [LogicalPlan](../micro-batch-execution/MicroBatchExecution.md#logicalPlan)

## Implementations

* [KafkaMicroBatchStream](../kafka/KafkaMicroBatchStream.md)
* [LowLatencyMemoryStream](../datasources/memory/LowLatencyMemoryStream.md)
