# FileContextBasedCheckpointFileManager

`FileContextBasedCheckpointFileManager` is a [CheckpointFileManager](CheckpointFileManager.md) that uses `FileContext` ([Apache Hadoop]({{ hadoop.api }}/org/apache/hadoop/fs/FileContext.html)).

`FileContextBasedCheckpointFileManager` is the default [CheckpointFileManager](CheckpointFileManager.md) (unless [spark.sql.streaming.checkpointFileManagerClass](configuration-properties.md#spark.sql.streaming.checkpointFileManagerClass) is defined) as HDFS's [FileContext.rename()]({{ hadoop.api }}/org/apache/hadoop/fs/FileContext.html#rename-org.apache.hadoop.fs.Path-org.apache.hadoop.fs.Path-org.apache.hadoop.fs.Options.Rename...-) gives atomic renames, which is used for [createAtomic](#createAtomic).

## Creating Instance

`FileContextBasedCheckpointFileManager` takes the following to be created:

* <span id="path"> `Path` ([Apache Hadoop]({{ hadoop.api }}/org/apache/hadoop/fs/Path.html))
* <span id="hadoopConf"> `Configuration` ([Apache Hadoop]({{ hadoop.api }}/org/apache/hadoop/conf/Configuration.html))

`FileContextBasedCheckpointFileManager` is created when:

* `CheckpointFileManager` is requested to [create a CheckpointFileManager](CheckpointFileManager.md#create) (and [spark.sql.streaming.checkpointFileManagerClass](configuration-properties.md#spark.sql.streaming.checkpointFileManagerClass) is not defined)

## <span id="createAtomic"> createAtomic

```scala
createAtomic(
  path: Path,
  overwriteIfPossible: Boolean): CancellableFSDataOutputStream
```

`createAtomic` is part of the [CheckpointFileManager](CheckpointFileManager.md#createAtomic) abstraction.

---

`createAtomic` creates a `RenameBasedFSDataOutputStream`.
