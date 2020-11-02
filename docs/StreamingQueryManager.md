# StreamingQueryManager

`StreamingQueryManager` is the <<methods, management interface>> for <<activeQueries, active streaming queries>> of a <<sparkSession, SparkSession>>.

[[methods]]
.StreamingQueryManager API
[cols="1,2",options="header",width="100%"]
|===
| Method
| Description

| <<active, active>>
a|

[source, scala]
----
active: Array[StreamingQuery]
----

Active <<StreamingQuery.md#, structured queries>>

| <<addListener, addListener>>
a|

[source, scala]
----
addListener(listener: StreamingQueryListener): Unit
----

Registers (_adds_) a [StreamingQueryListener](monitoring/StreamingQueryListener.md)

| <<awaitAnyTermination, awaitAnyTermination>>
a|

[source, scala]
----
awaitAnyTermination(): Unit
awaitAnyTermination(timeoutMs: Long): Boolean
----

Waits until any streaming query terminats or `timeoutMs` elapses

| <<get, get>>
a|

[source, scala]
----
get(id: String): StreamingQuery
get(id: UUID): StreamingQuery
----

Gets the <<StreamingQuery.md#, StreamingQuery>> by <<StreamingQuery.md#id, id>>

| <<removeListener, removeListener>>
a|

[source, scala]
----
removeListener(
  listener: StreamingQueryListener): Unit
----

De-registers (_removes_) the [StreamingQueryListener](monitoring/StreamingQueryListener.md)

| <<resetTerminated, resetTerminated>>
a|

[source, scala]
----
resetTerminated(): Unit
----

Resets the internal registry of the terminated streaming queries (that lets <<awaitAnyTermination, awaitAnyTermination>> to be used again)

|===

`StreamingQueryManager` is available using `SparkSession.streams` property.

[source, scala]
----
scala> :type spark
org.apache.spark.sql.SparkSession

scala> :type spark.streams
org.apache.spark.sql.streaming.StreamingQueryManager
----

`StreamingQueryManager` is <<creating-instance, created>> when `SessionState` is created.

.StreamingQueryManager
image::images/StreamingQueryManager.png[align="center"]

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-SessionState.html[SessionState] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] gitbook.

`StreamingQueryManager` is used (internally) to <<createQuery, create a StreamingQuery (and its StreamExecution)>>.

.StreamingQueryManager Creates StreamingQuery (and StreamExecution)
image::images/StreamingQueryManager-createQuery.png[align="center"]

`StreamingQueryManager` is <<postListenerEvent, notified about state changes of a structured query and passes them along (to registered listeners)>>.

[[creating-instance]][[sparkSession]]
`StreamingQueryManager` takes a single `SparkSession` when created.

=== [[listenerBus]] StreamingQueryListenerBus -- `listenerBus` Internal Property

[source, scala]
----
listenerBus: StreamingQueryListenerBus
----

`listenerBus` is a [StreamingQueryListenerBus](StreamingQueryListenerBus.md) (for the current <<sparkSession, SparkSession>>) that is created immediately when `StreamingQueryManager` is <<creating-instance, created>>.

`listenerBus` is used for the following:

* <<addListener, Register>> or <<removeListener, de-register>> a given [StreamingQueryListener](monitoring/StreamingQueryListener.md)

* <<postListenerEvent, Post a streaming event>> (and notify <<addListener, registered StreamingQueryListeners about the event>>)

=== [[active]] Getting All Active Streaming Queries -- `active` Method

[source, scala]
----
active: Array[StreamingQuery]
----

`active` gets <<activeQueries, all active streaming queries>>.

=== [[get]] Getting Active Continuous Query By Name -- `get` Method

[source, scala]
----
get(name: String): StreamingQuery
----

`get` method returns a StreamingQuery.md[StreamingQuery] by `name`.

It may throw an `IllegalArgumentException` when no StreamingQuery exists for the `name`.

