# ForeachBatchSink

`ForeachBatchSink` is a [streaming sink](../Sink.md) that represents [DataStreamWriter.foreachBatch](../DataStreamWriter.md#foreachBatch) streaming operator at runtime.

??? note "Type Constructor"
    `ForeachBatchSink[T]` is a Scala type constructor with the type parameter `T`.

`ForeachBatchSink` was added in Spark 2.4.0 as part of [SPARK-24565 Add API for in Structured Streaming for exposing output rows of each microbatch as a DataFrame](https://issues.apache.org/jira/browse/SPARK-24565).

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

## Creating Instance

`ForeachBatchSink` takes the following when created:

* <span id="batchWriter"> Batch Writer Function (`(Dataset[T], Long) => Unit`)
* <span id="encoder"> Encoder of type `T` (`ExpressionEncoder[T]`)

`ForeachBatchSink` is created when `DataStreamWriter` is requested to [start execution of the streaming query](../DataStreamWriter.md#start) (with the [foreachBatch](../DataStreamWriter.md#foreachBatch) source) for [DataStreamWriter.foreachBatch](../DataStreamWriter.md#foreachBatch) streaming operator.

## <span id="addBatch"> Adding Batch

```scala
addBatch(
  batchId: Long,
  data: DataFrame): Unit
```

`addBatch` requests the [encoder](#encoder) to `resolveAndBind` (using the output of the analyzed logical plan of the given `DataFrame`) that creates a "resolved" encoder. `addBatch` requests the resolved encoder to create an `Deserializer` (to convert a Spark SQL `Row` objects into objects of type `T`).

`addBatch` requests the `QueryExecution` (of the given `DataFrame`) for `RDD[InternalRow]` (_executes the query plan_) and applies `map` operator to convert rows to Scala objects.

!!! important
    At this point the "old" `DataFrame` is no longer a `DataFrame` but an `RDD[InternalRow]`. One of the "side-effects" is that whatever logical and physical optimizations may have been applied to the given `DataFrame` it is over now.

`addBatch` creates a new `Dataset` (for the RDD) and executes [batchWriter](#batchWriter) function (passing the `Dataset` and the `batchId`).

`addBatch` is a part of the [Sink](../Sink.md#addBatch) abstraction.

## <span id="toString"> Text Representation

`ForeachBatchSink` uses **ForeachBatchSink** name.
