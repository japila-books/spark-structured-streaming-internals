# StreamingWrite

`StreamingWrite` is an [abstraction](#contract) of [streaming writers](#implementations).

## Contract

### <span id="abort"> Aborting Writing Job

```java
void abort(
  long epochId,
  WriterCommitMessage[] messages)
```

Used when:

* `MicroBatchWrite` is requested to [abort](micro-batch-execution/MicroBatchWrite.md#abort)

### <span id="commit"> Committing Writing Job

```java
void commit(
  long epochId,
  WriterCommitMessage[] messages)
```

Used when:

* `EpochCoordinator` is requested to [commitEpoch](EpochCoordinator.md#commitEpoch)
* `MicroBatchWrite` is requested to [commit](micro-batch-execution/MicroBatchWrite.md#commit)

### <span id="createStreamingWriterFactory"> Creating StreamingDataWriterFactory

```java
StreamingDataWriterFactory createStreamingWriterFactory(
  PhysicalWriteInfo info)
```

Used when:

* `MicroBatchWrite` is requested to [createBatchWriterFactory](micro-batch-execution/MicroBatchWrite.md#createBatchWriterFactory)
* `WriteToContinuousDataSourceExec` physical operator is requested to [execute](physical-operators/WriteToContinuousDataSourceExec.md#doExecute)

## Implementations

* `ForeachWrite`
* `ConsoleWrite`
* [KafkaStreamingWrite](kafka/KafkaStreamingWrite.md)
* `MemoryStreamingWrite`
* `NoopStreamingWrite`
