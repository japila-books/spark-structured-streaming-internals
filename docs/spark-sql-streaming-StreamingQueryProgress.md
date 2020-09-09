== [[StreamingQueryProgress]] StreamingQueryProgress

`StreamingQueryProgress` holds information about the progress of a streaming query.

`StreamingQueryProgress` is created exclusively when `StreamExecution` link:spark-sql-streaming-ProgressReporter.adoc#finishTrigger[finishes a trigger].

[NOTE]
====
Use link:spark-sql-streaming-StreamingQuery.adoc#lastProgress[lastProgress] property of a `StreamingQuery` to access the most recent `StreamingQueryProgress` update.

[source, scala]
----
val sq: StreamingQuery = ...
sq.lastProgress
----
====

[NOTE]
====
Use link:spark-sql-streaming-StreamingQuery.adoc#recentProgress[recentProgress] property of a `StreamingQuery` to access the most recent `StreamingQueryProgress` updates.

[source, scala]
----
val sq: StreamingQuery = ...
sq.recentProgress
----
====

[NOTE]
====
Use link:spark-sql-streaming-StreamingQueryListener.adoc#QueryProgressEvent[StreamingQueryListener] to get notified about `StreamingQueryProgress` updates while a streaming query is executed.
====

[[events]]
.StreamingQueryProgress's Properties
[cols="m,3",options="header",width="100%"]
|===
| Name
| Description

| id
| link:spark-sql-streaming-StreamingQuery.adoc#id[Unique identifier of a streaming query]

| runId
| link:spark-sql-streaming-StreamingQuery.adoc#runId[Unique identifier of the current execution of a streaming query]

| name
| link:spark-sql-streaming-StreamingQuery.adoc#name[Optional query name]

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
