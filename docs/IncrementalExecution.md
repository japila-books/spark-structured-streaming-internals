# IncrementalExecution

`IncrementalExecution` is the `QueryExecution` ([Spark SQL]({{ book.spark_sql }}/QueryExecution)) of streaming queries.

## Creating Instance

`IncrementalExecution` takes the following to be created:

* <span id="sparkSession"> `SparkSession` ([Spark SQL]({{ book.spark_sql }}/SparkSession))
* <span id="logicalPlan"> `LogicalPlan` ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan))
* <span id="outputMode"> [OutputMode](OutputMode.md)
* <span id="checkpointLocation"> [State checkpoint location](#state-checkpoint-location)
* <span id="queryId"> Query ID
* <span id="runId"> Run ID
* <span id="currentBatchId"> Current Batch ID
* <span id="offsetSeqMetadata"> [OffsetSeqMetadata](OffsetSeqMetadata.md)

`IncrementalExecution` is created (and becomes the [StreamExecution.lastExecution](StreamExecution.md#lastExecution)) when:

* `MicroBatchExecution` is requested to [run a single streaming micro-batch](micro-batch-execution/MicroBatchExecution.md#runBatch) (in [queryPlanning](micro-batch-execution/MicroBatchExecution.md#runBatch-queryPlanning) phase)
* `ContinuousExecution` is requested to [run a streaming query in continuous mode](continuous-execution/ContinuousExecution.md#runContinuous) (in [queryPlanning](continuous-execution/ContinuousExecution.md#runContinuous-queryPlanning) phase)
* [Dataset.explain](operators/explain.md) operator is executed (on a streaming query)

## <span id="statefulOperatorId"> statefulOperatorId

`IncrementalExecution` uses the `statefulOperatorId` internal counter for the IDs of the stateful operators in the [optimized logical plan](#optimizedPlan) (while applying the [preparations](#preparations) rules) when requested to prepare the plan for execution (in [executedPlan](#executedPlan) phase).

## <span id="preparing-for-execution"><span id="optimizedPlan"><span id="executedPlan"><span id="preparations"> Preparing Logical Plan (of Streaming Query) for Execution

When requested for the optimized logical plan (of the [logical plan](#logicalPlan)), `IncrementalExecution` transforms `CurrentBatchTimestamp` and `ExpressionWithRandomSeed` expressions with the timestamp literal and new random seeds, respectively. When transforming `CurrentBatchTimestamp` expressions, `IncrementalExecution` prints out the following INFO message to the logs:

```text
Current batch timestamp = [timestamp]
```

Right after [being created](#creating-instance), `IncrementalExecution` is executed (in the **queryPlanning** phase by the [MicroBatchExecution](micro-batch-execution/MicroBatchExecution.md) and [ContinuousExecution](continuous-execution/ContinuousExecution.md) stream execution engines) and so the entire query execution pipeline is executed up to and including _executedPlan_. That means that the [extra planning strategies](#extraPlanningStrategies) and the [state preparation rule](#state) have been applied at this point and the [streaming query](#logicalPlan) is ready for execution.

## State Checkpoint Location

`IncrementalExecution` is given the [checkpoint location](#checkpointLocation) when [created](#creating-instance).

For the two available execution engines ([MicroBatchExecution](micro-batch-execution/MicroBatchExecution.md) and [ContinuousExecution](continuous-execution/ContinuousExecution.md)), the checkpoint location is actually **state** directory under the [checkpoint root directory](StreamExecution.md#resolvedCheckpointRoot).

```text
val queryName = "rate2memory"
val checkpointLocation = s"file:/tmp/checkpoint-$queryName"
val query = spark
  .readStream
  .format("rate")
  .load
  .writeStream
  .format("memory")
  .queryName(queryName)
  .option("checkpointLocation", checkpointLocation)
  .start

// Give the streaming query a moment (one micro-batch)
// So lastExecution is available for the checkpointLocation
import scala.concurrent.duration._
query.awaitTermination(1.second.toMillis)

import org.apache.spark.sql.execution.streaming.StreamingQueryWrapper
val stateCheckpointDir = query
  .asInstanceOf[StreamingQueryWrapper]
  .streamingQuery
  .lastExecution
  .checkpointLocation
val stateDir = s"$checkpointLocation/state"
assert(stateCheckpointDir equals stateDir)
```

State checkpoint location is used when `IncrementalExecution` is requested for the [state info of the next stateful operator](#nextStatefulOperationStateInfo) (when requested to optimize a streaming physical plan using the [state preparation rule](#state) that creates the stateful physical operators: [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md), [StateStoreRestoreExec](physical-operators/StateStoreRestoreExec.md), [StreamingDeduplicateExec](physical-operators/StreamingDeduplicateExec.md), [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md), [StreamingSymmetricHashJoinExec](physical-operators/StreamingSymmetricHashJoinExec.md), and [StreamingGlobalLimitExec](physical-operators/StreamingGlobalLimitExec.md)).

## <span id="numStateStores"> Number of State Stores (spark.sql.shuffle.partitions)

```scala
numStateStores: Int
```

`numStateStores` is the **number of state stores** which corresponds to `spark.sql.shuffle.partitions` configuration property (default: `200`).

!!! tip
    Learn more about [spark.sql.shuffle.partitions]({{ book.spark_sql }}/spark-sql-properties.html#spark.sql.shuffle.partitions) configuration property in [The Internals of Spark SQL]({{ book.spark_sql }}) online book.

Internally, `numStateStores` requests the [OffsetSeqMetadata](#offsetSeqMetadata) for the [spark.sql.shuffle.partitions](SQLConf.md#SHUFFLE_PARTITIONS) configuration property (using the [streaming configuration](OffsetSeqMetadata.md#conf)) or simply takes whatever was defined for the given [SparkSession](#sparkSession) (default: `200`).

`numStateStores` is initialized right when `IncrementalExecution` is [created](#creating-instance).

`numStateStores` is used when `IncrementalExecution` is requested for the [state info of the next stateful operator](#nextStatefulOperationStateInfo) (when requested to optimize a streaming physical plan using the [state preparation rule](#state) that creates the stateful physical operators: [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md), [StateStoreRestoreExec](physical-operators/StateStoreRestoreExec.md), [StreamingDeduplicateExec](physical-operators/StreamingDeduplicateExec.md), [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md), [StreamingSymmetricHashJoinExec](physical-operators/StreamingSymmetricHashJoinExec.md), and [StreamingGlobalLimitExec](physical-operators/StreamingGlobalLimitExec.md)).

## <span id="planner"><span id="extraPlanningStrategies"> Extra Planning Strategies for Streaming Queries

`IncrementalExecution` uses a custom `SparkPlanner` ([Spark SQL]({{ book.spark_sql }}/SparkPlanner)) with the following **extra planning strategies** to plan the [streaming query](#logicalPlan) for execution:

1. [StreamingJoinStrategy](execution-planning-strategies/StreamingJoinStrategy.md)
1. [StatefulAggregationStrategy](execution-planning-strategies/StatefulAggregationStrategy.md)
1. [FlatMapGroupsWithStateStrategy](execution-planning-strategies/FlatMapGroupsWithStateStrategy.md)
1. [StreamingRelationStrategy](execution-planning-strategies/StreamingRelationStrategy.md)
1. [StreamingDeduplicationStrategy](execution-planning-strategies/StreamingDeduplicationStrategy.md)
1. [StreamingGlobalLimitStrategy](execution-planning-strategies/StreamingGlobalLimitStrategy.md)

## <span id="state"> State Preparation Rule For Execution-Specific Configuration

```scala
state: Rule[SparkPlan]
```

`state` is a custom physical preparation rule (`Rule[SparkPlan]`) that can transform a streaming physical plan (`SparkPlan`) with the following physical operators:

* [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md) with any unary physical operator (`UnaryExecNode`) with a [StateStoreRestoreExec](physical-operators/StateStoreRestoreExec.md)

* [StreamingDeduplicateExec](physical-operators/StreamingDeduplicateExec.md)

* [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md)

* [StreamingSymmetricHashJoinExec](physical-operators/StreamingSymmetricHashJoinExec.md)

* [StreamingGlobalLimitExec](physical-operators/StreamingGlobalLimitExec.md)

`state` simply transforms the physical plan with the above physical operators and fills out the execution-specific configuration:

* [nextStatefulOperationStateInfo](#nextStatefulOperationStateInfo) for the state info

* [OutputMode](#outputMode)

* [batchWatermarkMs](OffsetSeqMetadata.md#batchWatermarkMs) (through the [OffsetSeqMetadata](#offsetSeqMetadata)) for the event-time watermark

* [batchTimestampMs](OffsetSeqMetadata.md#batchTimestampMs) (through the [OffsetSeqMetadata](#offsetSeqMetadata)) for the current timestamp

* [getStateWatermarkPredicates](StreamingSymmetricHashJoinHelper.md#getStateWatermarkPredicates) for the state watermark predicates (for [StreamingSymmetricHashJoinExec](physical-operators/StreamingSymmetricHashJoinExec.md))

`state` rule is used (as part of the physical query optimizations) when `IncrementalExecution` is requested to [optimize (prepare) the physical plan of the streaming query](#executedPlan) (once for [ContinuousExecution](continuous-execution/ContinuousExecution.md) and every trigger for [MicroBatchExecution](micro-batch-execution/MicroBatchExecution.md) in **queryPlanning** phase).

!!! tip
    Learn more about [Physical Query Optimizations]({{ book.spark_sql }}/QueryExecution#preparations) in [The Internals of Spark SQL]({{ book.spark_sql }}) online book.

## <span id="nextStatefulOperationStateInfo"> Next StatefulOperationStateInfo

```scala
nextStatefulOperationStateInfo(): StatefulOperatorStateInfo
```

`nextStatefulOperationStateInfo` simply creates a new [StatefulOperatorStateInfo](StatefulOperatorStateInfo.md) with the [state checkpoint location](#state-checkpoint-location), the [run ID](#runId) (of the streaming query), the next [statefulOperator ID](#statefulOperatorId), the [current batch ID](#currentBatchId), and the [number of state stores](#numStateStores).

!!! note
    The only changing part of `StatefulOperatorStateInfo` across calls of the `nextStatefulOperationStateInfo` method is the the next  [statefulOperator ID](#statefulOperatorId).
 
    All the other properties (the [state checkpoint location](#state-checkpoint-location), the [run ID](#runId), the [current batch ID](#currentBatchId), and the [number of state stores](#numStateStores)) are the same within a single `IncrementalExecution` instance.
 
    The only two properties that may ever change are the [run ID](#runId) (after a streaming query is restarted from the checkpoint) and  the [current batch ID](#currentBatchId) (every micro-batch in [MicroBatchExecution](micro-batch-execution/MicroBatchExecution.md) execution engine).

`nextStatefulOperationStateInfo` is used when `IncrementalExecution` is requested to optimize a streaming physical plan using the [state preparation rule](#state) (and creates the stateful physical operators: [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md), [StateStoreRestoreExec](physical-operators/StateStoreRestoreExec.md), [StreamingDeduplicateExec](physical-operators/StreamingDeduplicateExec.md), [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md), [StreamingSymmetricHashJoinExec](physical-operators/StreamingSymmetricHashJoinExec.md), and [StreamingGlobalLimitExec](physical-operators/StreamingGlobalLimitExec.md)).

## <span id="shouldRunAnotherBatch"> Checking Out Whether Last Execution Requires Another Non-Data Micro-Batch

```scala
shouldRunAnotherBatch(
   newMetadata: OffsetSeqMetadata): Boolean
```

`shouldRunAnotherBatch` is positive (`true`) if there is at least one [StateStoreWriter](physical-operators/StateStoreWriter.md) operator (in the [executedPlan physical query plan](#executedPlan)) that [requires another non-data batch](physical-operators/StateStoreWriter.md#shouldRunAnotherBatch) (per the given [OffsetSeqMetadata](OffsetSeqMetadata.md) with the event-time watermark and the batch timestamp).

Otherwise, `shouldRunAnotherBatch` is negative (`false`).

`shouldRunAnotherBatch` is used when `MicroBatchExecution` is requested to [construct the next streaming micro-batch](micro-batch-execution/MicroBatchExecution.md#constructNextBatch) (and checks out whether the last batch execution requires another non-data batch).

## Demo: State Checkpoint Directory

Using `setConf(SHUFFLE_PARTITIONS, 1)` will make for an easier debugging as the state is then only for one partition and makes monitoring easier.

```scala
import org.apache.spark.sql.internal.SQLConf.SHUFFLE_PARTITIONS
spark.sessionState.conf.setConf(SHUFFLE_PARTITIONS, 1)

assert(spark.sessionState.conf.numShufflePartitions == 1)
```

Using the `rate` source as an input.

```scala
val counts = spark
  .readStream
  .format("rate")
  .load
  .groupBy(window($"timestamp", "5 seconds") as "group")
  .agg(count("value") as "value_count") // <-- creates an Aggregate logical operator
  .orderBy("group")  // <-- makes for easier checking

assert(counts.isStreaming, "This should be a streaming query")
```

Searching for `checkpoint = <unknown>` in the following output for [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md) and [StateStoreRestoreExec](physical-operators/StateStoreRestoreExec.md) physical operators.

```text
scala> counts.explain
== Physical Plan ==
*(5) Sort [group#5 ASC NULLS FIRST], true, 0
+- Exchange rangepartitioning(group#5 ASC NULLS FIRST, 1)
   +- *(4) HashAggregate(keys=[window#11], functions=[count(value#1L)])
      +- StateStoreSave [window#11], state info [ checkpoint = <unknown>, runId = 558bf725-accb-487d-97eb-f790fa4a6138, opId = 0, ver = 0, numPartitions = 1], Append, 0, 2
         +- *(3) HashAggregate(keys=[window#11], functions=[merge_count(value#1L)])
            +- StateStoreRestore [window#11], state info [ checkpoint = <unknown>, runId = 558bf725-accb-487d-97eb-f790fa4a6138, opId = 0, ver = 0, numPartitions = 1], 2
               +- *(2) HashAggregate(keys=[window#11], functions=[merge_count(value#1L)])
                  +- Exchange hashpartitioning(window#11, 1)
                     +- *(1) HashAggregate(keys=[window#11], functions=[partial_count(value#1L)])
                        +- *(1) Project [named_struct(start, precisetimestampconversion(((((CASE WHEN (cast(CEIL((cast((precisetimestampconversion(timestamp#0, TimestampType, LongType) - 0) as double) / 5000000.0)) as double) = (cast((precisetimestampconversion(timestamp#0, TimestampType, LongType) - 0) as double) / 5000000.0)) THEN (CEIL((cast((precisetimestampconversion(timestamp#0, TimestampType, LongType) - 0) as double) / 5000000.0)) + 1) ELSE CEIL((cast((precisetimestampconversion(timestamp#0, TimestampType, LongType) - 0) as double) / 5000000.0)) END + 0) - 1) * 5000000) + 0), LongType, TimestampType), end, precisetimestampconversion(((((CASE WHEN (cast(CEIL((cast((precisetimestampconversion(timestamp#0, TimestampType, LongType) - 0) as double) / 5000000.0)) as double) = (cast((precisetimestampconversion(timestamp#0, TimestampType, LongType) - 0) as double) / 5000000.0)) THEN (CEIL((cast((precisetimestampconversion(timestamp#0, TimestampType, LongType) - 0) as double) / 5000000.0)) + 1) ELSE CEIL((cast((precisetimestampconversion(timestamp#0, TimestampType, LongType) - 0) as double) / 5000000.0)) END + 0) - 1) * 5000000) + 5000000), LongType, TimestampType)) AS window#11, value#1L]
                           +- *(1) Filter isnotnull(timestamp#0)
                              +- StreamingRelation rate, [timestamp#0, value#1L]
```

Start the query with the `checkpointLocation` option.

```scala
val checkpointLocation = "/tmp/spark-streams-state-checkpoint-root"

import scala.concurrent.duration._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
val t = Trigger.ProcessingTime(1.hour) // should be enough time for exploration
val sq = counts
  .writeStream
  .format("console")
  .option("truncate", false)
  .option("checkpointLocation", checkpointLocation)
  .trigger(t)
  .outputMode(OutputMode.Complete)
  .start
```

Wait till the first batch which should happen right after start and access `lastExecution` that has the checkpoint resolved.

```scala
import org.apache.spark.sql.execution.streaming._
val lastExecution = sq.asInstanceOf[StreamingQueryWrapper].streamingQuery.lastExecution
assert(lastExecution.checkpointLocation == s"file:${checkpointLocation}/state")
```
