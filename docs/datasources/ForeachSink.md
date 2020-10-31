# ForeachSink

`ForeachSink` is a typed [streaming sink](../Sink.md) that passes rows (of the type `T`) to [ForeachWriter](ForeachWriter.md) (one record at a time per partition).

!!! note
    `ForeachSink` is assigned a `ForeachWriter` when `DataStreamWriter` is [started](../DataStreamWriter.md#start).

`ForeachSink` is used exclusively in [foreach](../DataStreamWriter.md#foreach) operator.

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

Internally, `addBatch` (the only method from the <<contract, Sink Contract>>) takes records from the input spark-sql-dataframe.md[DataFrame] (as `data`), transforms them to expected type `T` (of this `ForeachSink`) and (now as a spark-sql-dataset.md[Dataset]) spark-sql-dataset.md#foreachPartition[processes each partition].

[source, scala]
----
addBatch(batchId: Long, data: DataFrame): Unit
----

`addBatch` then opens the constructor's datasources/ForeachWriter.md[ForeachWriter] (for the spark-taskscheduler-taskcontext.md#getPartitionId[current partition] and the input batch) and passes the records to process (one at a time per partition).

CAUTION: FIXME Why does Spark track whether the writer failed or not? Why couldn't it `finally` and do `close`?

CAUTION: FIXME Can we have a constant for `"foreach"` for `source` in `DataStreamWriter`?
