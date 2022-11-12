# StreamSinkProvider

`StreamSinkProvider` is the <<contract, abstraction>> of <<implementations, providers>> that can <<createSink, create a streaming sink>> for a file format (e.g. `parquet`) or system (e.g. `kafka`).

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

Creates a [streaming sink](Sink.md)

Used when `DataSource` is requested for a [streaming sink](DataSource.md#createSink) (when `DataStreamWriter` is requested to [start a streaming query](DataStreamWriter.md#start))

|===

[[implementations]]
NOTE: [KafkaSourceProvider](kafka/KafkaSourceProvider.md) is the only known `StreamSinkProvider` in Spark Structured Streaming.
