# EventTimeStatsAccum Accumulator

`EventTimeStatsAccum` is an `AccumulatorV2` ([Spark Core]({{ book.spark_core }}/accumulators/AccumulatorV2/)) that accumulates `Long` values and produces an [EventTimeStats](EventTimeStats.md).

```scala
AccumulatorV2[Long, EventTimeStats]
```

## Creating Instance

`EventTimeStatsAccum` takes the following to be created:

* [EventTimeStats](#currentStats) (default: [EventTimeStats.zero](EventTimeStats.md#zero))

`EventTimeStatsAccum` is created when:

* [EventTimeWatermarkExec](physical-operators/EventTimeWatermarkExec.md) unary physical operator is created (and initializes [eventTimeStats](physical-operators/EventTimeWatermarkExec.md#eventTimeStats))

!!! note "EventTimeWatermarkExec Physical Operator"
    When `EventTimeWatermarkExec` physical operator is requested to execute, every task simply [adds](#add) the values of the [event-time watermark column expression](physical-operators/EventTimeWatermarkExec.md#eventTime) to the [EventTimeStatsAccum](physical-operators/EventTimeWatermarkExec.md#eventTimeStats) accumulator.

    As per design of Spark accumulators in Apache Spark, accumulator updates are automatically sent out (_propagated_) from tasks to the driver every heartbeat and then they are accumulated together.

## <span id="currentStats"> EventTimeStats

`EventTimeStatsAccum` is given an [EventTimeStats](EventTimeStats.md) when [created](#creating-instance).

Every time `AccumulatorV2` methods are called, `EventTimeStatsAccum` simply relays them to the `EventTimeStats` (that is responsible for event-time statistics, i.e. max, min, avg, count).

## <span id="add"> Adding Value

```scala
add(
  v: Long): Unit
```

`add` is part of the `AccumulatorV2` ([Spark Core]({{ book.spark_core }}/accumulators/AccumulatorV2/#add)) abstraction.

`add` simply requests the [EventTimeStats](#currentStats) to [add](EventTimeStats.md#add) the given `v` value.

`add` is used when [EventTimeWatermarkExec](physical-operators/EventTimeWatermarkExec.md) physical operator is executed.
