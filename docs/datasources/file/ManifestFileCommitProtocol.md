# ManifestFileCommitProtocol

`ManifestFileCommitProtocol` is a `FileCommitProtocol` for tracking valid files (per micro-batch) in [FileStreamSinkLog](#fileLog).

!!! tip "The Internals of Apache Spark"
    Learn more on [FileCommitProtocol]({{ book.spark_core }}/FileCommitProtocol) in [The Internals of Apache Spark]({{ book.spark_core }}).

## Creating Instance

`ManifestFileCommitProtocol` takes the following to be created:

* <span id="jobId"> Job ID (_unused_)
* <span id="path"> Path to write the output to

`ManifestFileCommitProtocol` is created when `FileStreamSink` is requested to [add a batch](FileStreamSink.md#addBatch) (which is every micro-batch).

## <span id="fileLog"> FileStreamSinkLog

`ManifestFileCommitProtocol` is given a [FileStreamSinkLog](FileStreamSinkLog.md) when [setting up the manifest options](#setupManifestOptions) for a micro-batch (right after [having been created](#creating-instance)).

`FileStreamSinkLog` is used to [add](CompactibleFileStreamLog.md#add) the [SinkFileStatus](SinkFileStatus.md)es (in a micro-batch) when `ManifestFileCommitProtocol` is requested to [commit a write job](#commitJob).

## <span id="setupManifestOptions"> Setting Up Manifest Options

```scala
setupManifestOptions(
  fileLog: FileStreamSinkLog,
  batchId: Long): Unit
```

`setupManifestOptions` assigns the [FileStreamSinkLog](#fileLog) and [batchId](#batchId).

`setupManifestOptions` is used when `FileStreamSink` is requested to [add a batch](FileStreamSink.md#addBatch) (right after [having been created](#creating-instance)).

## <span id="setupJob"> Setting Up Job

```scala
setupJob(
  jobContext: JobContext): Unit
```

`setupJob` initializes [pendingCommitFiles](#pendingCommitFiles) to be an empty collection of Hadoop [Path]({{ hadoop.api }}/org/apache/hadoop/fs/Path.html)s.

`setupJob` is part of the `FileCommitProtocol` ([Spark SQL]({{ book.spark_core }}/FileCommitProtocol/#setupJob)) abstraction.

## <span id="setupTask"> Setting Up Task

```scala
setupTask(
  taskContext: TaskAttemptContext): Unit
```

`setupTask` initializes [addedFiles](#addedFiles) to be an empty collection of file locations (?)

`setupTask` is part of the `FileCommitProtocol` ([Spark SQL]({{ book.spark_core }}/FileCommitProtocol/#setupTask)) abstraction.

## <span id="newTaskTempFile"> newTaskTempFile

```scala
newTaskTempFile(
  taskContext: TaskAttemptContext,
  dir: Option[String],
  ext: String): String
```

`newTaskTempFile` creates a temporary file `part-[split]-[uuid][ext]` in the optional `dir` location or the [path](#path) and adds it to [addedFiles](#addedFiles) internal registry.

`newTaskTempFile` is part of the `FileCommitProtocol` ([Spark SQL]({{ book.spark_core }}/FileCommitProtocol/#newTaskTempFile)) abstraction.

## <span id="onTaskCommit"> Task Committed

```scala
onTaskCommit(
  taskCommit: TaskCommitMessage): Unit
```

`onTaskCommit` adds the [SinkFileStatus](SinkFileStatus.md)s from the given `taskCommits` to [pendingCommitFiles](#pendingCommitFiles) internal registry.

`onTaskCommit` is part of the `FileCommitProtocol` ([Spark SQL]({{ book.spark_core }}/FileCommitProtocol/#onTaskCommit)) abstraction.

## <span id="commitTask"> Committing Task

```scala
commitTask(
  taskContext: TaskAttemptContext): TaskCommitMessage
```

`commitTask` creates a `TaskCommitMessage` with [SinkFileStatus](SinkFileStatus.md)es for every [added file](#addedFiles).

`commitTask` is part of the `FileCommitProtocol` ([Spark SQL]({{ book.spark_core }}/FileCommitProtocol/#commitTask)) abstraction.

## <span id="abortTask"> Aborting Task

```scala
abortTask(
  taskContext: TaskAttemptContext): Unit
```

`abortTask` deletes [added files](#addedFiles).

`abortTask` is part of the `FileCommitProtocol` ([Spark SQL]({{ book.spark_core }}/FileCommitProtocol/#abortTask)) abstraction.

## <span id="commitJob"> Committing Job

```scala
commitJob(
  jobContext: JobContext,
  taskCommits: Seq[TaskCommitMessage]): Unit
```

`commitJob` takes [SinkFileStatus](SinkFileStatus.md)s from the given `taskCommits`.

In the end, `commitJob` requests the [FileStreamSinkLog](#fileLog) to [add](CompactibleFileStreamLog.md#add) the `SinkFileStatus`s as the [batchId](#batchId). If successful (`true`), `commitJob` prints out the following INFO message to the logs:

```text
Committed batch [batchId]
```

Otherwise, when failed (`false`), `commitJob` throws an `IllegalStateException`:

```text
Race while writing batch [batchId]
```

`commitJob` is part of the `FileCommitProtocol` ([Spark SQL]({{ book.spark_core }}/FileCommitProtocol/#commitJob)) abstraction.

## <span id="abortJob"> Aborting Job

```scala
abortJob(
  jobContext: JobContext): Unit
```

`abortJob` simply tries to remove all [pendingCommitFiles](#pendingCommitFiles) if there are any and clear it up.

`abortJob` is part of the `FileCommitProtocol` ([Spark SQL]({{ book.spark_core }}/FileCommitProtocol/#abortJob)) abstraction.
