# Source &mdash; Streaming Source in Micro-Batch Stream Processing

`Source` is an [extension](#contract) of the [SparkDataStream](SparkDataStream.md) abstraction for [streaming sources](#implementations) for "streamed reading" of continually arriving data for a streaming query (identified by [offset](spark-sql-streaming-Offset.md)).

`Source` is used in [Micro-Batch Stream Processing](micro-batch-stream-processing.md).

`Source` is created using [StreamSourceProvider.createSource](StreamSourceProvider.md#createSource) (and [DataSource.createSource](spark-sql-streaming-DataSource.md#createSource)).

For fault tolerance, `Source` must be able to replay an arbitrary sequence of past data in a stream using a range of offsets. This is the assumption so Structured Streaming can achieve end-to-end exactly-once guarantees.

## Contract

### <span id="commit"> commit

```scala
commit(
  end: Offset): Unit
```

Commits data up to the end [offset](spark-sql-streaming-Offset.md) (informs the source that Spark has completed processing all data for offsets less than or equal to the end offset and will only request offsets greater than the end offset in the future).

Used when [MicroBatchExecution](MicroBatchExecution.md) stream execution engine is requested to [write offsets to a commit log (walCommit phase)](MicroBatchExecution.md#constructNextBatch-walCommit) while [running an activated streaming query](MicroBatchExecution.md#runActivatedStream).

### <span id="getBatch"> getBatch

```scala
getBatch(
  start: Option[Offset],
  end: Offset): DataFrame
```

Generating a streaming `DataFrame` with data between the start and end [offsets](spark-sql-streaming-Offset.md)

Start offset can be undefined (`None`) to indicate that the batch should begin with the first record

Used when [MicroBatchExecution](MicroBatchExecution.md) stream execution engine is requested to [run an activated streaming query](MicroBatchExecution.md#runActivatedStream), namely:

* [Populate start offsets from checkpoint (resuming from checkpoint)](MicroBatchExecution.md#populateStartOffsets)

* [Request unprocessed data from all sources (getBatch phase)](MicroBatchExecution.md#runBatch-getBatch)

### <span id="getOffset"> getOffset

```scala
getOffset: Option[Offset]
```

Latest (maximum) <<spark-sql-streaming-Offset.md#, offset>> of the source (or `None` to denote no data)

Used when <<MicroBatchExecution.md#, MicroBatchExecution>> stream execution engine (<<micro-batch-stream-processing.md#, Micro-Batch Stream Processing>>) is requested for <<MicroBatchExecution.md#constructNextBatch-getOffset, latest offsets of all sources (getOffset phase)>> while <<MicroBatchExecution.md#runActivatedStream, running activated streaming query>>.

### <span id="schema"> schema

```scala
schema: StructType
```

Schema of the data from this source

## Implementations

* [FileStreamSource](datasources/file/FileStreamSource.md)
* [KafkaSource](datasources/kafka/KafkaSource.md)

## <span id="initialOffset"> initialOffset Method

```scala
initialOffset(): OffsetV2
```

`initialOffset` throws an `IllegalStateException`.

`initialOffset` is part of the [SparkDataStream](SparkDataStream.md#initialOffset) abstraction.

## <span id="deserializeOffset"> deserializeOffset Method

```scala
deserializeOffset(
  json: String): OffsetV2
```

`deserializeOffset` throws an `IllegalStateException`.

`deserializeOffset` is part of the [SparkDataStream](SparkDataStream.md#deserializeOffset) abstraction.
