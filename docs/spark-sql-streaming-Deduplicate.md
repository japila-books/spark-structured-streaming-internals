== [[Deduplicate]] Deduplicate Unary Logical Operator

`Deduplicate` is a unary logical operator (i.e. `LogicalPlan`) that is <<creating-instance, created>> to represent spark-sql-streaming-Dataset-operators.md#dropDuplicates[dropDuplicates] operator (that drops duplicate records for a given subset of columns).

`Deduplicate` has <<streaming, streaming>> flag enabled for streaming Datasets.

[source, scala]
----
val uniqueRates = spark.
  readStream.
  format("rate").
  load.
  dropDuplicates("value")  // <-- creates Deduplicate logical operator
// Note the streaming flag
scala> println(uniqueRates.queryExecution.logical.numberedTreeString)
00 Deduplicate [value#33L], true  // <-- streaming flag enabled
01 +- StreamingRelation DataSource(org.apache.spark.sql.SparkSession@4785f176,rate,List(),None,List(),None,Map(),None), rate, [timestamp#32, value#33L]
----

CAUTION: FIXME Example with duplicates across batches to show that `Deduplicate` keeps state and spark-sql-streaming-Dataset-operators.md#withWatermark[withWatermark] operator should also be used to limit how much is stored (to not cause OOM)

[NOTE]
====
`UnsupportedOperationChecker` spark-sql-streaming-UnsupportedOperationChecker.md#checkForStreaming[ensures] that spark-sql-streaming-Dataset-operators.md#dropDuplicates[dropDuplicates] operator is not used after aggregation on streaming Datasets.

The following code is not supported in Structured Streaming and results in an `AnalysisException`.

[source, scala]
----
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
----
====

[NOTE]
====
`Deduplicate` logical operator is translated (aka _planned_) to:

* spark-sql-streaming-StreamingDeduplicateExec.md[StreamingDeduplicateExec] physical operator in spark-sql-streaming-StreamingDeduplicationStrategy.md[StreamingDeduplicationStrategy] execution planning strategy for streaming Datasets (aka _streaming plans_)

* `Aggregate` physical operator in `ReplaceDeduplicateWithAggregate` execution planning strategy for non-streaming/batch Datasets (aka _batch plans_)
====

[[output]]
The output schema of `Deduplicate` is exactly the <<child, child>>'s output schema.

=== [[creating-instance]] Creating Deduplicate Instance

`Deduplicate` takes the following when created:

* [[keys]] Attributes for keys
* [[child]] Child logical operator (i.e. `LogicalPlan`)
* [[streaming]] Flag whether the logical operator is for streaming (enabled) or batch (disabled) mode
