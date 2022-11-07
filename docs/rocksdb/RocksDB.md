# RocksDB

## Creating Instance

`RocksDB` takes the following to be created:

* <span id="dfsRootDir"> DFS Root Directory
* <span id="conf"> [RocksDBConf](RocksDBConf.md)
* <span id="localRootDir"> Local root directory
* <span id="hadoopConf"> Hadoop `Configuration`
* <span id="loggingId"> Logging ID

`RocksDB` is created when:

* `RocksDBStateStoreProvider` is requested for the [RocksDB](RocksDBStateStoreProvider.md#rocksDB)

### <span id="commitLatencyMs"> commitLatencyMs

```scala
commitLatencyMs: HashMap[String, Long]
```

`RocksDB` creates an empty `commitLatencyMs` collection when [created](#creating-instance).

The following durations are added at the end of [committing state changes](#commit):

* `checkpoint`
* `compact`
* `fileSync`
* `flush`
* `pause`
* `writeBatch`

`commitLatencyMs` is used in [metrics](#metrics) to create a [RocksDBMetrics](RocksDBMetrics.md#lastCommitLatencyMs).

### <span id="readOptions"> ReadOptions

`RocksDB` creates a `ReadOptions` ([RocksDB]({{ rocksdb.api }}/org/rocksdb/ReadOptions.html)) when [created](#creating-instance).

Used when:

* [get](#get)
* [put](#put) (with [trackTotalNumberOfRows](RocksDBConf.md#trackTotalNumberOfRows) enabled)
* [remove](#remove) (with [trackTotalNumberOfRows](RocksDBConf.md#trackTotalNumberOfRows) enabled)

Closed in [close](#close)

### <span id="writeBatch"> WriteBatchWithIndex

`RocksDB` creates a `WriteBatchWithIndex` ([RocksDB]({{ rocksdb.api }}/org/rocksdb/WriteBatchWithIndex.html)) (with `overwriteKey` enabled) when [created](#creating-instance).

### <span id="tableFormatConfig"> BlockBasedTableConfig

`RocksDB` creates a `BlockBasedTableConfig` ([RocksDB]({{ rocksdb.api }}/org/rocksdb/BlockBasedTableConfig.html)) when [created](#creating-instance).

??? quote "BlockBasedTableConfig"
    `BlockBasedTableConfig` is the config for plain table sst format. `BlockBasedTable` is a RocksDB's default SST file format.

The `BlockBasedTableConfig` is used to create an [Options](#dbOptions).

### <span id="dbOptions"> Options

`RocksDB` creates an `Options` ([RocksDB]({{ rocksdb.api }}/org/rocksdb/Options.html)) when [created](#creating-instance).

The `Options` is used in the following:

* [Opening a RocksDB database](#openDB)
* [Creating a native logger](#createLogger)

## <span id="metrics"> Performance Metrics

```scala
metrics: RocksDBMetrics
```

`metrics` reads the following [RocksDB](#db) properties:

* `rocksdb.total-sst-files-size`
* `rocksdb.estimate-table-readers-mem`
* `rocksdb.size-all-mem-tables`
* `rocksdb.block-cache-usage`

`metrics` computes `writeBatchMemUsage` by requesting the RocksDB [WriteBatchWithIndex](#writeBatch) for `WriteBatch` to `getDataSize`.

`metrics` computes [nativeOpsHistograms](RocksDBMetrics.md#nativeOpsHistograms).

`metrics` computes [nativeOpsMetrics](RocksDBMetrics.md#nativeOpsMetrics).

In the end, `metrics` creates a [RocksDBMetrics](RocksDBMetrics.md) with the following:

* [numKeysOnLoadedVersion](#numKeysOnLoadedVersion)
* [numKeysOnWritingVersion](#numKeysOnWritingVersion)
* [commitLatencyMs](#commitLatencyMs)
* _others_

---

`metrics` is used when:

* `RocksDB` is requested to [commit](#commit)
* `RocksDBStateStore` is requested for [metrics](RocksDBStateStore.md#metrics)

## <span id="nativeStats"> Statistics

```scala
nativeStats: Statistics
```

`RocksDB` requests [Options](#dbOptions) for a `Statistics` ([RocksDB]({{ rocksdb.api }}/org/rocksdb/Statistics.html)) to initialize `nativeStats` when [created](#creating-instance).

`nativeStats` is used when:

* [Load a version](#load) (that can reset the statistics when [resetStatsOnLoad](RocksDBConf.md#resetStatsOnLoad) is enabled)
* Requested for the [metrics](#metrics) (for `getHistogramData` and `getTickerCount`)

## <span id="get"> Retrieving Value for Key

```scala
get(
  key: Array[Byte]): Array[Byte]
```

`get` requests the `WriteBatchWithIndex` to `getFromBatchAndDB` the given `key` from the [NativeRocksDB](#db) (with the [ReadOptions](#readOptions)).

---

`get` is used when:

* `RocksDBStateStore` is requested to [get a value for a key](RocksDBStateStore.md#get)

## <span id="commit"> Committing State Changes

```scala
commit(): Long
```

`commit` builds a new version by incrementing the [loadedVersion](#loadedVersion).

`commit` creates a new `checkpoint-`-prefixed directory under the [localRootDir](#localRootDir).

`commit` prints out the following INFO message to the logs and records the duration of requesting the [db](#db) to write out the updates (`writeTimeMs`).

```text
Writing updates for [newVersion]
```

`commit` prints out the following INFO message to the logs and records the duration of requesting the [db](#db) to flush the changes (`flushTimeMs`).

```text
Flushing updates for [newVersion]
```

With [spark.sql.streaming.stateStore.rocksdb.compactOnCommit](RocksDBConf.md#compactOnCommit) enabled, `commit` prints out the following INFO message to the logs and records the duration of requesting the [db](#db) to `compactRange` (`compactTimeMs`). Otherwise, the compact time is `0`.

```text
Compacting
```

`commit` prints out the following INFO message to the logs and records the duration of requesting the [db](#db) to `pauseBackgroundWork` (`pauseTimeMs`).

```text
Pausing background work
```

`commit` prints out the following INFO message to the logs and records the duration of requesting the [db](#db) to `createCheckpoint` in the checkpoint directory (`checkpointTimeMs`).

```text
Creating checkpoint for [newVersion] in [checkpointDir]
```

`commit` prints out the following INFO message to the logs and records the duration of requesting the [RocksDBFileManager](#fileManager) to [saveCheckpointToDfs](RocksDBFileManager.md#saveCheckpointToDfs) with the checkpoint directory and the [numKeysOnWritingVersion](#numKeysOnWritingVersion) (`fileSyncTimeMs`).

```text
Syncing checkpoint for [newVersion] to DFS
```

`commit` updates the internal registries.

Internal Registry | New Value
------------------|----------
[numKeysOnLoadedVersion](#numKeysOnLoadedVersion) | [numKeysOnWritingVersion](#numKeysOnWritingVersion)
[loadedVersion](#loadedVersion) | `newVersion`
[fileManagerMetrics](#fileManagerMetrics) | [latestSaveCheckpointMetrics](RocksDBFileManager.md#latestSaveCheckpointMetrics) from the [RocksDBFileManager](#fileManager)

In the end, `commit` updates the [commitLatencyMs](#commitLatencyMs) metrics and prints out the following INFO message to the logs:

```text
Committed [newVersion], stats = [metrics]
```

---

`commit` is used when:

* `RocksDBStateStore` is requested to [commit state changes](RocksDBStateStore.md#commit)

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.state.RocksDB` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.execution.streaming.state.RocksDB=ALL
```

Refer to [Logging](../spark-logging.md).
