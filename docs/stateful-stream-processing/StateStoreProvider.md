# StateStoreProvider

`StateStoreProvider` is an [abstraction](#contract) of [StateStore providers](#implementations) that manage [state stores](#getStore) (_state data_) in [Stateful Stream Processing](index.md) (e.g., for persisting running aggregates in [Streaming Aggregation](../streaming-aggregation/index.md)).

`StateStoreProvider` utility uses [spark.sql.streaming.stateStore.providerClass](../configuration-properties.md#spark.sql.streaming.stateStore.providerClass) configuration property to choose the default [implementation](#implementations).

## Contract

### <span id="init"> Initialization

```scala
init(
  stateStoreId: StateStoreId,
  keySchema: StructType,
  valueSchema: StructType,
  numColsPrefixKey: Int,
  storeConfs: StateStoreConf,
  hadoopConf: Configuration): Unit
```

Initializes a [StateStoreProvider](StateStoreProvider.md) for the given [StateStoreId](StateStoreId.md)

Used when:

* `StateStoreProvider` utility is used to [create and initialize a StateStoreProvider](#createAndInit)

## Implementations

* [HDFSBackedStateStoreProvider](HDFSBackedStateStoreProvider.md)
* [RocksDBStateStoreProvider](RocksDBStateStoreProvider.md)

## <span id="createAndInit"> Creating and Initializing StateStoreProvider

```scala
createAndInit(
  providerId: StateStoreProviderId,
  keySchema: StructType,
  valueSchema: StructType,
  numColsPrefixKey: Int,
  storeConf: StateStoreConf,
  hadoopConf: Configuration): StateStoreProvider
```

`createAndInit` [creates a StateStoreProvider](#create) based on [spark.sql.streaming.stateStore.providerClass](../configuration-properties.md#spark.sql.streaming.stateStore.providerClass).

In the end, `createAndInit` requests the `StateStoreProvider` to [initialize](#init).

---

`createAndInit` is used when:

* `StateStore` utility is used to [look up a StateStore by StateStoreProviderId](StateStore.md#get-StateStore)
