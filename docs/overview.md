# Spark Structured Streaming and Streaming Queries

**Spark Structured Streaming** (_Structured Streaming_ or _Spark Streams_) is the module of Apache Spark for stream processing using **streaming queries**.

Streaming queries can be expressed using a [high-level declarative streaming API](operators/index.md) (_Dataset API_) or good ol' SQL (_SQL over stream_ / _streaming SQL_). The declarative streaming Dataset API and SQL are executed on the underlying highly-optimized Spark SQL engine.

The semantics of the Structured Streaming model is as follows (see the article [Structured Streaming In Apache Spark](https://databricks.com/blog/2016/07/28/structured-streaming-in-apache-spark.html)):

> At any time, the output of a continuous application is equivalent to executing a batch job on a prefix of the data.

## Stream Execution Engines

Spark Structured Streaming comes with two [stream execution engines](StreamExecution.md) for executing streaming queries:

* [MicroBatchExecution](micro-batch-execution/MicroBatchExecution.md) for [Micro-Batch Stream Processing](micro-batch-execution/index.md)

* [ContinuousExecution](continuous-execution/ContinuousExecution.md) for [Continuous Stream Processing](continuous-execution/index.md)

## Features

* [Streaming Aggregation](streaming-aggregation/index.md)
* [Streaming Join](streaming-join/index.md)
* [Streaming Watermark](watermark/index.md)
* [Arbitrary Stateful Streaming Aggregation](arbitrary-stateful-streaming-aggregation/index.md)
* [Stateful Stream Processing](stateful-stream-processing/index.md)
* [Many more](features/index.md)

## Streaming Datasets

Structured Streaming introduces the concept of **Streaming Datasets** that are _infinite datasets_ with one or more [SparkDataStream](SparkDataStream.md)s.

A `Dataset` is streaming when its logical plan is streaming.

```scala
val batchQuery = spark.
  read. // <-- batch non-streaming query
  csv("sales")

assert(batchQuery.isStreaming == false)
```

```scala
val streamingQuery = spark.
  readStream. // <-- streaming query
  format("rate").
  load

assert(streamingQuery.isStreaming)
```

Structured Streaming models a stream of data as an infinite (and hence continuous) table that could be changed every streaming batch.

## Output Modes

You can specify [output mode](OutputMode.md) of a streaming dataset which is what gets written to a streaming sink (i.e. the infinite result table) when there is a new data available.

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
