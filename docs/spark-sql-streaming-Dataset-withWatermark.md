== [[withWatermark]] withWatermark Operator -- Event-Time Watermark

[source, scala]
----
withWatermark(eventTime: String, delayThreshold: String): Dataset[T]
----

`withWatermark` specifies the `eventTime` column for *event time watermark* and `delayThreshold` for *event lateness*.

`eventTime` specifies the column to use for watermark and can be either part of `Dataset` from the source or custom-generated using `current_time` or `current_timestamp` functions.

NOTE: *Watermark* tracks a point in time before which it is assumed no more late events are supposed to arrive (and if they have, the late events are considered really late and simply dropped).

[NOTE]
====
Spark Structured Streaming uses watermark for the following:

* To know when a given time window aggregation (using spark-sql-streaming-Dataset-groupBy.md[groupBy] operator with spark-sql-streaming-window.md[window] function) can be finalized and thus emitted when using output modes that do not allow updates, like spark-sql-streaming-OutputMode.md#Append[Append] output mode.

* To minimize the amount of state that we need to keep for ongoing aggregations, e.g. [mapGroupsWithState](spark-sql-streaming-KeyValueGroupedDataset-mapGroupsWithState.md) (for implicit state management), [flatMapGroupsWithState](spark-sql-streaming-KeyValueGroupedDataset-flatMapGroupsWithState.md) (for user-defined state management) and [dropDuplicates](spark-sql-streaming-Dataset-dropDuplicates.md) operators.
====

The *current watermark* is computed by looking at the maximum `eventTime` seen across all of the partitions in a query minus a user-specified `delayThreshold`. Due to the cost of coordinating this value across partitions, the actual watermark used is only guaranteed to be at least `delayThreshold` behind the actual event time.

NOTE: In some cases Spark may still process records that arrive more than `delayThreshold` late.
