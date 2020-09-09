== [[DataStreamWriter]] DataStreamWriter -- Writing Datasets To Streaming Sink

`DataStreamWriter` is the <<methods, interface>> to describe when and what rows of a streaming query are sent out to the <<format, streaming sink>>.

`DataStreamWriter` is available using <<spark-sql-streaming-Dataset-operators.adoc#writeStream, Dataset.writeStream>> method (on a streaming query).

[source, scala]
----
import org.apache.spark.sql.streaming.DataStreamWriter
import org.apache.spark.sql.Row

val streamingQuery: Dataset[Long] = ...

assert(streamingQuery.isStreaming)

val writer: DataStreamWriter[Row] = streamingQuery.writeStream
----

[[methods]]
.DataStreamWriter's Methods
[cols="1m,3",options="header",width="100%"]
|===
| Method
| Description

| <<foreach, foreach>>
a|

[source, scala]
----
foreach(writer: ForeachWriter[T]): DataStreamWriter[T]
----

Sets link:spark-sql-streaming-ForeachWriter.adoc[ForeachWriter] in the full control of streaming writes

| foreachBatch
a| [[foreachBatch]]

[source, scala]
----
foreachBatch(
  function: (Dataset[T], Long) => Unit): DataStreamWriter[T]
----

(*New in 2.4.0*) Sets the <<source, source>> to `foreachBatch` and the <<foreachBatchWriter, foreachBatchWriter>> to the given function.

As per https://issues.apache.org/jira/browse/SPARK-24565[SPARK-24565 Add API for in Structured Streaming for exposing output rows of each microbatch as a DataFrame], the purpose of the method is to expose the micro-batch output as a dataframe for the following:

* Pass the output rows of each batch to a library that is designed for the batch jobs only
* Reuse batch data sources for output whose streaming version does not exist
* Multi-writes where the output rows are written to multiple outputs by writing twice for every batch

| format
a| [[format]]

[source, scala]
----
format(source: String): DataStreamWriter[T]
----

Specifies the format of the <<source, data sink>> (aka _output format_)

The format is used internally as the name (_alias_) of the <<spark-sql-streaming-Sink.adoc#, streaming sink>> to use to write the data to

| <<option, option>>
a|

[source, scala]
----
option(key: String, value: Boolean): DataStreamWriter[T]
option(key: String, value: Double): DataStreamWriter[T]
option(key: String, value: Long): DataStreamWriter[T]
option(key: String, value: String): DataStreamWriter[T]
----

| options
a| [[options]]

[source, scala]
----
options(options: Map[String, String]): DataStreamWriter[T]
----

Specifies the configuration options of a data sink

NOTE: You could use <<option, option>> method if you prefer specifying the options one by one or there is only one in use.

| <<outputMode, outputMode>>
a|

[source, scala]
----
outputMode(outputMode: OutputMode): DataStreamWriter[T]
outputMode(outputMode: String): DataStreamWriter[T]
----

Specifies the <<spark-sql-streaming-OutputMode.adoc#, output mode>>

| partitionBy
a| [[partitionBy]]

[source, scala]
----
partitionBy(colNames: String*): DataStreamWriter[T]
----

| <<queryName, queryName>>
a|

[source, scala]
----
queryName(queryName: String): DataStreamWriter[T]
----

Assigns the name of a query

| <<start, start>>
a|

[source, scala]
----
start(): StreamingQuery
start(path: String): StreamingQuery // <1>
----
<1> Explicit `path` (that could also be specified as an <<option, option>>)

Creates and immediately starts a <<spark-sql-streaming-StreamingQuery.adoc#, StreamingQuery>>

| <<trigger, trigger>>
a|

[source, scala]
----
trigger(trigger: Trigger): DataStreamWriter[T]
----

Sets the link:spark-sql-streaming-Trigger.adoc[Trigger] for how often a streaming query should be executed and the result saved.

|===

[NOTE]
====
A streaming query is a link:spark-sql-Dataset.adoc[Dataset] with a link:spark-sql-LogicalPlan.adoc#isStreaming[streaming logical plan].

[source, scala]
----
import org.apache.spark.sql.streaming.Trigger
import scala.concurrent.duration._
import org.apache.spark.sql.DataFrame
val rates: DataFrame = spark.
  readStream.
  format("rate").
  load

scala> rates.isStreaming
res1: Boolean = true

scala> rates.queryExecution.logical.isStreaming
res2: Boolean = true
----
====

Like the batch `DataFrameWriter`, `DataStreamWriter` has a direct support for many <<writing-dataframes-to-files, file formats>> and <<format, an extension point to plug in new formats>>.

[source, scala]
----
// see above for writer definition

// Save dataset in JSON format
writer.format("json")
----

In the end, you start the actual continuous writing of the result of executing a `Dataset` to a sink using <<start, start>> operator.

[source, scala]
----
writer.save
----

Beside the above operators, there are the following to work with a `Dataset` as a whole.

NOTE: `hive` <<start, is not supported>> for streaming writing (and leads to a `AnalysisException`).

NOTE: `DataFrameWriter` is responsible for writing in a batch fashion.

=== [[option]] Specifying Write Option -- `option` Method

[source, scala]
----
option(key: String, value: String): DataStreamWriter[T]
option(key: String, value: Boolean): DataStreamWriter[T]
option(key: String, value: Long): DataStreamWriter[T]
option(key: String, value: Double): DataStreamWriter[T]
----

