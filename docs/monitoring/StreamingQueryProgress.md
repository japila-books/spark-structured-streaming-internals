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

* `StreamExecution` is requested to [finish a trigger](../ProgressReporter.md#finishTrigger)

### <span id="timestamp"> Batch Timestamp

`StreamingQueryProgress` is given a timestamp when [created](#creating-instance).

The time when a trigger has started (in ISO8601 format).

### <span id="eventTime"> Event Time Statistics

`StreamingQueryProgress` is given an **Event Time Statistics** when [created](#creating-instance).

## <span id="inputRowsPerSecond"> inputRowsPerSecond

```scala
inputRowsPerSecond: Double
```

`inputRowsPerSecond` is the total (_sum_) of the [inputRowsPerSecond](SourceProgress.md#inputRowsPerSecond)s of all the [SourceProgress](#sources)es (of this single `StreamingQueryProgress`).

!!! note "Streaming Query UI"
    `inputRowsPerSecond` is displayed as **Avg Input /sec** column in [Streaming Query UI](../webui/StreamingQueryPage.md#Avg-Input).

!!! note "Streaming Query Statistics UI"
    `inputRowsPerSecond` is displayed in **Input Rate** timeline and histogram in [Streaming Query Statistics UI](../webui/StreamingQueryStatisticsPage.md).

---

`inputRowsPerSecond` is used when:

* `MetricsReporter` is requested to register the [inputRate-total](MetricsReporter.md#inputRate-total) metric
* `StreamingQueryProgress` is requested for [jsonValue](#jsonValue) (for the `inputRowsPerSecond` field)
* `StreamingQueryStatisticsPage` is requested to [display Input Rate timeline and histogram](../webui/StreamingQueryStatisticsPage.md#generateStatTable)

## <span id="numInputRows"> Total Number of Input Rows

```scala
numInputRows: Long
```

`numInputRows` is a sum of the [numInputRows](SourceProgress.md#numInputRows) of all the [sources](#sources).

---

`numInputRows` is used when:

* `StreamingQueryProgress` is requested for [jsonValue](#jsonValue)
* `StreamingQueryStatisticsPage` is requested to [generateStatTable](../webui/StreamingQueryStatisticsPage.md#generateStatTable) (for `input-rows-histogram`)

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

## <span id="prettyJson"> prettyJson

```scala
prettyJson: String
```

`prettyJson`...FIXME

---

`prettyJson` is used for [toString](#toString).

## <span id="jsonValue"> jsonValue

```scala
jsonValue: JValue
```

`jsonValue`...FIXME

---

`jsonValue` is used in [json](#json) and [prettyJson](#prettyJson).
