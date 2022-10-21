# StreamMetadata

`StreamMetadata` is a metadata associated with a [StreamExecution](StreamExecution.md#streamMetadata) (and thus with a [StreamingQuery](StreamingQuery.md)).

`StreamMetadata` can be [loaded](#read) from a metadata file (e.g., when a [StreamExecution](StreamExecution.md) is restarted) or [created from scratch](#write) (e.g., when the streaming query is started for the first time).

`StreamMetadata` uses [json4s-jackson](http://json4s.org/) library for JSON persistence.

```text
import org.apache.spark.sql.execution.streaming.StreamMetadata
import org.apache.hadoop.fs.Path
val metadataPath = new Path("metadata")

assert(spark.isInstanceOf[org.apache.spark.sql.SparkSession])

val hadoopConf = spark.sessionState.newHadoopConf()
val sm = StreamMetadata.read(metadataPath, hadoopConf)

assert(sm.isInstanceOf[Option[org.apache.spark.sql.execution.streaming.StreamMetadata]])
```

## Creating Instance

`StreamMetadata` takes the following to be created:

* <span id="id"> ID (default: a randomly generated UUID)

`StreamMetadata` is created when:

* `StreamExecution` is [created](StreamExecution.md#streamMetadata)

## <span id="read"> Loading Stream Metadata

```scala
read(
  metadataFile: Path,
  hadoopConf: Configuration): Option[StreamMetadata]
```

`read` [creates a CheckpointFileManager](CheckpointFileManager.md#create) (with the parent directory of the given `metadataFile`).

`read` requests the `CheckpointFileManager` whether the given `metadataFile` [exists](CheckpointFileManager.md#exists) or not.

If the metadata file is available, `read` tries to unpersist a `StreamMetadata` from the given `metadataFile` file. Unless successful (the metadata file was available and the content was properly JSON-encoded), `read` returns `None`.

!!! note "json4s-jackson"
    `read` uses `org.json4s.jackson.Serialization.read` for JSON deserialization.

---

`read` is used when:

* `StreamExecution` is [created](StreamExecution.md#streamMetadata) (and the `metadata` checkpoint file is available)

## <span id="write"> Persisting Metadata

```scala
write(
  metadata: StreamMetadata,
  metadataFile: Path,
  hadoopConf: Configuration): Unit
```

`write` persists (_saves_) the given `StreamMetadata` to the given `metadataFile` file in JSON format.

!!! note "json4s-jackson"
    `write` uses `org.json4s.jackson.Serialization.write` for JSON serialization.

---

`write` is used when:

* `StreamExecution` is [created](StreamExecution.md#streamMetadata) (and the `metadata` checkpoint file is not available)
