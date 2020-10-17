# StreamingQueryWrapper &mdash; Serializable StreamExecution

<span id="_streamingQuery">
`StreamingQueryWrapper` is a serializable interface of a [StreamExecution](StreamExecution.md).

`StreamingQueryWrapper` has the same [StreamExecution](StreamExecution.md) API and simply passes all the method calls along to the underlying [StreamExecution](#_streamingQuery).

`StreamingQueryWrapper` is created when `StreamingQueryManager` is requested to [create a streaming query](StreamingQueryManager.md#createQuery) (when `DataStreamWriter` is requested to [start an execution of the streaming query](DataStreamWriter.md#start)).

## Demo: Any Streaming Query is StreamingQueryWrapper

```scala
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
```
