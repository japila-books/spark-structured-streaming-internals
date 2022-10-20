# RocksDBConf

`RocksDBConf` is the [configuration options](#configuration-options) for optimizing RocksDB.

## Configuration Options

The configuration options belong to `spark.sql.streaming.stateStore.rocksdb` namespace.

### <span id="blockCacheSizeMB"><span id="BLOCK_CACHE_SIZE_MB_CONF"><span id="spark.sql.streaming.stateStore.rocksdb.blockCacheSizeMB"> blockCacheSizeMB

**spark.sql.streaming.stateStore.rocksdb.blockCacheSizeMB**

Default: `8`

### <span id="blockSizeKB"><span id="BLOCK_SIZE_KB_CONF"><span id="spark.sql.streaming.stateStore.rocksdb.blockSizeKB"> blockSizeKB

**spark.sql.streaming.stateStore.rocksdb.blockSizeKB**

Default: `4`

### <span id="compactOnCommit"><span id="COMPACT_ON_COMMIT_CONF"><span id="spark.sql.streaming.stateStore.rocksdb.compactOnCommit"> compactOnCommit

**spark.sql.streaming.stateStore.rocksdb.compactOnCommit**

Whether to compact [RocksDB](RocksDB.md) data while [committing state changes](RocksDB.md#commit) (before checkpointing)

Default: `false`

### <span id="formatVersion"><span id="FORMAT_VERSION"><span id="spark.sql.streaming.stateStore.rocksdb.formatVersion"> formatVersion

**spark.sql.streaming.stateStore.rocksdb.formatVersion**

Default: `5`

### <span id="lockAcquireTimeoutMs"><span id="LOCK_ACQUIRE_TIMEOUT_MS_CONF"><span id="spark.sql.streaming.stateStore.rocksdb.lockAcquireTimeoutMs"> lockAcquireTimeoutMs

**spark.sql.streaming.stateStore.rocksdb.lockAcquireTimeoutMs**

Default: `60000`

### <span id="minVersionsToRetain"> minVersionsToRetain

[minVersionsToRetain](StateStoreConf.md#minVersionsToRetain)

### <span id="resetStatsOnLoad"><span id="RESET_STATS_ON_LOAD"><span id="spark.sql.streaming.stateStore.rocksdb.resetStatsOnLoad"> resetStatsOnLoad

**spark.sql.streaming.stateStore.rocksdb.resetStatsOnLoad**

Default: `true`

### <span id="trackTotalNumberOfRows"><span id="TRACK_TOTAL_NUMBER_OF_ROWS"><span id="spark.sql.streaming.stateStore.rocksdb.trackTotalNumberOfRows"> trackTotalNumberOfRows

**spark.sql.streaming.stateStore.rocksdb.trackTotalNumberOfRows**

Enables tracking the total number of rows

Default: `true`

Adds additional lookups on write operations ([put](RocksDB.md#put) and [remove](RocksDB.md#remove)) to track the changes of total number of rows, which can help observability on state store.

!!! danger "Possible Performance Degradation"
    The additional lookups bring non-trivial overhead on write-heavy workloads. If your query does lots of writes on state, it would be encouraged to turn off the config and turn on again when you really need the know the number for observability/debuggability.

Used when:

* `RocksDB` is requested to [load a version](RocksDB.md#load), [put a new value for a key](RocksDB.md#put) and [remove a key](RocksDB.md#remove)

## <span id="apply"> Creating RocksDBConf

```scala
apply(): RocksDBConf // (1)!
apply(
  storeConf: StateStoreConf): RocksDBConf
```

1. Used in tests only

`apply` requests the given [StateStoreConf](StateStoreConf.md) for the [state store configuration options](StateStoreConf.md#confs) and creates a `RocksDBConf`.

---

`apply` is used when:

* `RocksDBStateStoreProvider` is requested for the [RocksDB](RocksDBStateStoreProvider.md#rocksDB)
