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

## <span id="readOptions"> ReadOptions

`RocksDB` creates a `ReadOptions` ([RocksDB]({{ rocksdb.api }}/org/rocksdb/ReadOptions.html)) when [created](#creating-instance).

Used when:

* [get](#get)
* [put](#put) (with [trackTotalNumberOfRows](RocksDBConf.md#trackTotalNumberOfRows) enabled)
* [remove](#remove) (with [trackTotalNumberOfRows](RocksDBConf.md#trackTotalNumberOfRows) enabled)

Closed in [close](#close)

## <span id="writeBatch"> WriteBatchWithIndex

`RocksDB` creates a `WriteBatchWithIndex` ([RocksDB]({{ rocksdb.api }}/org/rocksdb/WriteBatchWithIndex.html)) (with `overwriteKey` enabled) when [created](#creating-instance).

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

## <span id="commit"> Committing Changes

```scala
commit(): Long
```

`commit`...FIXME

---

`commit` is used when:

* `RocksDBStateStore` is requested to [commit](RocksDBStateStore.md#commit)

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.state.RocksDB` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.execution.streaming.state.RocksDB=ALL
```

Refer to [Logging](../spark-logging.md).
