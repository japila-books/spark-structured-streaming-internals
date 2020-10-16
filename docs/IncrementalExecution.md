# IncrementalExecution &mdash; QueryExecution of Streaming Queries

`IncrementalExecution` is the `QueryExecution` of streaming queries.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-QueryExecution.html[QueryExecution] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

`IncrementalExecution` is <<creating-instance, created>> (and becomes the [StreamExecution.lastExecution](StreamExecution.md#lastExecution)) when:

* `MicroBatchExecution` is requested to <<MicroBatchExecution.md#runBatch, run a single streaming micro-batch>> (in <<MicroBatchExecution.md#runBatch-queryPlanning, queryPlanning>> phase)

* `ContinuousExecution` is requested to <<ContinuousExecution.md#runContinuous, run a streaming query in continuous mode>> (in <<ContinuousExecution.md#runContinuous-queryPlanning, queryPlanning>> phase)

* [Dataset.explain](operators/explain.md) operator is executed (on a streaming query)

[[statefulOperatorId]]
`IncrementalExecution` uses the `statefulOperatorId` internal counter for the IDs of the stateful operators in the <<optimizedPlan, optimized logical plan>> (while applying the <<preparations, preparations>> rules) when requested to prepare the plan for execution (in <<executedPlan, executedPlan>> phase).

=== [[preparing-for-execution]][[optimizedPlan]][[executedPlan]][[preparations]] Preparing Logical Plan (of Streaming Query) for Execution -- `optimizedPlan` and `executedPlan` Phases of Query Execution

When requested for the optimized logical plan (of the <<logicalPlan, logical plan>>), `IncrementalExecution` transforms `CurrentBatchTimestamp` and `ExpressionWithRandomSeed` expressions with the timestamp literal and new random seeds, respectively. When transforming `CurrentBatchTimestamp` expressions, `IncrementalExecution` prints out the following INFO message to the logs:

```
Current batch timestamp = [timestamp]
```

Once <<creating-instance, created>>, `IncrementalExecution` is immediately executed (by the <<MicroBatchExecution.md#, MicroBatchExecution>> and <<ContinuousExecution.md#, ContinuousExecution>> stream execution engines in the *queryPlanning* phase) and so the entire query execution pipeline is executed up to and including _executedPlan_. That means that the <<extraPlanningStrategies, extra planning strategies>> and the <<state, state preparation rule>> have been applied at this point and the <<logicalPlan, streaming query>> is ready for execution.

=== [[creating-instance]] Creating IncrementalExecution Instance

`IncrementalExecution` takes the following to be created:

* [[sparkSession]] `SparkSession`
* [[logicalPlan]] Logical plan (`LogicalPlan`)
* [[outputMode]] [OutputMode](OutputMode.md) (as specified using [DataStreamWriter.outputMode](DataStreamWriter.md#outputMode) method)
* [[checkpointLocation]] <<state-checkpoint-location, State checkpoint location>>
* [[runId]] Run ID of a streaming query (`UUID`)
* [[currentBatchId]] Batch ID
* [[offsetSeqMetadata]] <<spark-sql-streaming-OffsetSeqMetadata.md#, OffsetSeqMetadata>>

=== [[state-checkpoint-location]] State Checkpoint Location (Directory)

When <<creating-instance, created>>, `IncrementalExecution` is given the <<checkpointLocation, checkpoint location>>.

For the two available execution engines (<<MicroBatchExecution.md#, MicroBatchExecution>> and <<ContinuousExecution.md#, ContinuousExecution>>), the checkpoint location is actually *state* directory under the [checkpoint root directory](StreamExecution.md#resolvedCheckpointRoot).

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

State checkpoint location is used exclusively when `IncrementalExecution` is requested for the <<nextStatefulOperationStateInfo, state info of the next stateful operator>> (when requested to optimize a streaming physical plan using the <<state, state preparation rule>> that creates the stateful physical operators: <<StateStoreSaveExec.md#, StateStoreSaveExec>>, <<spark-sql-streaming-StateStoreRestoreExec.md#, StateStoreRestoreExec>>, <<physical-operators/StreamingDeduplicateExec.md#, StreamingDeduplicateExec>>, [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md), <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>>, and <<physical-operators/StreamingGlobalLimitExec.md#, StreamingGlobalLimitExec>>).

=== [[numStateStores]] Number of State Stores (spark.sql.shuffle.partitions) -- `numStateStores` Internal Property

[source, scala]
----
numStateStores: Int
----

`numStateStores` is the *number of state stores* which corresponds to `spark.sql.shuffle.partitions` configuration property (default: `200`).

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-properties.html#spark.sql.shuffle.partitions[spark.sql.shuffle.partitions] configuration property (and the others) in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

Internally, `numStateStores` requests the <<offsetSeqMetadata, OffsetSeqMetadata>> for the <<SQLConf.md#SHUFFLE_PARTITIONS, spark.sql.shuffle.partitions>> configuration property (using the <<spark-sql-streaming-OffsetSeqMetadata.md#conf, streaming configuration>>) or simply takes whatever was defined for the given <<sparkSession, SparkSession>> (default: `200`).

`numStateStores` is initialized right when `IncrementalExecution` is <<creating-instance, created>>.

`numStateStores` is used exclusively when `IncrementalExecution` is requested for the <<nextStatefulOperationStateInfo, state info of the next stateful operator>> (when requested to optimize a streaming physical plan using the <<state, state preparation rule>> that creates the stateful physical operators: <<StateStoreSaveExec.md#, StateStoreSaveExec>>, <<spark-sql-streaming-StateStoreRestoreExec.md#, StateStoreRestoreExec>>, <<physical-operators/StreamingDeduplicateExec.md#, StreamingDeduplicateExec>>, [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md), <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>>, and <<physical-operators/StreamingGlobalLimitExec.md#, StreamingGlobalLimitExec>>).

=== [[planner]][[extraPlanningStrategies]] Extra Planning Strategies for Streaming Queries -- `planner` Property

`IncrementalExecution` uses a custom `SparkPlanner` with the following *extra planning strategies* to plan the <<logicalPlan, streaming query>> for execution:

* <<spark-sql-streaming-StreamingJoinStrategy.md#, StreamingJoinStrategy>>
* <<spark-sql-streaming-StatefulAggregationStrategy.md#, StatefulAggregationStrategy>>
* <<spark-sql-streaming-FlatMapGroupsWithStateStrategy.md#, FlatMapGroupsWithStateStrategy>>
* <<spark-sql-streaming-StreamingRelationStrategy.md#, StreamingRelationStrategy>>
* <<spark-sql-streaming-StreamingDeduplicationStrategy.md#, StreamingDeduplicationStrategy>>
* <<spark-sql-streaming-StreamingGlobalLimitStrategy.md#, StreamingGlobalLimitStrategy>>

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-SparkPlanner.html[SparkPlanner] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

=== [[state]] State Preparation Rule For Execution-Specific Configuration -- `state` Property

[source, scala]
----
state: Rule[SparkPlan]
----

`state` is a custom physical preparation rule (`Rule[SparkPlan]`) that can transform a streaming physical plan (`SparkPlan`) with the following physical operators:

* <<StateStoreSaveExec.md#, StateStoreSaveExec>> with any unary physical operator (`UnaryExecNode`) with a <<spark-sql-streaming-StateStoreRestoreExec.md#, StateStoreRestoreExec>>

* <<physical-operators/StreamingDeduplicateExec.md#, StreamingDeduplicateExec>>

* [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md)

* <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>>

* <<physical-operators/StreamingGlobalLimitExec.md#, StreamingGlobalLimitExec>>

`state` simply transforms the physical plan with the above physical operators and fills out the execution-specific configuration:

* <<nextStatefulOperationStateInfo, nextStatefulOperationStateInfo>> for the state info

* <<outputMode, OutputMode>>

* <<spark-sql-streaming-OffsetSeqMetadata.md#batchWatermarkMs, batchWatermarkMs>> (through the <<offsetSeqMetadata, OffsetSeqMetadata>>) for the event-time watermark

* <<spark-sql-streaming-OffsetSeqMetadata.md#batchTimestampMs, batchTimestampMs>> (through the <<offsetSeqMetadata, OffsetSeqMetadata>>) for the current timestamp

* <<spark-sql-streaming-StreamingSymmetricHashJoinHelper.md#getStateWatermarkPredicates, getStateWatermarkPredicates>> for the state watermark predicates (for <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>>)

`state` rule is used (as part of the physical query optimizations) when `IncrementalExecution` is requested to <<executedPlan, optimize (prepare) the physical plan of the streaming query>> (once for <<ContinuousExecution.md#, ContinuousExecution>> and every trigger for <<MicroBatchExecution.md#, MicroBatchExecution>> in their *queryPlanning* phases).

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-QueryExecution.html#preparations[Physical Query Optimizations] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

=== [[nextStatefulOperationStateInfo]] `nextStatefulOperationStateInfo` Internal Method

[source, scala]
----
nextStatefulOperationStateInfo(): StatefulOperatorStateInfo
----

`nextStatefulOperationStateInfo` simply creates a new <<spark-sql-streaming-StatefulOperatorStateInfo.md#, StatefulOperatorStateInfo>> with the <<state-checkpoint-location, state checkpoint location>>, the <<runId, run ID>> (of the streaming query), the next <<statefulOperatorId, statefulOperator ID>>, the <<currentBatchId, current batch ID>>, and the <<numStateStores, number of state stores>>.

[NOTE]
====
The only changing part of `StatefulOperatorStateInfo` across calls of the `nextStatefulOperationStateInfo` method is the the next <<statefulOperatorId, statefulOperator ID>>.

All the other properties (the <<state-checkpoint-location, state checkpoint location>>, the <<runId, run ID>>, the <<currentBatchId, current batch ID>>, and the <<numStateStores, number of state stores>>) are the same within a single `IncrementalExecution` instance.

The only two properties that may ever change are the <<runId, run ID>> (after a streaming query is restarted from the checkpoint) and the <<currentBatchId, current batch ID>> (every micro-batch in <<MicroBatchExecution.md#, MicroBatchExecution>> execution engine).
====

NOTE: `nextStatefulOperationStateInfo` is used exclusively when `IncrementalExecution` is requested to optimize a streaming physical plan using the <<state, state preparation rule>> (and creates the stateful physical operators: <<StateStoreSaveExec.md#, StateStoreSaveExec>>, <<spark-sql-streaming-StateStoreRestoreExec.md#, StateStoreRestoreExec>>, <<physical-operators/StreamingDeduplicateExec.md#, StreamingDeduplicateExec>>, [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md), <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>>, and <<physical-operators/StreamingGlobalLimitExec.md#, StreamingGlobalLimitExec>>).

=== [[shouldRunAnotherBatch]] Checking Out Whether Last Execution Requires Another Non-Data Micro-Batch -- `shouldRunAnotherBatch` Method

[source, scala]
----
shouldRunAnotherBatch(newMetadata: OffsetSeqMetadata): Boolean
----

`shouldRunAnotherBatch` is positive (`true`) if there is at least one [StateStoreWriter](physical-operators/StateStoreWriter.md) operator (in the <<executedPlan, executedPlan physical query plan>>) that [requires another non-data batch](physical-operators/StateStoreWriter.md#shouldRunAnotherBatch) (per the given <<spark-sql-streaming-OffsetSeqMetadata.md#, OffsetSeqMetadata>> with the event-time watermark and the batch timestamp).

Otherwise, `shouldRunAnotherBatch` is negative (`false`).

NOTE: `shouldRunAnotherBatch` is used exclusively when `MicroBatchExecution` is requested to <<MicroBatchExecution.md#constructNextBatch, construct the next streaming micro-batch>> (and checks out whether the last batch execution requires another non-data batch).

=== [[demo]] Demo: State Checkpoint Directory

[source, scala]
----
// START: Only for easier debugging
// The state is then only for one partition
// which should make monitoring easier
import org.apache.spark.sql.internal.SQLConf.SHUFFLE_PARTITIONS
spark.sessionState.conf.setConf(SHUFFLE_PARTITIONS, 1)

assert(spark.sessionState.conf.numShufflePartitions == 1)
// END: Only for easier debugging

val counts = spark
  .readStream
  .format("rate")
  .load
  .groupBy(window($"timestamp", "5 seconds") as "group")
  .agg(count("value") as "value_count") // <-- creates an Aggregate logical operator
  .orderBy("group")  // <-- makes for easier checking

assert(counts.isStreaming, "This should be a streaming query")

// Search for "checkpoint = <unknown>" in the following output
// Looks for StateStoreSave and StateStoreRestore
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

// Start the query to access lastExecution that has the checkpoint resolved
import scala.concurrent.duration._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
val t = Trigger.ProcessingTime(1.hour) // should be enough time for exploration
val sq = counts
  .writeStream
  .format("console")
  .option("truncate", false)
  .option("checkpointLocation", "/tmp/spark-streams-state-checkpoint-root")
  .trigger(t)
  .outputMode(OutputMode.Complete)
  .start

// wait till the first batch which should happen right after start

import org.apache.spark.sql.execution.streaming._
val lastExecution = sq.asInstanceOf[StreamingQueryWrapper].streamingQuery.lastExecution
scala> println(lastExecution.checkpointLocation)
file:/tmp/spark-streams-state-checkpoint-root/state
----
