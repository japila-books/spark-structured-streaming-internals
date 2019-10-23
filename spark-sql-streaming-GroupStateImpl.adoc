== [[GroupStateImpl]] GroupStateImpl

`GroupStateImpl` is the default and only known <<spark-sql-streaming-GroupState.adoc#, GroupState>> in Spark Structured Streaming.

`GroupStateImpl` holds per-group <<optionalValue, state value>> of type `S` per group key.

`GroupStateImpl` is <<creating-instance, created>> when `GroupStateImpl` helper object is requested for the following:

* <<createForStreaming, createForStreaming>>

* <<createForBatch, createForBatch>>

=== [[creating-instance]] Creating GroupStateImpl Instance

`GroupStateImpl` takes the following to be created:

* [[optionalValue]] State value (of type `S`)
* [[batchProcessingTimeMs]] <<spark-structured-streaming-batch-processing-time.adoc#, Batch processing time>>
* [[eventTimeWatermarkMs]] `eventTimeWatermarkMs`
* [[timeoutConf]] <<spark-sql-streaming-GroupStateTimeout.adoc#, GroupStateTimeout>>
* [[hasTimedOut]] `hasTimedOut` flag
* [[watermarkPresent]] `watermarkPresent` flag

`GroupStateImpl` initializes the <<internal-properties, internal properties>>.

=== [[createForStreaming]] Creating GroupStateImpl for Streaming Query -- `createForStreaming` Factory Method

[source, scala]
----
createForStreaming[S](
  optionalValue: Option[S],
  batchProcessingTimeMs: Long,
  eventTimeWatermarkMs: Long,
  timeoutConf: GroupStateTimeout,
  hasTimedOut: Boolean,
  watermarkPresent: Boolean): GroupStateImpl[S]
----

`createForStreaming` simply creates a <<creating-instance, new GroupStateImpl>> with the given input arguments.

NOTE: `createForStreaming` is used exclusively when `InputProcessor` is requested to <<spark-sql-streaming-InputProcessor.adoc#callFunctionAndUpdateState, callFunctionAndUpdateState>> (when `InputProcessor` is requested to <<spark-sql-streaming-InputProcessor.adoc#processNewData, processNewData>> and <<spark-sql-streaming-InputProcessor.adoc#processTimedOutState, processTimedOutState>>).

=== [[createForBatch]] Creating GroupStateImpl for Batch Query -- `createForBatch` Factory Method

[source, scala]
----
createForBatch(
  timeoutConf: GroupStateTimeout,
  watermarkPresent: Boolean): GroupStateImpl[Any]
----

`createForBatch`...FIXME

NOTE: `createForBatch` is used when...FIXME

=== [[toString]] Textual Representation -- `toString` Method

[source, scala]
----
toString: String
----

NOTE: `toString` is part of the link:++https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#toString--++[java.lang.Object] contract for the string representation of the object.

`toString`...FIXME

=== [[setTimeoutDuration]] Specifying Timeout Duration for ProcessingTimeTimeout -- `setTimeoutDuration` Method

[source, scala]
----
setTimeoutDuration(durationMs: Long): Unit
----

NOTE: `setTimeoutDuration` is part of the <<spark-sql-streaming-GroupState.adoc#setTimeoutDuration, GroupState Contract>> to specify timeout duration for the state key (in millis or as a string).

`setTimeoutDuration`...FIXME

=== [[setTimeoutTimestamp]] Specifying Timeout Timestamp for EventTimeTimeout -- `setTimeoutTimestamp` Method

[source, scala]
----
setTimeoutTimestamp(durationMs: Long): Unit
----

NOTE: `setTimeoutTimestamp` is part of the <<spark-sql-streaming-GroupState.adoc#setTimeoutTimestamp, GroupState Contract>> to specify timeout timestamp for the state key.

`setTimeoutTimestamp`...FIXME

=== [[getCurrentProcessingTimeMs]] Getting Processing Time -- `getCurrentProcessingTimeMs` Method

[source, scala]
----
getCurrentProcessingTimeMs(): Long
----

NOTE: `getCurrentProcessingTimeMs` is part of the <<spark-sql-streaming-GroupState.adoc#getCurrentProcessingTimeMs, GroupState Contract>> to get the current processing time (as milliseconds in epoch time).

`getCurrentProcessingTimeMs` simply returns the <<batchProcessingTimeMs, batchProcessingTimeMs>>.

=== [[update]] Updating State -- `update` Method

[source, scala]
----
update(newValue: S): Unit
----

NOTE: `update` is part of the <<spark-sql-streaming-GroupState.adoc#update, GroupState Contract>> to update the state.

`update`...FIXME

=== [[remove]] Removing State -- `remove` Method

[source, scala]
----
remove(): Unit
----

NOTE: `remove` is part of the <<spark-sql-streaming-GroupState.adoc#remove, GroupState Contract>> to remove the state.

`remove`...FIXME

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| value
a| [[value]] FIXME

Used when...FIXME

| defined
a| [[defined]] FIXME

Used when...FIXME

| updated
a| [[updated]][[hasUpdated]] *Updated flag* that says whether the state has been <<update, updated>> or not

Default: `false`

Disabled (`false`) when `GroupStateImpl` is requested to <<remove, remove the state>>

Enabled (`true`) when `GroupStateImpl` is requested to <<update, update the state>>

| removed
a| [[removed]][[hasRemoved]] *Removed flag* that says whether the state is marked <<remove, removed>> or not

Default: `false`

Disabled (`false`) when `GroupStateImpl` is requested to <<update, update the state>>

Enabled (`true`) when `GroupStateImpl` is requested to <<remove, remove the state>>

| timeoutTimestamp
a| [[timeoutTimestamp]][[getTimeoutTimestamp]] Current *timeout timestamp* (in millis) for <<spark-sql-streaming-GroupStateTimeout.adoc#EventTimeTimeout, GroupStateTimeout.EventTimeTimeout>> or <<spark-sql-streaming-GroupStateTimeout.adoc#ProcessingTimeTimeout, GroupStateTimeout.ProcessingTimeTimeout>>

[[NO_TIMESTAMP]] Default: `-1`

Defined using <<setTimeoutTimestamp, setTimeoutTimestamp>> (for `EventTimeTimeout`) and <<setTimeoutDuration, setTimeoutDuration>> (for `ProcessingTimeTimeout`)
|===
