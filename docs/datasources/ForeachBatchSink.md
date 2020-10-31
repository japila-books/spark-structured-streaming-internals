# ForeachBatchSink

`ForeachBatchSink` is a [streaming sink](../Sink.md) that is used for [DataStreamWriter.foreachBatch](../DataStreamWriter.md#foreachBatch) streaming operator.

`ForeachBatchSink` is <<creating-instance, created>> exclusively when `DataStreamWriter` is requested to [start execution of the streaming query](../DataStreamWriter.md#start) (with the [foreachBatch](../DataStreamWriter.md#foreachBatch) source).

[[toString]]
`ForeachBatchSink` uses *ForeachBatchSink* name.

```text
import org.apache.spark.sql.Dataset
val q = spark.readStream
  .format("rate")
  .load
  .writeStream
  .foreachBatch { (output: Dataset[_], batchId: Long) => // <-- creates a ForeachBatchSink
    println(s"Batch ID: $batchId")
    output.show
  }
  .start
// q.stop

scala> println(q.lastProgress.sink.description)
ForeachBatchSink
```

NOTE: `ForeachBatchSink` was added in Spark 2.4.0 as part of https://issues.apache.org/jira/browse/SPARK-24565[SPARK-24565 Add API for in Structured Streaming for exposing output rows of each microbatch as a DataFrame].

## Creating Instance

`ForeachBatchSink` takes the following when created:

* [[batchWriter]] Batch writer (`(Dataset[T], Long) => Unit`)
* [[encoder]] Encoder (`ExpressionEncoder[T]`)

## <span id="addBatch"> Adding Batch

```scala
addBatch(
  batchId: Long,
  data: DataFrame): Unit
```

`addBatch`...FIXME

`addBatch` is a part of the [Sink](../Sink.md#addBatch) abstraction.
