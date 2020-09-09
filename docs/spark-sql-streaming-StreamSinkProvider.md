== [[StreamSinkProvider]] StreamSinkProvider Contract

`StreamSinkProvider` is the <<contract, abstraction>> of <<implementations, providers>> that can <<createSink, create a streaming sink>> for a file format (e.g. `parquet`) or system (e.g. `kafka`).

IMPORTANT: <<spark-sql-streaming-StreamWriteSupport.adoc#, StreamWriteSupport>> is a newer version of `StreamSinkProvider` (aka `DataSource API V2`) and new data sources should use the contract instead.

[[contract]]
.StreamSinkProvider Contract
[cols="30m,70",options="header",width="100%"]
|===
| Method
| Description

| createSink
a| [[createSink]]

[source, scala]
----
createSink(
  sqlContext: SQLContext,
  parameters: Map[String, String],
  partitionColumns: Seq[String],
  outputMode: OutputMode): Sink
----

Creates a <<spark-sql-streaming-Sink.adoc#, streaming sink>>

Used exclusively when `DataSource` is requested for a <<spark-sql-streaming-DataSource.adoc#createSink, streaming sink>> (when `DataStreamWriter` is requested to <<spark-sql-streaming-DataStreamWriter.adoc#start, start a streaming query>>)

|===

[[implementations]]
NOTE: <<spark-sql-streaming-KafkaSourceProvider.adoc#, KafkaSourceProvider>> is the only known `StreamSinkProvider` in Spark Structured Streaming.
