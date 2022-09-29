# Offsets and Metadata Checkpointing

A streaming query can be started from scratch or from checkpoint (that gives fault-tolerance as the state is preserved even when a failure happens).

[Stream execution engines](StreamExecution.md) use **checkpoint location** to resume stream processing and get **start offsets** to start query processing from.

`StreamExecution` resumes (populates the start offsets) from the latest checkpointed offsets from the [Write-Ahead Log (WAL) of Offsets](StreamExecution.md#offsetLog) that may have already been processed (and, if so, committed to the [Offset Commit Log](StreamExecution.md#commitLog)).

* [Hadoop DFS-based metadata storage](OffsetSeqLog.md) of [OffsetSeq](OffsetSeq.md)s

* [OffsetSeq](OffsetSeq.md) and [StreamProgress](StreamProgress.md)

* [StreamProgress](StreamProgress.md) and [StreamExecutions](StreamExecution.md) ([committed](StreamExecution.md#committedOffsets) and [available](StreamExecution.md#availableOffsets) offsets)

## Micro-Batch Stream Processing

In [Micro-Batch Stream Processing](micro-batch-execution/index.md), the [available offsets](StreamExecution.md#availableOffsets) registry is populated with the [latest offsets](HDFSMetadataLog.md#getLatest) from the [Write-Ahead Log (WAL)](StreamExecution.md#offsetLog) when `MicroBatchExecution` stream processing engine is requested to [populate start offsets from checkpoint](micro-batch-execution/MicroBatchExecution.md#populateStartOffsets) (if available) when `MicroBatchExecution` is requested to [run an activated streaming query](micro-batch-execution/MicroBatchExecution.md#runActivatedStream) ([before the first "zero" micro-batch](micro-batch-execution/MicroBatchExecution.md#runActivatedStream-triggerExecution-populateStartOffsets)).

The [available offsets](StreamExecution.md#availableOffsets) are then [added](StreamProgress.md#plusplus) to the [committed offsets](StreamExecution.md#committedOffsets) when the latest batch ID available (as described above) is exactly the [latest batch ID](HDFSMetadataLog.md#getLatest) committed to the [Offset Commit Log](StreamExecution.md#commitLog) when `MicroBatchExecution` stream processing engine is requested to [populate start offsets from checkpoint](micro-batch-execution/MicroBatchExecution.md#populateStartOffsets).

When a streaming query is started from scratch (with no checkpoint that has offsets in the [Offset Write-Ahead Log](StreamExecution.md#offsetLog)), `MicroBatchExecution` prints out the following INFO message:

```text
Starting new streaming query.
```

When a streaming query is resumed (restarted) from a checkpoint with offsets in the [Offset Write-Ahead Log](StreamExecution.md#offsetLog), `MicroBatchExecution` prints out the following INFO message:

```text
Resuming at batch [currentBatchId] with committed offsets [committedOffsets] and available offsets [availableOffsets]
```

Every time `MicroBatchExecution` is requested to [check whether a new data is available](micro-batch-execution/MicroBatchExecution.md#isNewDataAvailable) (in any of the streaming sources)...FIXME

When `MicroBatchExecution` is requested to [construct the next streaming micro-batch](micro-batch-execution/MicroBatchExecution.md#constructNextBatch) (when `MicroBatchExecution` requested to [run the activated streaming query](micro-batch-execution/MicroBatchExecution.md#runActivatedStream)), every [streaming source](StreamExecution.md#uniqueSources) is requested for the latest offset available that are [added](StreamProgress.md#plusplus) to the [availableOffsets](StreamExecution.md#availableOffsets) registry. Streaming sources report some offsets or none at all (if this source has never received any data). Streaming sources with no data are excluded (_filtered out_).

`MicroBatchExecution` prints out the following TRACE message to the logs:

```text
noDataBatchesEnabled = [noDataBatchesEnabled], lastExecutionRequiresAnotherBatch = [lastExecutionRequiresAnotherBatch], isNewDataAvailable = [isNewDataAvailable], shouldConstructNextBatch = [shouldConstructNextBatch]
```

With [shouldConstructNextBatch](micro-batch-execution/MicroBatchExecution.md#constructNextBatch-shouldConstructNextBatch) internal flag enabled, `MicroBatchExecution` commits ([adds](HDFSMetadataLog.md#add)) the available offsets for the batch to the [Write-Ahead Log (WAL)](StreamExecution.md#offsetLog) and prints out the following INFO message to the logs:

```text
Committed offsets for batch [currentBatchId]. Metadata [offsetSeqMetadata]
```

When [running a single streaming micro-batch](micro-batch-execution/MicroBatchExecution.md#runBatch), `MicroBatchExecution`...FIXME

In the end (of [running a single streaming micro-batch](micro-batch-execution/MicroBatchExecution.md#runBatch)), `MicroBatchExecution` commits ([adds](HDFSMetadataLog.md#add)) the available offsets (to the [committedOffsets](#committedOffsets) registry) so they are considered processed already.

`MicroBatchExecution` prints out the following DEBUG message to the logs:

```text
Completed batch [currentBatchId]
```

## Limitations (Assumptions)

It is assumed that the order of [streaming sources](monitoring/ProgressReporter.md#sources) in a [streaming query](StreamExecution.md#analyzedPlan) matches the order of the [offsets](OffsetSeq.md#offsets) of [OffsetSeq](OffsetSeq.md) (in [offsetLog](StreamExecution.md#offsetLog)) and [availableOffsets](StreamExecution.md#availableOffsets).

In other words, a streaming query can be modified and then restarted from a checkpoint (to maintain stream processing state) only when the number of streaming sources and their order are preserved across restarts.
