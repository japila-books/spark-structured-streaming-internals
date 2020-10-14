# StreamingQuery

`StreamingQuery` is the <<contract, contract>> of streaming queries that are executed continuously and concurrently (i.e. on a [separate thread](StreamExecution.md#queryExecutionThread)).

`StreamingQuery` is a Scala trait with the only implementation being [StreamExecution](StreamExecution.md) (and less importanly [StreamingQueryWrapper](spark-sql-streaming-StreamingQueryWrapper.md)).

[[contract]]
.StreamingQuery Contract
[cols="1m,2",options="header",width="100%"]
|===
| Method
| Description

| awaitTermination
a| [[awaitTermination]]

[source, scala]
----
awaitTermination(): Unit
awaitTermination(timeoutMs: Long): Boolean
----

Used when...FIXME

| exception
a| [[exception]]

[source, scala]
----
exception: Option[StreamingQueryException]
----

`StreamingQueryException` if the query has finished due to an exception

Used when...FIXME

| explain
a| [[explain]]

[source, scala]
----
explain(): Unit
explain(extended: Boolean): Unit
----

Used when...FIXME

| id
a| [[id]]

[source, scala]
----
id: UUID
----

The unique identifier of the streaming query (that does not change across restarts unlike <<runId, runId>>)

Used when...FIXME

| isActive
a| [[isActive]]

[source, scala]
----
isActive: Boolean
----

Indicates whether the streaming query is active (`true`) or not (`false`)

Used when...FIXME

| lastProgress
a| [[lastProgress]]

[source, scala]
----
lastProgress: StreamingQueryProgress
----

The last [StreamingQueryProgress](monitoring/StreamingQueryProgress.md) of the streaming query

Used when...FIXME

| name
a| [[name]]

[source, scala]
----
name: String
----

The name of the query that is unique across all active queries

Used when...FIXME

| processAllAvailable
a| [[processAllAvailable]]

[source, scala]
----
processAllAvailable(): Unit
----

Pauses (_blocks_) the current thread until the streaming query has no more data to be processed or has been <<stop, stopped>>

Intended for testing

| recentProgress
a| [[recentProgress]]

[source, scala]
----
recentProgress: Array[StreamingQueryProgress]
----

Collection of the recent [StreamingQueryProgress](monitoring/StreamingQueryProgress.md) updates.

Used when...FIXME

| runId
a| [[runId]]

[source, scala]
----
runId: UUID
----

The unique identifier of the current execution of the streaming query (that is different every restart unlike <<id, id>>)

Used when...FIXME

| sparkSession
a| [[sparkSession]]

[source, scala]
----
sparkSession: SparkSession
----

Used when...FIXME

| status
a| [[status]]

[source, scala]
----
status: StreamingQueryStatus
----

[StreamingQueryStatus](monitoring/StreamingQueryStatus.md) of the streaming query (as `StreamExecution` [has accumulated](monitoring/ProgressReporter.md#currentStatus) being a `ProgressReporter` while running the streaming query)

Used when...FIXME

| stop
a| [[stop]]

[source, scala]
----
stop(): Unit
----

Stops the streaming query

|===

`StreamingQuery` can be in two states:

* active (started)
* inactive (stopped)

If inactive, `StreamingQuery` may have transitioned into the state due to an `StreamingQueryException` (that is available under `exception`).

`StreamingQuery` tracks current state of all the sources, i.e. `SourceStatus`, as `sourceStatuses`.

There could only be a single [streaming sink](Sink.md) for a `StreamingQuery` with many [streaming sources](Source.md).

`StreamingQuery` can be stopped by `stop` or an exception.
