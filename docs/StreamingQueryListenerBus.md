# StreamingQueryListenerBus

`StreamingQueryListenerBus` is an event bus for [dispatching streaming events](#post) (of [active streaming queries](#activeQueryRunIds)) to [StreamingQueryListener](monitoring/StreamingQueryListener.md)s.

!!! tip
    Learn more about [event buses]({{ book.spark_core }}/ListenerBus) in [The Internals of Apache Spark]({{ book.spark_core }}) online book.

## Creating Instance

`StreamingQueryListenerBus` takes the following to be created:

* <span id="sparkListenerBus"> `LiveListenerBus` ([Spark Core]({{ book.spark_core }}/scheduler/LiveListenerBus))

When created, `StreamingQueryListenerBus` registers itself with the [LiveListenerBus](#sparkListenerBus) to `streams` event queue.

`StreamingQueryListenerBus` is created for [StreamingQueryManager](StreamingQueryManager.md#listenerBus) (once per `SparkSession`).

![StreamingQueryListenerBus is Created Once In SparkSession](images/StreamingQueryListenerBus.png)

## <span id="SparkListener"> SparkListener

`StreamingQueryListenerBus` is an event listener (`SparkListener`) and registers itself with the [LiveListenerBus](#sparkListenerBus) to [intercept QueryStartedEvents](#post).

!!! tip
    Learn more about [SparkListener]({{ book.spark_core }}/SparkListener) in [The Internals of Apache Spark]({{ book.spark_core }}) online book.

## <span id="activeQueryRunIds"> Run IDs of Active Streaming Queries

```scala
activeQueryRunIds: Set[UUID]
```

`activeQueryRunIds` is an internal registry of [run IDs](StreamingQuery.md#runId) of active streaming queries in the `SparkSession`.

* A `runId` is added when `StreamingQueryListenerBus` is requested to [post a QueryStartedEvent](#post)

* A `runId` is removed when `StreamingQueryListenerBus` is requested to [post a QueryTerminatedEvent](#postToAll)

`activeQueryRunIds` is used internally to [dispatch a streaming event to a StreamingQueryListener](#doPostEvent) (so the events gets sent out to streaming queries in the `SparkSession`).

## <span id="post"> Posting Streaming Event to LiveListenerBus

```scala
post(
  event: StreamingQueryListener.Event): Unit
```

`post` simply posts the input `event` directly to the [LiveListenerBus](#sparkListenerBus) unless it is a [QueryStartedEvent](monitoring/StreamingQueryListener.md#QueryStartedEvent).

For a [QueryStartedEvent](monitoring/StreamingQueryListener.md#QueryStartedEvent), `post` adds the `runId` (of the streaming query that has been started) to the [activeQueryRunIds](#activeQueryRunIds) internal registry first, posts the event to the [LiveListenerBus](#sparkListenerBus) and then [postToAll](#postToAll).

`post` is used when `StreamingQueryManager` is requested to [post a streaming event](StreamingQueryManager.md#postListenerEvent).

## <span id="doPostEvent"> Notifying Listener about Event

```scala
doPostEvent(
  listener: StreamingQueryListener,
  event: StreamingQueryListener.Event): Unit
```

`doPostEvent` is part of the `ListenerBus` ([Spark Core]({{ book.spark_core }}/ListenerBus#doPostEvent)) abstraction.

`doPostEvent` branches per the type of [StreamingQueryListener.Event](monitoring/StreamingQueryListener.md#events):

* For a [QueryStartedEvent](monitoring/StreamingQueryListener.md#QueryStartedEvent), requests the [StreamingQueryListener](monitoring/StreamingQueryListener.md) to [onQueryStarted](monitoring/StreamingQueryListener.md#onQueryStarted)

* For a [QueryProgressEvent](monitoring/StreamingQueryListener.md#QueryProgressEvent), requests the [StreamingQueryListener](monitoring/StreamingQueryListener.md) to [onQueryProgress](monitoring/StreamingQueryListener.md#onQueryProgress)

* For a [QueryTerminatedEvent](monitoring/StreamingQueryListener.md#QueryTerminatedEvent), requests the [StreamingQueryListener](monitoring/StreamingQueryListener.md) to [onQueryTerminated](monitoring/StreamingQueryListener.md#onQueryTerminated)

For any other event, `doPostEvent` simply does nothing (_swallows it_).

## <span id="postToAll"> Posting Event To All Listeners

```scala
postToAll(
  event: Event): Unit
```

`postToAll` is part of the `ListenerBus` ([Spark Core]({{ book.spark_core }}/ListenerBus#postToAll)) abstraction.

`postToAll` first requests the parent `ListenerBus` to post the event to all registered listeners.

For a [QueryTerminatedEvent](monitoring/StreamingQueryListener.md#QueryTerminatedEvent), `postToAll` simply removes the `runId` (of the streaming query that has been terminated) from the [activeQueryRunIds](#activeQueryRunIds) internal registry.
