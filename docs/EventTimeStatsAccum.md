# EventTimeStatsAccum Accumulator

`EventTimeStatsAccum` is an `AccumulatorV2` ([Spark Core]({{ book.spark_core }}/accumulators/AccumulatorV2/)) that accumulates `Long` values and produces an [EventTimeStats](#currentStats).

## Creating Instance

`EventTimeStatsAccum` takes the following to be created:

* <span id="currentStats"> [EventTimeStats](EventTimeStats.md) (default: [EventTimeStats.zero](EventTimeStats.md#zero))

`EventTimeStatsAccum` is created when [EventTimeWatermarkExec](physical-operators/EventTimeWatermarkExec.md) unary physical operator is created (and initializes [eventTimeStats](physical-operators/EventTimeWatermarkExec.md#eventTimeStats)).

 that is used for the <<EventTimeStats, statistics of the event-time column>> (that [EventTimeWatermarkExec](physical-operators/EventTimeWatermarkExec.md) physical operator uses for event-time watermark):

* [[max]] Maximum value
* [[min]] Minimum value
* [[avg]] Average value
* [[count]] Number of updates (count)

`EventTimeStatsAccum` is <<creating-instance, created>> and registered exclusively for [EventTimeWatermarkExec](physical-operators/EventTimeWatermarkExec.md) physical operator.

!!! note
    When `EventTimeWatermarkExec` physical operator is requested to execute, every task simply <<add, adds>> the values of the [event-time watermark column](physical-operators/EventTimeWatermarkExec.md#eventTime) to the [EventTimeStatsAccum](physical-operators/EventTimeWatermarkExec.md#eventTimeStats) accumulator.

    As per design of Spark accumulators in Apache Spark, accumulator updates are automatically sent out (_propagated_) from tasks to the driver every heartbeat and then they are accumulated together.

[[currentStats]]
[[creating-instance]]
`EventTimeStatsAccum` takes a single <<EventTimeStats, EventTimeStats>> to be created (default: <<zero, zero>>).

=== [[add]] Accumulating Value -- `add` Method

```scala
add(
  v: Long): Unit
```

`add` is part of the `AccumulatorV2` abstraction.

`add` simply requests the <<currentStats, EventTimeStats>> to <<EventTimeStats-add, add>> the given `v` value.

`add` is used when [EventTimeWatermarkExec](physical-operators/EventTimeWatermarkExec.md) physical operator is executed.

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
