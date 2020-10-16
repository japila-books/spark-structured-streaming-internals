# FileStreamSink

`FileStreamSink` is a [streaming sink](../../Sink.md) that writes out to files (in a given [file format](#fileFormat)) in a [directory](#path).

`FileStreamSink` is used with [Append output mode](spark-sql-streaming-OutputMode.md#Append) only.

## Demo

```text
import scala.concurrent.duration._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
val sq = in.
  writeStream.
  format("parquet").
  option("path", "parquet-output-dir").
  option("checkpointLocation", "checkpoint-dir").
  trigger(Trigger.ProcessingTime(10.seconds)).
  outputMode(OutputMode.Append).
  start
```

## Creating Instance

`FileStreamSink` takes the following to be created:

* <span id="sparkSession"> `SparkSession`
* <span id="path"> Path
* <span id="fileFormat"> `FileFormat`
* <span id="partitionColumnNames"> Names of the Partition Columns (if any)
* <span id="options"> Options (`Map[String, String]`)

`FileStreamSink` is created when `DataSource` is requested to [create a streaming sink](../../DataSource.md#createSink) for `FileFormat` data sources.

## <span id="metadataDir"> metadataDir

`FileStreamSink` uses *_spark_metadata* directory for...FIXME

## <span id="toString"> Text Representation

The text representation of `FileStreamSink` uses the [path](#path) and is as follows:

```text
FileSink[path]
```

## <span id="addBatch"> "Adding" Batch of Data to Sink

```scala
addBatch(
  batchId: Long,
  data: DataFrame): Unit
```

`addBatch`...FIXME

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

`hasMetadata` is used when:

* `DataSource` (Spark SQL) is requested to resolve a `FileFormat` relation (`resolveRelation`) and creates a `HadoopFsRelation`
* `FileStreamSource` is requested to [fetchAllFiles](FileStreamSource.md#fetchAllFiles)

## <span id="logPath"> Metadata Log Path

```scala
logPath: Path
```

`logPath` is the location of the **Metadata Log** (as Hadoop's [Path]({{ hadoop.api }}/org/apache/hadoop/fs/Path.html) for the [path](#path) and the [_spark_metadata](#metadataDir))

Used to create the [FileStreamSinkLog](#fileLog)

## <span id="fileLog"> FileStreamSinkLog

```scala
fileLog: FileStreamSinkLog
```

`fileLog` is a [FileStreamSinkLog](FileStreamSinkLog.md) (for the [version 1](FileStreamSinkLog.md#VERSION) and the [metadata log path](#logPath))

Used for [addBatch](#addBatch)

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.FileStreamSink` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.execution.streaming.FileStreamSink=ALL
```

Refer to [Logging](../../spark-logging.md).
