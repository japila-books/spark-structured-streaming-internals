# StateStoreProvider

`StateStoreProvider` is an [abstraction](#contract) of [StateStore providers](#implementations) that manage [state stores](#getStore) (_state data_) in [Stateful Stream Processing](index.md).

A concrete [StateStoreProvider](#implementations) is selected based on [spark.sql.streaming.stateStore.providerClass](../configuration-properties.md#spark.sql.streaming.stateStore.providerClass) configuration property.

## Contract

### <span id="getStore"> Looking Up StateStore by Version

```scala
getStore(
  version: Long): StateStore
```

[StateStore](StateStore.md) for the given `version`

See:

* [HDFSBackedStateStoreProvider](HDFSBackedStateStoreProvider.md#getStore)
* [RocksDBStateStoreProvider](../rocksdb/RocksDBStateStoreProvider.md#getStore)

Used when:

* `StateStoreProvider` is requested for a [ReadStateStore by version](#getReadStore)
* `StateStore` utility is used to [get a StateStore by provider id and version](StateStore.md#get)

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

Initializes a [StateStoreProvider](StateStoreProvider.md) with the following:

* [StateStoreId](StateStoreId.md)
* Key and value schema
* [StateStoreConf](StateStoreConf.md)

??? note "numColsPrefixKey"
    The input `numColsPrefixKey` is different based on a stateful operator:

    Stateful Operator | numColsPrefixKey
    ------------------|-----------------
    [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md#doExecute) | 0
    [StateStoreRDD](StateStoreRDD.md#compute) | [numColsPrefixKey](StateStoreRDD.md#numColsPrefixKey)
    [SymmetricHashJoinStateManager.StateStoreHandler](../join/StateStoreHandler.md#getStateStore) | 0

See:

* [HDFSBackedStateStoreProvider](HDFSBackedStateStoreProvider.md#init)
* [RocksDBStateStoreProvider](../rocksdb/RocksDBStateStoreProvider.md#init)

Used when:

* `StateStoreProvider` utility is used to [create and initialize a StateStoreProvider](#createAndInit)

### <span id="supportedCustomMetrics"> Supported Custom Metrics

```scala
supportedCustomMetrics: Seq[StateStoreCustomMetric]
```

[StateStoreCustomMetric](StateStoreCustomMetric.md)s (e.g., to report in [web UI](../webui/StreamingQueryStatisticsPage.md#supportedCustomMetrics) or [StateOperatorProgress](../monitoring/StateOperatorProgress.md#customMetrics))

Default: empty

See:

* [RocksDBStateStoreProvider](../rocksdb/RocksDBStateStoreProvider.md#supportedCustomMetrics)
* [HDFSBackedStateStoreProvider](HDFSBackedStateStoreProvider.md#supportedCustomMetrics)

Used when:

* `StateStoreWriter` is requested for the [stateStoreCustomMetrics](../physical-operators/StateStoreWriter.md#stateStoreCustomMetrics)

## Implementations

* [HDFSBackedStateStoreProvider](HDFSBackedStateStoreProvider.md)
* [RocksDBStateStoreProvider](../rocksdb/RocksDBStateStoreProvider.md)

## <span id="create"> Creating StateStoreProvider

```scala
create(
  providerClassName: String): StateStoreProvider
```

`create` creates a `StateStoreProvider` based on the fully-qualified class name (`providerClassName`).

---

`create` is used when:

* `StateStoreWriter` physical operator is requested to [stateStoreCustomMetrics](../physical-operators/StateStoreWriter.md#stateStoreCustomMetrics)
* `StateStoreProvider` utility is used to [create and initialize a StateStoreProvider](#createAndInit)
* `StreamingQueryStatisticsPage` is requested for the [supportedCustomMetrics](../webui/StreamingQueryStatisticsPage.md#supportedCustomMetrics)

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

* `StateStore` utility is used to [look up a StateStore by StateStoreProviderId](StateStore.md#getStateStoreProvider)

## <span id="getReadStore"> Looking Up ReadStateStore by Version

```scala
getReadStore(
  version: Long): ReadStateStore
```

`getReadStore` creates a `WrappedReadStateStore` for the [StateStore](#getStore) for the given `version`.

---

`getReadStore` is used when:

* `StateStore` utility is used to [get a ReadStateStore](StateStore.md#getReadOnly)
