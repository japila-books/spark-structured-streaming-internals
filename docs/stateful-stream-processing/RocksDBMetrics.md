# RocksDBMetrics

`RocksDBMetrics` represents RocksDB stats of a commit.

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

`RocksDBMetrics` is given a `Map[String, RocksDBNativeHistogram]` when [created](#creating-instance).

`nativeOpsHistograms` is created when `RocksDB` is requested for the [metrics](RocksDB.md#metrics) (based on the _nativeOpsLatencyMicros_ with RocksDB's `HistogramType` and the [nativeStats](RocksDB.md#nativeStats)).

Native Op | HistogramType
----------|---------
 get | DB_GET
 put | DB_WRITE
 compaction | COMPACTION_TIME
