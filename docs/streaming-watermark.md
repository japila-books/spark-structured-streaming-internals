# Streaming Watermark

**Streaming Watermark** of a [stateful streaming query](spark-sql-streaming-stateful-stream-processing.md) is how long to wait for late and possibly out-of-order events until a streaming state can be considered final and not to change. Streaming watermark is used to mark events  (modeled as a row in the streaming Dataset) that are older than the threshold as "too late", and not "interesting" to update partial non-final streaming state.

In Spark Structured Streaming, streaming watermark is defined using [Dataset.withWatermark](operators/withWatermark.md) high-level operator.

```scala
withWatermark(
  eventTime: String,
  delayThreshold: String): Dataset[T]
```

In [Dataset.withWatermark](operators/withWatermark.md) operator, `eventTime` is the name of the column to use to monitor event time whereas `delayThreshold` is a delay threshold.

**Watermark Delay** says how late and possibly out-of-order events are still acceptable and contribute to the final result of a stateful streaming query. Event-time watermark delay is used to calculate the difference between the event time of an event and the time in the past.

**Event-Time Watermark** is then a **time threshold** (_point in time_) that is the minimum acceptable time of an event (modeled as a row in the streaming Dataset) that is accepted in a stateful streaming query.

With streaming watermark, memory usage of a streaming state can be controlled as late events can easily be dropped, and old state (e.g. aggregates or join) that are never going to be updated removed. That avoids unbounded streaming state that would inevitably use up all the available memory of long-running streaming queries and end up in out of memory errors.

In [Append](OutputMode.md#Append) output mode the current event-time streaming watermark is used for the following:

* Output saved state rows that became expired (*Expired events* in the demo)

* Dropping late events, i.e. don't save them to a state store or include in aggregation (*Late events* in the demo)

Streaming watermark is [required](UnsupportedOperationChecker.md#streaming-aggregation-append-mode-requires-watermark) for a [streaming aggregation](streaming-aggregation.md) in [append](OutputMode.md#Append) output mode.

## Streaming Aggregation

In [streaming aggregation](streaming-aggregation.md), a streaming watermark has to be defined on one or many grouping expressions of a streaming aggregation (directly or using [window](spark-sql-streaming-window.md) standard function).

!!! note
    [Dataset.withWatermark](operators/withWatermark.md) operator has to be used before an aggregation operator (for the watermark to have an effect).

## Streaming Join

In [streaming join](spark-sql-streaming-join.md), a streaming watermark can be defined on [join keys or any of the join sides](spark-sql-streaming-join.md#join-state-watermark).

## Demos

Use the following demos to learn more:

* [Demo: Streaming Watermark with Aggregation in Append Output Mode](demo/watermark-aggregation-append.md)

## Internals

Under the covers, [Dataset.withWatermark](operators/withWatermark.md) high-level operator creates a logical query plan with [EventTimeWatermark](logical-operators/EventTimeWatermark.md) logical operator.

`EventTimeWatermark` logical operator is planned to [EventTimeWatermarkExec](physical-operators/EventTimeWatermarkExec.md) physical operator that extracts the event times (from the data being processed) and adds them to an accumulator.

Since the execution (data processing) happens on Spark executors, using the accumulator is the only _Spark-approved way_ for communication between the tasks (on the executors) and the driver. Using accumulator updates the driver with the current event-time watermark.

During the query planning phase (in [MicroBatchExecution](micro-batch-execution/MicroBatchExecution.md#runBatch-queryPlanning) and [ContinuousExecution](ContinuousExecution.md#runContinuous-queryPlanning)) that also happens on the driver, `IncrementalExecution` is given the current [OffsetSeqMetadata](OffsetSeqMetadata.md) with the current event-time watermark.

## Further Reading Or Watching

* [SPARK-18124 Observed delay based event time watermarks](https://issues.apache.org/jira/browse/SPARK-18124)
