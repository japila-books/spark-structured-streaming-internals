# RocksDBFileManager

`RocksDBFileManager` is the file manager of [RocksDB](RocksDB.md#fileManager).

## Creating Instance

`RocksDBFileManager` takes the following to be created:

* <span id="dfsRootDir"> DFS Root Directory
* <span id="localTempDir"> Local Temporary Directory
* <span id="hadoopConf"> Hadoop `Configuration`
* <span id="loggingId"> Logging ID

`RocksDBFileManager` is created when:

* `RocksDB` is [created](RocksDB.md#fileManager)
