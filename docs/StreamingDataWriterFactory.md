# StreamingDataWriterFactory

`StreamingDataWriterFactory` is an [abstraction](#contract) of [factories](#implementations) to [create a DataWriter](#createWriter).

## Contract

### <span id="createWriter"> Creating DataWriter

```java
DataWriter<InternalRow> createWriter(
  int partitionId,
  long taskId,
  long epochId)
```

Creates a `DataWriter` ([Spark SQL]({{ book.spark_sql }}/connector/DataWriter))

Used when:

* `ContinuousWriteRDD` is requested to [compute a partition](ContinuousWriteRDD.md#compute)
* `MicroBatchWriterFactory` is requested to [create a DataWriter](micro-batch-execution/MicroBatchWriterFactory.md#createWriter)

## Implementations

* `ForeachWriterFactory`
* [KafkaStreamWriterFactory](kafka/KafkaStreamWriterFactory.md)
* `MemoryWriterFactory`
* `NoopStreamingDataWriterFactory`
* `PackedRowWriterFactory`
