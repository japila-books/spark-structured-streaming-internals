# MemoryStreamBaseClass

`MemoryStreamBaseClass` is a marker extension of the [MemoryStreamBase](MemoryStreamBase.md) and [MicroBatchStream](../../MicroBatchStream.md) abstractions for [memory streaming data sources](#implementations) for [Micro-Batch Stream Processing](../../micro-batch-execution/index.md) that support [Trigger.AvailableNow](../../SupportsTriggerAvailableNow.md).

## Implementations

* [LowLatencyMemoryStream](LowLatencyMemoryStream.md)
* [MemoryStream](MemoryStream.md)

## Creating Instance

`MemoryStreamBaseClass` takes the following to be created:

* <span id="id"> ID (unused)
* <span id="sparkSession"> `SparkSession` ([Spark SQL]({{ book.spark_sql }}/SparkSession))
* <span id="numPartitions"> Number of partitions (optional; default: undefined)

??? note "Abstract Class"
    `MemoryStreamBaseClass` is an abstract class and cannot be created directly.
    It is created indirectly for the [concrete CLASSs](#implementations).
