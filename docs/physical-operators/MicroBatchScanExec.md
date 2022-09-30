# MicroBatchScanExec Physical Operator

`MicroBatchScanExec` is a `DataSourceV2ScanExecBase` ([Spark SQL]({{ book.spark_sql }}/physical-operators/DataSourceV2ScanExecBase)) that represents [StreamingDataSourceV2Relation](../logical-operators/StreamingDataSourceV2Relation.md) logical operator at execution.

`MicroBatchScanExec` is just a very thin wrapper over [MicroBatchStream](#stream) and does nothing but delegates all the important execution-specific preparation to it.

## Creating Instance

`MicroBatchScanExec` takes the following to be created:

* <span id="output"> Output `Attribute`s ([Spark SQL]({{ book.spark_sql }}/expressions/Attribute))
* <span id="scan"> `Scan` ([Spark SQL]({{ book.spark_sql }}/connector/Scan))
* [MicroBatchStream](#stream)
* <span id="start"> Start [Offset](../Offset.md)
* <span id="end"> End [Offset](../Offset.md)
* <span id="keyGroupedPartitioning"> Key-Grouped Partitioning

`MicroBatchScanExec` is created when:

* `DataSourceV2Strategy` ([Spark SQL]({{ book.spark_sql }}/execution-planning-strategies/DataSourceV2Strategy)) execution planning strategy is requested to plan a logical query plan with a [StreamingDataSourceV2Relation](../logical-operators/StreamingDataSourceV2Relation.md)

!!! note
    All the properties to create a [MicroBatchScanExec](#creating-instance) are copied straight from a [StreamingDataSourceV2Relation](../logical-operators/StreamingDataSourceV2Relation.md) directly.

### <span id="stream"> MicroBatchStream

`MicroBatchScanExec` is given a [MicroBatchStream](../MicroBatchStream.md) when [created](#creating-instance).

The `MicroBatchStream` is the [SparkDataStream](../logical-operators/StreamingDataSourceV2Relation.md#stream) of the [StreamingDataSourceV2Relation](../logical-operators/StreamingDataSourceV2Relation.md) logical operator (it was created from).

`MicroBatchScanExec` uses the `MicroBatchStream` to handle [inputPartitions](#inputPartitions), [readerFactory](#readerFactory) and [inputRDD](#inputRDD) (that are all the custom _overloaded_ methods of `MicroBatchScanExec`).

## <span id="inputPartitions"> Input Partitions

```scala
inputPartitions: Seq[InputPartition]
```

`inputPartitions` is part of the `DataSourceV2ScanExecBase` ([Spark SQL]({{ book.spark_sql }}/physical-operators/DataSourceV2ScanExecBase#inputPartitions)) abstraction.

---

`inputPartitions` requests the [MicroBatchStream](#stream) to [planInputPartitions](../MicroBatchStream.md#planInputPartitions) for the [start](#start) and [end](#end) offsets.

## <span id="readerFactory"> PartitionReaderFactory

```scala
readerFactory: PartitionReaderFactory
```

`readerFactory` is part of the `DataSourceV2ScanExecBase` ([Spark SQL]({{ book.spark_sql }}/physical-operators/DataSourceV2ScanExecBase#readerFactory)) abstraction.

---

`readerFactory` requests the [MicroBatchStream](#stream) to [createReaderFactory](../MicroBatchStream.md#createReaderFactory).

## <span id="inputRDD"> Input RDD

```scala
inputRDD: RDD[InternalRow]
```

`inputRDD` is part of the `DataSourceV2ScanExecBase` ([Spark SQL]({{ book.spark_sql }}/physical-operators/DataSourceV2ScanExecBase#inputRDD)) abstraction.

---

`inputRDD` creates a `DataSourceRDD` ([Spark SQL]({{ book.spark_sql }}/DataSourceRDD)) for the partitions and the [PartitionReaderFactory](#readerFactory) (_and the others_).
