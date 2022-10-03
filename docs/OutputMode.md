# OutputMode

`OutputMode` of a streaming query describes what data is written to a [streaming sink](Sink.md).

## <span id="DataStreamWriter"> DataStreamWriter

The output mode is specified on the _writing side_ of a streaming query using [DataStreamWriter.outputMode](DataStreamWriter.md#outputMode) method (by alias or a value of `org.apache.spark.sql.streaming.OutputMode` object).

```scala
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

## <span id="Append"> Append Output Mode

**Append** (alias: **append**) is the [default output mode](DataStreamWriter.md#outputMode) that writes "new" rows only.

In [streaming aggregations](streaming-aggregation/index.md), a "new" row is when the intermediate state becomes final, i.e. when new events for the grouping key can only be considered late which is when watermark moves past the event time of the key.

`Append` output mode requires that a streaming query defines event-time watermark (using [withWatermark](operators/withWatermark.md) operator) on the event time column that is used in aggregation (directly or using [window](spark-sql-streaming-window.md) standard function).

Required for datasets with `FileFormat` format (to create [FileStreamSink](datasources/file/FileStreamSink.md))

`Append` is [mandatory](UnsupportedOperationChecker.md#multiple-flatMapGroupsWithState) when multiple `flatMapGroupsWithState` operators are used in a structured query.

## <span id="Complete"> Complete Output Mode

**Complete** (alias: **complete**) writes all the rows of a Result Table (and corresponds to a traditional batch structured query).

Complete mode does not drop old aggregation state and preserves all data in the Result Table.

Supported only for [streaming aggregations](streaming-aggregation/index.md) (as asserted by [UnsupportedOperationChecker](UnsupportedOperationChecker.md#checkForStreaming)).

## <span id="Update"> Update Output Mode

**Update** (alias: **update**) writes only the rows that were updated (every time there are updates).

For queries that are not [streaming aggregations](streaming-aggregation/index.md), `Update` is equivalent to the [Append](#Append) output mode.
