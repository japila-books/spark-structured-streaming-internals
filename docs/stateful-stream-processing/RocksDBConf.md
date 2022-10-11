# RocksDBConf

`RocksDBConf` is the [configuration options](#configuration-options) for optimizing RocksDB.

## Configuration Options

The configuration options belong to `spark.sql.streaming.stateStore.rocksdb` namespace.

### <span id="minVersionsToRetain"> minVersionsToRetain

[minVersionsToRetain](StateStoreConf.md#minVersionsToRetain)

### <span id="compactOnCommit"><span id="COMPACT_ON_COMMIT_CONF"> compactOnCommit

**spark.sql.streaming.stateStore.rocksdb.compactOnCommit**

Default: `false`

### <span id="blockSizeKB"><span id="BLOCK_SIZE_KB_CONF"> blockSizeKB

**spark.sql.streaming.stateStore.rocksdb.blockSizeKB**

Default: `4`

### <span id="blockCacheSizeMB"><span id="BLOCK_CACHE_SIZE_MB_CONF"> blockCacheSizeMB

**spark.sql.streaming.stateStore.rocksdb.blockCacheSizeMB**

Default: `8`

### <span id="lockAcquireTimeoutMs"><span id="LOCK_ACQUIRE_TIMEOUT_MS_CONF"> lockAcquireTimeoutMs

**spark.sql.streaming.stateStore.rocksdb.lockAcquireTimeoutMs**

Default: `60000`

### <span id="resetStatsOnLoad"><span id="RESET_STATS_ON_LOAD"> resetStatsOnLoad

**spark.sql.streaming.stateStore.rocksdb.resetStatsOnLoad**

Default: `true`

### <span id="formatVersion"><span id="FORMAT_VERSION"> formatVersion

**spark.sql.streaming.stateStore.rocksdb.formatVersion**

Default: `5`

### <span id="trackTotalNumberOfRows"><span id="TRACK_TOTAL_NUMBER_OF_ROWS"> trackTotalNumberOfRows

**spark.sql.streaming.stateStore.rocksdb.trackTotalNumberOfRows**

Enables tracking the total number of rows

Default: `true`

Adds additional lookups on write operations ([put](RocksDB.md#put) and [remove](RocksDB.md#remove)) to track the changes of total number of rows, which would help observability on state store.
The additional lookups bring non-trivial overhead on write-heavy workloads - if your query does lots of writes on state, it would be encouraged to turn off the config and turn on again when you really need the know the number for observability/debuggability.

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
