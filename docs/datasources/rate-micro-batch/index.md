# Rate Per Micro-Batch Data Source

**Rate Per Micro-Batch Data Source** provides a consistent number of rows per microbatch.

From [this commit]({{ spark.commit }}/70fde44e930926cbcd1fc95fa7cfb915c25cff9c):

> This proposes to introduce a new data source having short name as "rate-micro-batch", which produces similar input rows as "rate" (increment long values with timestamps), but ensures that each micro-batch has a "predictable" set of input rows.

> "rate-micro-batch" data source receives a config to specify the number of rows per micro-batch, which defines the set of input rows for further micro-batches. For example, if the number of rows per micro-batch is set to 1000, the first batch would have 1000 rows having value range as `0~999`, the second batch would have 1000 rows having value range as `1000~1999`, and so on. This characteristic brings different use cases compared to rate data source, as we can't predict the input rows for rate data source like this.

> For generated time (timestamp column), the data source applies the same mechanism to make the value of column be predictable. `startTimestamp` option defines the starting value of generated time, and `advanceMillisPerBatch` option defines how much time the generated time should advance per micro-batch. All input rows in the same micro-batch will have same timestamp.

Rate Per Micro-Batch data source is a new feature of Apache Spark 3.3.0 ([SPARK-37062]({{ spark.jira }}/SPARK-37062)).

## Internals

Rate Per Micro-Batch Data Source is registered by [RatePerMicroBatchProvider](RatePerMicroBatchProvider.md) to be available under [rate-micro-batch](RatePerMicroBatchProvider.md#rate-micro-batch) alias.

`RatePerMicroBatchProvider` uses [RatePerMicroBatchTable](RatePerMicroBatchTable.md) as the [Table](RatePerMicroBatchProvider.md#getTable) ([Spark SQL]({{ book.spark_sql }}/connector/Table/)).

When requested for a [MicroBatchStream](RatePerMicroBatchTable.md#newScanBuilder), `RatePerMicroBatchTable` creates a [RatePerMicroBatchStream](RatePerMicroBatchStream.md) with extra support for [Trigger.AvailableNow](../../Trigger.md#AvailableNow) mode.

Rate Per Micro-Batch Data Source supports [options](options.md) (esp. [rowsPerBatch](options.md#rowsPerBatch) and [advanceMillisPerBatch](options.md#advanceMillisPerBatch) for [Trigger.AvailableNow](RatePerMicroBatchStream.md#latestOffset) mode).
