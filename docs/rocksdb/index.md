# RocksDB State Store

[RocksDB](https://rocksdb.org/) can be used as a state store backend in Spark Structured Streaming.

!!! quote "RocksDB's Notable Features"

    RocksDB is an embeddable persistent key-value store with the following features:

    * Uses a log structured database engine
    * Keys and values are arbitrarily-sized byte streams
    * Optimized for fast, low latency storage (flash drives and high-speed disk drives) for high read/write rates

    The full documentation is currently on the [GitHub wiki](https://github.com/facebook/rocksdb/wiki).

## stateStore.providerClass

[spark.sql.streaming.stateStore.providerClass](../configuration-properties.md#spark.sql.streaming.stateStore.providerClass) with `org.apache.spark.sql.execution.streaming.state.RocksDBStateStoreProvider` enables [RocksDBStateStoreProvider](RocksDBStateStoreProvider.md) as the default [StateStoreProvider](../stateful-stream-processing/StateStoreProvider.md).

## Logging

`RocksDB` is used to [create a native logger](RocksDB.md#createLogger) and configure a logging level accordingly.

## Demo

* [Demo: RocksDB State Store for Streaming Aggregation](../demo/rocksdb-state-store-for-streaming-aggregation.md)
