# HDFSBackedStateStoreProvider

`HDFSBackedStateStoreProvider` is a <<spark-sql-streaming-StateStoreProvider.md#, StateStoreProvider>> that uses a Hadoop DFS-compatible file system for <<baseDir, versioned state checkpointing>>.

`HDFSBackedStateStoreProvider` is the default `StateStoreProvider` per the [spark.sql.streaming.stateStore.providerClass](configuration-properties.md#spark.sql.streaming.stateStore.providerClass) internal configuration property.

`HDFSBackedStateStoreProvider` is <<creating-instance, created>> and immediately requested to <<init, initialize>> when `StateStoreProvider` utility is requested to <<spark-sql-streaming-StateStoreProvider.md#createAndInit, create and initialize a StateStoreProvider>>. That is when `HDFSBackedStateStoreProvider` is given the <<stateStoreId, StateStoreId>> that uniquely identifies the <<spark-sql-streaming-StateStore.md#, state store>> to use for a stateful operator and a partition.

`HDFSStateStoreProvider` uses [HDFSBackedStateStores](HDFSBackedStateStore.md) to manage state (<<getStore, one per version>>).

`HDFSBackedStateStoreProvider` manages versioned state in delta and snapshot files (and uses a <<loadedMaps, cache>> internally for faster access to state versions).

[[creating-instance]]
`HDFSBackedStateStoreProvider` takes no arguments to be created.

[[logging]]
[TIP]
====
Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.state.HDFSBackedStateStoreProvider` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.execution.streaming.state.HDFSBackedStateStoreProvider=ALL
```

Refer to <<spark-sql-streaming-spark-logging.md#, Logging>>.
====

=== [[metrics]] Performance Metrics

[cols="30,70",options="header",width="100%"]
|===
| Name (in web UI)
| Description

| memoryUsedBytes
a| [[memoryUsedBytes]] Estimated size of the <<loadedMaps, loadedMaps>> internal registry

| count of cache hit on states cache in provider
a| [[metricLoadedMapCacheHit]][[loadedMapCacheHitCount]] The number of times <<loadMap, loading the specified version of state>> was successful and found (_hit_) the requested state version in the <<loadedMaps, loadedMaps>> internal cache

| count of cache miss on states cache in provider
a| [[metricLoadedMapCacheMiss]][[loadedMapCacheMissCount]] The number of times <<loadMap, loading the specified version of state>> could not find (_missed_) the requested state version in the <<loadedMaps, loadedMaps>> internal cache

| estimated size of state only on current version
a| [[metricStateOnCurrentVersionSizeBytes]][[stateOnCurrentVersionSizeBytes]] Estimated size of the [current state](HDFSBackedStateStore.md#mapToUpdate) (of the [HDFSBackedStateStore](HDFSBackedStateStore.md))

|===

=== [[baseDir]] State Checkpoint Base Directory -- `baseDir` Lazy Internal Property

[source,scala]
----
baseDir: Path
----

`baseDir` is the base directory (as a Hadoop [Path]({{ hadoop.api }}/org/apache/hadoop/fs/Path.html)) for [state checkpointing](offsets-and-metadata-checkpointing.md) (for <<deltaFile, delta>> and <<snapshotFile, snapshot>> state files).

`baseDir` is initialized lazily since it is not yet known when `HDFSBackedStateStoreProvider` is <<creating-instance, created>>.

`baseDir` is initialized and created based on the <<spark-sql-streaming-StateStoreId.md#storeCheckpointLocation, state checkpoint base directory>> of the <<stateStoreId, StateStoreId>> when `HDFSBackedStateStoreProvider` is requested to <<init, initialize>>.

=== [[stateStoreId]][[stateStoreId_]] StateStoreId -- Unique Identifier of State Store

As a <<spark-sql-streaming-StateStoreProvider.md#, StateStoreProvider>>, `HDFSBackedStateStoreProvider` is associated with a <<spark-sql-streaming-StateStoreProvider.md#stateStoreId, StateStoreId>> (which is a unique identifier of the <<spark-sql-streaming-StateStore.md#, state store>> for a stateful operator and a partition).

`HDFSBackedStateStoreProvider` is given the <<stateStoreId, StateStoreId>> at <<init, initialization>> (as requested by the <<spark-sql-streaming-StateStoreProvider.md#, StateStoreProvider>> contract).

The <<stateStoreId, StateStoreId>> is then used for the following:

* `HDFSBackedStateStore` is requested for the [id](HDFSBackedStateStore.md#id)

* `HDFSBackedStateStoreProvider` is requested for the <<toString, textual representation>> and the <<baseDir, state checkpoint base directory>>

=== [[toString]] Textual Representation -- `toString` Method

[source, scala]
----
toString: String
----

NOTE: `toString` is part of the ++https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#toString--++[java.lang.Object] contract for the string representation of the object.

`HDFSBackedStateStoreProvider` uses the <<stateStoreId, StateStoreId>> and the <<baseDir, state checkpoint base directory>> for the textual representation:

```
HDFSStateStoreProvider[id = (op=[operatorId],part=[partitionId]),dir = [baseDir]]
```

=== [[getStore]] Loading Specified Version of State (Store) For Update -- `getStore` Method

[source, scala]
----
getStore(
  version: Long): StateStore
----

NOTE: `getStore` is part of the <<spark-sql-streaming-StateStoreProvider.md#getStore, StateStoreProvider Contract>> for the <<spark-sql-streaming-StateStore.md#, StateStore>> for a specified version.

`getStore` creates a new empty state (`ConcurrentHashMap[UnsafeRow, UnsafeRow]`) and <<loadMap, loads the specified version of state (from internal cache or snapshot and delta files)>> for versions greater than `0`.

In the end, `getStore` creates a new [HDFSBackedStateStore](HDFSBackedStateStore.md) for the specified version with the new state and prints out the following INFO message to the logs:

```
Retrieved version [version] of [this] for update
```

`getStore` throws an `IllegalArgumentException` when the specified version is less than `0` (negative):

```
Version cannot be less than 0
```

=== [[deltaFile]] `deltaFile` Internal Method

[source, scala]
----
deltaFile(version: Long): Path
----

`deltaFile` simply returns the Hadoop https://hadoop.apache.org/docs/r2.7.3/api/org/apache/hadoop/fs/Path.html[Path] of the `[version].delta` file in the <<baseDir, state checkpoint base directory>>.

`deltaFile` is used when:

* [HDFSBackedStateStore](HDFSBackedStateStore.md) is created (and creates the <<finalDeltaFile, final delta file>>)

* `HDFSBackedStateStoreProvider` is requested to [updateFromDeltaFile](#updateFromDeltaFile)

=== [[snapshotFile]] `snapshotFile` Internal Method

[source, scala]
----
snapshotFile(version: Long): Path
----

`snapshotFile` simply returns the Hadoop https://hadoop.apache.org/docs/r2.7.3/api/org/apache/hadoop/fs/Path.html[Path] of the `[version].snapshot` file in the <<baseDir, state checkpoint base directory>>.

NOTE: `snapshotFile` is used when `HDFSBackedStateStoreProvider` is requested to <<writeSnapshotFile, writeSnapshotFile>> or <<readSnapshotFile, readSnapshotFile>>.

=== [[fetchFiles]] Listing All Delta And Snapshot Files In State Checkpoint Directory -- `fetchFiles` Internal Method

[source, scala]
----
fetchFiles(): Seq[StoreFile]
----

`fetchFiles` requests the <<fm, CheckpointFileManager>> for [all the files](CheckpointFileManager.md#list) in the <<baseDir, state checkpoint directory>>.

For every file, `fetchFiles` splits the name into two parts with `.` (dot) as a separator (files with more or less than two parts are simply ignored) and registers a new `StoreFile` for `snapshot` and `delta` files:

* For `snapshot` files, `fetchFiles` creates a new `StoreFile` with `isSnapshot` flag on (`true`)

* For `delta` files, `fetchFiles` creates a new `StoreFile` with `isSnapshot` flag off (`false`)

NOTE: `delta` files are only registered if there was no `snapshot` file for the version.

`fetchFiles` prints out the following WARN message to the logs for any other files:

```
Could not identify file [path] for [this]
```

In the end, `fetchFiles` sorts the `StoreFiles` based on their version, prints out the following DEBUG message to the logs, and returns the files.

```
Current set of files for [this]: [storeFiles]
```

NOTE: `fetchFiles` is used when `HDFSBackedStateStoreProvider` is requested to <<doSnapshot, doSnapshot>> and <<cleanup, cleanup>>.

=== [[init]] Initializing StateStoreProvider -- `init` Method

[source, scala]
----
init(
  stateStoreId: StateStoreId,
  keySchema: StructType,
  valueSchema: StructType,
  indexOrdinal: Option[Int],
  storeConf: StateStoreConf,
  hadoopConf: Configuration): Unit
----

NOTE: `init` is part of the <<spark-sql-streaming-StateStoreProvider.md#init, StateStoreProvider Contract>> to initialize itself.

`init` records the values of the input arguments as the <<stateStoreId, stateStoreId>>, <<keySchema, keySchema>>, <<valueSchema, valueSchema>>, <<storeConf, storeConf>>, and <<hadoopConf, hadoopConf>> internal properties.

`init` requests the given `StateStoreConf` for the [spark.sql.streaming.maxBatchesToRetainInMemory](StateStoreConf.md#maxVersionsToRetainInMemory) configuration property (that is then recorded in the <<numberOfVersionsToRetainInMemory, numberOfVersionsToRetainInMemory>> internal property).

In the end, `init` requests the <<fm, CheckpointFileManager>> to [create](CheckpointFileManager.md#mkdirs) the <<baseDir, baseDir>> directory (with parent directories).

=== [[filesForVersion]] Finding Snapshot File and Delta Files For Version -- `filesForVersion` Internal Method

[source, scala]
----
filesForVersion(
  allFiles: Seq[StoreFile],
  version: Long): Seq[StoreFile]
----

`filesForVersion` finds the latest snapshot version among the given `allFiles` files up to and including the given version (it may or may not be available).

If a snapshot file was found (among the given file up to and including the given version), `filesForVersion` takes all delta files between the version of the snapshot file (exclusive) and the given version (inclusive) from the given `allFiles` files.

NOTE: The number of delta files should be the given version minus the snapshot version.

If a snapshot file was not found, `filesForVersion` takes all delta files up to the given version (inclusive) from the given `allFiles` files.

In the end, `filesForVersion` returns a snapshot version (if available) and all delta files up to the given version (inclusive).

NOTE: `filesForVersion` is used when `HDFSBackedStateStoreProvider` is requested to <<doSnapshot, doSnapshot>> and <<cleanup, cleanup>>.

=== [[doMaintenance]] State Maintenance (Snapshotting and Cleaning Up) -- `doMaintenance` Method

[source, scala]
----
doMaintenance(): Unit
----

NOTE: `doMaintenance` is part of the <<spark-sql-streaming-StateStoreProvider.md#doMaintenance, StateStoreProvider Contract>> for optional state maintenance.

`doMaintenance` simply does <<doSnapshot, state snapshoting>> followed by <<cleanup, cleaning up (removing old state files)>>.

In case of any non-fatal errors, `doMaintenance` simply prints out the following WARN message to the logs:

```
Error performing snapshot and cleaning up [this]
```

==== [[doSnapshot]] State Snapshoting (Rolling Up Delta Files into Snapshot File) -- `doSnapshot` Internal Method

[source, scala]
----
doSnapshot(): Unit
----

`doSnapshot` <<fetchFiles, lists all delta and snapshot files in the state checkpoint directory>> (`files`) and prints out the following DEBUG message to the logs:

```
fetchFiles() took [time] ms.
```

`doSnapshot` returns immediately (and does nothing) when there are no delta and snapshot files.

`doSnapshot` takes the version of the latest file (`lastVersion`).

`doSnapshot` <<filesForVersion, finds the snapshot file and delta files for the version>> (among the files and for the last version).

`doSnapshot` looks up the last version in the <<loadedMaps, internal state cache>>.

When the last version was found in the cache and the number of delta files is above [spark.sql.streaming.stateStore.minDeltasForSnapshot](configuration-properties.md#spark.sql.streaming.stateStore.minDeltasForSnapshot) internal threshold, `doSnapshot` <<writeSnapshotFile, writes a compressed snapshot file for the last version>>.

In the end, `doSnapshot` prints out the following DEBUG message to the logs:

```
writeSnapshotFile() took [time] ms.
```

In case of non-fatal errors, `doSnapshot` simply prints out the following WARN message to the logs:

```
Error doing snapshots for [this]
```

NOTE: `doSnapshot` is used exclusively when `HDFSBackedStateStoreProvider` is requested to <<doMaintenance, do state maintenance (state snapshotting and cleaning up)>>.

==== [[cleanup]] Cleaning Up (Removing Old State Files) -- `cleanup` Internal Method

[source, scala]
----
cleanup(): Unit
----

`cleanup` <<fetchFiles, lists all delta and snapshot files in the state checkpoint directory>> (`files`) and prints out the following DEBUG message to the logs:

```
fetchFiles() took [time] ms.
```

`cleanup` returns immediately (and does nothing) when there are no delta and snapshot files.

`cleanup` takes the version of the latest state file (`lastVersion`) and decrements it by [spark.sql.streaming.minBatchesToRetain](configuration-properties.md#spark.sql.streaming.minBatchesToRetain) configuration property that gives the earliest version to retain (and all older state files to be removed).

`cleanup` requests the <<fm, CheckpointFileManager>> to [delete the path](CheckpointFileManager.md#delete) of every old state file.

`cleanup` prints out the following DEBUG message to the logs:

```
deleting files took [time] ms.
```

In the end, `cleanup` prints out the following INFO message to the logs:

```
Deleted files older than [version] for [this]: [filesToDelete]
```

In case of a non-fatal exception, `cleanup` prints out the following WARN message to the logs:

```
Error cleaning up files for [this]
```

NOTE: `cleanup` is used exclusively when `HDFSBackedStateStoreProvider` is requested for <<doMaintenance, state maintenance (state snapshotting and cleaning up)>>.

=== [[close]] Closing State Store Provider -- `close` Method

[source, scala]
----
close(): Unit
----

NOTE: `close` is part of the <<spark-sql-streaming-StateStoreProvider.md#close, StateStoreProvider Contract>> to close the state store provider.

`close`...FIXME

=== [[getMetricsForProvider]] `getMetricsForProvider` Method

[source, scala]
----
getMetricsForProvider(): Map[String, Long]
----

`getMetricsForProvider` returns the following <<metrics, performance metrics>>:

* <<memoryUsedBytes, memoryUsedBytes>>

* <<metricLoadedMapCacheHit, metricLoadedMapCacheHit>>

* <<metricLoadedMapCacheMiss, metricLoadedMapCacheMiss>>

`getMetricsForProvider` is used when `HDFSBackedStateStore` is requested for [performance metrics](HDFSBackedStateStore.md#metrics).

=== [[supportedCustomMetrics]] Supported StateStoreCustomMetrics -- `supportedCustomMetrics` Method

[source, scala]
----
supportedCustomMetrics: Seq[StateStoreCustomMetric]
----

NOTE: `supportedCustomMetrics` is part of the <<spark-sql-streaming-StateStoreProvider.md#supportedCustomMetrics, StateStoreProvider Contract>> for the <<spark-sql-streaming-StateStoreCustomMetric.md#, StateStoreCustomMetrics>> of a state store provider.

`supportedCustomMetrics` includes the following <<spark-sql-streaming-StateStoreCustomMetric.md#, StateStoreCustomMetrics>>:

* <<metricStateOnCurrentVersionSizeBytes, metricStateOnCurrentVersionSizeBytes>>

* <<metricLoadedMapCacheHit, metricLoadedMapCacheHit>>

* <<metricLoadedMapCacheMiss, metricLoadedMapCacheMiss>>

=== [[commitUpdates]] Committing State Changes (As New Version of State) -- `commitUpdates` Internal Method

[source, scala]
----
commitUpdates(
  newVersion: Long,
  map: ConcurrentHashMap[UnsafeRow, UnsafeRow],
  output: DataOutputStream): Unit
----

`commitUpdates` <<finalizeDeltaFile, finalizeDeltaFile>> (with the given `DataOutputStream`) followed by <<putStateIntoStateCacheMap, caching the new version of state>> (with the given `newVersion` and the `map` state).

`commitUpdates` is used when `HDFSBackedStateStore` is requested to [commit state changes](HDFSBackedStateStore.md#commit).

=== [[loadMap]] Loading Specified Version of State (from Internal Cache or Snapshot and Delta Files) -- `loadMap` Internal Method

[source, scala]
----
loadMap(
  version: Long): ConcurrentHashMap[UnsafeRow, UnsafeRow]
----

`loadMap` firstly tries to find the state version in the <<loadedMaps, loadedMaps>> internal cache and, if found, returns it immediately and increments the <<loadedMapCacheHitCount, loadedMapCacheHitCount>> metric.

If the requested state version could not be found in the <<loadedMaps, loadedMaps>> internal cache, `loadMap` prints out the following WARN message to the logs:

[options="wrap"]
----
The state for version [version] doesn't exist in loadedMaps. Reading snapshot file and delta files if needed...Note that this is normal for the first batch of starting query.
----

`loadMap` increments the <<loadedMapCacheMissCount, loadedMapCacheMissCount>> metric.

`loadMap` <<readSnapshotFile, tries to load the state snapshot file for the version>> and, if found, <<putStateIntoStateCacheMap, puts the version of state in the internal cache>> and returns it.

If not found, `loadMap` tries to find the most recent state version by decrementing the requested version until one is found in the <<loadedMaps, loadedMaps>> internal cache or <<readSnapshotFile, loaded from a state snapshot (file)>>.

`loadMap` <<updateFromDeltaFile, updateFromDeltaFile>> for all the remaining versions (from the snapshot version up to the requested one). `loadMap` <<putStateIntoStateCacheMap, puts the final version of state in the internal cache>> (the closest snapshot and the remaining delta versions) and returns it.

In the end, `loadMap` prints out the following DEBUG message to the logs:

```
Loading state for [version] takes [elapsedMs] ms.
```

NOTE: `loadMap` is used exclusively when `HDFSBackedStateStoreProvider` is requested for the <<getStore, specified version of a state store for update>>.

=== [[readSnapshotFile]] Loading State Snapshot File For Specified Version -- `readSnapshotFile` Internal Method

[source, scala]
----
readSnapshotFile(
  version: Long): Option[ConcurrentHashMap[UnsafeRow, UnsafeRow]]
----

`readSnapshotFile` <<snapshotFile, creates the path of the snapshot file>> for the given `version`.

`readSnapshotFile` requests the <<fm, CheckpointFileManager>> to [open the snapshot file for reading](CheckpointFileManager.md#open) and <<decompressStream, creates a decompressed DataInputStream>> (`input`).

`readSnapshotFile` reads the decompressed input stream until an EOF (that is marked as the integer `-1` in the stream) and inserts key and value rows in a state map (`ConcurrentHashMap[UnsafeRow, UnsafeRow]`):

* First integer is the size of a key (buffer) followed by the key itself (of the size). `readSnapshotFile` creates an `UnsafeRow` for the key (with the number of fields as indicated by the number of fields of the <<keySchema, key schema>>).

* Next integer is the size of a value (buffer) followed by the value itself (of the size). `readSnapshotFile` creates an `UnsafeRow` for the value (with the number of fields as indicated by the number of fields of the <<valueSchema, value schema>>).

In the end, `readSnapshotFile` prints out the following INFO message to the logs and returns the key-value map.

```
Read snapshot file for version [version] of [this] from [fileToRead]
```

In case of `FileNotFoundException` `readSnapshotFile` simply returns `None` (to indicate no snapshot state file was available and so no state for the version).

`readSnapshotFile` throws an `IOException` for the size of a key or a value below `0`:

```
Error reading snapshot file [fileToRead] of [this]: [key|value] size cannot be [keySize|valueSize]
```

NOTE: `readSnapshotFile` is used exclusively when `HDFSBackedStateStoreProvider` is requested to <<loadMap, load the specified version of state (from the internal cache or snapshot and delta files)>>.

=== [[updateFromDeltaFile]] Updating State with State Changes For Specified Version (per Delta File) -- `updateFromDeltaFile` Internal Method

[source, scala]
----
updateFromDeltaFile(
  version: Long,
  map: ConcurrentHashMap[UnsafeRow, UnsafeRow]): Unit
----

[NOTE]
====
`updateFromDeltaFile` is very similar code-wise to <<readSnapshotFile, readSnapshotFile>> with the two main differences:

* `updateFromDeltaFile` is given the state map to update (while <<readSnapshotFile, readSnapshotFile>> loads the state from a snapshot file)

* `updateFromDeltaFile` removes a key from the state map when the value (size) is `-1` (while <<readSnapshotFile, readSnapshotFile>> throws an `IOException`)

The following description is almost an exact copy of <<readSnapshotFile, readSnapshotFile>> just for completeness.
====

`updateFromDeltaFile` <<deltaFile, creates the path of the delta file>> for the requested `version`.

`updateFromDeltaFile` requests the <<fm, CheckpointFileManager>> to [open the delta file for reading](CheckpointFileManager.md#open) and <<decompressStream, creates a decompressed DataInputStream>> (`input`).

`updateFromDeltaFile` reads the decompressed input stream until an EOF (that is marked as the integer `-1` in the stream) and inserts key and value rows in the given state map:

* First integer is the size of a key (buffer) followed by the key itself (of the size). `updateFromDeltaFile` creates an `UnsafeRow` for the key (with the number of fields as indicated by the number of fields of the <<keySchema, key schema>>).

* Next integer is the size of a value (buffer) followed by the value itself (of the size). `updateFromDeltaFile` creates an `UnsafeRow` for the value (with the number of fields as indicated by the number of fields of the <<valueSchema, value schema>>) or removes the corresponding key from the state map (if the value size is `-1`)

NOTE: `updateFromDeltaFile` removes the key-value entry from the state map if the value (size) is `-1`.

In the end, `updateFromDeltaFile` prints out the following INFO message to the logs and returns the key-value map.

```
Read delta file for version [version] of [this] from [fileToRead]
```

`updateFromDeltaFile` throws an `IllegalStateException` in case of `FileNotFoundException` while opening the delta file for the specified version:

```
Error reading delta file [fileToRead] of [this]: [fileToRead] does not exist
```

NOTE: `updateFromDeltaFile` is used exclusively when `HDFSBackedStateStoreProvider` is requested to <<loadMap, load the specified version of state (from the internal cache or snapshot and delta files)>>.

=== [[putStateIntoStateCacheMap]] Caching New Version of State -- `putStateIntoStateCacheMap` Internal Method

[source, scala]
----
putStateIntoStateCacheMap(
  newVersion: Long,
  map: ConcurrentHashMap[UnsafeRow, UnsafeRow]): Unit
----

`putStateIntoStateCacheMap` registers state for a given version, i.e. adds the `map` state under the `newVersion` key in the <<loadedMaps, loadedMaps>> internal registry.

With the <<numberOfVersionsToRetainInMemory, numberOfVersionsToRetainInMemory>> threshold as `0` or below, `putStateIntoStateCacheMap` simply removes all entries from the <<loadedMaps, loadedMaps>> internal registry and returns.

`putStateIntoStateCacheMap` removes the oldest state version(s) in the <<loadedMaps, loadedMaps>> internal registry until its size is at the <<numberOfVersionsToRetainInMemory, numberOfVersionsToRetainInMemory>> threshold.

With the size of the <<loadedMaps, loadedMaps>> internal registry is at the <<numberOfVersionsToRetainInMemory, numberOfVersionsToRetainInMemory>> threshold, `putStateIntoStateCacheMap` does two more optimizations per `newVersion`

* It does not add the given state when the version of the oldest state is earlier (larger) than the given `newVersion`

* It removes the oldest state when older (smaller) than the given `newVersion`

NOTE: `putStateIntoStateCacheMap` is used when `HDFSBackedStateStoreProvider` is requested to <<commitUpdates, commit state (as a new version)>> and <<loadMap, load the specified version of state (from the internal cache or snapshot and delta files)>>.

=== [[writeSnapshotFile]] Writing Compressed Snapshot File for Specified Version -- `writeSnapshotFile` Internal Method

[source, scala]
----
writeSnapshotFile(
  version: Long,
  map: ConcurrentHashMap[UnsafeRow, UnsafeRow]): Unit
----

`writeSnapshotFile` <<snapshotFile, snapshotFile>> for the given version.

`writeSnapshotFile` requests the <<fm, CheckpointFileManager>> to [create the snapshot file](CheckpointFileManager.md#createAtomic) (with overwriting enabled) and <<compressStream, compress the stream>>.

For every key-value `UnsafeRow` pair in the given map, `writeSnapshotFile` writes the size of the key followed by the key itself (as bytes). `writeSnapshotFile` then writes the size of the value followed by the value itself (as bytes).

In the end, `writeSnapshotFile` prints out the following INFO message to the logs:

```text
Written snapshot file for version [version] of [this] at [targetFile]
```

In case of any `Throwable` exception, `writeSnapshotFile` <<cancelDeltaFile, cancelDeltaFile>> and re-throws the exception.

NOTE: `writeSnapshotFile` is used exclusively when `HDFSBackedStateStoreProvider` is requested to <<doSnapshot, doSnapshot>>.

=== [[compressStream]] `compressStream` Internal Method

[source, scala]
----
compressStream(
  outputStream: DataOutputStream): DataOutputStream
----

`compressStream` creates a new `LZ4CompressionCodec` (based on the <<sparkConf, SparkConf>>) and requests it to create a `LZ4BlockOutputStream` with the given `DataOutputStream`.

In the end, `compressStream` creates a new `DataOutputStream` with the `LZ4BlockOutputStream`.

NOTE: `compressStream` is used when...FIXME

=== [[cancelDeltaFile]] `cancelDeltaFile` Internal Method

[source, scala]
----
cancelDeltaFile(
  compressedStream: DataOutputStream,
  rawStream: CancellableFSDataOutputStream): Unit
----

`cancelDeltaFile`...FIXME

NOTE: `cancelDeltaFile` is used when...FIXME

=== [[finalizeDeltaFile]] `finalizeDeltaFile` Internal Method

[source, scala]
----
finalizeDeltaFile(
  output: DataOutputStream): Unit
----

`finalizeDeltaFile` simply writes `-1` to the given `DataOutputStream` (to indicate end of file) and closes it.

NOTE: `finalizeDeltaFile` is used exclusively when `HDFSBackedStateStoreProvider` is requested to <<commitUpdates, commit state changes (a new version of state)>>.

=== [[loadedMaps]] Lookup Table (Cache) of States By Version -- `loadedMaps` Internal Method

[source, scala]
----
loadedMaps: TreeMap[
  Long,                                    // version
  ConcurrentHashMap[UnsafeRow, UnsafeRow]] // state (as keys and values)
----

`loadedMaps` is a https://docs.oracle.com/javase/8/docs/api/java/util/TreeMap.html[java.util.TreeMap] of state versions sorted according to the reversed ordering of the versions (i.e. long numbers).

A new entry (a version and the state updates) can only be added when `HDFSBackedStateStoreProvider` is requested to <<putStateIntoStateCacheMap, putStateIntoStateCacheMap>> (and only when the [spark.sql.streaming.maxBatchesToRetainInMemory](configuration-properties.md#spark.sql.streaming.maxBatchesToRetainInMemory) internal configuration is above `0`).

`loadedMaps` is mainly used when `HDFSBackedStateStoreProvider` is requested to <<loadMap, load the specified version of state (from the internal cache or snapshot and delta files)>>. Positive hits (when a version could be found in the cache) is available as the <<loadedMapCacheHitCount, count of cache hit on states cache in provider>> performance metric while misses are counted in the <<loadedMapCacheMissCount, count of cache miss on states cache in provider>> performance metric.

NOTE: With no or missing versions in cache <<loadedMapCacheMissCount, count of cache miss on states cache in provider>> metric should be above `0` while <<loadedMapCacheHitCount, count of cache hit on states cache in provider>> always `0` (or smaller than the other metric).

The estimated size of `loadedMaps` is available as the <<memoryUsedBytes, memoryUsedBytes>> performance metric.

The [spark.sql.streaming.maxBatchesToRetainInMemory](configuration-properties.md#spark.sql.streaming.maxBatchesToRetainInMemory) internal configuration is used as the threshold of the number of elements in `loadedMaps`. When `0` or negative, every <<putStateIntoStateCacheMap, putStateIntoStateCacheMap>> removes all elements in (_clears_) `loadedMaps`.

NOTE: It is possible to change the configuration at restart of a structured query.

The state deltas (the values) in `loadedMaps` are cleared (all entries removed) when `HDFSBackedStateStoreProvider` is requested to <<close, close>>.

Used when `HDFSBackedStateStoreProvider` is requested for the following:

* <<putStateIntoStateCacheMap, Cache a version of state>>

* <<loadMap, Loading the specified version of state (from the internal cache or snapshot and delta files)>>

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| fm
a| [[fm]] [CheckpointFileManager](CheckpointFileManager.md) for the <<baseDir, state checkpoint base directory>> (and the <<hadoopConf, Hadoop Configuration>>)

Used when:

* Creating a new [HDFSBackedStateStore](HDFSBackedStateStore.md) (to create the [CancellableFSDataOutputStream](HDFSBackedStateStore.md#deltaFileStream) for the [finalDeltaFile](HDFSBackedStateStore.md#finalDeltaFile))

* `HDFSBackedStateStoreProvider` is requested to <<init, initialize>> (to create the <<baseDir, state checkpoint base directory>>), <<updateFromDeltaFile, updateFromDeltaFile>>, <<writeSnapshotFile, write the compressed snapshot file for a specified state version>>, <<readSnapshotFile, readSnapshotFile>>, <<cleanup, clean up>>, and <<fetchFiles, list all delta and snapshot files in the state checkpoint directory>>

| hadoopConf
a| [[hadoopConf]] Hadoop https://hadoop.apache.org/docs/r2.7.3/api/org/apache/hadoop/conf/Configuration.html[Configuration] of the <<fm, CheckpointFileManager>>

Given when `HDFSBackedStateStoreProvider` is requested to <<init, initialize>>

| keySchema
a| [[keySchema]]

[source, scala]
----
keySchema: StructType
----

Schema of the state keys

| valueSchema
a| [[valueSchema]]

[source, scala]
----
valueSchema: StructType
----

Schema of the state values

| numberOfVersionsToRetainInMemory
a| [[numberOfVersionsToRetainInMemory]]

[source, scala]
----
numberOfVersionsToRetainInMemory: Int
----

`numberOfVersionsToRetainInMemory` is the maximum number of entries in the <<loadedMaps, loadedMaps>> internal registry and is configured by the [spark.sql.streaming.maxBatchesToRetainInMemory](configuration-properties.md#spark.sql.streaming.maxBatchesToRetainInMemory) internal configuration.

`numberOfVersionsToRetainInMemory` is a threshold when `HDFSBackedStateStoreProvider` removes the last key from the <<loadedMaps, loadedMaps>> internal registry (per reverse ordering of state versions) when requested to <<putStateIntoStateCacheMap, putStateIntoStateCacheMap>>.

| sparkConf
a| [[sparkConf]] `SparkConf`

|===
