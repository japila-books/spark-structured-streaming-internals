== [[KafkaSourceInitialOffsetWriter]] KafkaSourceInitialOffsetWriter

`KafkaSourceInitialOffsetWriter` is a <<spark-sql-streaming-HDFSMetadataLog.md#, Hadoop DFS-based metadata storage>> for <<spark-sql-streaming-KafkaSourceOffset.md#, KafkaSourceOffsets>>.

`KafkaSourceInitialOffsetWriter` is <<creating-instance, created>> exclusively when `KafkaMicroBatchReader` is requested to <<spark-sql-streaming-KafkaMicroBatchReader.md#getOrCreateInitialPartitionOffsets, getOrCreateInitialPartitionOffsets>>.

[[VERSION]]
`KafkaSourceInitialOffsetWriter` uses `1` for the version.

=== [[creating-instance]] Creating KafkaSourceInitialOffsetWriter Instance

`KafkaSourceInitialOffsetWriter` takes the following to be created:

* [[sparkSession]] `SparkSession`
* [[metadataPath]] Path of the metadata log directory

=== [[deserialize]] Deserializing Metadata (Reading Metadata from Serialized Format) -- `deserialize` Method

[source, scala]
----
deserialize(
  in: InputStream): KafkaSourceOffset
----

NOTE: `deserialize` is part of the <<spark-sql-streaming-HDFSMetadataLog.md#deserialize, HDFSMetadataLog Contract>> to deserialize metadata (reading metadata from a serialized format)

`deserialize`...FIXME
