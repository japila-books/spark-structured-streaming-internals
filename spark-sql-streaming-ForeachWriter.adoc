== [[ForeachWriter]] ForeachWriter

`ForeachWriter` is the <<contract, contract>> for a *foreach writer* that is a link:spark-sql-streaming-DataStreamWriter.adoc#foreach[streaming format] that controls streaming writes.

NOTE: `ForeachWriter` is set using link:spark-sql-streaming-DataStreamWriter.adoc#foreach[foreach] operator.

[source, scala]
----
val foreachWriter = new ForeachWriter[String] { ... }
streamingQuery.
  writeStream.
  foreach(foreachWriter).
  start
----

=== [[contract]] ForeachWriter Contract

[source, scala]
----
package org.apache.spark.sql

abstract class ForeachWriter[T] {
  def open(partitionId: Long, version: Long): Boolean
  def process(value: T): Unit
  def close(errorOrNull: Throwable): Unit
}
----

.ForeachWriter Contract
[cols="1,2",options="header",width="100%"]
|===
| Method
| Description

| [[open]] `open`
| Used when...

| [[process]] `process`
| Used when...

| [[close]] `close`
| Used when...
|===
