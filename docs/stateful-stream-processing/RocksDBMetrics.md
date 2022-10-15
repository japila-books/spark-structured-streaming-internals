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
* [lastCommitLatencyMs](#lastCommitLatencyMs)
* <span id="filesCopied"> filesCopied
* <span id="bytesCopied"> bytesCopied
* <span id="filesReused"> filesReused
* <span id="zipFileBytesUncompressed"> zipFileBytesUncompressed
* <span id="nativeOpsMetrics"> nativeOpsMetrics (`Map[String, Long]`)

`RocksDBMetrics` is created when:

* `RocksDB` is requested for the [metrics](RocksDB.md#metrics)

### <span id="lastCommitLatencyMs"> lastCommitLatencyMs

```scala
lastCommitLatencyMs: Map[String, Long]
```

`RocksDBMetrics` is given durations (of each named commit phase of [RocksDB committing a state version](RocksDB.md#commitLatencyMs)) when [created](#creating-instance).

Commit Phase | Metric Name | Metric Description
-------------|-------------|-------------
 `checkpoint` | `rocksdbCommitCheckpointLatency` | RocksDB: commit - checkpoint time
 `compact` | `rocksdbCommitCompactLatency` | RocksDB: commit - compact time
 `fileSync` | `rocksdbCommitFileSyncLatencyMs` | RocksDB: commit - file sync to external storage time
 `flush` | `rocksdbCommitFlushLatency` | RocksDB: commit - flush time
 `pauseBg` | `rocksdbCommitPauseLatency` | RocksDB: commit - pause bg time
 `writeBatch` | `rocksdbCommitWriteBatchLatency` | RocksDB: commit - write batch time

!!! danger "SPARK-40807"
    [SPARK-40807 "RocksDB: commit - pause bg time total" metric always 0](https://issues.apache.org/jira/browse/SPARK-40807)

Used when `RocksDBStateStore` is requested for the [metrics](RocksDBStateStore.md#metrics).

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
    * [RocksDB: total get call latency](RocksDBStateStore.md#rocksdbGetLatency)
    * [RocksDB: total put call latency](RocksDBStateStore.md#rocksdbPutLatency)
    * [RocksDB: compaction - total compaction time including background](RocksDBStateStore.md#rocksdbTotalCompactionLatencyMs)
* Counts
    * [RocksDB: number of get calls](RocksDBStateStore.md#rocksdbGetCount)
    * [RocksDB: number of put calls](RocksDBStateStore.md#rocksdbPutCount)
