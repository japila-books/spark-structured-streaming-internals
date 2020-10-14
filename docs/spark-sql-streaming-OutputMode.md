# OutputMode

`OutputMode` of a streaming query describes what data is written to a [streaming sink](Sink.md).

[[available-output-modes]]
There are three available output modes:

* <<Append, Append>>

* <<Complete, Complete>>

* <<Update, Update>>

The output mode is specified on the _writing side_ of a streaming query using [DataStreamWriter.outputMode](DataStreamWriter.md#outputMode) method (by alias or a value of `org.apache.spark.sql.streaming.OutputMode` object).

```text
import org.apache.spark.sql.streaming.OutputMode.Update
val inputStream = spark
  .readStream
  .format("rate")
  .load
  .writeStream
  .format("console")
  .outputMode(Update) // <-- update output mode
  .start
```

=== [[Append]] Append Output Mode

*Append* (alias: *append*) is the [default output mode](DataStreamWriter.md#outputMode) that writes "new" rows only.

In <<spark-sql-streaming-aggregation.md#, streaming aggregations>>, a "new" row is when the intermediate state becomes final, i.e. when new events for the grouping key can only be considered late which is when watermark moves past the event time of the key.

`Append` output mode requires that a streaming query defines event-time watermark (using [withWatermark](operators/withWatermark.md) operator) on the event time column that is used in aggregation (directly or using [window](spark-sql-streaming-window.md) standard function).

Required for datasets with `FileFormat` format (to create [FileStreamSink](datasources/file/FileStreamSink.md))

`Append` is spark-sql-streaming-UnsupportedOperationChecker.md#multiple-flatMapGroupsWithState[mandatory] when multiple `flatMapGroupsWithState` operators are used in a structured query.

=== [[Complete]] Complete Output Mode

*Complete* (alias: *complete*) writes all the rows of a Result Table (and corresponds to a traditional batch structured query).

Complete mode does not drop old aggregation state and preserves all data in the Result Table.

Supported only for <<spark-sql-streaming-aggregation.md#, streaming aggregations>> (as asserted by spark-sql-streaming-UnsupportedOperationChecker.md#checkForStreaming[UnsupportedOperationChecker]).

=== [[Update]] Update Output Mode

*Update* (alias: *update*) writes only the rows that were updated (every time there are updates).

For queries that are not <<spark-sql-streaming-aggregation.md#, streaming aggregations>>, `Update` is equivalent to the <<Append, Append>> output mode.
