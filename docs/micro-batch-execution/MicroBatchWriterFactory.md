# MicroBatchWriterFactory

`MicroBatchWriterFactory` is a `DataWriterFactory` ([Spark SQL]({{ book.spark_sql }}/connector/DataWriterFactory)).

## Creating Instance

`MicroBatchWriterFactory` takes the following to be created:

* <span id="epochId"> Epoch ID
* <span id="streamingWriterFactory"> [StreamingDataWriterFactory](../StreamingDataWriterFactory.md)

`MicroBatchWriterFactory` is created when:

* `MicroBatchWrite` is requested to [create a DataWriterFactory for batch write](MicroBatchWrite.md#createBatchWriterFactory)

## <span id="createWriter"> Creating DataWriter

```scala
createWriter(
  partitionId: Int,
  taskId: Long): DataWriter[InternalRow]
```

`createWriter` is part of the `DataWriterFactory` ([Spark SQL]({{ book.spark_sql }}/connector/DataWriterFactory#createWriter)) abstraction.

---

`createWriter` requests the [StreamingDataWriterFactory](#streamingWriterFactory) for a [DataWriter](../StreamingDataWriterFactory.md#createWriter).
