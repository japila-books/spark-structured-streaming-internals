== [[StreamingQueryListener]] StreamingQueryListener -- Intercepting Life Cycle Events of Streaming Queries

`StreamingQueryListener` is the <<contract, contract>> of listeners that want to be notified about the <<events, life cycle events>> of streaming queries, i.e. <<onQueryStarted, start>>, <<onQueryProgress, progress>> and <<onQueryTerminated, termination>>.

[[contract]]
.StreamingQueryListener Contract
[cols="30m,70",options="header",width="100%"]
|===
| Method
| Description

| onQueryStarted
a| [[onQueryStarted]]

[source, scala]
----
onQueryStarted(
  event: QueryStartedEvent): Unit
----

Informs that `DataStreamWriter` was requested to [start execution of the streaming query](DataStreamWriter.md#start) (on the [stream execution thread](StreamExecution.md#queryExecutionThread))

| onQueryProgress
a| [[onQueryProgress]]

[source, scala]
----
onQueryProgress(
  event: QueryProgressEvent): Unit
----

Informs that `MicroBatchExecution` has finished <<spark-sql-streaming-MicroBatchExecution.md#runActivatedStream-triggerExecution, triggerExecution phase>> (the end of a streaming batch)

| onQueryTerminated
a| [[onQueryTerminated]]

[source, scala]
----
onQueryTerminated(
  event: QueryTerminatedEvent): Unit
----

Informs that a streaming query was <<spark-sql-streaming-StreamingQuery.md#stop, stopped>> or terminated due to an error

|===

`StreamingQueryListener` is informed about the <<events, life cycle events>> when `StreamingQueryListenerBus` is requested to <<spark-sql-streaming-StreamingQueryListenerBus.md#doPostEvent, doPostEvent>>.

[[events]]
.StreamingQueryListener's Life Cycle Events and Callbacks
[cols="1,1,1",options="header",width="100%"]
|===
| Event
| Callback
| Description

a| QueryStartedEvent

- <<spark-sql-streaming-StreamingQuery.md#id, id>>
- <<spark-sql-streaming-StreamingQuery.md#runId, runId>>
- <<spark-sql-streaming-StreamingQuery.md#name, name>>

| <<onQueryStarted, onQueryStarted>>
| [[QueryStartedEvent]] Posted when `StreamExecution` is requested to [run stream processing](StreamExecution.md#runStream) (when `DataStreamWriter` is requested to [start execution of the streaming query](DataStreamWriter.md#start) on the [stream execution thread](StreamExecution.md#queryExecutionThread))

a| QueryProgressEvent

- [StreamingQueryProgress](StreamingQueryProgress.md)

| <<onQueryProgress, onQueryProgress>>
| [[QueryProgressEvent]] Posted when `ProgressReporter` is requested to [update progress of a streaming query](ProgressReporter.md#updateProgress) (after `MicroBatchExecution` has finished <<spark-sql-streaming-MicroBatchExecution.md#runActivatedStream-triggerExecution, triggerExecution phase>> at the end of a streaming batch)

a| QueryTerminatedEvent

- <<spark-sql-streaming-StreamingQuery.md#id, id>>
- <<spark-sql-streaming-StreamingQuery.md#runId, runId>>
- [exception](StreamExecution.md#exception) if terminated due to an error

| <<onQueryTerminated, onQueryTerminated>>
| [[QueryTerminatedEvent]] Posted when `StreamExecution` is requested to [run stream processing](StreamExecution.md#runStream) (and the streaming query was <<spark-sql-streaming-StreamingQuery.md#stop, stopped>> or terminated due to an error)

|===

You can register a `StreamingQueryListener` using <<spark-sql-streaming-StreamingQueryManager.md#addListener, StreamingQueryManager.addListener>> method.

```text
val queryListener: StreamingQueryListener = ...
spark.streams.addListener(queryListener)
```

You can remove a `StreamingQueryListener` using <<spark-sql-streaming-StreamingQueryManager.md#removeListener, StreamingQueryManager.removeListener>> method.

[source, scala]
----
val queryListener: StreamingQueryListener = ...
spark.streams.removeListener(queryListener)
----

.StreamingQueryListener Notified about Query's Start (onQueryStarted)
image::images/StreamingQueryListener-onQueryStarted.png[align="center"]

NOTE: `onQueryStarted` is used internally to unblock the StreamExecution.md#start[starting thread] of `StreamExecution`.

.StreamingQueryListener Notified about Query's Progress (onQueryProgress)
image::images/StreamingQueryListener-onQueryProgress.png[align="center"]

.StreamingQueryListener Notified about Query's Termination (onQueryTerminated)
image::images/StreamingQueryListener-onQueryTerminated.png[align="center"]

[NOTE]
====
You can also register a streaming event listener using the general `SparkListener` interface.

Read up on http://books.japila.pl/apache-spark-internals/apache-spark-internals/2.4.3/spark-scheduler-SparkListener.html[SparkListener] in the http://books.japila.pl/apache-spark-internals[The Internals of Apache Spark] book.
====
