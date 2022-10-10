# RocksDBStateStore

`RocksDBStateStore` is a [StateStore](StateStore.md).

## Creating Instance

`RocksDBStateStore` takes the following to be created:

* <span id="lastVersion"> Version

`RocksDBStateStore` is created when:

* `RocksDBStateStoreProvider` is requested for the [StateStore](RocksDBStateStoreProvider.md#getStore) (for the [version](#version))

## <span id="metrics"> Performance Metrics

```scala
metrics: StateStoreMetrics
```

`metrics` is part of the [StateStore](StateStore.md#metrics) abstraction.

---

`metrics` requests the [RocksDB](RocksDBStateStoreProvider.md#rocksDB) for the [metrics](RocksDB.md#metrics) and defines `StateStoreCustomMetric`s.

In the end, `metrics` creates a [StateStoreMetrics](StateStoreMetrics.md) with the following:

* [numUncommittedKeys](RocksDBMetrics.md#numUncommittedKeys) of the [RocksDBMetrics](RocksDBMetrics.md)
* [totalMemUsageBytes](RocksDBMetrics.md#totalMemUsageBytes) of the [RocksDBMetrics](RocksDBMetrics.md)
* The `StateStoreCustomMetric`s

### <span id="rocksdbGetCount"><span id="CUSTOM_METRIC_GET_COUNT"> RocksDB: number of get calls

`rocksdbGetCount` is `get` entry from the [nativeOpsHistograms](RocksDBMetrics.md#nativeOpsHistograms)
