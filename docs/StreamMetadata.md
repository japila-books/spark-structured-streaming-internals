# StreamMetadata

`StreamMetadata` is a metadata associated with a <<StreamingQuery.md#, StreamingQuery>> (indirectly through [StreamExecution](StreamExecution.md#streamMetadata)).

[[creating-instance]]
[[id]]
`StreamMetadata` takes an ID to be created.

`StreamMetadata` is <<creating-instance, created>> exclusively when [StreamExecution](StreamExecution.md#streamMetadata) is created (with a randomly-generated 128-bit universally unique identifier (UUID)).

`StreamMetadata` can be <<write, persisted>> to and <<read, unpersisted>> from a JSON file. `StreamMetadata` uses http://json4s.org/[json4s-jackson] library for JSON persistence.

```text
import org.apache.spark.sql.execution.streaming.StreamMetadata
import org.apache.hadoop.fs.Path
val metadataPath = new Path("metadata")

scala> :type spark
org.apache.spark.sql.SparkSession

val hadoopConf = spark.sessionState.newHadoopConf()
val sm = StreamMetadata.read(metadataPath, hadoopConf)

scala> :type sm
Option[org.apache.spark.sql.execution.streaming.StreamMetadata]
```

=== [[read]] Unpersisting StreamMetadata (from JSON File) -- `read` Object Method

[source, scala]
----
read(
  metadataFile: Path,
  hadoopConf: Configuration): Option[StreamMetadata]
----

`read` unpersists `StreamMetadata` from the given `metadataFile` file if available.

`read` returns a `StreamMetadata` if the metadata file was available and the content could be read in JSON format. Otherwise, `read` returns `None`.

NOTE: `read` uses `org.json4s.jackson.Serialization.read` for JSON deserialization.

NOTE: `read` is used exclusively when [StreamExecution](StreamExecution.md#streamMetadata) is created> (and tries to read the `metadata` checkpoint file).

=== [[write]] Persisting Metadata -- `write` Object Method

[source, scala]
----
write(
  metadata: StreamMetadata,
  metadataFile: Path,
  hadoopConf: Configuration): Unit
----

`write` persists the given `StreamMetadata` to the given `metadataFile` file in JSON format.

NOTE: `write` uses `org.json4s.jackson.Serialization.write` for JSON serialization.

`write` is used when [StreamExecution](StreamExecution.md#streamMetadata) is created (and the `metadata` checkpoint file is not available).
