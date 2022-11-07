# RocksDBStateStore

`RocksDBStateStore` is a [StateStore](../stateful-stream-processing/StateStore.md) of [RocksDBStateStoreProvider](RocksDBStateStoreProvider.md).

## Creating Instance

`RocksDBStateStore` takes the following to be created:

* <span id="lastVersion"> Version

`RocksDBStateStore` is created when:

* `RocksDBStateStoreProvider` is requested for the [StateStore](RocksDBStateStoreProvider.md#getStore) (for the [version](#version))

## <span id="metrics"> Performance Metrics

```scala
metrics: StateStoreMetrics
```

`metrics` is part of the [StateStore](../stateful-stream-processing/StateStore.md#metrics) abstraction.

---

`metrics` requests the [RocksDB](RocksDBStateStoreProvider.md#rocksDB) for the [metrics](RocksDB.md#metrics) and defines `StateStoreCustomMetric`s.

In the end, `metrics` creates a [StateStoreMetrics](../stateful-stream-processing/StateStoreMetrics.md) with the following:

* [numUncommittedKeys](RocksDBMetrics.md#numUncommittedKeys) of the [RocksDBMetrics](RocksDBMetrics.md)
* [totalMemUsageBytes](RocksDBMetrics.md#totalMemUsageBytes) of the [RocksDBMetrics](RocksDBMetrics.md)
* The `StateStoreCustomMetric`s

### <span id="rocksdbCommitCompactLatency"><span id="CUSTOM_METRIC_COMMIT_COMPACT_TIME"> RocksDB: commit - compact time

`rocksdbCommitCompactLatency` is the `compact` entry in the [lastCommitLatencyMs](RocksDBMetrics.md#lastCommitLatencyMs)

### <span id="rocksdbCommitFileSyncLatencyMs"><span id="CUSTOM_METRIC_FILESYNC_TIME"><span id="fileSync"> RocksDB: commit - file sync to external storage time

`rocksdbCommitFileSyncLatencyMs` is the `fileSync` entry in the [lastCommitLatencyMs](RocksDBMetrics.md#lastCommitLatencyMs)

### <span id="rocksdbTotalCompactionLatencyMs"><span id="CUSTOM_METRIC_TOTAL_COMPACT_TIME"> RocksDB: compaction - total compaction time including background

`rocksdbTotalCompactionLatencyMs` is the **sum** on `compaction` entry in the [nativeOpsHistograms](RocksDBMetrics.md#nativeOpsHistograms)

### <span id="rocksdbGetCount"><span id="CUSTOM_METRIC_GET_COUNT"> RocksDB: number of get calls

`rocksdbGetCount` is the **count** on `get` entry in the [nativeOpsHistograms](RocksDBMetrics.md#nativeOpsHistograms)

### <span id="rocksdbPutCount"><span id="CUSTOM_METRIC_PUT_COUNT"> RocksDB: number of put calls

`rocksdbPutCount` is the **count** on `put` entry in the [nativeOpsHistograms](RocksDBMetrics.md#nativeOpsHistograms)

### <span id="rocksdbGetLatency"><span id="CUSTOM_METRIC_GET_TIME"> RocksDB: total get call latency

`rocksdbGetLatency` is the **sum** on `get` entry in the [nativeOpsHistograms](RocksDBMetrics.md#nativeOpsHistograms)

### <span id="rocksdbPutLatency"><span id="CUSTOM_METRIC_PUT_TIME"> RocksDB: total put call latency

`rocksdbPutLatency` is the **sum** on `put` entry in the [nativeOpsHistograms](RocksDBMetrics.md#nativeOpsHistograms)

## <span id="commit"> Committing State Changes

```scala
commit(): Long
```

`commit` is part of the [StateStore](../stateful-stream-processing/StateStore.md#commit) abstraction.

---

`commit` requests the [RocksDB](RocksDBStateStoreProvider.md#rocksDB) to [commit state changes](RocksDB.md#commit) (that, in the end, gives a new version).

`commit` sets the [state](#state) to `COMMITTED`.

`commit` prints out the following INFO message to the logs:

```text
Committed [newVersion] for [id]
```

In the end, `commit` returns the new version (from [committing state changes](RocksDB.md#commit)).