```
java.lang.IllegalArgumentException: There is no active query with name hello
  at org.apache.spark.sql.StreamingQueryManager$$anonfun$get$1.apply(StreamingQueryManager.scala:59)
  at org.apache.spark.sql.StreamingQueryManager$$anonfun$get$1.apply(StreamingQueryManager.scala:59)
  at scala.collection.MapLike$class.getOrElse(MapLike.scala:128)
  at scala.collection.AbstractMap.getOrElse(Map.scala:59)
  at org.apache.spark.sql.StreamingQueryManager.get(StreamingQueryManager.scala:58)
  ... 49 elided
```

## <span id="addListener"> Registering StreamingQueryListener

```scala
addListener(
  listener: StreamingQueryListener): Unit
```

`addListener` requests the [StreamingQueryListenerBus](#listenerBus) to [add](StreamingQueryListenerBus.md#addListener) the input [StreamingQueryListener](monitoring/StreamingQueryListener.md).

## <span id="removeListener"> De-Registering StreamingQueryListener

```scala
removeListener(
  listener: StreamingQueryListener): Unit
```

`removeListener` requests [StreamingQueryListenerBus](#listenerBus) to [remove](StreamingQueryListenerBus.md#removeListener) the input [StreamingQueryListener](monitoring/StreamingQueryListener.md).

=== [[awaitAnyTermination]] Waiting for Any Streaming Query Termination -- `awaitAnyTermination` Method

[source, scala]
----
awaitAnyTermination(): Unit
awaitAnyTermination(timeoutMs: Long): Boolean
----

`awaitAnyTermination` acquires a lock on <<awaitTerminationLock, awaitTerminationLock>> and waits until any streaming query has finished (i.e. <<lastTerminatedQuery, lastTerminatedQuery>> is available) or `timeoutMs` has expired.

`awaitAnyTermination` re-throws the `StreamingQueryException` from <<lastTerminatedQuery, lastTerminatedQuery>> if StreamingQuery.md#exception[it reported one].

=== [[resetTerminated]] `resetTerminated` Method

[source, scala]
----
resetTerminated(): Unit
----

`resetTerminated` forgets about the past-terminated query (so that <<awaitAnyTermination, awaitAnyTermination>> can be used again to wait for a new streaming query termination).

Internally, `resetTerminated` acquires a lock on <<awaitTerminationLock, awaitTerminationLock>> and simply resets <<lastTerminatedQuery, lastTerminatedQuery>> (i.e. sets it to `null`).

=== [[createQuery]] Creating Streaming Query -- `createQuery` Internal Method

[source, scala]
----
createQuery(
  userSpecifiedName: Option[String],
  userSpecifiedCheckpointLocation: Option[String],
  df: DataFrame,
  extraOptions: Map[String, String],
  sink: BaseStreamingSink,
  outputMode: OutputMode,
  useTempCheckpointLocation: Boolean,
  recoverFromCheckpointLocation: Boolean,
  trigger: Trigger,
  triggerClock: Clock): StreamingQueryWrapper
----

`createQuery` creates a [StreamingQueryWrapper](StreamingQueryWrapper.md) (for a [StreamExecution](StreamExecution.md) per the input user-defined properties).

Internally, `createQuery` first finds the name of the checkpoint directory of a query (aka *checkpoint location*) in the following order:

. Exactly the input `userSpecifiedCheckpointLocation` if defined

. spark-sql-streaming-properties.md#spark.sql.streaming.checkpointLocation[spark.sql.streaming.checkpointLocation] Spark property if defined for the parent directory with a subdirectory per the optional `userSpecifiedName` (or a randomly-generated UUID)

. (only when `useTempCheckpointLocation` is enabled) A temporary directory (as specified by `java.io.tmpdir` JVM property) with a subdirectory with `temporary` prefix.

NOTE: `userSpecifiedCheckpointLocation` can be any path that is acceptable by Hadoop's https://hadoop.apache.org/docs/stable/api/org/apache/hadoop/fs/Path.html[Path].

If the directory name for the checkpoint location could not be found, `createQuery` reports a `AnalysisException`.

```
checkpointLocation must be specified either through option("checkpointLocation", ...) or SparkSession.conf.set("spark.sql.streaming.checkpointLocation", ...)
```

`createQuery` reports a `AnalysisException` when the input `recoverFromCheckpointLocation` flag is turned off but there is *offsets* directory in the checkpoint location.

`createQuery` makes sure that the logical plan of the structured query is analyzed (i.e. no logical errors have been found).

Unless spark-sql-streaming-properties.md#spark.sql.streaming.unsupportedOperationCheck[spark.sql.streaming.unsupportedOperationCheck] Spark property is turned on, `createQuery` spark-sql-streaming-UnsupportedOperationChecker.md#checkForStreaming[checks the logical plan of the streaming query for unsupported operations].

(only when `spark.sql.adaptive.enabled` Spark property is turned on) `createQuery` prints out a WARN message to the logs:

```text
spark.sql.adaptive.enabled is not supported in streaming DataFrames/Datasets and will be disabled.
```

In the end, `createQuery` creates a [StreamingQueryWrapper](StreamingQueryWrapper.md) with a new [MicroBatchExecution](MicroBatchExecution.md).

[NOTE]
====
`recoverFromCheckpointLocation` flag corresponds to `recoverFromCheckpointLocation` flag that `StreamingQueryManager` uses to <<startQuery, start a streaming query>> and which is enabled by default (and is in fact the only place where `createQuery` is used).

* `memory` sink has the flag enabled for [Complete](OutputMode.md#Complete) output mode only

* `foreach` sink has the flag always enabled

* `console` sink has the flag always disabled

* all other sinks have the flag always enabled
====

NOTE: `userSpecifiedName` corresponds to `queryName` option (that can be defined using ``DataStreamWriter``'s [queryName](DataStreamWriter.md#queryName) method) while `userSpecifiedCheckpointLocation` is `checkpointLocation` option.

NOTE: `createQuery` is used when `StreamingQueryManager` is requested to [start a streaming query](#startQuery) (when `DataStreamWriter` is requested to [start an execution of a streaming query](DataStreamWriter.md#start)).

=== [[startQuery]] Starting Streaming Query Execution -- `startQuery` Internal Method

[source, scala]
----
startQuery(
  userSpecifiedName: Option[String],
  userSpecifiedCheckpointLocation: Option[String],
  df: DataFrame,
  extraOptions: Map[String, String],
  sink: BaseStreamingSink,
  outputMode: OutputMode,
  useTempCheckpointLocation: Boolean = false,
  recoverFromCheckpointLocation: Boolean = true,
  trigger: Trigger = ProcessingTime(0),
  triggerClock: Clock = new SystemClock()): StreamingQuery
----

`startQuery` starts a StreamingQuery.md[streaming query] and returns a handle to it.

Internally, `startQuery` first <<createQuery, creates a StreamingQueryWrapper>>, registers it in <<activeQueries, activeQueries>> internal registry (by the [id](StreamExecution.md#id)), requests it for the underlying [StreamExecution](StreamingQueryWrapper.md#streamingQuery) and [starts it](StreamExecution.md#start).

In the end, `startQuery` returns the [StreamingQueryWrapper](StreamingQueryWrapper.md) (as part of the fluent API so you can chain operators) or throws the exception that was reported when attempting to start the query.

`startQuery` throws an `IllegalArgumentException` when there is another query registered under `name`. `startQuery` looks it up in the <<activeQueries, activeQueries>> internal registry.

```text
Cannot start query with name [name] as a query with that name is already active
```

`startQuery` throws an `IllegalStateException` when a query is started again from checkpoint. `startQuery` looks it up in <<activeQueries, activeQueries>> internal registry.

```text
Cannot start query with id [id] as another query with same id is already active. Perhaps you are attempting to restart a query from checkpoint that is already active.
```

`startQuery` is used when `DataStreamWriter` is requested to [start an execution of the streaming query](DataStreamWriter.md#start).

=== [[postListenerEvent]] Posting StreamingQueryListener Event to StreamingQueryListenerBus -- `postListenerEvent` Internal Method

[source, scala]
----
postListenerEvent(event: StreamingQueryListener.Event): Unit
----

`postListenerEvent` simply posts the input `event` to the internal <<listenerBus, event bus for streaming events (StreamingQueryListenerBus)>>.

![StreamingQueryManager Propagates StreamingQueryListener Events](images/StreamingQueryManager-postListenerEvent.png)

`postListenerEvent` is used when `StreamExecution` is requested to [post a streaming event](StreamExecution.md#postEvent).

=== [[notifyQueryTermination]] Handling Termination of Streaming Query (and Deactivating Query in StateStoreCoordinator) -- `notifyQueryTermination` Internal Method

[source, scala]
----
notifyQueryTermination(terminatedQuery: StreamingQuery): Unit
----

`notifyQueryTermination` removes the `terminatedQuery` from <<activeQueries, activeQueries>> internal registry (by the StreamingQuery.md#id[query id]).

`notifyQueryTermination` records the `terminatedQuery` in <<lastTerminatedQuery, lastTerminatedQuery>> internal registry (when no earlier streaming query was recorded or the `terminatedQuery` terminated due to an exception).

`notifyQueryTermination` notifies others that are blocked on <<awaitTerminationLock, awaitTerminationLock>>.

In the end, `notifyQueryTermination` requests <<stateStoreCoordinator, StateStoreCoordinator>> to spark-sql-streaming-StateStoreCoordinatorRef.md#deactivateInstances[deactivate all active runs of the streaming query].

.StreamingQueryManager's Marking Streaming Query as Terminated
image::images/StreamingQueryManager-notifyQueryTermination.png[align="center"]

`notifyQueryTermination` is used when `StreamExecution` is requested to [run a streaming query](StreamExecution.md#runStream) and the query [has finished (running streaming batches)](StreamExecution.md#runStream-finally) (with or without an exception).

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| activeQueries
| [[activeQueries]] Registry of <<StreamingQuery.md#, StreamingQueries>> per `UUID`

Used when `StreamingQueryManager` is requested for <<active, active streaming queries>>, <<get, get a streaming query by id>>, <<startQuery, starts a streaming query>> and <<notifyQueryTermination, is notified that a streaming query has terminated>>.

| activeQueriesLock
| [[activeQueriesLock]]

| awaitTerminationLock
| [[awaitTerminationLock]]

| lastTerminatedQuery
a| [[lastTerminatedQuery]] <<StreamingQuery.md#, StreamingQuery>> that has recently been terminated, i.e. StreamingQuery.md#stop[stopped] or StreamingQuery.md#exception[due to an exception].

`null` when no streaming query has terminated yet or <<resetTerminated, resetTerminated>>.

* Used in <<awaitAnyTermination, awaitAnyTermination>> to know when a streaming query has terminated

* Set when `StreamingQueryManager` <<notifyQueryTermination, is notified that a streaming query has terminated>>

| stateStoreCoordinator
a| [[stateStoreCoordinator]] [StateStoreCoordinatorRef](spark-sql-streaming-StateStoreCoordinatorRef.md) to the `StateStoreCoordinator` RPC Endpoint

* [Created](spark-sql-streaming-StateStoreCoordinatorRef.md#forDriver) when `StreamingQueryManager` is [created](#creating-instance)

Used when:

* `StreamingQueryManager` <<notifyQueryTermination, is notified that a streaming query has terminated>>

* Stateful operators are executed ([FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md), [StateStoreRestoreExec](physical-operators/StateStoreRestoreExec.md), [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md), [StreamingDeduplicateExec](physical-operators/StreamingDeduplicateExec.md) and [StreamingSymmetricHashJoinExec](physical-operators/StreamingSymmetricHashJoinExec.md))

* [Creating StateStoreRDD (with storeUpdateFunction aborting StateStore when a task fails)](StateStoreOps.md#mapPartitionsWithStateStore)

|===
