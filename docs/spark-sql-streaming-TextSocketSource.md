# TextSocketSource

`TextSocketSource` is a [streaming source](Source.md) that reads lines from a socket at the `host` and `port` (defined by parameters).

It uses <<lines, lines>> internal in-memory buffer to keep all of the lines that were read from a socket forever.

[CAUTION]
====
This source is *not* for production use due to design contraints, e.g. infinite in-memory collection of lines read and no fault recovery.

It is designed only for tutorials and debugging.
====

[source, scala]
----
import org.apache.spark.sql.SparkSession
val spark: SparkSession = SparkSession.builder.getOrCreate()

// Connect to localhost:9999
// You can use "nc -lk 9999" for demos
val textSocket = spark.
  readStream.
  format("socket").
  option("host", "localhost").
  option("port", 9999).
  load

import org.apache.spark.sql.Dataset
val lines: Dataset[String] = textSocket.as[String].map(_.toUpperCase)

val query = lines.writeStream.format("console").start

// Start typing the lines in nc session
// They will appear UPPERCASE in the terminal

-------------------------------------------
Batch: 0
-------------------------------------------
+---------+
|    value|
+---------+
|UPPERCASE|
+---------+

scala> query.explain
== Physical Plan ==
*SerializeFromObject [staticinvoke(class org.apache.spark.unsafe.types.UTF8String, StringType, fromString, input[0, java.lang.String, true], true) AS value#21]
+- *MapElements <function1>, obj#20: java.lang.String
   +- *DeserializeToObject value#43.toString, obj#19: java.lang.String
      +- LocalTableScan [value#43]

scala> query.stop
----

=== [[lines]] lines Internal Buffer

[source, scala]
----
lines: ArrayBuffer[(String, Timestamp)]
----

`lines` is the internal buffer of all the lines `TextSocketSource` read from the socket.

=== [[getOffset]] Maximum Available Offset (getOffset method)

``TextSocketSource``'s offset can either be none or `LongOffset` of the number of lines in the internal <<lines, lines>> buffer.

`getOffset` is a part of the [Source](Source.md#getOffset) abstraction.

=== [[schema]] Schema (schema method)

`TextSocketSource` supports two spark-sql-schema.md[schemas]:

1. A single `value` field of String type.
2. `value` field of `StringType` type and `timestamp` field of spark-sql-DataType.md#TimestampType[TimestampType] type of format `yyyy-MM-dd HH:mm:ss`.

TIP: Refer to spark-sql-streaming-TextSocketSourceProvider.md#sourceSchema[sourceSchema] for `TextSocketSourceProvider`.

=== [[creating-instance]] Creating TextSocketSource Instance

[source, scala]
----
TextSocketSource(
  host: String,
  port: Int,
  includeTimestamp: Boolean,
  sqlContext: SQLContext)
----

When `TextSocketSource` is created (see spark-sql-streaming-TextSocketSourceProvider.md#createSource[TextSocketSourceProvider]), it gets 4 parameters passed in:

1. `host`
2. `port`
3. spark-sql-streaming-TextSocketSourceProvider.md#includeTimestamp[includeTimestamp] flag
4. spark-sql-sqlcontext.md[SQLContext]

CAUTION: It appears that the source did not get "renewed" to use spark-sql-sparksession.md[SparkSession] instead.

It opens a socket at given `host` and `port` parameters and reads a buffering character-input stream using the default charset and the default-sized input buffer (of `8192` bytes) line by line.

CAUTION: FIXME Review Java's `Charset.defaultCharset()`

It starts a `readThread` daemon thread (called `TextSocketSource(host, port)`) to read lines from the socket. The lines are added to the internal <<lines, lines>> buffer.

=== [[stop]] Stopping TextSocketSource (stop method)

When stopped, `TextSocketSource` closes the socket connection.
