# HDFSBackedStateStore

`HDFSBackedStateStore` is a concrete [StateStore](StateStore.md) that uses a Hadoop DFS-compatible file system for versioned state persistence.

`HDFSBackedStateStore` is <<creating-instance, created>> exclusively when `HDFSBackedStateStoreProvider` is requested for the [specified version of state (store) for update](HDFSBackedStateStoreProvider.md#getStore) (when `StateStore` utility is requested to [look up a StateStore by provider id](StateStore.md#get-StateStore)).

[[id]]
`HDFSBackedStateStore` uses the [StateStoreId](StateStoreId.md) of the owning [HDFSBackedStateStoreProvider](HDFSBackedStateStoreProvider.md#stateStoreId).

[[toString]]
When requested for the textual representation, `HDFSBackedStateStore` gives *HDFSStateStore[id=(op=[operatorId],part=[partitionId]),dir=[baseDir]]*.

[[logging]]
[TIP]
====
`HDFSBackedStateStore` is an internal class of [HDFSBackedStateStoreProvider](HDFSBackedStateStoreProvider.md) and uses its [logger](HDFSBackedStateStoreProvider.md#logging).
====

=== [[creating-instance]] Creating HDFSBackedStateStore Instance

`HDFSBackedStateStore` takes the following to be created:

* [[version]] Version
* [[mapToUpdate]] State Map (`ConcurrentHashMap[UnsafeRow, UnsafeRow]`)

`HDFSBackedStateStore` initializes the <<internal-properties, internal properties>>.

=== [[state]] Internal State -- `state` Internal Property

[source, scala]
----
state: STATE
----

`state` is the current state of `HDFSBackedStateStore` and can be in one of the three possible states: <<ABORTED, ABORTED>>, <<COMMITTED, COMMITTED>>, and <<UPDATING, UPDATING>>.

State changes (to the internal <<mapToUpdate, mapToUpdate>> registry) are allowed as long as `HDFSBackedStateStore` is in the default <<UPDATING, UPDATING>> state. Right after a `HDFSBackedStateStore` transitions to either <<COMMITTED, COMMITTED>> or <<ABORTED, ABORTED>> state, no further state changes are allowed.

NOTE: Don't get confused with the term "state" as there are two states: the internal <<state, state>> of `HDFSBackedStateStore` and the state of a streaming query (that `HDFSBackedStateStore` is responsible for).

[[states]]
.Internal States
[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| ABORTED
a| [[ABORTED]] After <<abort, abort>>

| COMMITTED
a| [[COMMITTED]] After <<commit, commit>>

<<hasCommitted, hasCommitted>> flag indicates whether `HDFSBackedStateStore` is in this state or not.

| UPDATING
a| [[UPDATING]] *(default)* Initial state after the `HDFSBackedStateStore` was <<creating-instance, created>>

Allows for state changes (e.g. <<put, put>>, <<remove, remove>>, <<getRange, getRange>>) and eventually <<commit, committing>> or <<abort, aborting>> them

|===

=== [[writeUpdateToDeltaFile]] `writeUpdateToDeltaFile` Internal Method

[source, scala]
----
writeUpdateToDeltaFile(
  output: DataOutputStream,
  key: UnsafeRow,
  value: UnsafeRow): Unit
----

CAUTION: FIXME

=== [[put]] `put` Method

[source, scala]
----
put(
  key: UnsafeRow,
  value: UnsafeRow): Unit
----

NOTE: `put` is a part of StateStore.md#put[StateStore Contract] to...FIXME

`put` stores the copies of the key and value in <<mapToUpdate, mapToUpdate>> internal registry followed by <<writeUpdateToDeltaFile, writing them to a delta file>> (using <<tempDeltaFileStream, tempDeltaFileStream>>).

`put` reports an `IllegalStateException` when `HDFSBackedStateStore` is not in <<UPDATING, UPDATING>> state:

```
Cannot put after already committed or aborted
```

=== [[commit]] Committing State Changes -- `commit` Method

[source, scala]
----
commit(): Long
----

`commit` is part of the [StateStore](StateStore.md#commit) abstraction.

`commit` requests the parent `HDFSBackedStateStoreProvider` to [commit state changes (as a new version of state)](HDFSBackedStateStoreProvider.md#commitUpdates) (with the <<newVersion, newVersion>>, the <<mapToUpdate, mapToUpdate>> and the <<compressedStream, compressed stream>>).

`commit` transitions `HDFSBackedStateStore` to <<COMMITTED, COMMITTED>> state.

`commit` prints out the following INFO message to the logs:

```text
Committed version [newVersion] for [this] to file [finalDeltaFile]
```

`commit` returns a <<newVersion, newVersion>>.

`commit` throws an `IllegalStateException` when `HDFSBackedStateStore` is not in <<UPDATING, UPDATING>> state:

```
Cannot commit after already committed or aborted
```

`commit` throws an `IllegalStateException` for any `NonFatal` exception:

```
Error committing version [newVersion] into [this]
```

=== [[abort]] Aborting State Changes -- `abort` Method

[source, scala]
----
abort(): Unit
----

`abort` is part of the [StateStore](StateStore.md#abort) abstraction.

`abort`...FIXME

=== [[metrics]] Performance Metrics -- `metrics` Method

[source, scala]
----
metrics: StateStoreMetrics
----

`metrics` is part of the [StateStore](StateStore.md#metrics) abstraction.

`metrics` requests the [performance metrics](HDFSBackedStateStoreProvider.md#getMetricsForProvider) of the parent `HDFSBackedStateStoreProvider`.

The performance metrics of the provider used are only the ones listed in [supportedCustomMetrics](HDFSBackedStateStoreProvider.md#supportedCustomMetrics).

In the end, `metrics` returns a new [StateStoreMetrics](StateStoreMetrics.md) with the following:

* [Total number of keys](StateStoreMetrics.md#numKeys) as the size of <<mapToUpdate, mapToUpdate>>

* [Memory used (in bytes)](StateStoreMetrics.md#memoryUsedBytes) as the [memoryUsedBytes](HDFSBackedStateStoreProvider.md#memoryUsedBytes) metric (of the parent provider)

* [StateStoreCustomMetrics](StateStoreMetrics.md#customMetrics) as the [supportedCustomMetrics](HDFSBackedStateStoreProvider.md#supportedCustomMetrics) and the [metricStateOnCurrentVersionSizeBytes](HDFSBackedStateStoreProvider.md#metricStateOnCurrentVersionSizeBytes) metric of the parent provider

=== [[hasCommitted]] Are State Changes Committed? -- `hasCommitted` Method

[source, scala]
----
hasCommitted: Boolean
----

`hasCommitted` is part of the [StateStore](StateStore.md#hasCommitted) abstraction.

`hasCommitted` returns `true` when `HDFSBackedStateStore` is in <<COMMITTED, COMMITTED>> state and `false` otherwise.

## Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| compressedStream
a| [[compressedStream]]

[source, scala]
----
compressedStream: DataOutputStream
----

The compressed https://docs.oracle.com/javase/8/docs/api/java/io/DataOutputStream.html[java.io.DataOutputStream] for the <<deltaFileStream, deltaFileStream>>

| deltaFileStream
a| [[deltaFileStream]]

[source, scala]
----
deltaFileStream: CheckpointFileManager.CancellableFSDataOutputStream
----

| finalDeltaFile
a| [[finalDeltaFile]]

[source, scala]
----
finalDeltaFile: Path
----

The Hadoop https://hadoop.apache.org/docs/r2.7.3/api/org/apache/hadoop/fs/Path.html[Path] of the [deltaFile](HDFSBackedStateStoreProvider.md#deltaFile) for the [version](#newVersion)

| newVersion
a| [[newVersion]]

[source, scala]
----
newVersion: Long
----

Used exclusively when `HDFSBackedStateStore` is requested for the <<finalDeltaFile, finalDeltaFile>>, to <<commit, commit>> and <<abort, abort>>

|===
