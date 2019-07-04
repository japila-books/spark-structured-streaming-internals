== [[EventTimeStatsAccum]] EventTimeStatsAccum Accumulator -- Event-Time Column Statistics for EventTimeWatermarkExec Physical Operator

[[event-time-statistics]]
`EventTimeStatsAccum` is a Spark accumulator that is used for the <<EventTimeStats, statistics of the event-time column>> (that <<spark-sql-streaming-EventTimeWatermarkExec.adoc#, EventTimeWatermarkExec>> physical operator uses for event-time watermark):

* [[max]] Maximum value
* [[min]] Minimum value
* [[avg]] Average value
* [[count]] Number of updates (count)

`EventTimeStatsAccum` is <<creating-instance, created>> and registered exclusively for <<spark-sql-streaming-EventTimeWatermarkExec.adoc#, EventTimeWatermarkExec>> physical operator.

[NOTE]
====
When `EventTimeWatermarkExec` physical operator is requested to <<spark-sql-streaming-EventTimeWatermarkExec.adoc#doExecute, execute and generate a recipe for a distributed computation (as a RDD[InternalRow])>>, every task simply <<add, adds>> the values of the <<spark-sql-streaming-EventTimeWatermarkExec.adoc#eventTime, event-time watermark column>> to the <<spark-sql-streaming-EventTimeWatermarkExec.adoc#eventTimeStats, EventTimeStatsAccum>> accumulator.

As per design of Spark accumulators in Apache Spark, accumulator updates are automatically sent out (_propagated_) from tasks to the driver every heartbeat and then they are accumulated together.
====

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-apache-spark/spark-accumulators.html[Accumulators] in https://bit.ly/apache-spark-internals[The Internals of Apache Spark] book.

[[currentStats]]
[[creating-instance]]
`EventTimeStatsAccum` takes a single <<EventTimeStats, EventTimeStats>> to be created (default: <<zero, zero>>).

=== [[add]] Accumulating Value -- `add` Method

[source, scala]
----
add(v: Long): Unit
----

NOTE: `add` is part of the `AccumulatorV2` Contract to add (_accumulate_) a given value.

`add` simply requests the <<currentStats, EventTimeStats>> to <<EventTimeStats-add, add>> the given `v` value.

NOTE: `add` is used exclusively when `EventTimeWatermarkExec` physical operator is requested to <<spark-sql-streaming-EventTimeWatermarkExec.adoc#doExecute, execute and generate a recipe for a distributed computation (as a RDD[InternalRow])>>.

=== [[EventTimeStats]] EventTimeStats

`EventTimeStats` is a Scala case class for the <<event-time-statistics, event-time column statistics>>.

[[zero]]
`EventTimeStats` defines a special value *zero* with the following values:

* `Long.MinValue` for the <<max, max>>
* `Long.MaxValue` for the <<min, min>>
* `0.0` for the <<avg, avg>>
* `0L` for the <<count, count>>

=== [[EventTimeStats-add]] `EventTimeStats.add` Method

[source, scala]
----
add(eventTime: Long): Unit
----

`add` simply updates the <<event-time-statistics, event-time column statistics>> per given `eventTime`.

NOTE: `add` is used exclusively when `EventTimeStatsAccum` is requested to <<add, accumulate the value of an event-time column>>.

=== [[EventTimeStats-merge]] `EventTimeStats.merge` Method

[source, scala]
----
merge(that: EventTimeStats): Unit
----

`merge`...FIXME

NOTE: `merge` is used when...FIXME
