# FileStreamSink

`FileStreamSink` is a [streaming sink](../../Sink.md) that writes data out to files (in a given [file format](#fileFormat) and a [directory](#path)).

`FileStreamSink` can only be used with [Append](../../OutputMode.md#Append) output mode.

!!! tip
    Learn more in [Demo: Deep Dive into FileStreamSink](../../demo/deep-dive-into-filestreamsink.md).

## Creating Instance

`FileStreamSink` takes the following to be created:

* <span id="sparkSession"> `SparkSession`
* <span id="path"> Path
* <span id="fileFormat"> `FileFormat`
* <span id="partitionColumnNames"> Names of the Partition Columns (if any)
* <span id="options"> Options (`Map[String, String]`)

`FileStreamSink` is created when `DataSource` is requested to [create a streaming sink](../../DataSource.md#createSink) for `FileFormat` data sources.

## <span id="metadataDir"><span id="getMetadataLogPath"> Metadata Log Directory

`FileStreamSink` uses **_spark_metadata** directory (under the [path](#path)) as the **Metadata Log Directory** to store metadata indicating which files are valid and can be read (and skipping already committed batch).

Metadata Log Directory is managed by [FileStreamSinkLog](#fileLog).

### <span id="logPath"> Hadoop Path of Metadata Log

```scala
logPath: Path
```

`logPath` is the location of the [Metadata Log](#getMetadataLogPath) (as a Hadoop [Path]({{ hadoop.api }}/org/apache/hadoop/fs/Path.html)).

### <span id="fileLog"> FileStreamSinkLog

```scala
fileLog: FileStreamSinkLog
```

`fileLog` is a [FileStreamSinkLog](FileStreamSinkLog.md) (for the [version 1](FileStreamSinkLog.md#VERSION) and the [metadata log path](#logPath))

Used for ["adding" batch](#addBatch).

## <span id="toString"> Text Representation

`FileStreamSink` uses the [path](#path) for the text representation (`toString`):

```text
FileSink[path]
```

## <span id="addBatch"> "Adding" Batch of Data to Sink

```scala
addBatch(
  batchId: Long,
  data: DataFrame): Unit
```

`addBatch` requests the [FileStreamSinkLog](#fileLog) for the [latest committed batch ID](../../HDFSMetadataLog.md#getLatest).

With a newer `batchId`, `addBatch` creates a `FileCommitProtocol` based on [spark.sql.streaming.commitProtocolClass](../../configuration-properties.md#spark.sql.streaming.commitProtocolClass) configuration property.

!!! tip "The Internals of Apache Spark"
    Learn more on [FileCommitProtocol]({{ book.spark_core }}/FileCommitProtocol) in [The Internals of Apache Spark]({{ book.spark_core }}).

For a [ManifestFileCommitProtocol](ManifestFileCommitProtocol.md), `addBatch` requests it to [setupManifestOptions](ManifestFileCommitProtocol.md#setupManifestOptions) (with the [FileStreamSinkLog](#fileLog) and the given `batchId`).

In the end, `addBatch` writes out the data using `FileFormatWriter.write` workflow (with the `FileCommitProtocol` and [BasicWriteJobStatsTracker](#basicWriteJobStatsTracker)).

!!! tip "The Internals of Spark SQL"
    Learn more on [FileFormatWriter]({{ book.spark_sql }}/spark-sql-FileFormatWriter/) in [The Internals of Spark SQL]({{ book.spark_sql }}).

`addBatch` prints out the following INFO message to the logs when the given `batchId` is below the latest committed batch ID:

```text
Skipping already committed batch [batchId]
```

`addBatch` is a part of the [Sink](../../Sink.md#addBatch) abstraction.

### <span id="basicWriteJobStatsTracker"> Creating BasicWriteJobStatsTracker

```scala
basicWriteJobStatsTracker: BasicWriteJobStatsTracker
```

`basicWriteJobStatsTracker` creates a `BasicWriteJobStatsTracker` with the basic metrics:

* number of written files
* bytes of written output
* number of output rows
* number of dynamic partitions

!!! tip
    Learn more about [BasicWriteJobStatsTracker]({{ book.spark_sql }}/spark-sql-BasicWriteJobStatsTracker) in [The Internals of Spark SQL]({{ book.spark_sql }}) online book.

`basicWriteJobStatsTracker` is used when `FileStreamSink` is requested to [addBatch](#addBatch).

## <span id="hasMetadata"> hasMetadata Utility

```scala
hasMetadata(
  path: Seq[String],
  hadoopConf: Configuration): Boolean
```

`hasMetadata`...FIXME

`hasMetadata` is used (to short-circut listing files using [MetadataLogFileIndex](MetadataLogFileIndex.md) instead of using HDFS API) when:

* `DataSource` (Spark SQL) is requested to resolve a `FileFormat` relation
* `FileTable` (Spark SQL) is requested for a `PartitioningAwareFileIndex`
* `FileStreamSource` is requested to [fetchAllFiles](FileStreamSource.md#fetchAllFiles)

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.FileStreamSink` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.execution.streaming.FileStreamSink=ALL
```

Refer to [Logging](../../spark-logging.md).
