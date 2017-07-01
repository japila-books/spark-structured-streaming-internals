== ForeachSink

`ForeachSink` is a typed link:spark-sql-streaming-Sink.adoc[streaming sink] that passes rows (of the type `T`) to link:spark-sql-streaming-ForeachWriter.adoc[ForeachWriter] (one record at a time per partition).

NOTE: `ForeachSink` is assigned a `ForeachWriter` when `DataStreamWriter` is link:spark-sql-streaming-DataStreamWriter.adoc#start[started].

`ForeachSink` is used exclusively in link:spark-sql-streaming-DataStreamWriter.adoc#foreach[foreach] operator.

[source, scala]
----
val records = spark.
  readStream
  format("text").
  load("server-logs/*.out").
  as[String]

import org.apache.spark.sql.ForeachWriter
val writer = new ForeachWriter[String] {
  override def open(partitionId: Long, version: Long) = true
  override def process(value: String) = println(value)
  override def close(errorOrNull: Throwable) = {}
}

records.writeStream
  .queryName("server-logs processor")
  .foreach(writer)
  .start
----

Internally, `addBatch` (the only method from the <<contract, Sink Contract>>) takes records from the input link:spark-sql-dataframe.adoc[DataFrame] (as `data`), transforms them to expected type `T` (of this `ForeachSink`) and (now as a link:spark-sql-dataset.adoc[Dataset]) link:spark-sql-dataset.adoc#foreachPartition[processes each partition].

[source, scala]
----
addBatch(batchId: Long, data: DataFrame): Unit
----

`addBatch` then opens the constructor's link:spark-sql-streaming-ForeachWriter.adoc[ForeachWriter] (for the link:spark-taskscheduler-taskcontext.adoc#getPartitionId[current partition] and the input batch) and passes the records to process (one at a time per partition).

CAUTION: FIXME Why does Spark track whether the writer failed or not? Why couldn't it `finally` and do `close`?

CAUTION: FIXME Can we have a constant for `"foreach"` for `source` in `DataStreamWriter`?
