# LowLatencyMemoryStream

`LowLatencyMemoryStream` is a [MemoryStreamBaseClass](MemoryStreamBaseClass.md) that supports [Real-Time Mode](../../real-time-mode/SupportsRealTimeMode.md).

## Creating Instance

`LowLatencyMemoryStream` takes the following to be created:

* <span id="id"> ID
* <span id="sparkSession"> `SparkSession` ([Spark SQL]({{ book.spark_sql }}/SparkSession))
* <span id="numPartitions"> Number of partitions (default: 2)
* <span id="clock"> `Clock` (default: `LowLatencyClock`)

`LowLatencyMemoryStream` can be created using [apply](#apply) and [singlePartition](#singlePartition) factory methods.

### apply { #apply }

```scala
apply[A: Encoder](
  implicit sparkSession: SparkSession): LowLatencyMemoryStream[A]
apply[A: Encoder](
  numPartitions: Int)(
  implicit sparkSession: SparkSession): LowLatencyMemoryStream[A]
```

`apply`...FIXME

### singlePartition { #singlePartition }

```scala
singlePartition[A: Encoder](
  implicit sparkSession: SparkSession): LowLatencyMemoryStream[A]
```

`singlePartition`...FIXME

## mergeOffsets { #mergeOffsets }

??? note "SupportsRealTimeMode"

    ```scala
    mergeOffsets(
      offsets: Array[PartitionOffset]): LowLatencyMemoryStreamOffset
    ```

    `mergeOffsets` is part of the [SupportsRealTimeMode](../../real-time-mode/SupportsRealTimeMode.md#mergeOffsets) abstraction.

`mergeOffsets`...FIXME

## planInputPartitions { #planInputPartitions }

??? note "SupportsRealTimeMode"

    ```scala
    planInputPartitions(
      start: Offset): Array[InputPartition]
    ```

    `planInputPartitions` is part of the [SupportsRealTimeMode](../../real-time-mode/SupportsRealTimeMode.md#planInputPartitions) abstraction.

`planInputPartitions`...FIXME
