# RatePerMicroBatchTable

`RatePerMicroBatchTable` is a `Table` ([Spark SQL]({{ book.spark_sql }}/connector/Table)) that `SupportsRead` ([Spark SQL]({{ book.spark_sql }}/connector/SupportsRead)).

## Creating Instance

`RatePerMicroBatchTable` takes the following to be created:

* <span id="rowsPerBatch"> [rowsPerBatch](options.md#rowsPerBatch)
* <span id="numPartitions"> [numPartitions](options.md#numPartitions)
* <span id="startTimestamp"> [startTimestamp](options.md#startTimestamp)
* <span id="advanceMillisPerBatch"> [advanceMillisPerBatch](options.md#advanceMillisPerBatch)

`RatePerMicroBatchTable` is created when:

* `RatePerMicroBatchProvider` is requested for the [table](RatePerMicroBatchProvider.md#getTable)

## <span id="schema"> schema

```scala
schema(): StructType
```

Name | Data Type
-----|----------
timestamp | TimestampType
value | LongType

`schema` is part of the `Table` ([Spark SQL]({{ book.spark_sql }}/connector/Table#schema)) abstraction.

## <span id="newScanBuilder"> Creating ScanBuilder

```scala
newScanBuilder(
  options: CaseInsensitiveStringMap): ScanBuilder
```

`newScanBuilder` is part of the `SupportsRead` ([Spark SQL]({{ book.spark_sql }}/connector/SupportsRead#newScanBuilder)) abstraction.

---

`newScanBuilder` creates a new `Scan` ([Spark SQL]({{ book.spark_sql }}/connector/Scan)) that creates a [RatePerMicroBatchStream](RatePerMicroBatchStream.md) when requested for a `MicroBatchStream` ([Spark SQL]({{ book.spark_sql }}/connector/Scan#toMicroBatchStream)).
