== Offsets and Metadata Checkpointing

A streaming query can be started from scratch or from checkpoint (that gives fault-tolerance as the state is preserved even when a failure happens).

<<spark-sql-streaming-StreamExecution.md#extensions, Stream execution engines>> use *checkpoint location* to resume stream processing and get *start offsets* to start query processing from.

`StreamExecution` resumes (populates the start offsets) from the latest checkpointed offsets from the <<spark-sql-streaming-StreamExecution.md#offsetLog, Write-Ahead Log (WAL) of Offsets>> that may have already been processed (and, if so, committed to the <<spark-sql-streaming-StreamExecution.md#commitLog, Offset Commit Log>>).

* <<spark-sql-streaming-OffsetSeqLog.md#, Hadoop DFS-based metadata storage>> of <<spark-sql-streaming-OffsetSeq.md#, OffsetSeqs>>

* <<spark-sql-streaming-OffsetSeq.md#, OffsetSeq>> and <<spark-sql-streaming-StreamProgress.md#, StreamProgress>>

* <<spark-sql-streaming-StreamProgress.md#, StreamProgress>> and <<spark-sql-streaming-StreamExecution.md#, StreamExecutions>> (<<spark-sql-streaming-StreamExecution.md#committedOffsets, committed>> and <<spark-sql-streaming-StreamExecution.md#availableOffsets, available>> offsets)

=== [[micro-batch-stream-processing]] Micro-Batch Stream Processing

In <<spark-sql-streaming-micro-batch-stream-processing.md#, Micro-Batch Stream Processing>>, the <<spark-sql-streaming-StreamExecution.md#availableOffsets, available offsets>> registry is populated with the <<spark-sql-streaming-HDFSMetadataLog.md#getLatest, latest offsets>> from the <<spark-sql-streaming-StreamExecution.md#offsetLog, Write-Ahead Log (WAL)>> when `MicroBatchExecution` stream processing engine is requested to <<spark-sql-streaming-MicroBatchExecution.md#populateStartOffsets, populate start offsets from checkpoint>> (if available) when `MicroBatchExecution` is requested to <<spark-sql-streaming-MicroBatchExecution.md#runActivatedStream, run an activated streaming query>> (<<spark-sql-streaming-MicroBatchExecution.md#runActivatedStream-triggerExecution-populateStartOffsets, before the first "zero" micro-batch>>).

The <<spark-sql-streaming-StreamExecution.md#availableOffsets, available offsets>> are then <<spark-sql-streaming-StreamProgress.md#plusplus, added>> to the <<spark-sql-streaming-StreamExecution.md#committedOffsets, committed offsets>> when the latest batch ID available (as described above) is exactly the <<spark-sql-streaming-HDFSMetadataLog.md#getLatest, latest batch ID>> committed to the <<spark-sql-streaming-StreamExecution.md#commitLog, Offset Commit Log>> when `MicroBatchExecution` stream processing engine is requested to <<spark-sql-streaming-MicroBatchExecution.md#populateStartOffsets, populate start offsets from checkpoint>>.

When a streaming query is started from scratch (with no checkpoint that has offsets in the <<spark-sql-streaming-StreamExecution.md#offsetLog, Offset Write-Ahead Log>>), `MicroBatchExecution` prints out the following INFO message:

```
Starting new streaming query.
```

When a streaming query is resumed (restarted) from a checkpoint with offsets in the <<spark-sql-streaming-StreamExecution.md#offsetLog, Offset Write-Ahead Log>>, `MicroBatchExecution` prints out the following INFO message:

[options="wrap"]
----
Resuming at batch [currentBatchId] with committed offsets [committedOffsets] and available offsets [availableOffsets]
----

Every time `MicroBatchExecution` is requested to <<spark-sql-streaming-MicroBatchExecution.md#isNewDataAvailable, check whether a new data is available>> (in any of the streaming sources)...FIXME

When `MicroBatchExecution` is requested to <<spark-sql-streaming-MicroBatchExecution.md#constructNextBatch, construct the next streaming micro-batch>> (when `MicroBatchExecution` requested to <<spark-sql-streaming-MicroBatchExecution.md#runActivatedStream, run the activated streaming query>>), every <<spark-sql-streaming-StreamExecution.md#uniqueSources, streaming source>> is requested for the latest offset available that are <<spark-sql-streaming-StreamProgress.md#plusplus, added>> to the <<spark-sql-streaming-StreamExecution.md#availableOffsets, availableOffsets>> registry. <<spark-sql-streaming-BaseStreamingSource.md#, Streaming sources>> report some offsets or none at all (if this source has never received any data). Streaming sources with no data are excluded (_filtered out_).

`MicroBatchExecution` prints out the following TRACE message to the logs:

[options="wrap"]
----
noDataBatchesEnabled = [noDataBatchesEnabled], lastExecutionRequiresAnotherBatch = [lastExecutionRequiresAnotherBatch], isNewDataAvailable = [isNewDataAvailable], shouldConstructNextBatch = [shouldConstructNextBatch]
----

With <<spark-sql-streaming-MicroBatchExecution.md#constructNextBatch-shouldConstructNextBatch, shouldConstructNextBatch>> internal flag enabled, `MicroBatchExecution` commits (<<spark-sql-streaming-HDFSMetadataLog.md#add, adds>>) the available offsets for the batch to the <<spark-sql-streaming-StreamExecution.md#offsetLog, Write-Ahead Log (WAL)>> and prints out the following INFO message to the logs:

[options="wrap"]
----
Committed offsets for batch [currentBatchId]. Metadata [offsetSeqMetadata]
----

When <<spark-sql-streaming-MicroBatchExecution.md#runBatch, running a single streaming micro-batch>>, `MicroBatchExecution` requests every [Source](Source.md) and <<spark-sql-streaming-MicroBatchReader.md#, MicroBatchReader>> (in the <<spark-sql-streaming-StreamExecution.md#availableOffsets, availableOffsets>> registry) for unprocessed data (that has not been <<spark-sql-streaming-StreamExecution.md#committedOffsets, committed>> yet and so considered unprocessed).

In the end (of <<spark-sql-streaming-MicroBatchExecution.md#runBatch, running a single streaming micro-batch>>), `MicroBatchExecution` commits (<<spark-sql-streaming-HDFSMetadataLog.md#add, adds>>) the available offsets (to the <<committedOffsets, committedOffsets>> registry) so they are considered processed already.

`MicroBatchExecution` prints out the following DEBUG message to the logs:

```
Completed batch [currentBatchId]
```

=== Limitations (Assumptions)

It is assumed that the order of <<spark-sql-streaming-ProgressReporter.md#sources, streaming sources>> in a <<spark-sql-streaming-StreamExecution.md#analyzedPlan, streaming query>> matches the order of the <<spark-sql-streaming-OffsetSeq.md#offsets, offsets>> of <<spark-sql-streaming-OffsetSeq.md#, OffsetSeq>> (in <<spark-sql-streaming-StreamExecution.md#offsetLog, offsetLog>>) and <<spark-sql-streaming-StreamExecution.md#availableOffsets, availableOffsets>>.

In other words, a streaming query can be modified and then restarted from a checkpoint (to maintain stream processing state) only when the number of streaming sources and their order are preserved across restarts.
