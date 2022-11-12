# KafkaSourceInitialOffsetWriter

`KafkaSourceInitialOffsetWriter` is a [Hadoop DFS-based metadata storage](../HDFSMetadataLog.md) for [KafkaSourceOffsets](KafkaSourceOffset.md).

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

`deserialize`...FIXME

`deserialize` is part of the [HDFSMetadataLog](../HDFSMetadataLog.md#deserialize) abstraction.
