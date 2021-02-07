# DataSource

!!! tip
    Learn more about [DataSource]({{ book.spark_sql }}/DataSource) in [The Internals of Spark SQL]({{ book.spark_sql }}) online book.

## <span id="createSource"> Creating Streaming Source (Data Source V1)

```scala
createSource(
  metadataPath: String): Source
```

`createSource` creates a new instance of the data source class and branches off per the type:

* [StreamSourceProvider](#createSource-StreamSourceProvider)
* [FileFormat](#createSource-FileFormat)
* [other types](#createSource-other)

`createSource` is used when `MicroBatchExecution` is requested for an [analyzed logical plan](micro-batch-execution/MicroBatchExecution.md#logicalPlan).

### <span id="createSource-StreamSourceProvider"> StreamSourceProvider

For a [StreamSourceProvider](StreamSourceProvider.md), `createSource` requests the `StreamSourceProvider` to [create a source](StreamSourceProvider.md#createSource).

### <span id="createSource-FileFormat"> FileFormat

For a `FileFormat`, `createSource` creates a new [FileStreamSource](datasources/file/FileStreamSource.md).

`createSource` throws an `IllegalArgumentException` when `path` option was not specified for a `FileFormat` data source:

```text
'path' is not specified
```

### <span id="createSource-other"> Other Types

For any other data source type, `createSource` simply throws an `UnsupportedOperationException`:

```text
Data source [className] does not support streamed reading
```

## <span id="sourceInfo"> SourceInfo

```scala
sourceInfo: SourceInfo
```

Metadata of a [Source](Source.md) with the following:

* Name (alias)
* Schema
* Partitioning columns

`sourceInfo` is initialized (lazily) using [sourceSchema](#sourceSchema).

??? note "Lazy Value"
    `sourceInfo` is a Scala **lazy value** to guarantee that the code to initialize it is executed once only (when accessed for the first time) and cached afterwards.

Used when:

* `DataSource` is requested to [create a streaming source](#createSource) for a [File-Based Data Source](datasources/file/index.md) (when `MicroBatchExecution` is requested to [initialize the analyzed logical plan](micro-batch-execution/MicroBatchExecution.md#logicalPlan))

* `StreamingRelation` utility is used to [create a StreamingRelation](logical-operators/StreamingRelation.md#apply) (when `DataStreamReader` is requested for a [streaming query](DataStreamReader.md#load))

## <span id="sourceSchema"> Generating Metadata of Streaming Source

```scala
sourceSchema(): SourceInfo
```

`sourceSchema` creates a new instance of the data source class and branches off per the type:

* [StreamSourceProvider](#sourceSchema-StreamSourceProvider)
* [FileFormat](#sourceSchema-FileFormat)
* [other types](#sourceSchema-other)

`sourceSchema` is used when `DataSource` is requested for the [SourceInfo](#sourceInfo).

### <span id="sourceSchema-StreamSourceProvider"> StreamSourceProvider

For a [StreamSourceProvider](StreamSourceProvider.md), `sourceSchema` requests the `StreamSourceProvider` for the [name and schema](StreamSourceProvider.md#sourceSchema) (of the [streaming source](Source.md)).

In the end, `sourceSchema` returns the name and the schema as part of `SourceInfo` (with partition columns unspecified).

### <span id="sourceSchema-FileFormat"> FileFormat

For a `FileFormat`, `sourceSchema`...FIXME

### <span id="sourceSchema-other"> Other Types

For any other data source type, `sourceSchema` simply throws an `UnsupportedOperationException`:

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
    Learn more about [FileFormat]({{ book.spark_sql }}/FileFormat) in [The Internals of Spark SQL]({{ book.spark_sql }}) online book.

`createSink` creates a new instance of the data source class and branches off per the type:

* For a [StreamSinkProvider](StreamSinkProvider.md), `createSink` simply delegates the call and requests it to [create a streaming sink](StreamSinkProvider.md#createSink)

* For a `FileFormat`, `createSink` creates a [FileStreamSink](datasources/file/FileStreamSink.md) when `path` option is specified and the output mode is [Append](OutputMode.md#Append).

`createSink` throws a `IllegalArgumentException` when `path` option is not specified for a `FileFormat` data source:

```text
'path' is not specified
```

`createSink` throws an `AnalysisException` when the given [OutputMode](OutputMode.md) is different from [Append](OutputMode.md#Append) for a `FileFormat` data source:

```text
Data source [className] does not support [outputMode] output mode
```

`createSink` throws an `UnsupportedOperationException` for unsupported data source formats:

```text
Data source [className] does not support streamed writing
```

`createSink` is used when `DataStreamWriter` is requested to [start a streaming query](DataStreamWriter.md#start).
