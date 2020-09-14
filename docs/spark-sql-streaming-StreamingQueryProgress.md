== [[StreamingQueryProgress]] StreamingQueryProgress

`StreamingQueryProgress` holds information about the progress of a streaming query.

`StreamingQueryProgress` is created exclusively when `StreamExecution` spark-sql-streaming-ProgressReporter.md#finishTrigger[finishes a trigger].

[NOTE]
====
Use spark-sql-streaming-StreamingQuery.md#lastProgress[lastProgress] property of a `StreamingQuery` to access the most recent `StreamingQueryProgress` update.

[source, scala]
----
val sq: StreamingQuery = ...
sq.lastProgress
----
====

[NOTE]
====
Use spark-sql-streaming-StreamingQuery.md#recentProgress[recentProgress] property of a `StreamingQuery` to access the most recent `StreamingQueryProgress` updates.

[source, scala]
----
val sq: StreamingQuery = ...
sq.recentProgress
----
====

[NOTE]
====
Use spark-sql-streaming-StreamingQueryListener.md#QueryProgressEvent[StreamingQueryListener] to get notified about `StreamingQueryProgress` updates while a streaming query is executed.
====

[[events]]
.StreamingQueryProgress's Properties
[cols="m,3",options="header",width="100%"]
|===
| Name
| Description

| id
| spark-sql-streaming-StreamingQuery.md#id[Unique identifier of a streaming query]

| runId
| spark-sql-streaming-StreamingQuery.md#runId[Unique identifier of the current execution of a streaming query]

| name
| spark-sql-streaming-StreamingQuery.md#name[Optional query name]

| timestamp
| Time when the trigger has started (in ISO8601 format).

| batchId
| Unique id of the current batch

| durationMs
| Durations of the internal phases (in milliseconds)

| eventTime
| Statistics of event time seen in this batch

| stateOperators
| Information about stateful operators in the query that store state.

| sources
| Statistics about the data read from every streaming source in a streaming query

| sink
| Information about progress made for a sink
|===
