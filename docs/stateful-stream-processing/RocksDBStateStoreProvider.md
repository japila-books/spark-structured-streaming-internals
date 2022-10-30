# RocksDBStateStoreProvider

`RocksDBStateStoreProvider` is a [StateStoreProvider](StateStoreProvider.md).

## <span id="rocksDB"> RocksDB

```scala
rocksDB: RocksDB
```

??? note "Lazy Value"
    `rocksDB` is a Scala **lazy value** to guarantee that the code to initialize it is executed once only (when accessed for the first time) and the computed value never changes afterwards.

    Learn more in the [Scala Language Specification]({{ scala.spec }}/05-classes-and-objects.html#lazy).

`rocksDB` requests the [StateStoreId](#stateStoreId) for [storeCheckpointLocation](StateStoreId.md#storeCheckpointLocation).

`rocksDB` builds a store identifier (using the [StateStoreId](#stateStoreId)):

```text
StateStoreId(opId=[operatorId],partId=[partitionId],name=[storeName])
```

`rocksDB` creates a local root directory (in a temp directory for a directory with the store identifier).

!!! note "spark.local.dir"
    Use `spark.local.dir` Spark property to set up the local root directory.

In the end, `rocksDB` creates a [RocksDB](RocksDB.md) for the store identifier.

---

`rocksDB` lazy value is initialized in [init](#init).

---

`rocksDB` is used when:

* `RocksDBStateStore` is requested to [get the value for a key](RocksDBStateStore.md#get), [put](RocksDBStateStore.md#put), [remove](RocksDBStateStore.md#remove), [iterator](RocksDBStateStore.md#iterator), [prefixScan](RocksDBStateStore.md#prefixScan), [commit](RocksDBStateStore.md#commit), [abort](RocksDBStateStore.md#abort), [metrics](RocksDBStateStore.md#metrics)
* `RocksDBStateStoreProvider` is requested to [getStore](#getStore), [doMaintenance](#doMaintenance), [close](#close), [latestVersion](#latestVersion)

## <span id="init"> Initialization

```scala
init(
  stateStoreId: StateStoreId,
  keySchema: StructType,
  valueSchema: StructType,
  numColsPrefixKey: Int,
  storeConf: StateStoreConf,
  hadoopConf: Configuration): Unit
```

`init` is part of the [StateStoreProvider](StateStoreProvider.md#init) abstraction.

---

`init` sets the internal registries (based on the given arguments):

* [stateStoreId_](#stateStoreId_)
* [keySchema](#keySchema)
* [valueSchema](#valueSchema)
* [storeConf](#storeConf)
* [hadoopConf](#hadoopConf)
* [encoder](#encoder)

In the end, `init` initializes the [RocksDB](#rocksDB) (lazy value).

## <span id="getStore"> Looking Up StateStore by Version

```scala
getStore(
  version: Long): StateStore
```

`getStore` is part of the [StateStoreProvider](StateStoreProvider.md#getStore) abstraction.

---

`getStore` requests the [RocksDB](#rocksDB) to [load data](RocksDB.md#load) for the given `version`.

In the end, `getStore` creates a [RocksDBStateStore](RocksDBStateStore.md) for the given `version`.

## <span id="supportedCustomMetrics"><span id="ALL_CUSTOM_METRICS"> Supported Custom Metrics

```scala
supportedCustomMetrics: Seq[StateStoreCustomMetric]
```

`supportedCustomMetrics` is part of the [StateStoreProvider](StateStoreProvider.md#supportedCustomMetrics) abstraction.

!!! note
    The following is a subset of the supported custom metrics.

### <span id="CUSTOM_METRIC_BYTES_COPIED"><span id="rocksdbBytesCopied"> rocksdbBytesCopied

RocksDB: file manager - bytes copied

### <span id="CUSTOM_METRIC_CHECKPOINT_TIME"><span id="rocksdbCommitCheckpointLatency"> rocksdbCommitCheckpointLatency

RocksDB: commit - checkpoint time

### <span id="CUSTOM_METRIC_COMMIT_COMPACT_TIME"><span id="rocksdbCommitCompactLatency"> rocksdbCommitCompactLatency

RocksDB: commit - compact time

### <span id="CUSTOM_METRIC_FILESYNC_TIME"><span id="rocksdbCommitFileSyncLatencyMs"> rocksdbCommitFileSyncLatencyMs

RocksDB: commit - file sync to external storage time

### <span id="CUSTOM_METRIC_FLUSH_TIME"><span id="rocksdbCommitFlushLatency"> rocksdbCommitFlushLatency

RocksDB: commit - flush time

### <span id="CUSTOM_METRIC_PAUSE_TIME"><span id="rocksdbCommitPauseLatency"> rocksdbCommitPauseLatency

RocksDB: commit - pause bg time

### <span id="CUSTOM_METRIC_WRITEBATCH_TIME"><span id="rocksdbCommitWriteBatchLatency"> rocksdbCommitWriteBatchLatency

RocksDB: commit - write batch time

### <span id="CUSTOM_METRIC_FILES_COPIED"><span id="rocksdbFilesCopied"> rocksdbFilesCopied

RocksDB: file manager - files copied

### <span id="CUSTOM_METRIC_GET_COUNT"><span id="rocksdbGetCount"> rocksdbGetCount

RocksDB: number of get calls

### <span id="CUSTOM_METRIC_GET_TIME"><span id="rocksdbGetLatency"> rocksdbGetLatency

RocksDB: total get call latency

### <span id="CUSTOM_METRIC_PUT_COUNT"><span id="rocksdbPutCount"> rocksdbPutCount

RocksDB: number of put calls

### <span id="CUSTOM_METRIC_PUT_TIME"><span id="rocksdbPutLatency"> rocksdbPutLatency

RocksDB: total put call latency

### <span id="CUSTOM_METRIC_BLOCK_CACHE_HITS"><span id="rocksdbReadBlockCacheHitCount"> rocksdbReadBlockCacheHitCount

RocksDB: read - count of cache hits in RocksDB block cache avoiding disk read

### <span id="CUSTOM_METRIC_BLOCK_CACHE_MISS"><span id="rocksdbReadBlockCacheMissCount"> rocksdbReadBlockCacheMissCount

RocksDB: read - count of cache misses that required reading from local disk

### <span id="CUSTOM_METRIC_BYTES_READ"><span id="rocksdbTotalBytesRead"> rocksdbTotalBytesRead

RocksDB: read - total of uncompressed bytes read (from memtables/cache/sst) from DB::Get()

### <span id="CUSTOM_METRIC_COMPACT_READ_BYTES"><span id="rocksdbTotalBytesReadByCompaction"> rocksdbTotalBytesReadByCompaction

RocksDB: compaction - total bytes read by the compaction process

### <span id="CUSTOM_METRIC_BYTES_WRITTEN"><span id="rocksdbTotalBytesWritten"> rocksdbTotalBytesWritten

RocksDB: write - total of uncompressed bytes written by DB::{Put(), Delete(), Merge(), Write()}

### <span id="CUSTOM_METRIC_COMPACT_WRITTEN_BYTES"><span id="rocksdbTotalBytesWrittenByCompaction"> rocksdbTotalBytesWrittenByCompaction

RocksDB: compaction - total bytes written by the compaction process

### <span id="CUSTOM_METRIC_TOTAL_COMPACT_TIME"><span id="rocksdbTotalCompactionLatencyMs"> rocksdbTotalCompactionLatencyMs

RocksDB: compaction - total compaction time including background
