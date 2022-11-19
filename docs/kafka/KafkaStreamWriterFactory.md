# KafkaStreamWriterFactory

`KafkaStreamWriterFactory` is a [StreamingDataWriterFactory](../StreamingDataWriterFactory.md).

## Creating Instance

`KafkaStreamWriterFactory` takes the following to be created:

* <span id="topic"> Topic Name (optional)
* <span id="producerParams"> `KafkaProducer` Parameters
* <span id="schema"> Schema (`StructType`)

`KafkaStreamWriterFactory` is created when:

* `KafkaStreamingWrite` is requested for a [StreamingDataWriterFactory](KafkaStreamingWrite.md#createStreamingWriterFactory)

## <span id="createWriter"> createWriter

```scala
createWriter(
  partitionId: Int,
  taskId: Long,
  epochId: Long): DataWriter[InternalRow]
```

`createWriter` is part of the [StreamingDataWriterFactory](../StreamingDataWriterFactory.md#createWriter) abstraction.

---

`createWriter` creates a `KafkaDataWriter` ([Spark SQL]({{ book.spark_sql }}/kafka/KafkaDataWriter)) for the given [topic](#topic), [KafkaProducer parameters](#producerParams) and [schema](#schema).
