# Spark Structured Streaming and Streaming Queries

**Spark Structured Streaming** (_Structured Streaming_ or _Spark Streams_) is the module of Apache Spark for stream processing using **streaming queries**.

Streaming queries can be expressed using a [high-level declarative streaming API](operators/) (_Dataset API_) or good ol' SQL (_SQL over stream_ / _streaming SQL_). The declarative streaming Dataset API and SQL are executed on the underlying highly-optimized Spark SQL engine.

The semantics of the Structured Streaming model is as follows (see the article [Structured Streaming In Apache Spark](https://databricks.com/blog/2016/07/28/structured-streaming-in-apache-spark.html)):

> At any time, the output of a continuous application is equivalent to executing a batch job on a prefix of the data.

As of Spark 2.2.0, Structured Streaming has been marked stable and ready for production use. With that the other older streaming module **Spark Streaming** is considered obsolete and not for developing new streaming applications with Apache Spark.

Spark Structured Streaming comes with two [stream execution engines](StreamExecution.md) for executing streaming queries:

* [MicroBatchExecution](micro-batch-execution/MicroBatchExecution.md) for [Micro-Batch Stream Processing](micro-batch-execution/index.md)

* [ContinuousExecution](continuous-execution/ContinuousExecution.md) for [Continuous Stream Processing](continuous-execution/index.md)

The goal of Spark Structured Streaming is to unify streaming, interactive, and batch queries over structured datasets for developing end-to-end stream processing applications dubbed **continuous applications** using Spark SQL's Datasets API with additional support for the following features:

* [Streaming Aggregation](streaming-aggregation/index.md)

* [Streaming Join](streaming-join/index.md)

* [Streaming Watermark](streaming-watermark/index.md)

* [Arbitrary Stateful Streaming Aggregation](arbitrary-stateful-streaming-aggregation/index.md)

* [Stateful Stream Processing](stateful-stream-processing/index.md)

In Structured Streaming, Spark developers describe custom streaming computations in the same way as with Spark SQL. Internally, Structured Streaming applies the user-defined structured query to the continuously and indefinitely arriving data to analyze real-time streaming data.

Structured Streaming introduces the concept of **streaming datasets** that are _infinite datasets_ with primitives like input [streaming data sources](Source.md) and output [streaming data sinks](Sink.md).

A `Dataset` is **streaming** when its logical plan is streaming.

```scala
val batchQuery = spark.
  read. // <-- batch non-streaming query
  csv("sales")

assert(batchQuery.isStreaming == false)

val streamingQuery = spark.
  readStream. // <-- streaming query
  format("rate").
  load

assert(streamingQuery.isStreaming)
```

!!! note
    Read up on Spark SQL, Datasets and logical plans in [The Internals of Spark SQL]({{ book.spark_sql }}/) online book.

Structured Streaming models a stream of data as an infinite (and hence continuous) table that could be changed every streaming batch.

You can specify [output mode](OutputMode.md) of a streaming dataset which is what gets written to a streaming sink (i.e. the infinite result table) when there is a new data available.

Streaming Datasets use **streaming query plans** (as opposed to regular batch Datasets that are based on batch query plans).

!!! note
    From this perspective, batch queries can be considered streaming Datasets executed once only (and is why some batch queries, e.g. [KafkaSource](datasources/kafka/KafkaSource.md), can easily work in batch mode).

    ```scala
    val batchQuery = spark.read.format("rate").load

    assert(batchQuery.isStreaming == false)

    val streamingQuery = spark.readStream.format("rate").load

    assert(streamingQuery.isStreaming)
    ```

With Structured Streaming, Spark 2 aims at simplifying **streaming analytics** with little to no need to reason about effective data streaming (trying to hide the unnecessary complexity in your streaming analytics architectures).

Structured streaming is defined by the following data abstractions in `org.apache.spark.sql.streaming` package:

* [StreamingQuery](StreamingQuery.md)
* [Streaming Source](Source.md)
* [Streaming Sink](Sink.md)
* [StreamingQueryManager](StreamingQueryManager.md)

Structured Streaming follows micro-batch model and periodically fetches data from the data source (and uses the `DataFrame` data abstraction to represent the fetched data for a certain batch).

With Datasets as Spark SQL's view of structured data, structured streaming checks input sources for new data every [trigger](Trigger.md) (time) and executes the (continuous) queries.

!!! note
    The feature has also been called **Streaming Spark SQL Query**, **Streaming DataFrames**, **Continuous DataFrame** or **Continuous Query**. There have been lots of names before the Spark project settled on Structured Streaming.

## References

### Articles

* [SPARK-8360 Structured Streaming (aka Streaming DataFrames)](https://issues.apache.org/jira/browse/SPARK-8360)
* The official [Structured Streaming Programming Guide](http://spark.apache.org/docs/latest/structured-streaming-programming-guide.html)
* [Structured Streaming In Apache Spark](https://databricks.com/blog/2016/07/28/structured-streaming-in-apache-spark.html)
* [What Spark's Structured Streaming really means](http://www.infoworld.com/article/3052924/analytics/what-sparks-structured-streaming-really-means.html)

### Videos

* [The Future of Real Time in Spark](https://youtu.be/oXkxXDG0gNk) from Spark Summit East 2016 in which Reynold Xin presents the concept of **Streaming DataFrames**
* [Structuring Spark: DataFrames, Datasets, and Streaming](https://youtu.be/i7l3JQRx7Qw?t=19m15s)
* [A Deep Dive Into Structured Streaming](https://youtu.be/rl8dIzTpxrI) by Tathagata "TD" Das from Spark Summit 2016
* [Arbitrary Stateful Aggregations in Structured Streaming in Apache Spark](https://youtu.be/rl8dIzTpxrI) by Burak Yavuz
