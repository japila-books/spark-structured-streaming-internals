# Internals of Streaming Queries

The page is to keep notes about how to guide readers through the codebase and may disappear if merged with the other pages or become an intro page.

* [DataStreamReader and Streaming Data Source](#DataStreamReader)
* [Data Source Resolution, Streaming Dataset and Logical Query Plan](#data-source-resolution)
* [Dataset API &mdash; High-Level DSL to Build Logical Query Plan](#dataset)
* [DataStreamWriter and Streaming Data Sink](#DataStreamWriter)
* [StreamingQuery](#StreamingQuery)
* [StreamingQueryManager](#StreamingQueryManager)

## <span id="DataStreamReader"> DataStreamReader and Streaming Data Source

It all starts with `SparkSession.readStream` method which lets you define a [streaming source](Source.md) in a **stream processing pipeline** (_streaming processing graph_ or _dataflow graph_).

```scala
import org.apache.spark.sql.SparkSession
assert(spark.isInstanceOf[SparkSession])

val reader = spark.readStream

import org.apache.spark.sql.streaming.DataStreamReader
assert(reader.isInstanceOf[DataStreamReader])
```

`SparkSession.readStream` method creates a [DataStreamReader](DataStreamReader.md).

The fluent API of `DataStreamReader` allows you to describe the input data source (e.g. [DataStreamReader.format](DataStreamReader.md#format) and [DataStreamReader.options](DataStreamReader.md#options)) using method chaining (with the goal of making the readability of the source code close to that of ordinary written prose, essentially creating a domain-specific language within the interface. See [Fluent interface](https://en.wikipedia.org/wiki/Fluent_interface) article in Wikipedia).

```scala
reader
  .format("csv")
  .option("delimiter", "|")
```

There are a couple of built-in data source formats. Their names are the names of the corresponding `DataStreamReader` methods and so act like shortcuts of `DataStreamReader.format` (where you have to specify the format by name), i.e. [csv](DataStreamReader.md#csv), [json](DataStreamReader.md#json), [orc](DataStreamReader.md#orc), [parquet](DataStreamReader.md#parquet) and [text](DataStreamReader.md#text), followed by [DataStreamReader.load](DataStreamReader.md#load).

You may also want to use [DataStreamReader.schema](DataStreamReader.md#schema) method to specify the schema of the streaming data source.

```scala
reader.schema("a INT, b STRING")
```

In the end, you use [DataStreamReader.load](DataStreamReader.md#load) method that simply creates a streaming Dataset (the good ol' Dataset that you may have already used in Spark SQL).

```scala
val input = reader
  .format("csv")
  .option("delimiter", "\t")
  .schema("word STRING, num INT")
  .load("data/streaming")

import org.apache.spark.sql.DataFrame
assert(input.isInstanceOf[DataFrame])
```

The Dataset has the `isStreaming` property enabled that is basically the only way you could distinguish streaming Datasets from regular, batch Datasets.

```scala
assert(input.isStreaming)
```

In other words, Spark Structured Streaming is designed to extend the features of Spark SQL and let your structured queries be streaming queries.

## <span id="data-source-resolution"> Data Source Resolution, Streaming Dataset and Logical Query Plan

Whenever you create a Dataset (be it batch in Spark SQL or streaming in Spark Structured Streaming) is when you create a **logical query plan** using the **High-Level Dataset DSL**.

A logical query plan is made up of logical operators.

Spark Structured Streaming gives you two logical operators to represent streaming sources ([StreamingRelationV2](logical-operators/StreamingRelationV2.md) and [StreamingRelation](logical-operators/StreamingRelation.md)).

When [DataStreamReader.load](DataStreamReader.md#load) method is executed, `load` first looks up the requested data source (that you specified using [DataStreamReader.format](DataStreamReader.md#format)) and creates an instance of it (_instantiation_). That'd be **data source resolution** step (that I described in...FIXME).

`DataStreamReader.load` is where you can find the intersection of the former [Micro-Batch Stream Processing](micro-batch-stream-processing.md) V1 API with the new [Continuous Stream Processing](spark-sql-streaming-continuous-stream-processing.md) V2 API.

### V2 Code Path

For [MicroBatchStream](MicroBatchStream.md) or [ContinuousReadSupport](ContinuousReadSupport.md) data sources, `DataStreamReader.load` creates a logical query plan with a [StreamingRelationV2](logical-operators/StreamingRelationV2.md) leaf logical operator. That is the new **V2 code path**.

```text
// rate data source is V2
val rates = spark.readStream.format("rate").load
val plan = rates.queryExecution.logical
scala> println(plan.numberedTreeString)
00 StreamingRelationV2 org.apache.spark.sql.execution.streaming.sources.RateStreamProvider@2ed03b1a, rate, [timestamp#12, value#13L]
```

### V1 Code Path

For all other types of streaming data sources, `DataStreamReader.load` creates a logical query plan with a [StreamingRelation](logical-operators/StreamingRelation.md) leaf logical operator. That is the former **V1 code path**.

```text
// text data source is V1
val texts = spark.readStream.format("text").load("data/streaming")
val plan = texts.queryExecution.logical
scala> println(plan.numberedTreeString)
00 StreamingRelation DataSource(org.apache.spark.sql.SparkSession@35edd886,text,List(),None,List(),None,Map(path -> data/streaming),None), FileSource[data/streaming], [value#18]
```

## <span id="dataset"> Dataset API &mdash; High-Level DSL to Build Logical Query Plan

With a streaming Dataset created, you can now use all the methods of `Dataset` API, including but not limited to the following operators:

* [Dataset.dropDuplicates](operators/dropDuplicates.md) for streaming deduplication

* [Dataset.groupBy](operators/groupBy.md) and [Dataset.groupByKey](operators/groupByKey.md) for streaming aggregation

* [Dataset.withWatermark](operators/withWatermark.md) for event time watermark

Please note that a streaming Dataset is a regular Dataset (_with some streaming-related limitations_).

```text
val rates = spark
  .readStream
  .format("rate")
  .load
val countByTime = rates
  .withWatermark("timestamp", "10 seconds")
  .groupBy($"timestamp")
  .agg(count("*") as "count")

import org.apache.spark.sql.Dataset
assert(countByTime.isInstanceOf[Dataset[_]])
```

The point is to understand that the Dataset API is a domain-specific language (DSL) to build a more sophisticated stream processing pipeline that you could also build using the low-level logical operators directly.

Use [Dataset.explain](operators/explain.md) to learn the underlying logical and physical query plans.

```text
assert(countByTime.isStreaming)

scala> countByTime.explain(extended = true)
== Parsed Logical Plan ==
'Aggregate ['timestamp], [unresolvedalias('timestamp, None), count(1) AS count#131L]
+- EventTimeWatermark timestamp#88: timestamp, interval 10 seconds
   +- StreamingRelationV2 org.apache.spark.sql.execution.streaming.sources.RateStreamProvider@2fcb3082, rate, [timestamp#88, value#89L]

== Analyzed Logical Plan ==
timestamp: timestamp, count: bigint
Aggregate [timestamp#88-T10000ms], [timestamp#88-T10000ms, count(1) AS count#131L]
+- EventTimeWatermark timestamp#88: timestamp, interval 10 seconds
   +- StreamingRelationV2 org.apache.spark.sql.execution.streaming.sources.RateStreamProvider@2fcb3082, rate, [timestamp#88, value#89L]

== Optimized Logical Plan ==
Aggregate [timestamp#88-T10000ms], [timestamp#88-T10000ms, count(1) AS count#131L]
+- EventTimeWatermark timestamp#88: timestamp, interval 10 seconds
   +- Project [timestamp#88]
      +- StreamingRelationV2 org.apache.spark.sql.execution.streaming.sources.RateStreamProvider@2fcb3082, rate, [timestamp#88, value#89L]

== Physical Plan ==
*(5) HashAggregate(keys=[timestamp#88-T10000ms], functions=[count(1)], output=[timestamp#88-T10000ms, count#131L])
+- StateStoreSave [timestamp#88-T10000ms], state info [ checkpoint = <unknown>, runId = 28606ba5-9c7f-4f1f-ae41-e28d75c4d948, opId = 0, ver = 0, numPartitions = 200], Append, 0, 2
   +- *(4) HashAggregate(keys=[timestamp#88-T10000ms], functions=[merge_count(1)], output=[timestamp#88-T10000ms, count#136L])
      +- StateStoreRestore [timestamp#88-T10000ms], state info [ checkpoint = <unknown>, runId = 28606ba5-9c7f-4f1f-ae41-e28d75c4d948, opId = 0, ver = 0, numPartitions = 200], 2
         +- *(3) HashAggregate(keys=[timestamp#88-T10000ms], functions=[merge_count(1)], output=[timestamp#88-T10000ms, count#136L])
            +- Exchange hashpartitioning(timestamp#88-T10000ms, 200)
               +- *(2) HashAggregate(keys=[timestamp#88-T10000ms], functions=[partial_count(1)], output=[timestamp#88-T10000ms, count#136L])
                  +- EventTimeWatermark timestamp#88: timestamp, interval 10 seconds
                     +- *(1) Project [timestamp#88]
                        +- StreamingRelation rate, [timestamp#88, value#89L]
```

Or go pro and talk to `QueryExecution` directly.

```text
val plan = countByTime.queryExecution.logical
scala> println(plan.numberedTreeString)
00 'Aggregate ['timestamp], [unresolvedalias('timestamp, None), count(1) AS count#131L]
01 +- EventTimeWatermark timestamp#88: timestamp, interval 10 seconds
02    +- StreamingRelationV2 org.apache.spark.sql.execution.streaming.sources.RateStreamProvider@2fcb3082, rate, [timestamp#88, value#89L]
```

Please note that most of the stream processing operators you may also have used in batch structured queries in Spark SQL. Again, the distinction between Spark SQL and Spark Structured Streaming is very thin from a developer's point of view.

## <span id="DataStreamWriter"> DataStreamWriter and Streaming Data Sink

Once you're satisfied with building a stream processing pipeline (using the APIs of [DataStreamReader](#DataStreamReader), [Dataset](operators/), `RelationalGroupedDataset` and `KeyValueGroupedDataset`), you should define how and when the result of the streaming query is persisted in (_sent out to_) an external data system using a [streaming sink](Sink.md).

You should use [Dataset.writeStream](operators/writeStream.md) method that simply creates a [DataStreamWriter](DataStreamWriter.md).

```text
// Not only is this a Dataset, but it is also streaming
assert(countByTime.isStreaming)

val writer = countByTime.writeStream

import org.apache.spark.sql.streaming.DataStreamWriter
assert(writer.isInstanceOf[DataStreamWriter[_]])
```

The fluent API of `DataStreamWriter` allows you to describe the output data sink ([DataStreamWriter.format](DataStreamWriter.md#format) and [DataStreamWriter.options](DataStreamWriter.md#options)) using method chaining (with the goal of making the readability of the source code close to that of ordinary written prose, essentially creating a domain-specific language within the interface. See [Fluent interface](https://en.wikipedia.org/wiki/Fluent_interface) article in Wikipedia).

```text
writer
  .format("csv")
  .option("delimiter", "\t")
```

Like in [DataStreamReader](#DataStreamReader) data source formats, there are a couple of built-in data sink formats. Unlike data source formats, their names do not have corresponding `DataStreamWriter` methods. The reason is that you will use [DataStreamWriter.start](DataStreamWriter.md#start) to create and immediately start a [StreamingQuery](StreamingQuery.md).

There are however two special output formats that do have corresponding `DataStreamWriter` methods, i.e. [DataStreamWriter.foreach](DataStreamWriter.md#foreach) and [DataStreamWriter.foreachBatch](DataStreamWriter.md#foreachBatch), that allow for persisting query results to external data systems that do not have streaming sinks available. They give you a trade-off between developing a full-blown streaming sink and simply using the methods (that lay the basis of what a custom sink would have to do anyway).

`DataStreamWriter` API defines two new concepts (that are not available in the "base" Spark SQL):

* [OutputMode](OutputMode.md) that you specify using [DataStreamWriter.outputMode](DataStreamWriter.md#outputMode) method

* [Trigger](Trigger.md) that you specify using [DataStreamWriter.trigger](DataStreamWriter.md#trigger) method

You may also want to give a streaming query a name using [DataStreamWriter.queryName](DataStreamWriter.md#queryName) method.

In the end, you use [DataStreamWriter.start](DataStreamWriter.md#start) method to create and immediately start a [StreamingQuery](StreamingQuery.md).

```text
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.streaming.Trigger
import scala.concurrent.duration._
val sq = writer
  .format("console")
  .option("truncate", false)
  .option("checkpointLocation", "/tmp/csv-to-csv-checkpoint")
  .outputMode(OutputMode.Append)
  .trigger(Trigger.ProcessingTime(30.seconds))
  .queryName("csv-to-csv")
  .start("/tmp")

import org.apache.spark.sql.streaming.StreamingQuery
assert(sq.isInstanceOf[StreamingQuery])
```

When `DataStreamWriter` is requested to [start a streaming query](DataStreamWriter.md#start), it allows for the following data source formats:

* **memory** with [MemorySinkV2](spark-sql-streaming-MemorySinkV2.md) (with [ContinuousTrigger](Trigger.md#ContinuousTrigger)) or [MemorySink](spark-sql-streaming-MemorySink.md)

* **foreach** with [ForeachWriterProvider](datasources/ForeachWriterProvider.md) sink

* **foreachBatch** with [ForeachBatchSink](datasources/ForeachBatchSink.md) sink (that does not support [ContinuousTrigger](Trigger.md#ContinuousTrigger))

* Any `DataSourceRegister` data source

* Custom data sources specified by their fully-qualified class names or **[name].DefaultSource**

* **avro**, **kafka** _and some others_ (see `DataSource.lookupDataSource` object method)

* `DataSource` is requested to [create a streaming sink](DataSource.md#createSink) that accepts [StreamSinkProvider](StreamSinkProvider.md) or `FileFormat` data sources only

With a streaming sink, `DataStreamWriter` requests the [StreamingQueryManager](#StreamingQueryManager) to [start a streaming query](StreamingQueryManager.md#startQuery).

## <span id="StreamingQuery"> StreamingQuery

When a stream processing pipeline is started (using [DataStreamWriter.start](DataStreamWriter.md#start) method), `DataStreamWriter` creates a [StreamingQuery](StreamingQuery.md) and requests the [StreamingQueryManager](#StreamingQueryManager) to [start a streaming query](StreamingQueryManager.md#startQuery).

## <span id="StreamingQueryManager"> StreamingQueryManager

[StreamingQueryManager](StreamingQueryManager.md) is used to manage streaming queries.
