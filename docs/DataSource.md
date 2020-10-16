# DataSource

!!! tip
    Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-DataSource.html[DataSource &mdash; Pluggable Data Provider Framework]  in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] online book.

## Creating Instance

`DataSource` takes the following to be created:

* [[sparkSession]] `SparkSession`
* [[className]] `className`, i.e. the fully-qualified class name or an alias of the data source
* [[paths]] Paths (default: `Nil`, i.e. an empty collection)
* [[userSpecifiedSchema]] Optional user-defined schema (default: `None`)
* [[partitionColumns]] Names of the partition columns (default: (empty))
* [[bucketSpec]] Optional `BucketSpec` (default: `None`)
* [[options]] Configuration options (default: empty)
* [[catalogTable]] Optional `CatalogTable` (default: `None`)

`DataSource` initializes the <<internal-properties, internal properties>>.

=== [[sourceSchema]] Generating Metadata of Streaming Source (Data Source API V1) -- `sourceSchema` Internal Method

[source, scala]
----
sourceSchema(): SourceInfo
----

`sourceSchema` creates a new instance of the <<providingClass, data source class>> and branches off per the type, e.g. <<sourceSchema-StreamSourceProvider, StreamSourceProvider>>, <<sourceSchema-FileFormat, FileFormat>> and <<sourceSchema-other, other types>>.

NOTE: `sourceSchema` is used exclusively when `DataSource` is requested for the <<sourceInfo, SourceInfo>>.

==== [[sourceSchema-StreamSourceProvider]] StreamSourceProvider

For a [StreamSourceProvider](StreamSourceProvider.md), `sourceSchema` requests the `StreamSourceProvider` for the [name and schema](StreamSourceProvider.md#sourceSchema) (of the [streaming source](Source.md)).

In the end, `sourceSchema` returns the name and the schema as part of `SourceInfo` (with partition columns unspecified).

==== [[sourceSchema-FileFormat]] FileFormat

For a `FileFormat`, `sourceSchema`...FIXME

==== [[sourceSchema-other]] Other Types

For any other data source type, `sourceSchema` simply throws an `UnsupportedOperationException`:

```
Data source [className] does not support streamed reading
```

=== [[createSource]] Creating Streaming Source (Micro-Batch Stream Processing / Data Source API V1) -- `createSource` Method

[source, scala]
----
createSource(
  metadataPath: String): Source
----

`createSource` creates a new instance of the <<providingClass, data source class>> and branches off per the type, e.g. <<createSource-StreamSourceProvider, StreamSourceProvider>>, <<createSource-FileFormat, FileFormat>> and <<createSource-other, other types>>.

NOTE: `createSource` is used exclusively when `MicroBatchExecution` is requested to <<MicroBatchExecution.md#logicalPlan, initialize the analyzed logical plan>>.

==== [[createSource-StreamSourceProvider]] StreamSourceProvider

For a [StreamSourceProvider](StreamSourceProvider.md), `createSource` requests the `StreamSourceProvider` to [create a source](StreamSourceProvider.md#createSource).

==== [[createSource-FileFormat]] FileFormat

For a `FileFormat`, `createSource` creates a new [FileStreamSource](datasources/file/FileStreamSource.md).

`createSource` throws an `IllegalArgumentException` when `path` option was not specified for a `FileFormat` data source:

```text
'path' is not specified
```

==== [[createSource-other]] Other Types

For any other data source type, `createSource` simply throws an `UnsupportedOperationException`:

```text
Data source [className] does not support streamed reading
```

## <span id="createSink"> Creating Streaming Sink

```scala
createSink(
  outputMode: OutputMode): Sink
```

`createSink` creates a [streaming sink](Sink.md) for [StreamSinkProvider](StreamSinkProvider.md) or `FileFormat` data sources.

!!! tip
    Learn more about [FileFormat Data Source]({{ book.spark_sql }}/spark-sql-FileFormat) in [The Internals of Spark SQL]({{ book.spark_sql }}) online book.

Internally, `createSink` creates a new instance of the <<providingClass, providingClass>> and branches off per type:

* For a [StreamSinkProvider](StreamSinkProvider.md), `createSink` simply delegates the call and requests it to [create a streaming sink](StreamSinkProvider.md#createSink)

* For a `FileFormat`, `createSink` creates a [FileStreamSink](datasources/file/FileStreamSink.md) when `path` option is specified and the output mode is [Append](OutputMode.md#Append).

`createSink` throws a `IllegalArgumentException` when `path` option is not specified for a `FileFormat` data source:

```text
'path' is not specified
```

`createSink` throws an `AnalysisException` when the given [OutputMode](OutputMode.md) is different from [Append](OutputMode.md#Append) for a `FileFormat` data source:

```
Data source [className] does not support [outputMode] output mode
```

`createSink` throws an `UnsupportedOperationException` for unsupported data source formats:

```
Data source [className] does not support streamed writing
```

`createSink` is used when `DataStreamWriter` is requested to [start a streaming query](DataStreamWriter.md#start).

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| providingClass
a| [[providingClass]] https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html[java.lang.Class] for the <<className, className>> (that can be a fully-qualified class name or an alias of the data source)

| sourceInfo
a| [[sourceInfo]]

[source, scala]
----
sourceInfo: SourceInfo
----

Metadata of a [Source](Source.md) with the alias (short name), the schema, and optional partitioning columns

`sourceInfo` is a lazy value and so initialized once (the very first time) when accessed.

Used when:

* `DataSource` is requested to <<createSource, create a source (for a FileFormat data source)>> (when `MicroBatchExecution` is requested to <<MicroBatchExecution.md#logicalPlan, initialize the analyzed logical plan>>)

* `StreamingRelation` utility is requested for a <<spark-sql-streaming-StreamingRelation.md#apply, StreamingRelation>> (when `DataStreamReader` is requested for a [streaming query](DataStreamReader.md#load))

|===
