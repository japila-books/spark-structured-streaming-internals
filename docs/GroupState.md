# GroupState

`GroupState` is an <<contract, abstraction>> of <<implementations, group state>> (of type `S`) in [Arbitrary Stateful Streaming Aggregation](arbitrary-stateful-streaming-aggregation/index.md).

`GroupState` is used with the following [KeyValueGroupedDataset](KeyValueGroupedDataset.md) operations:

* [mapGroupsWithState](KeyValueGroupedDataset.md#mapGroupsWithState)

* [flatMapGroupsWithState](KeyValueGroupedDataset.md#flatMapGroupsWithState)

`GroupState` is created separately for every *aggregation key* to hold a state as an *aggregation state value*.

[[contract]]
.GroupState Contract
[cols="30m,70",options="header",width="100%"]
|===
| Method
| Description

| exists
a| [[exists]]

[source, scala]
----
exists: Boolean
----

Checks whether the state value exists or not

If not exists, <<get, get>> throws a `NoSuchElementException`. Use <<getOption, getOption>> instead.

| get
a| [[get]]

[source, scala]
----
get: S
----

Gets the state value if it <<exists, exists>> or throws a `NoSuchElementException`

| getCurrentProcessingTimeMs
a| [[getCurrentProcessingTimeMs]]

[source, scala]
----
getCurrentProcessingTimeMs(): Long
----

Gets the current processing time (as milliseconds in epoch time)

| getCurrentWatermarkMs
a| [[getCurrentWatermarkMs]]

[source, scala]
----
getCurrentWatermarkMs(): Long
----

Gets the current event time watermark (as milliseconds in epoch time)

| getOption
a| [[getOption]]

[source, scala]
----
getOption: Option[S]
----

Gets the state value as a Scala `Option` (regardless whether it <<exists, exists>> or not)

Used when:

* `InputProcessor` is requested to `callFunctionAndUpdateState` (when the row iterator is consumed and a state value has been updated, removed or timeout changed)

* `GroupStateImpl` is requested for the [textual representation](GroupStateImpl.md#toString)

| hasTimedOut
a| [[hasTimedOut]]

[source, scala]
----
hasTimedOut: Boolean
----

Whether the state (for a given key) has timed out or not.

Can only be `true` when timeouts are enabled using <<setTimeoutDuration, setTimeoutDuration>>

| remove
a| [[remove]]

[source, scala]
----
remove(): Unit
----

Removes the state

| setTimeoutDuration
a| [[setTimeoutDuration]]

[source, scala]
----
setTimeoutDuration(durationMs: Long): Unit
setTimeoutDuration(duration: String): Unit
----

Specifies the *timeout duration* for the state key (in millis or as a string, e.g. "10 seconds", "1 hour") for [GroupStateTimeout.ProcessingTimeTimeout](GroupStateTimeout.md#ProcessingTimeTimeout)

| setTimeoutTimestamp
a| [[setTimeoutTimestamp]]

[source, scala]
----
setTimeoutTimestamp(timestamp: java.sql.Date): Unit
setTimeoutTimestamp(
  timestamp: java.sql.Date,
  additionalDuration: String): Unit
setTimeoutTimestamp(timestampMs: Long): Unit
setTimeoutTimestamp(
  timestampMs: Long,
  additionalDuration: String): Unit
----

Specifies the *timeout timestamp* for the state key for [GroupStateTimeout.EventTimeTimeout](GroupStateTimeout.md#EventTimeTimeout)

| update
a| [[update]]

[source, scala]
----
update(newState: S): Unit
----

Updates the state (sets the state to a new value)

|===

[[implementations]]
[GroupStateImpl](GroupStateImpl.md) is the default and only known implementation of the <<contract, GroupState Contract>> in Spark Structured Streaming.
