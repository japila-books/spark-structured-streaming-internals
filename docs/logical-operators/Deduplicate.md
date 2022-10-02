# Deduplicate Unary Logical Operator

`Deduplicate` is a unary logical operator that represents [dropDuplicates](../operators/dropDuplicates.md) operator.

`Deduplicate` has <<streaming, streaming>> flag enabled for streaming Datasets.

```text
val uniqueRates = spark.
  readStream.
  format("rate").
  load.
  dropDuplicates("value")  // <-- creates Deduplicate logical operator
// Note the streaming flag
scala> println(uniqueRates.queryExecution.logical.numberedTreeString)
00 Deduplicate [value#33L], true  // <-- streaming flag enabled
01 +- StreamingRelation DataSource(org.apache.spark.sql.SparkSession@4785f176,rate,List(),None,List(),None,Map(),None), rate, [timestamp#32, value#33L]
```

CAUTION: FIXME Example with duplicates across batches to show that `Deduplicate` keeps state and [withWatermark](../operators/withWatermark.md) operator should also be used to limit how much is stored (to not cause OOM)

!!! note
    `UnsupportedOperationChecker` [ensures](../UnsupportedOperationChecker.md#checkForStreaming) that [dropDuplicates](../operators/dropDuplicates.md) operator is not used after aggregation on streaming Datasets.

    The following code is not supported in Structured Streaming and results in an `AnalysisException`.

    ```text
    val counts = spark.
      readStream.
      format("rate").
      load.
      groupBy(window($"timestamp", "5 seconds") as "group").
      agg(count("value") as "value_count").
      dropDuplicates  // <-- after groupBy

    import scala.concurrent.duration._
    import org.apache.spark.sql.streaming.{OutputMode, Trigger}
    val sq = counts.
      writeStream.
      format("console").
      trigger(Trigger.ProcessingTime(10.seconds)).
      outputMode(OutputMode.Complete).
      start
    org.apache.spark.sql.AnalysisException: dropDuplicates is not supported after aggregation on a streaming DataFrame/Dataset;;
    ```

!!! note
    `Deduplicate` logical operator is translated (_planned_) to:

    * [StreamingDeduplicateExec](../physical-operators/StreamingDeduplicateExec.md) physical operator in [StreamingDeduplicationStrategy](../execution-planning-strategies/StreamingDeduplicationStrategy.md) execution planning strategy for streaming Datasets (aka _streaming plans_)

    * `Aggregate` physical operator in `ReplaceDeduplicateWithAggregate` execution planning strategy for non-streaming/batch Datasets (_batch plans_)

[[output]]
The output schema of `Deduplicate` is exactly the <<child, child>>'s output schema.

## Creating Instance

`Deduplicate` takes the following when created:

* [[keys]] Attributes for keys
* [[child]] Child logical operator (i.e. `LogicalPlan`)
* [[streaming]] Flag whether the logical operator is for streaming (enabled) or batch (disabled) mode
