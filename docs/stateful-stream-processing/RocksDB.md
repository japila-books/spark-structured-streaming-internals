# RocksDB

## Creating Instance

`RocksDB` takes the following to be created:

* <span id="dfsRootDir"> DFS Root Directory
* <span id="conf"> [RocksDBConf](RocksDBConf.md)
* <span id="localRootDir"> Local root directory
* <span id="hadoopConf"> Hadoop `Configuration`
* <span id="loggingId"> Logging ID

`RocksDB` is created when:

* `RocksDBStateStoreProvider` is requested for the [RocksDB](RocksDBStateStoreProvider.md#rocksDB)
