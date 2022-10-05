# StreamingQueryProgress

`StreamingQueryProgress` is metadata of a single micro-batch (_progress_) of a [StreamingQuery](../StreamingQuery.md).

## Creating Instance

`StreamingQueryProgress` takes the following to be created:

* <span id="id"> [Unique identifier](../StreamingQuery.md#id)
* <span id="runId"> [Unique identifier of a query execution](../StreamingQuery.md#runId)
* <span id="name"> [Name](../StreamingQuery.md#name)
* [Batch Timestamp](#timestamp)
* <span id="batchId"> Unique ID of a micro-batch
* <span id="batchDuration"> Batch Duration
* <span id="durationMs"> Durations of the internal phases (in ms)
* [EventTime Statistics](#eventTime)
* <span id="stateOperators"> [StateOperatorProgress](StateOperatorProgress.md) for every stateful operator
* <span id="sources"> [SourceProgress](SourceProgress.md) for every streaming source
* <span id="sink"> [SinkProgress](SinkProgress.md)
* <span id="observedMetrics"> Observed Metrics

`StreamingQueryProgress` is created when:

* `StreamExecution` is requested to [finish a trigger](ProgressReporter.md#finishTrigger)

### <span id="timestamp"> Batch Timestamp

`StreamingQueryProgress` is given a timestamp when [created](#creating-instance).

The time when a trigger has started (in ISO8601 format).

### <span id="eventTime"> Event Time Statistics

`StreamingQueryProgress` is given an **Event Time Statistics** when [created](#creating-instance).

## Last and Recent Progresses

Use [lastProgress](../StreamingQuery.md#lastProgress) property of a `StreamingQuery` to access the most recent `StreamingQueryProgress` update.

```scala
val sq: StreamingQuery = ...
sq.lastProgress
```

Use [recentProgress](../StreamingQuery.md#recentProgress) property of a `StreamingQuery` to access the most recent `StreamingQueryProgress` updates.

```scala
val sq: StreamingQuery = ...
sq.recentProgress
```

## StreamingQueryListener

Use [StreamingQueryListener](StreamingQueryListener.md#QueryProgressEvent) to be notified about `StreamingQueryProgress` updates while a streaming query is executed.
