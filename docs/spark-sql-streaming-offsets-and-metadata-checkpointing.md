# Offsets and Metadata Checkpointing

A streaming query can be started from scratch or from checkpoint (that gives fault-tolerance as the state is preserved even when a failure happens).

[Stream execution engines](StreamExecution.md) use **checkpoint location** to resume stream processing and get **start offsets** to start query processing from.

`StreamExecution` resumes (populates the start offsets) from the latest checkpointed offsets from the [Write-Ahead Log (WAL) of Offsets](StreamExecution.md#offsetLog) that may have already been processed (and, if so, committed to the [Offset Commit Log](StreamExecution.md#commitLog)).

* <<spark-sql-streaming-OffsetSeqLog.md#, Hadoop DFS-based metadata storage>> of <<spark-sql-streaming-OffsetSeq.md#, OffsetSeqs>>

* <<spark-sql-streaming-OffsetSeq.md#, OffsetSeq>> and <<spark-sql-streaming-StreamProgress.md#, StreamProgress>>

* <<spark-sql-streaming-StreamProgress.md#, StreamProgress>> and [StreamExecutions](StreamExecution.md) ([committed](StreamExecution.md#committedOffsets) and [available](StreamExecution.md#availableOffsets) offsets)

## Micro-Batch Stream Processing

In <<micro-batch-stream-processing.md#, Micro-Batch Stream Processing>>, the [available offsets](StreamExecution.md#availableOffsets) registry is populated with the <<spark-sql-streaming-HDFSMetadataLog.md#getLatest, latest offsets>> from the [Write-Ahead Log (WAL)](StreamExecution.md#offsetLog) when `MicroBatchExecution` stream processing engine is requested to <<MicroBatchExecution.md#populateStartOffsets, populate start offsets from checkpoint>> (if available) when `MicroBatchExecution` is requested to <<MicroBatchExecution.md#runActivatedStream, run an activated streaming query>> ([before the first "zero" micro-batch](MicroBatchExecution.md#runActivatedStream-triggerExecution-populateStartOffsets)).

The [available offsets](StreamExecution.md#availableOffsets) are then <<spark-sql-streaming-StreamProgress.md#plusplus, added>> to the [committed offsets](StreamExecution.md#committedOffsets) when the latest batch ID available (as described above) is exactly the <<spark-sql-streaming-HDFSMetadataLog.md#getLatest, latest batch ID>> committed to the [Offset Commit Log](StreamExecution.md#commitLog) when `MicroBatchExecution` stream processing engine is requested to <<MicroBatchExecution.md#populateStartOffsets, populate start offsets from checkpoint>>.

When a streaming query is started from scratch (with no checkpoint that has offsets in the [Offset Write-Ahead Log](StreamExecution.md#offsetLog)), `MicroBatchExecution` prints out the following INFO message:

```text
Starting new streaming query.
```

When a streaming query is resumed (restarted) from a checkpoint with offsets in the [Offset Write-Ahead Log](StreamExecution.md#offsetLog), `MicroBatchExecution` prints out the following INFO message:

```text
Resuming at batch [currentBatchId] with committed offsets [committedOffsets] and available offsets [availableOffsets]
```

Every time `MicroBatchExecution` is requested to <<MicroBatchExecution.md#isNewDataAvailable, check whether a new data is available>> (in any of the streaming sources)...FIXME

When `MicroBatchExecution` is requested to <<MicroBatchExecution.md#constructNextBatch, construct the next streaming micro-batch>> (when `MicroBatchExecution` requested to <<MicroBatchExecution.md#runActivatedStream, run the activated streaming query>>), every [streaming source](StreamExecution.md#uniqueSources) is requested for the latest offset available that are <<spark-sql-streaming-StreamProgress.md#plusplus, added>> to the [availableOffsets](StreamExecution.md#availableOffsets) registry. Streaming sources report some offsets or none at all (if this source has never received any data). Streaming sources with no data are excluded (_filtered out_).

`MicroBatchExecution` prints out the following TRACE message to the logs:

```text
noDataBatchesEnabled = [noDataBatchesEnabled], lastExecutionRequiresAnotherBatch = [lastExecutionRequiresAnotherBatch], isNewDataAvailable = [isNewDataAvailable], shouldConstructNextBatch = [shouldConstructNextBatch]
```

With <<MicroBatchExecution.md#constructNextBatch-shouldConstructNextBatch, shouldConstructNextBatch>> internal flag enabled, `MicroBatchExecution` commits (<<spark-sql-streaming-HDFSMetadataLog.md#add, adds>>) the available offsets for the batch to the [Write-Ahead Log (WAL)](StreamExecution.md#offsetLog) and prints out the following INFO message to the logs:

```text
Committed offsets for batch [currentBatchId]. Metadata [offsetSeqMetadata]
```

When <<MicroBatchExecution.md#runBatch, running a single streaming micro-batch>>, `MicroBatchExecution` requests every [Source](Source.md) and <<spark-sql-streaming-MicroBatchReader.md#, MicroBatchReader>> (in the [availableOffsets](StreamExecution.md#availableOffsets) registry) for unprocessed data (that has not been [committed](StreamExecution.md#committedOffsets) yet and so considered unprocessed).

In the end (of <<MicroBatchExecution.md#runBatch, running a single streaming micro-batch>>), `MicroBatchExecution` commits (<<spark-sql-streaming-HDFSMetadataLog.md#add, adds>>) the available offsets (to the <<committedOffsets, committedOffsets>> registry) so they are considered processed already.

`MicroBatchExecution` prints out the following DEBUG message to the logs:

```text
Completed batch [currentBatchId]
```

## Limitations (Assumptions)

It is assumed that the order of [streaming sources](monitoring/ProgressReporter.md#sources) in a [streaming query](StreamExecution.md#analyzedPlan) matches the order of the <<spark-sql-streaming-OffsetSeq.md#offsets, offsets>> of <<spark-sql-streaming-OffsetSeq.md#, OffsetSeq>> (in [offsetLog](StreamExecution.md#offsetLog)) and [availableOffsets](StreamExecution.md#availableOffsets).

In other words, a streaming query can be modified and then restarted from a checkpoint (to maintain stream processing state) only when the number of streaming sources and their order are preserved across restarts.
