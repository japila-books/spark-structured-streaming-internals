== [[window]] window Function -- Stream Time Windows

`window` is a standard function that generates *tumbling*, *sliding* or *delayed* stream time window ranges (on a timestamp column).

[source, scala]
----
window(
  timeColumn: Column,
  windowDuration: String): Column  // <1>
window(
  timeColumn: Column,
  windowDuration: String,
  slideDuration: String): Column   // <2>
window(
  timeColumn: Column,
  windowDuration: String,
  slideDuration: String,
  startTime: String): Column       // <3>
----
<1> Creates a tumbling time window with `slideDuration` as `windowDuration` and `0 second` for `startTime`
<2> Creates a sliding time window with `0 second` for `startTime`
<3> Creates a delayed time window

[NOTE]
====
From https://msdn.microsoft.com/en-us/library/azure/dn835055.aspx[Tumbling Window (Azure Stream Analytics)]:

> *Tumbling windows* are a series of fixed-sized, non-overlapping and contiguous time intervals.
====

[NOTE]
====
From https://flink.apache.org/news/2015/12/04/Introducing-windows.html[Introducing Stream Windows in Apache Flink]:

> *Tumbling windows* group elements of a stream into finite sets where each set corresponds to an interval.

> *Tumbling windows* discretize a stream into non-overlapping windows.
====

[source, scala]
----
scala> val timeColumn = window($"time", "5 seconds")
timeColumn: org.apache.spark.sql.Column = timewindow(time, 5000000, 5000000, 0) AS `window`
----

`timeColumn` should be of `TimestampType`, i.e. with https://docs.oracle.com/javase/8/docs/api/java/sql/Timestamp.html[java.sql.Timestamp] values.

TIP: Use link:++https://docs.oracle.com/javase/8/docs/api/java/sql/Timestamp.html#from-java.time.Instant-++[java.sql.Timestamp.from] or link:++https://docs.oracle.com/javase/8/docs/api/java/sql/Timestamp.html#valueOf-java.time.LocalDateTime-++[java.sql.Timestamp.valueOf] factory methods to create `Timestamp` instances.

[source, scala]
----
// https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html
import java.time.LocalDateTime
// https://docs.oracle.com/javase/8/docs/api/java/sql/Timestamp.html
import java.sql.Timestamp
val levels = Seq(
  // (year, month, dayOfMonth, hour, minute, second)
  ((2012, 12, 12, 12, 12, 12), 5),
  ((2012, 12, 12, 12, 12, 14), 9),
  ((2012, 12, 12, 13, 13, 14), 4),
  ((2016, 8,  13, 0, 0, 0), 10),
  ((2017, 5,  27, 0, 0, 0), 15)).
  map { case ((yy, mm, dd, h, m, s), a) => (LocalDateTime.of(yy, mm, dd, h, m, s), a) }.
  map { case (ts, a) => (Timestamp.valueOf(ts), a) }.
  toDF("time", "level")
scala> levels.show
+-------------------+-----+
|               time|level|
+-------------------+-----+
|2012-12-12 12:12:12|    5|
|2012-12-12 12:12:14|    9|
|2012-12-12 13:13:14|    4|
|2016-08-13 00:00:00|   10|
|2017-05-27 00:00:00|   15|
+-------------------+-----+

val q = levels.select(window($"time", "5 seconds"), $"level")
scala> q.show(truncate = false)
+---------------------------------------------+-----+
|window                                       |level|
+---------------------------------------------+-----+
|[2012-12-12 12:12:10.0,2012-12-12 12:12:15.0]|5    |
|[2012-12-12 12:12:10.0,2012-12-12 12:12:15.0]|9    |
|[2012-12-12 13:13:10.0,2012-12-12 13:13:15.0]|4    |
|[2016-08-13 00:00:00.0,2016-08-13 00:00:05.0]|10   |
|[2017-05-27 00:00:00.0,2017-05-27 00:00:05.0]|15   |
+---------------------------------------------+-----+

scala> q.printSchema
root
 |-- window: struct (nullable = true)
 |    |-- start: timestamp (nullable = true)
 |    |-- end: timestamp (nullable = true)
 |-- level: integer (nullable = false)

// calculating the sum of levels every 5 seconds
val sums = levels.
  groupBy(window($"time", "5 seconds")).
  agg(sum("level") as "level_sum").
  select("window.start", "window.end", "level_sum")
scala> sums.show
+-------------------+-------------------+---------+
|              start|                end|level_sum|
+-------------------+-------------------+---------+
|2012-12-12 13:13:10|2012-12-12 13:13:15|        4|
|2012-12-12 12:12:10|2012-12-12 12:12:15|       14|
|2016-08-13 00:00:00|2016-08-13 00:00:05|       10|
|2017-05-27 00:00:00|2017-05-27 00:00:05|       15|
+-------------------+-------------------+---------+
----

`windowDuration` and `slideDuration` are strings specifying the width of the window for duration and sliding identifiers, respectively.

TIP: Use `CalendarInterval` for valid window identifiers.

There are a couple of rules governing the durations:

1. The window duration must be greater than 0

1. The slide duration must be greater than 0.

1. The start time must be greater than or equal to 0.

1. The slide duration must be less than or equal to the window duration.

1. The start time must be less than the slide duration.

NOTE: Only one `window` expression is supported in a query.

NOTE: `null` values are filtered out in `window` expression.

Internally, `window` creates a link:spark-sql-Column.md[Column] with `TimeWindow` Catalyst expression under `window` alias.

[source, scala]
----
scala> val timeColumn = window($"time", "5 seconds")
timeColumn: org.apache.spark.sql.Column = timewindow(time, 5000000, 5000000, 0) AS `window`

val windowExpr = timeColumn.expr
scala> println(windowExpr.numberedTreeString)
00 timewindow('time, 5000000, 5000000, 0) AS window#23
01 +- timewindow('time, 5000000, 5000000, 0)
02    +- 'time
----

Internally, `TimeWindow` Catalyst expression is simply a struct type with two fields, i.e. `start` and `end`, both of `TimestampType` type.

[source, scala]
----
scala> println(windowExpr.dataType)
StructType(StructField(start,TimestampType,true), StructField(end,TimestampType,true))

scala> println(windowExpr.dataType.prettyJson)
{
  "type" : "struct",
  "fields" : [ {
    "name" : "start",
    "type" : "timestamp",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "end",
    "type" : "timestamp",
    "nullable" : true,
    "metadata" : { }
  } ]
}
----

[NOTE]
====
`TimeWindow` time window Catalyst expression is planned (i.e. _converted_) in `TimeWindowing` logical optimization rule (i.e. `Rule[LogicalPlan]`) of the Spark SQL logical query plan analyzer.

Find more about the Spark SQL logical query plan analyzer in https://jaceklaskowski.gitbooks.io/mastering-apache-spark/spark-sql-Analyzer.html[Mastering Apache Spark 2] gitbook.
====

==== [[window-example]] Example -- Traffic Sensor

NOTE: The example is borrowed from https://flink.apache.org/news/2015/12/04/Introducing-windows.html[Introducing Stream Windows in Apache Flink].

The example shows how to use `window` function to model a traffic sensor that counts every 15 seconds the number of vehicles passing a certain location.
