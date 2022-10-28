# MemoryStreamBase

`MemoryStreamBase` is an [extension](#contract) of the [SparkDataStream](../../SparkDataStream.md) abstraction for [data streams](#implementations) that [keep data](#addData) in memory.

## Contract

### <span id="addData"> Adding Data

```scala
addData(
  data: A*): Offset
addData(
  data: TraversableOnce[A]): Offset
```

Adds new data to this memory stream (and advances the current [Offset](../../Offset.md))

See [MemoryStream](MemoryStream.md#addData)

## Implementations

* [ContinuousMemoryStream](ContinuousMemoryStream.md)
* [MemoryStream](MemoryStream.md)

## Creating Instance

`MemoryStreamBase` takes the following to be created:

* <span id="sqlContext"> `SQLContext` ([Spark SQL]({{ book.spark_sql }}/SQLContext))

!!! note "Abstract Class"
    `MemoryStreamBase` is an abstract class and cannot be created directly. It is created indirectly for the [concrete MemoryStreamBases](#implementations).
