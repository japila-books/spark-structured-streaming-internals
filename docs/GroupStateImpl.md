== [[GroupStateImpl]] GroupStateImpl

`GroupStateImpl` is the default and only known [GroupState](GroupState.md) in Spark Structured Streaming.

`GroupStateImpl` holds per-group <<optionalValue, state value>> of type `S` per group key.

`GroupStateImpl` is <<creating-instance, created>> when `GroupStateImpl` helper object is requested for the following:

* <<createForStreaming, createForStreaming>>

* <<createForBatch, createForBatch>>

=== [[creating-instance]] Creating GroupStateImpl Instance

`GroupStateImpl` takes the following to be created:

* [[optionalValue]] State value (of type `S`)
* [[batchProcessingTimeMs]] <<spark-structured-streaming-batch-processing-time.md#, Batch processing time>>
* [[eventTimeWatermarkMs]] `eventTimeWatermarkMs`
* [[timeoutConf]] <<spark-sql-streaming-GroupStateTimeout.md#, GroupStateTimeout>>
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

NOTE: `createForStreaming` is used exclusively when `InputProcessor` is requested to [callFunctionAndUpdateState](InputProcessor.md#callFunctionAndUpdateState) (when `InputProcessor` is requested to [processNewData](InputProcessor.md#processNewData) and [processTimedOutState](InputProcessor.md#processTimedOutState)).

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

`setTimeoutDuration`...FIXME

`setTimeoutDuration` is part of the [GroupState](GroupState.md#setTimeoutDuration) abstraction.

=== [[setTimeoutTimestamp]] Specifying Timeout Timestamp for EventTimeTimeout -- `setTimeoutTimestamp` Method

[source, scala]
----
setTimeoutTimestamp(durationMs: Long): Unit
----

`setTimeoutTimestamp`...FIXME

`setTimeoutTimestamp` is part of the [GroupState](GroupState.md#setTimeoutTimestamp) abstraction.

=== [[getCurrentProcessingTimeMs]] Getting Processing Time -- `getCurrentProcessingTimeMs` Method

[source, scala]
----
getCurrentProcessingTimeMs(): Long
----

`getCurrentProcessingTimeMs` simply returns the <<batchProcessingTimeMs, batchProcessingTimeMs>>.

`getCurrentProcessingTimeMs` is part of the [GroupState](GroupState.md#getCurrentProcessingTimeMs) abstraction.

=== [[update]] Updating State -- `update` Method

[source, scala]
----
update(newValue: S): Unit
----

`update`...FIXME

`update` is part of the [GroupState](GroupState.md#update) abstraction.

=== [[remove]] Removing State -- `remove` Method

[source, scala]
----
remove(): Unit
----

`remove`...FIXME

`remove` is part of the [GroupState](GroupState.md#remove) abstraction.

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
a| [[timeoutTimestamp]][[getTimeoutTimestamp]] Current *timeout timestamp* (in millis) for <<spark-sql-streaming-GroupStateTimeout.md#EventTimeTimeout, GroupStateTimeout.EventTimeTimeout>> or <<spark-sql-streaming-GroupStateTimeout.md#ProcessingTimeTimeout, GroupStateTimeout.ProcessingTimeTimeout>>

[[NO_TIMESTAMP]] Default: `-1`

Defined using <<setTimeoutTimestamp, setTimeoutTimestamp>> (for `EventTimeTimeout`) and <<setTimeoutDuration, setTimeoutDuration>> (for `ProcessingTimeTimeout`)
|===
