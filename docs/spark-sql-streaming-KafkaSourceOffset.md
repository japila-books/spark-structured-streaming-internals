== [[KafkaSourceOffset]] KafkaSourceOffset

`KafkaSourceOffset` is a custom <<spark-sql-streaming-Offset.md#, Offset>> for <<spark-sql-streaming-kafka-data-source.md#, kafka data source>>.

`KafkaSourceOffset` is <<creating-instance, created>> (directly or indirectly using <<apply, apply>>) when:

* `KafkaContinuousReader` is requested to <<spark-sql-streaming-KafkaContinuousReader.md#setStartOffset, setStartOffset>>, <<spark-sql-streaming-KafkaContinuousReader.md#deserializeOffset, deserializeOffset>>, and <<spark-sql-streaming-KafkaContinuousReader.md#mergeOffsets, mergeOffsets>>

* `KafkaMicroBatchReader` is requested to <<spark-sql-streaming-KafkaMicroBatchReader.md#getStartOffset, getStartOffset>>, <<spark-sql-streaming-KafkaMicroBatchReader.md#getEndOffset, getEndOffset>>, <<spark-sql-streaming-KafkaMicroBatchReader.md#deserializeOffset, deserializeOffset>>, and <<spark-sql-streaming-KafkaMicroBatchReader.md#getOrCreateInitialPartitionOffsets, getOrCreateInitialPartitionOffsets>>

* `KafkaOffsetReader` is requested to <<spark-sql-streaming-KafkaOffsetReader.md#fetchSpecificOffsets, fetchSpecificOffsets>>

* `KafkaSource` is requested for the <<spark-sql-streaming-KafkaSource.md#initialPartitionOffsets, initial partition offsets (of 0th batch)>> and <<spark-sql-streaming-KafkaSource.md#getOffset, getOffset>>

* `KafkaSourceInitialOffsetWriter` is requested to <<spark-sql-streaming-KafkaSourceInitialOffsetWriter.md#deserialize, deserialize a KafkaSourceOffset (from an InputStream)>>

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

```
Invalid conversion from offset of [class] to KafkaSourceOffset
```

[NOTE]
====
`getPartitionOffsets` is used when:

* `KafkaContinuousReader` is requested to <<spark-sql-streaming-KafkaContinuousReader.md#planInputPartitions, planInputPartitions>>

* `KafkaSource` is requested to <<spark-sql-streaming-KafkaSource.md#getBatch, generate a streaming DataFrame with records from Kafka for a streaming micro-batch>>
====

=== [[json]] JSON-Encoded Offset -- `json` Method

[source, scala]
----
json: String
----

NOTE: `json` is part of the <<spark-sql-streaming-Offset.md#json, Offset Contract>> for a JSON-encoded offset.

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

[NOTE]
====
`apply` is used when:

* `KafkaSourceInitialOffsetWriter` is requested to <<spark-sql-streaming-KafkaSourceInitialOffsetWriter.md#deserialize, deserialize a KafkaSourceOffset (from an InputStream)>>

* `KafkaSource` is requested for the <<spark-sql-streaming-KafkaSource.md#initialPartitionOffsets, initial partition offsets (of 0th batch)>>

* `KafkaSourceOffset` is requested to <<getPartitionOffsets, getPartitionOffsets>>
====