Internally, `option` adds the `key` and `value` to <<extraOptions, extraOptions>> internal option registry.

=== [[outputMode]] Specifying Output Mode -- `outputMode` Method

[source, scala]
----
outputMode(outputMode: String): DataStreamWriter[T]
outputMode(outputMode: OutputMode): DataStreamWriter[T]
----

`outputMode` specifies the link:spark-sql-streaming-OutputMode.adoc[output mode] of a streaming query, i.e.  what data is sent out to a link:spark-sql-streaming-Sink.adoc[streaming sink] when there is new data available in link:spark-sql-streaming-Source.adoc[streaming data sources].

NOTE: When not defined explicitly, `outputMode` defaults to <<spark-sql-streaming-OutputMode.adoc#Append, Append>> output mode.

`outputMode` can be specified by name or one of the <<spark-sql-streaming-OutputMode.adoc#, OutputMode>> values.

=== [[queryName]] Setting Query Name -- `queryName` method

[source, scala]
----
queryName(queryName: String): DataStreamWriter[T]
----

`queryName` sets the name of a link:spark-sql-streaming-StreamingQuery.adoc[streaming query].

Internally, it is just an additional <<option, option>> with the key `queryName`.

=== [[trigger]] Setting How Often to Execute Streaming Query -- `trigger` method

[source, scala]
----
trigger(trigger: Trigger): DataStreamWriter[T]
----

`trigger` method sets the time interval of the *trigger* (that executes a batch runner) for a streaming query.

NOTE: `Trigger` specifies how often results should be produced by a link:spark-sql-streaming-StreamingQuery.adoc[StreamingQuery]. See link:spark-sql-streaming-Trigger.adoc[Trigger].

The default trigger is link:spark-sql-streaming-Trigger.adoc#ProcessingTime[ProcessingTime(0L)] that runs a streaming query as often as possible.

TIP: Consult link:spark-sql-streaming-Trigger.adoc[Trigger] to learn about `Trigger` and `ProcessingTime` types.

=== [[start]] Creating and Starting Execution of Streaming Query -- `start` Method

[source, scala]
----
start(): StreamingQuery
start(path: String): StreamingQuery  // <1>
----
<1> Sets `path` option to `path` and passes the call on to `start()`

`start` starts a streaming query.

`start` gives a link:spark-sql-streaming-StreamingQuery.adoc[StreamingQuery] to control the execution of the continuous query.

NOTE: Whether or not you have to specify `path` option depends on the streaming sink in use.

Internally, `start` branches off per `source`.

* `memory`
* `foreach`
* other formats

...FIXME

[[start-options]]
.start's Options
[cols="1,2",options="header",width="100%"]
|===
| Option
| Description

| `queryName`
| Name of active streaming query

| [[checkpointLocation]] `checkpointLocation`
| Directory for checkpointing (and to store query metadata like offsets before and after being processed, the link:spark-sql-streaming-StreamExecution.adoc#id[query id], etc.)
|===

`start` reports a `AnalysisException` when `source` is `hive`.

[source, scala]
----
val q =  spark.
  readStream.
  text("server-logs/*").
  writeStream.
  format("hive") <-- hive format used as a streaming sink
scala> q.start
org.apache.spark.sql.AnalysisException: Hive data source can only be used with tables, you can not write files of Hive data source directly.;
  at org.apache.spark.sql.streaming.DataStreamWriter.start(DataStreamWriter.scala:234)
  ... 48 elided
----

NOTE: Define options using <<option, option>> or <<options, options>> methods.

=== [[foreach]] Making ForeachWriter in Charge of Streaming Writes -- `foreach` method

[source, scala]
----
foreach(writer: ForeachWriter[T]): DataStreamWriter[T]
----

`foreach` sets the input link:spark-sql-streaming-ForeachWriter.adoc[ForeachWriter] to be in control of streaming writes.

Internally, `foreach` sets the streaming output <<format, format>> as `foreach` and `foreachWriter` as the input `writer`.

NOTE: `foreach` uses `SparkSession` to access `SparkContext` to clean the `ForeachWriter`.

[NOTE]
====
`foreach` reports an `IllegalArgumentException` when `writer` is `null`.

```
foreach writer cannot be null
```
====

=== [[internal-properties]] Internal Properties

[cols="20m,20,60",options="header",width="100%"]
|===
| Name
| Initial Value
| Description

| extraOptions
|
| [[extraOptions]]

| foreachBatchWriter
| `null`
a| [[foreachBatchWriter]]

[source, scala]
----
foreachBatchWriter: (Dataset[T], Long) => Unit
----

The function that is used as the batch writer in the <<spark-sql-streaming-ForeachBatchSink.adoc#, ForeachBatchSink>> for <<foreachBatch, foreachBatch>>

| foreachWriter
|
| [[foreachWriter]]

| partitioningColumns
|
| [[partitioningColumns]]

| source
|
| [[source]]

| outputMode
| <<spark-sql-streaming-OutputMode.adoc#Append, Append>>
| [[outputMode-property]] link:spark-sql-streaming-OutputMode.adoc[OutputMode] of the streaming sink

Set using <<outputMode, outputMode>> method.

| trigger
|
| [[trigger-property]]
|===
