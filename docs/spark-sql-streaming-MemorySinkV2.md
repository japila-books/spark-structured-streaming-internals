== [[MemorySinkV2]] MemorySinkV2 -- Writable Streaming Sink for Continuous Stream Processing

`MemorySinkV2` is a `DataSourceV2` with <<spark-sql-streaming-StreamWriteSupport.md#, StreamWriteSupport>> for *memory* data source format in <<spark-sql-streaming-continuous-stream-processing.md#, Continuous Stream Processing>>.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-DataSourceV2.html[DataSourceV2 Contract] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

`MemorySinkV2` is a custom <<spark-sql-streaming-MemorySinkBase.md#, MemorySinkBase>>.

[[createStreamWriter]]
When requested for a <<spark-sql-streaming-StreamWriteSupport.md#createStreamWriter, StreamWriter>>, `MemorySinkV2` simply creates a <<spark-sql-streaming-MemoryStreamWriter.md#, MemoryStreamWriter>>.
