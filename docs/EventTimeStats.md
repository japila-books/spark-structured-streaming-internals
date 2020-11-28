# EventTimeStats

`EventTimeStats` is used to help calculate **event-time column statistics** (statistics of the values of an event-time column):

* <span id="max"> Maximum
* <span id="min"> Minimum
* <span id="avg"> Average
* <span id="count"> Count

`EventTimeStats` is used by [EventTimeStatsAccum](EventTimeStatsAccum.md) accumulator.

## <span id="zero"> Zero Value

`EventTimeStats` defines a special value **zero** with the following values:

* `Long.MinValue` for the [max](#max)
* `Long.MaxValue` for the [min](#min)
* `0.0` for the [avg](#avg)
* `0L` for the [count](#count)

## <span id="add"> Adding Event-Time Value

```scala
add(
  eventTime: Long): Unit
```

`add` updates the statistics given the `eventTime` value.

## <span id="merge"> Merging EventTimeStats

```scala
merge(
  that: EventTimeStats): Unit
```

`merge`...FIXME
