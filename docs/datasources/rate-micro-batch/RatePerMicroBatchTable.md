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

## Table Capabilities { #capabilities }

??? note "Table"

    ```scala
    capabilities(): Set[TableCapability]
    ```

    `capabilities` is part of the `Table` ([Spark SQL]({{ book.spark_sql }}/connector/Table#capabilities)) abstraction.

`capabilities` is exactly `MICRO_BATCH_READ` table capability.

## Schema { #schema }

??? note "Table"

    ```scala
    schema(): StructType
    ```

    `schema` is part of the `Table` ([Spark SQL]({{ book.spark_sql }}/connector/Table#schema)) abstraction.

Name | Data Type
-----|----------
`timestamp` | `TimestampType`
`value` | `LongType`

## Create ScanBuilder { #newScanBuilder }

??? note "SupportsRead"

    ```scala
    newScanBuilder(
      options: CaseInsensitiveStringMap): ScanBuilder
    ```

    `newScanBuilder` is part of the `SupportsRead` ([Spark SQL]({{ book.spark_sql }}/connector/SupportsRead#newScanBuilder)) abstraction.

`newScanBuilder` creates a new `Scan` ([Spark SQL]({{ book.spark_sql }}/connector/Scan)) that creates a [RatePerMicroBatchStream](RatePerMicroBatchStream.md) when requested for a `MicroBatchStream` ([Spark SQL]({{ book.spark_sql }}/connector/Scan#toMicroBatchStream)).
