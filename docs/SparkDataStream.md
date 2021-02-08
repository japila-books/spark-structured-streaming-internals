# SparkDataStream

`SparkDataStream` is an [abstraction](#contract) of [readable data streams](#implementations).

## Contract

### <span id="commit"> commit

```java
void commit(
  Offset end)
```

Used when:

* `ContinuousExecution` stream execution engine is requested to [commit](continuous-execution/ContinuousExecution.md#commit)
* `MicroBatchExecution` stream execution engine is requested to [constructNextBatch](micro-batch-execution/MicroBatchExecution.md#constructNextBatch)

### <span id="deserializeOffset"> deserializeOffset

```java
Offset deserializeOffset(
  String json)
```

Used when:

* `ContinuousExecution` stream execution engine is requested to [runContinuous](continuous-execution/ContinuousExecution.md#runContinuous) and [commit](continuous-execution/ContinuousExecution.md#commit)
* `MicroBatchExecution` stream execution engine is requested to [constructNextBatch](micro-batch-execution/MicroBatchExecution.md#constructNextBatch) and [runBatch](micro-batch-execution/MicroBatchExecution.md#runBatch)

### <span id="initialOffset"> initialOffset

```java
Offset initialOffset()
```

Used when:

* `ContinuousExecution` stream execution engine is requested to [runContinuous](continuous-execution/ContinuousExecution.md#runContinuous)
* `MicroBatchExecution` stream execution engine is requested to [constructNextBatch](micro-batch-execution/MicroBatchExecution.md#constructNextBatch) and [runBatch](micro-batch-execution/MicroBatchExecution.md#runBatch)

### <span id="stop"> stop

```java
void stop()
```

Used when:

* `StreamExecution` is requested to [stop sources](StreamExecution.md#stopSources)

## Implementations

* [ContinuousStream](ContinuousStream.md)
* [MemoryStreamBase](MemoryStreamBase.md)
* [MicroBatchStream](MicroBatchStream.md)
* [Source](Source.md)
* [SupportsAdmissionControl](SupportsAdmissionControl.md)
