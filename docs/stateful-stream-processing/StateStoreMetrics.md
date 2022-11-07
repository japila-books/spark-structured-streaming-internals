# StateStoreMetrics

`StateStoreMetrics` holds the performance metrics of [StateStore](StateStore.md#metrics)s and [SymmetricHashJoinStateManager](../streaming-join/SymmetricHashJoinStateManager.md).

## Creating Instance

`StateStoreMetrics` takes the following to be created:

* <span id="numKeys"> Number of Keys
* <span id="memoryUsedBytes"> Memory used (in bytes)
* [Custom Metrics](#customMetrics)

`StateStoreMetrics` is created when:

* `HDFSBackedStateStore` is requested for [metrics](HDFSBackedStateStore.md#metrics)
* `RocksDBStateStore` is requested for [metrics](../rocksdb/RocksDBStateStore.md#metrics)
* `StateStoreMetrics` is requested to [combine metrics](#combine)
* `SymmetricHashJoinStateManager` is requested for [metrics](../streaming-join/SymmetricHashJoinStateManager.md#metrics)

### <span id="customMetrics"> Custom Metrics

`StateStoreMetrics` is given [StateStoreCustomMetric](StateStoreCustomMetric.md)s and their current values when [created](#creating-instance).
