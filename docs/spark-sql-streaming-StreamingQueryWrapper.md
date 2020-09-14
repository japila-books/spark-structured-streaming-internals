== [[StreamingQueryWrapper]] StreamingQueryWrapper -- Serializable StreamExecution

[[creating-instance]][[_streamingQuery]]
`StreamingQueryWrapper` is a serializable interface of a <<spark-sql-streaming-StreamExecution.md#, StreamExecution>>.

.Demo: Any Streaming Query is StreamingQueryWrapper
[source, scala]
----
import org.apache.spark.sql.execution.streaming.StreamingQueryWrapper
val query = spark
  .readStream
  .format("rate")
  .load
  .writeStream
  .format("memory")
  .queryName("rate2memory")
  .start
assert(query.isInstanceOf[StreamingQueryWrapper])
----

[[stop]][[explainInternal]]
`StreamingQueryWrapper` has the same <<spark-sql-streaming-StreamExecution.md#, StreamExecution>> API and simply passes all the method calls along to the underlying <<_streamingQuery, StreamExecution>>.

`StreamingQueryWrapper` is <<creating-instance, created>> when `StreamingQueryManager` is requested to <<spark-sql-streaming-StreamingQueryManager.md#createQuery, create a streaming query>> (when `DataStreamWriter` is requested to [start an execution of the streaming query](DataStreamWriter.md#start)).
