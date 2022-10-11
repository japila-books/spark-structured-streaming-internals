# RocksDBMetrics

`RocksDBMetrics` represents RocksDB stats of a [load](RocksDB.md#load) or a [commit](RocksDB.md#commit).

## Creating Instance

`RocksDBMetrics` takes the following to be created:

* <span id="numCommittedKeys"> numCommittedKeys
* <span id="numUncommittedKeys"> numUncommittedKeys
* <span id="totalMemUsageBytes"> totalMemUsageBytes
* <span id="writeBatchMemUsageBytes"> writeBatchMemUsageBytes
* <span id="totalSSTFilesBytes"> totalSSTFilesBytes
* [nativeOpsHistograms](#nativeOpsHistograms)
* <span id="lastCommitLatencyMs"> lastCommitLatencyMs (`Map[String, Long]`)
* <span id="filesCopied"> filesCopied
* <span id="bytesCopied"> bytesCopied
* <span id="filesReused"> filesReused
* <span id="zipFileBytesUncompressed"> zipFileBytesUncompressed
* <span id="nativeOpsMetrics"> nativeOpsMetrics (`Map[String, Long]`)

`RocksDBMetrics` is created when:

* `RocksDB` is requested for the [metrics](RocksDB.md#metrics)

### <span id="nativeOpsHistograms"> nativeOpsHistograms

```scala
nativeOpsHistograms: Map[String, RocksDBNativeHistogram]
```

`RocksDBMetrics` is given a lookup table of `RocksDBNativeHistogram`s by name when [created](#creating-instance).

`nativeOpsHistograms` is created when `RocksDB` is requested for the [metrics](RocksDB.md#metrics) (based on the _nativeOpsLatencyMicros_ with RocksDB's `HistogramType` and the [nativeStats](RocksDB.md#nativeStats)).

Native Op | HistogramType
----------|---------
 get | DB_GET
 put | DB_WRITE
 compaction | COMPACTION_TIME

Used when `RocksDBStateStore` is requested for the [metrics](RocksDBStateStore.md#metrics) for the following:

* Latencies (`sum / 1000`)
    * [RocksDB: total get call latency](RocksDBStateStore.md#CUSTOM_METRIC_GET_TIME)
    * [RocksDB: total put call latency](RocksDBStateStore.md#CUSTOM_METRIC_PUT_TIME)
    * [RocksDB: compaction - total compaction time including background](RocksDBStateStore.md#rocksdbTotalCompactionLatencyMs)
* Counts
    * [RocksDB: number of get calls](RocksDBStateStore.md#rocksdbGetCount)
    * [RocksDB: number of put calls](RocksDBStateStore.md#rocksdbPutCount)
