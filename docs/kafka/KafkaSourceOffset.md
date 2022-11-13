# KafkaSourceOffset

`KafkaSourceOffset` is an [Offset](../Offset.md) in [Kafka Data Source](index.md).

## Creating Instance

`KafkaSourceOffset` takes the following to be created:

* <span id="partitionToOffsets"> Offsets by Partitions (`Map[TopicPartition, Long]`)

`KafkaSourceOffset` is created when:

* `KafkaContinuousStream` is requested for the [initial offset](KafkaContinuousStream.md#initialOffset), [deserializeOffset](KafkaContinuousStream.md#deserializeOffset), [mergeOffsets](KafkaContinuousStream.md#mergeOffsets)
* `KafkaMicroBatchStream` is requested for the [initial offset](KafkaMicroBatchStream.md#initialOffset), [reportLatestOffset](KafkaMicroBatchStream.md#reportLatestOffset), [latestOffset](KafkaMicroBatchStream.md#latestOffset), [deserializeOffset](KafkaMicroBatchStream.md#deserializeOffset), [getOrCreateInitialPartitionOffsets](KafkaMicroBatchStream.md#getOrCreateInitialPartitionOffsets)
* `KafkaOffsetReaderAdmin` is requested to [fetchSpecificOffsets0](KafkaOffsetReaderAdmin.md#fetchSpecificOffsets0)
* `KafkaOffsetReaderConsumer` is requested to [fetchSpecificOffsets0](KafkaOffsetReaderConsumer.md#fetchSpecificOffsets0)
* `KafkaSource` is requested for the [initialPartitionOffsets](KafkaSource.md#initialPartitionOffsets), [reportLatestOffset](KafkaSource.md#reportLatestOffset), [latestOffset](KafkaSource.md#latestOffset)
* `KafkaSourceOffset` utility is used to [create a KafkaSourceOffset](#apply)

## <span id="apply"> Creating KafkaSourceOffset Instance

```scala
apply(
  offsetTuples: (String, Int, Long)*): KafkaSourceOffset
apply(
  offset: SerializedOffset): KafkaSourceOffset
apply(
  offset: Offset): KafkaSourceOffset
```

`apply` creates a [KafkaSourceOffset](#creating-instance).

---

`apply` is used when:

* `KafkaMicroBatchStream` is requested for the [performance metrics](KafkaMicroBatchStream.md#metrics)
* `KafkaSourceInitialOffsetWriter` is requested to [deserialize a KafkaSourceOffset](KafkaSourceInitialOffsetWriter.md#deserialize)
* `KafkaSourceOffset` is requested for the [partition offsets](#getPartitionOffsets)

## <span id="getPartitionOffsets"> Partition Offsets

```scala
getPartitionOffsets(
  offset: Offset): Map[TopicPartition, Long]
```

`getPartitionOffsets` requests the given [KafkaSourceOffset](KafkaSourceOffset.md) or `SerializedOffset` (from the given [Offset](../Offset.md)) for the [partitionToOffsets](KafkaSourceOffset.md#partitionToOffsets).

---

`getPartitionOffsets` is used when:

* `KafkaSource` is requested to [getBatch](KafkaSource.md#getBatch)
