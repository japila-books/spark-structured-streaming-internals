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
