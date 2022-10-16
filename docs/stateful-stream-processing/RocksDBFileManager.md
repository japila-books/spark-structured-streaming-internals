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

## <span id="saveCheckpointToDfs"> saveCheckpointToDfs

```scala
saveCheckpointToDfs(
  checkpointDir: File,
  version: Long,
  numKeys: Long): Unit
```

Saves all the files in the given local `checkpointDir` checkpoint directory as a committed version to DFS.

!!! note "RocksDB: commit - file sync to external storage time"
    The duration of `saveCheckpointToDfs` is tracked and available as [RocksDB: commit - file sync to external storage time](RocksDBStateStore.md#rocksdbCommitFileSyncLatencyMs) metric (via [fileSync](RocksDB.md#commitLatencyMs)).

---

`saveCheckpointToDfs` logs the files in the given `checkpointDir` directory. `saveCheckpointToDfs` prints out the following INFO message to the logs:

```text
Saving checkpoint files for version [version] - [num] files
    [path] - [length] bytes
```

`saveCheckpointToDfs` [listRocksDBFiles](#listRocksDBFiles) in the given `checkpointDir` directory.

`saveCheckpointToDfs` [saveImmutableFilesToDfs](#saveImmutableFilesToDfs).

`saveCheckpointToDfs` creates a `RocksDBCheckpointMetadata`.

`saveCheckpointToDfs` [localMetadataFile](#localMetadataFile) from the given `checkpointDir` directory.

`saveCheckpointToDfs` requests the `RocksDBCheckpointMetadata` to `writeToFile`.

`saveCheckpointToDfs` prints out the following INFO message to the logs:

```text
Written metadata for version [version]:
[metadata]
```

`saveCheckpointToDfs` [zipToDfsFile](#zipToDfsFile).

In the end, `saveCheckpointToDfs` prints out the following INFO message to the logs:

```text
Saved checkpoint file for version [version]
```

---

`saveCheckpointToDfs` is used when:

* `RocksDB` is requested to [commit state changes](RocksDB.md#commit)

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.state.RocksDBFileManager` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.execution.streaming.state.RocksDBFileManager=ALL
```

Refer to [Logging](../spark-logging.md).
