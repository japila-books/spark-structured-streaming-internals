== [[MicroBatchWriter]] MicroBatchWriter -- Data Source Writer in Micro-Batch Stream Processing (Data Source API V2)

[[batchId]][[writer]][[creating-instance]][[commit]][[abort]]
`MicroBatchWriter` is a `DataSourceWriter` (Spark SQL) that uses the given batch ID as the epoch when requested to commit, abort and create a `WriterFactory` for a given <<spark-sql-streaming-StreamWriter.md#, StreamWriter>> in <<spark-sql-streaming-micro-batch-stream-processing.md#, Micro-Batch Stream Processing>>.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-DataSourceWriter.html[DataSourceWriter] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

`MicroBatchWriter` is part of the novel Data Source API V2 in Spark SQL.

`MicroBatchWriter` is <<creating-instance, created>> exclusively when `MicroBatchExecution` is requested to <<spark-sql-streaming-MicroBatchExecution.md#runBatch, run a streaming batch>> (with a <<spark-sql-streaming-StreamWriteSupport.md#, StreamWriteSupport>> streaming sink).
