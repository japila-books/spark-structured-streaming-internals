# FileStreamSink

`FileStreamSink` is a concrete [streaming sink](../../Sink.md) that writes out the results of a streaming query to files (of the specified <<fileFormat, FileFormat>>) in the <<path, root path>>.

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

`FileStreamSink` is <<creating-instance, created>> exclusively when `DataSource` is requested to <<spark-sql-streaming-DataSource.md#createSink, create a streaming sink>> for a file-based data source (i.e. `FileFormat`).

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-FileFormat.html[FileFormat] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

`FileStreamSink` supports spark-sql-streaming-OutputMode.md#Append[Append output mode] only.

`FileStreamSink` uses spark-sql-SQLConf.md#spark.sql.streaming.fileSink.log.deletion[spark.sql.streaming.fileSink.log.deletion] (as `isDeletingExpiredLog`)

[[toString]]
The textual representation of `FileStreamSink` is *FileSink[path]*

[[metadataDir]]
`FileStreamSink` uses *_spark_metadata* directory for...FIXME

[[logging]]
[TIP]
====
Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.FileStreamSink` to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.execution.streaming.FileStreamSink=ALL
```

Refer to <<spark-sql-streaming-spark-logging.md#, Logging>>.
====

=== [[creating-instance]] Creating FileStreamSink Instance

`FileStreamSink` takes the following to be created:

* [[sparkSession]] `SparkSession`
* [[path]] Root directory
* [[fileFormat]] `FileFormat`
* [[partitionColumnNames]] Names of the partition columns
* [[options]] Configuration options

`FileStreamSink` initializes the <<internal-properties, internal properties>>.

=== [[addBatch]] "Adding" Batch of Data to Sink -- `addBatch` Method

[source, scala]
----
addBatch(
  batchId: Long,
  data: DataFrame): Unit
----

`addBatch`...FIXME

`addBatch` is a part of [Sink Contract](../../Sink.md#addBatch) abstraction.

=== [[basicWriteJobStatsTracker]] Creating BasicWriteJobStatsTracker -- `basicWriteJobStatsTracker` Internal Method

[source, scala]
----
basicWriteJobStatsTracker: BasicWriteJobStatsTracker
----

`basicWriteJobStatsTracker` simply creates a `BasicWriteJobStatsTracker` with the basic metrics:

* number of written files
* bytes of written output
* number of output rows
* number of dynamic partitions

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-BasicWriteJobStatsTracker.html[BasicWriteJobStatsTracker] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

NOTE: `basicWriteJobStatsTracker` is used exclusively when `FileStreamSink` is requested to <<addBatch, addBatch>>.

=== [[hasMetadata]] `hasMetadata` Object Method

[source, scala]
----
hasMetadata(
  path: Seq[String],
  hadoopConf: Configuration): Boolean
----

`hasMetadata`...FIXME

`hasMetadata` is used when:

* `DataSource` (Spark SQL) is requested to resolve a `FileFormat` relation (`resolveRelation`) and creates a `HadoopFsRelation`
* `FileStreamSource` is requested to [fetchAllFiles](FileStreamSource.md#fetchAllFiles)

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| basePath
a| [[basePath]] *Base path* (Hadoop's https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/Path.html[Path] for the given <<path, path>>)

Used when...FIXME

| logPath
a| [[logPath]] *Metadata log path* (Hadoop's https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/Path.html[Path] for the <<basePath, base path>> and the <<metadataDir, _spark_metadata>>)

Used exclusively to create the <<fileLog, FileStreamSinkLog>>

| fileLog
a| [[fileLog]] [FileStreamSinkLog](FileStreamSinkLog.md) (for the [version 1](FileStreamSinkLog.md#VERSION) and the <<logPath, metadata log path>>)

Used exclusively when `FileStreamSink` is requested to <<addBatch, addBatch>>

| hadoopConf
a| [[hadoopConf]] Hadoop's http://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/conf/Configuration.html[Configuration]

Used when...FIXME

|===
