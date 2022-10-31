# StreamingQueryStatusListener

`StreamingQueryStatusListener` is a [StreamingQueryListener](../monitoring/StreamingQueryListener.md) that intercepts streaming events and writes them out to the [ElementTrackingStore](#store) (for [StreamingQueryTab](StreamingQueryTab.md)).

## Creating Instance

`StreamingQueryStatusListener` takes the following to be created:

* <span id="conf"> `SparkConf` ([Spark Core]({{ book.spark_core }}/SparkConf))
* <span id="store"> `ElementTrackingStore` ([Spark Core]({{ book.spark_core }}/status/ElementTrackingStore))

`StreamingQueryStatusListener` is created when:

* `StreamingQueryHistoryServerPlugin` is requested to `createListeners`
* `SharedState` ([Spark SQL]({{ book.spark_sql }}/SharedState)) is created (with [spark.sql.streaming.ui.enabled](../configuration-properties.md#spark.sql.streaming.ui.enabled) enabled)

## <span id="streamingProgressRetention"> spark.sql.streaming.ui.retainedProgressUpdates

`StreamingQueryStatusListener` uses [spark.sql.streaming.ui.retainedProgressUpdates](../configuration-properties.md#spark.sql.streaming.ui.retainedProgressUpdates) configuration property in [onQueryProgress](#onQueryProgress) to control when to delete excessive [StreamingQueryProgress](../monitoring/StreamingQueryProgress.md)s (in the [ElementTrackingStore](#store)).

## <span id="onQueryStarted"> onQueryStarted

```scala
onQueryStarted(
  event: StreamingQueryListener.QueryStartedEvent): Unit
```

`onQueryStarted` is part of the [StreamingQueryListener](../monitoring/StreamingQueryListener.md#onQueryStarted) abstraction.

---

`onQueryStarted` writes out a new "start" `StreamingQueryData` to the [ElementTrackingStore](#store).

## <span id="onQueryProgress"> onQueryProgress

```scala
onQueryProgress(
  event: StreamingQueryListener.QueryProgressEvent): Unit
```

`onQueryProgress` is part of the [StreamingQueryListener](../monitoring/StreamingQueryListener.md#onQueryProgress) abstraction.

---

`onQueryProgress`...FIXME

## <span id="onQueryTerminated"> onQueryTerminated

```scala
onQueryTerminated(
  event: StreamingQueryListener.QueryTerminatedEvent): Unit
```

`onQueryTerminated` is part of the [StreamingQueryListener](../monitoring/StreamingQueryListener.md#onQueryTerminated) abstraction.

---

`onQueryTerminated` finds the query summary (the `StreamingQueryData`) for the `runId` in the [ElementTrackingStore](#store).

`onQueryTerminated` writes out a new "terminate" `StreamingQueryData` to the [ElementTrackingStore](#store).

In the end, `onQueryTerminated` removes the streaming query (by `runId`) from the [queryToProgress](#queryToProgress) registry.
