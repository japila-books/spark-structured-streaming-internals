---
title: withWatermark
---

# withWatermark Operator &mdash; Event-Time Watermark

```scala
withWatermark(
  eventTime: String,
  delayThreshold: String): Dataset[T]
```

`withWatermark` defines a [Streaming Watermark](../watermark/index.md) of a streaming query.

`withWatermark` creates a [EventTimeWatermark](../logical-operators/EventTimeWatermark.md) logical operator.

`withWatermark` uses the following for a complete watermark specification:

* **Event Time Watermark** (`eventTime`) - the name of the column with event time of rows.
* **Event Lateness** (`delayThreshold`) - an interval string that is the minimum delay to wait for data to arrive late and still be considered valid (e.g., `1 minute` or `5 hours`).
    Must not be negative.

`eventTime` specifies the column to use for watermark and can be either part of the input streaming `Dataset` (the source) or custom-generated (e.g., `current_time` or `current_timestamp` functions).

## Watermark

**Watermark** tracks a point in time before which it is assumed no more late events are supposed to arrive (and if they have, the late events are considered really late and simply dropped).

Spark Structured Streaming uses watermark for the following:

* To know when a given time window aggregation (using [groupBy](groupBy.md) operator with [window](window.md) standard function) can be finalized and thus emitted when using output modes that do not allow updates (e.g., [Append](../OutputMode.md#Append) output mode).
* To minimize the amount of state that we need to keep for ongoing aggregations (i.e., [mapGroupsWithState](../operators/mapGroupsWithState.md) for implicit state management, [flatMapGroupsWithState](flatMapGroupsWithState.md) for user-defined state management, and [dropDuplicates](dropDuplicates.md) operators).

The **current watermark** is computed by looking at the maximum `eventTime` seen across all of the partitions in a streaming query minus a user-specified `delayThreshold`.
Due to the cost of coordinating this value across partitions, the actual watermark used is only guaranteed to be at least `delayThreshold` behind the actual event time.

In some cases, records that arrive more than `delayThreshold` late can still be processed.
