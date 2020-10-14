# KafkaSourceInitialOffsetWriter

`KafkaSourceInitialOffsetWriter` is a [Hadoop DFS-based metadata storage](../../spark-sql-streaming-HDFSMetadataLog.md) for [KafkaSourceOffsets](KafkaSourceOffset.md).

`KafkaSourceInitialOffsetWriter` is <<creating-instance, created>> exclusively when `KafkaMicroBatchReader` is requested to [getOrCreateInitialPartitionOffsets](KafkaMicroBatchReader.md#getOrCreateInitialPartitionOffsets).

[[VERSION]]
`KafkaSourceInitialOffsetWriter` uses `1` for the version.

## Creating Instance

`KafkaSourceInitialOffsetWriter` takes the following to be created:

* [[sparkSession]] `SparkSession`
* [[metadataPath]] Path of the metadata log directory

=== [[deserialize]] Deserializing Metadata (Reading Metadata from Serialized Format) -- `deserialize` Method

[source, scala]
----
deserialize(
  in: InputStream): KafkaSourceOffset
----

NOTE: `deserialize` is part of the <<../../spark-sql-streaming-HDFSMetadataLog.md#deserialize, HDFSMetadataLog Contract>> to deserialize metadata (reading metadata from a serialized format)

`deserialize`...FIXME
