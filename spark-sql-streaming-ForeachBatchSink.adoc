== [[ForeachBatchSink]] ForeachBatchSink

`ForeachBatchSink` is a <<spark-sql-streaming-Sink.adoc#, streaming sink>> that is used for the <<spark-sql-streaming-DataStreamWriter.adoc#foreachBatch, DataStreamWriter.foreachBatch>> streaming operator.

`ForeachBatchSink` is <<creating-instance, created>> exclusively when `DataStreamWriter` is requested to <<spark-sql-streaming-DataStreamWriter.adoc#start, start execution of the streaming query>> (with the <<spark-sql-streaming-DataStreamWriter.adoc#foreachBatch, foreachBatch>> source).

[[toString]]
`ForeachBatchSink` uses *ForeachBatchSink* name.

[source, scala]
----
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
----

NOTE: `ForeachBatchSink` was added in Spark 2.4.0 as part of https://issues.apache.org/jira/browse/SPARK-24565[SPARK-24565 Add API for in Structured Streaming for exposing output rows of each microbatch as a DataFrame].

=== [[creating-instance]] Creating ForeachBatchSink Instance

`ForeachBatchSink` takes the following when created:

* [[batchWriter]] Batch writer (`(Dataset[T], Long) => Unit`)
* [[encoder]] Encoder (`ExpressionEncoder[T]`)

=== [[addBatch]] Adding Batch -- `addBatch` Method

[source, scala]
----
addBatch(batchId: Long, data: DataFrame): Unit
----

NOTE: `addBatch` is a part of <<spark-sql-streaming-Sink.adoc#addBatch, Sink Contract>> to "add" a batch of data to the sink.

`addBatch`...FIXME
