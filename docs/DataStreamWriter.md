# DataStreamWriter

`DataStreamWriter` is an interface that Spark developers use to describe when the result of executing a streaming query is sent out to a [streaming data source](#format).

## <span id="writeStream"> Accessing DataStreamWriter

`DataStreamWriter` is available using [Dataset.writeStream](operators/writeStream.md) method.

```text
import org.apache.spark.sql.streaming.DataStreamWriter
import org.apache.spark.sql.Row

val streamingQuery: Dataset[Long] = ...

assert(streamingQuery.isStreaming)

val writer: DataStreamWriter[Row] = streamingQuery.writeStream
```

## <span id="foreach"> Writing to ForeachWriter

```scala
foreach(
  writer: ForeachWriter[T]): DataStreamWriter[T]
```

Sets [ForeachWriter](datasources/ForeachWriter.md) as responsible for streaming writes

## <span id="foreachBatch"> Writing Micro-Batches to ForeachBatchSink

```scala
foreachBatch(
  function: (Dataset[T], Long) => Unit): DataStreamWriter[T]
```

Sets the [source](#source) as **foreachBatch** and creates a [ForeachBatchSink](datasources/ForeachBatchSink.md) to be responsible for streaming writes.

!!! quote "SPARK-24565"
    As per [SPARK-24565 Add API for in Structured Streaming for exposing output rows of each microbatch as a DataFrame](https://issues.apache.org/jira/browse/SPARK-24565), the purpose of the method is to expose the micro-batch output as a dataframe for the following:

    Pass the output rows of each batch to a library that is designed for the batch jobs only
    Reuse batch data sources for output whose streaming version does not exist
    Multi-writes where the output rows are written to multiple outputs by writing twice for every batch

## <span id="format"> Streaming Sink by Name

```scala
format(
  source: String): DataStreamWriter[T]
```

Specifies the [streaming sink](Sink.md) by name (_alias_)

## <span id="outputMode"> Output Mode

```scala
outputMode(
  outputMode: OutputMode): DataStreamWriter[T]
outputMode(
  outputMode: String): DataStreamWriter[T]
```

Specifies the [OutputMode](OutputMode.md) of the streaming query (what data is sent out to a [streaming sink](Sink.md) when there is new data available in [streaming data sources](Source.md))

Default: [Append](OutputMode.md#Append)

```scala
import org.apache.spark.sql.streaming.OutputMode.Update
val inputStream = spark
  .readStream
  .format("rate")
  .load
  .writeStream
  .format("console")
  .outputMode(Update) // <-- update output mode
  .start
```

## <span id="partitionBy"> Partitioning Streaming Writes

```scala
partitionBy(
  colNames: String*): DataStreamWriter[T]
```

## <span id="queryName"> Query Name

```scala
queryName(
  queryName: String): DataStreamWriter[T]
```

Assigns the name of a query that is just an additional option with the key `queryName`.

## <span id="start"> Starting Streaming Query (Streaming Writes)

```scala
start(): StreamingQuery
// Explicit `path` (that could also be specified as an option)
start(
  path: String): StreamingQuery
```

Creates and immediately starts a [StreamingQuery](StreamingQuery.md) that is returned as a handle to control the execution of the query

Internally, `start` branches off per `source`.

* `memory`
* `foreach`
* other formats

...FIXME

`start` throws an `AnalysisException` for `source` to be `hive`.

```text
val q =  spark.
  readStream.
  text("server-logs/*").
  writeStream.
  format("hive") <-- hive format used as a streaming sink
scala> q.start
org.apache.spark.sql.AnalysisException: Hive data source can only be used with tables, you can not write files of Hive data source directly.;
  at org.apache.spark.sql.streaming.DataStreamWriter.start(DataStreamWriter.scala:234)
  ... 48 elided
```

## <span id="trigger"> Trigger

```scala
trigger(
  trigger: Trigger): DataStreamWriter[T]
```

Sets the [Trigger](Trigger.md) for how often the streaming query should be executed

Default: [ProcessingTime(0L)](Trigger.md#ProcessingTime) that runs a streaming query as often as possible.
