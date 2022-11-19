# MicroBatchWrite

`MicroBatchWrite` is a `BatchWrite` ([Spark SQL]({{ book.spark_sql }}/connector/BatchWrite)) for [WriteToDataSourceV2](../logical-operators/WriteToDataSourceV2.md) logical operator in [Micro-Batch Stream Processing](index.md).

!!! note "WriteToMicroBatchDataSource"
    `WriteToDataSourceV2` logical operator replaces [WriteToMicroBatchDataSource](../logical-operators/WriteToMicroBatchDataSource.md) logical operator at logical optimization (using `V2Writes` logical optimization).

`MicroBatchWrite` is just a very thin wrapper over [StreamingWrite](#writeSupport) and does nothing but delegates (_relays_) all the important execution-specific calls to it.

## Creating Instance

`MicroBatchWrite` takes the following to be created:

* <span id="epochId"> Epoch ID
* <span id="writeSupport"> [StreamingWrite](../StreamingWrite.md)

`MicroBatchWrite` is created when:

* `V2Writes` ([Spark SQL]({{ book.spark_sql }}/logical-optimizations/V2Writes)) logical optimization is requested to optimize a logical plan (with a [WriteToMicroBatchDataSource](../logical-operators/WriteToMicroBatchDataSource.md))

## <span id="commit"> Committing Writing Job

```scala
commit(
  messages: Array[WriterCommitMessage]): Unit
```

`commit` is part of the `BatchWrite` ([Spark SQL]({{ book.spark_sql }}/connector/BatchWrite#commit)) abstraction.

---

`commit` requests the [StreamingWrite](#writeSupport) to [commit](../StreamingWrite.md#commit).

## <span id="createBatchWriterFactory"> Creating DataWriterFactory for Batch Write

```scala
createBatchWriterFactory(
  info: PhysicalWriteInfo): DataWriterFactory
```

`createBatchWriterFactory` is part of the `BatchWrite` ([Spark SQL]({{ book.spark_sql }}/connector/BatchWrite#createBatchWriterFactory)) abstraction.

---

`createBatchWriterFactory` requests the [StreamingWrite](#writeSupport) to [create a StreamingDataWriterFactory](../StreamingWrite.md#createStreamingWriterFactory).

In the end, `createBatchWriterFactory` creates a [MicroBatchWriterFactory](MicroBatchWriterFactory.md) (with the given [epochId](#epochId) and the `StreamingDataWriterFactory`).
