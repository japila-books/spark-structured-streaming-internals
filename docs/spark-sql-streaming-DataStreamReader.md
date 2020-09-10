== [[DataStreamReader]] DataStreamReader -- Loading Data from Streaming Source

`DataStreamReader` is the <<methods, interface>> to describe how data is <<load, loaded>> to a streaming `Dataset` from a <<spark-sql-streaming-Source.md#, streaming source>>.

[[methods]]
.DataStreamReader's Methods
[cols="1,3",options="header",width="100%"]
|===
| Method
| Description

| <<csv, csv>>
a|

[source, scala]
----
csv(path: String): DataFrame
----

Sets `csv` as the <<format, format>> of the data source

| `format`
a| [[format]]

[source, scala]
----
format(source: String): DataStreamReader
----

Specifies the format of the <<source, data source>>

The format is used internally as the name (_alias_) of the <<spark-sql-streaming-Source.md#, streaming source>> to use to load the data

| <<json, json>>
a|

[source, scala]
----
json(path: String): DataFrame
----

Sets `json` as the <<format, format>> of the data source

| <<load-internals, load>>
a| [[load]]

[source, scala]
----
load(): DataFrame
load(path: String): DataFrame // <1>
----
<1> Explicit `path` (that could also be specified as an <<option, option>>)

Creates a streaming `DataFrame` that represents "loading" streaming data (and is internally a logical plan with a <<spark-sql-streaming-StreamingRelationV2.md#, StreamingRelationV2>> or <<spark-sql-streaming-StreamingRelation.md#, StreamingRelation>> leaf logical operators)

| <<option, option>>
a|

[source, scala]
----
option(key: String, value: Boolean): DataStreamReader
option(key: String, value: Double): DataStreamReader
option(key: String, value: Long): DataStreamReader
option(key: String, value: String): DataStreamReader
----

Sets a loading option

| `options`
a| [[options]]

[source, scala]
----
options(options: Map[String, String]): DataStreamReader
----

Specifies the configuration options of a data source

NOTE: You could use <<option, option>> method if you prefer specifying the options one by one or there is only one in use.

| <<orc, orc>>
a|

[source, scala]
----
orc(path: String): DataFrame
----

Sets `orc` as the <<format, format>> of the data source

| <<parquet, parquet>>
a|

[source, scala]
----
parquet(path: String): DataFrame
----

Sets `parquet` as the <<format, format>> of the data source

| `schema`
a| [[schema]]

[source, scala]
----
schema(schema: StructType): DataStreamReader
schema(schemaString: String): DataStreamReader // <1>
----
<1> Uses a DDL-formatted table schema

Specifies the <<userSpecifiedSchema, user-defined schema>> of the streaming data source (as a `StructType` or DDL-formatted table schema, e.g. `a INT, b STRING`)

| <<text, text>>
a|

[source, scala]
----
text(path: String): DataFrame
----

Sets `text` as the <<format, format>> of the data source

| <<textFile, textFile>>
a|

[source, scala]
----
textFile(path: String): Dataset[String]
----

|===

.DataStreamReader and The Others
image::images/DataStreamReader-SparkSession-StreamingRelation.png[align="center"]

`DataStreamReader` is used for a Spark developer to describe how Spark Structured Streaming loads datasets from a streaming source (that <<load, in the end>> creates a logical plan for a streaming query).

NOTE: `DataStreamReader` is the Spark developer-friendly API to create a link:spark-sql-streaming-StreamingRelation.md[StreamingRelation] logical operator (that represents a link:spark-sql-streaming-Source.md[streaming source] in a logical plan).

You can access `DataStreamReader` using `SparkSession.readStream` method.

[source, scala]
----
import org.apache.spark.sql.SparkSession
val spark: SparkSession = ...

val streamReader = spark.readStream
----

`DataStreamReader` supports many <<format, source formats>> natively and offers the <<format, interface to define custom formats>>:

* <<json, json>>
* <<csv, csv>>
* <<parquet, parquet>>
* <<text, text>>

NOTE: `DataStreamReader` assumes <<parquet, parquet>> file format by default that you can change using `spark.sql.sources.default` property.

NOTE: `hive` source format is not supported.

After you have described the *streaming pipeline* to read datasets from an external streaming data source, you eventually trigger the loading using format-agnostic <<load, load>> or format-specific (e.g. <<json, json>>, <<csv, csv>>) operators.

[[internal-properties]]
.DataStreamReader's Internal Properties (in alphabetical order)
[cols="1,1,2",options="header",width="100%"]
|===
| Name
| Initial Value
| Description

| [[source]] `source`
| `spark.sql.sources.default` property
| Source format of datasets in a streaming data source

| [[userSpecifiedSchema]] `userSpecifiedSchema`
| (empty)
| Optional user-defined schema

| [[extraOptions]] `extraOptions`
| (empty)
| Collection of key-value configuration options
|===

=== [[option]] Specifying Loading Options -- `option` Method

[source, scala]
----
option(key: String, value: String): DataStreamReader
option(key: String, value: Boolean): DataStreamReader
option(key: String, value: Long): DataStreamReader
option(key: String, value: Double): DataStreamReader
----

`option` family of methods specifies additional options to a streaming data source.

There is support for values of `String`, `Boolean`, `Long`, and `Double` types for user convenience, and internally are converted to `String` type.

Internally, `option` sets <<extraOptions, extraOptions>> internal property.

NOTE: You can also set options in bulk using <<options, options>> method. You have to do the type conversion yourself, though.

=== [[load-internals]] Creating Streaming Dataset (to Represent Loading Data From Streaming Source) -- `load` Method

[source, scala]
----
load(): DataFrame
load(path: String): DataFrame // <1>
----
<1> Specifies `path` option before passing the call to parameterless `load()`

`load`...FIXME

=== [[builtin-formats]][[json]][[csv]][[parquet]][[text]][[textFile]] Built-in Formats

[source, scala]
----
json(path: String): DataFrame
csv(path: String): DataFrame
parquet(path: String): DataFrame
text(path: String): DataFrame
textFile(path: String): Dataset[String] // <1>
----
<1> Returns `Dataset[String]` not `DataFrame`

`DataStreamReader` can load streaming datasets from data sources of the following <<format, formats>>:

* `json`
* `csv`
* `parquet`
* `text`

The methods simply pass calls to <<format, format>> followed by <<load, load(path)>>.
