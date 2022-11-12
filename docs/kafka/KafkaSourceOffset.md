# KafkaSourceOffset

`KafkaSourceOffset` is an [Offset](../Offset.md) for [kafka data source](index.md).

`KafkaSourceOffset` is <<creating-instance, created>> (directly or indirectly using <<apply, apply>>) when:

* `KafkaOffsetReader` is requested to [fetchSpecificOffsets](KafkaOffsetReader.md#fetchSpecificOffsets)

* `KafkaSource` is requested for the [initial partition offsets (of 0th batch)](KafkaSource.md#initialPartitionOffsets) and [getOffset](KafkaSource.md#getOffset)

* `KafkaSourceInitialOffsetWriter` is requested to [deserialize a KafkaSourceOffset (from an InputStream)](KafkaSourceInitialOffsetWriter.md#deserialize)

* `KafkaSourceOffset` is requested for <<getPartitionOffsets, partition offsets>>

[[creating-instance]][[partitionToOffsets]]
`KafkaSourceOffset` takes a collection of Kafka `TopicPartitions` with offsets to be created.

=== [[getPartitionOffsets]] Partition Offsets -- `getPartitionOffsets` Method

[source, scala]
----
getPartitionOffsets(
  offset: Offset): Map[TopicPartition, Long]
----

`getPartitionOffsets` takes <<partitionToOffsets, KafkaSourceOffset.partitionToOffsets>> from `offset`.

If `offset` is `KafkaSourceOffset`, `getPartitionOffsets` takes the partitions and offsets straight from it.

If however `offset` is `SerializedOffset`, `getPartitionOffsets` deserializes the offsets from JSON.

`getPartitionOffsets` reports an `IllegalArgumentException` when `offset` is neither `KafkaSourceOffset` or `SerializedOffset`.

```text
Invalid conversion from offset of [class] to KafkaSourceOffset
```

`getPartitionOffsets` is used when:

* `KafkaSource` is requested to [generate a streaming DataFrame with records from Kafka for a streaming micro-batch](KafkaSource.md#getBatch)

=== [[json]] JSON-Encoded Offset -- `json` Method

[source, scala]
----
json: String
----

`json` is part of the [Offset](../Offset.md#json) abstraction.

`json`...FIXME

=== [[apply]] Creating KafkaSourceOffset Instance -- `apply` Utility Method

[source, scala]
----
apply(
  offsetTuples: (String, Int, Long)*): KafkaSourceOffset // <1>
apply(
  offset: SerializedOffset): KafkaSourceOffset
----
<1> Used in tests only

`apply`...FIXME

`apply` is used when:

* `KafkaSourceInitialOffsetWriter` is requested to [deserialize a KafkaSourceOffset (from an InputStream)](KafkaSourceInitialOffsetWriter.md#deserialize)

* `KafkaSource` is requested for the [initial partition offsets (of 0th batch)](KafkaSource.md#initialPartitionOffsets)

* `KafkaSourceOffset` is requested to [getPartitionOffsets](#getPartitionOffsets)
