# KafkaStreamingWrite

`KafkaStreamingWrite` is a [StreamingWrite](../StreamingWrite.md).

## Creating Instance

`KafkaStreamingWrite` takes the following to be created:

* <span id="topic"> Topic Name (Optional)
* <span id="producerParams"> Kafka Producer Parameters
* <span id="schema"> Schema

`KafkaStreamingWrite` is created when:

* `KafkaWrite` ([Spark SQL]({{ book.spark_sql }}/kafka/KafkaWrite)) is requested to `toStreaming`

## <span id="createStreamingWriterFactory"> createStreamingWriterFactory

```scala
createStreamingWriterFactory(
  info: PhysicalWriteInfo): KafkaStreamWriterFactory
```

`createStreamingWriterFactory` is part of the [StreamingWrite](../StreamingWrite.md#createStreamingWriterFactory) abstraction.

---

`createStreamingWriterFactory` creates a [KafkaStreamWriterFactory](KafkaStreamWriterFactory.md) for the given [topic](#topic), [producerParams](#producerParams) and [schema](#schema).
