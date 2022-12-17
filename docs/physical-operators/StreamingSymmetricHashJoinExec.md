# StreamingSymmetricHashJoinExec Physical Operator

`StreamingSymmetricHashJoinExec` is a binary physical operator ([Spark SQL]({{ book.spark_sql }}/physical-operators/#BinaryExecNode)) for executing [stream-stream equi-join](../join/index.md).

## Creating Instance

`StreamingSymmetricHashJoinExec` takes the following to be created:

* <span id="leftKeys"> Left Keys
* <span id="rightKeys"> Right Keys
* [JoinType](#joinType)
* <span id="condition"> `JoinConditionSplitPredicates`
* <span id="stateInfo"> [StatefulOperatorStateInfo](../stateful-stream-processing/StatefulOperatorStateInfo.md)
* <span id="eventTimeWatermark"> [Event-Time Watermark](../watermark/index.md)
* <span id="stateWatermarkPredicates"> [JoinStateWatermarkPredicates](../join/JoinStateWatermarkPredicates.md)
* <span id="stateFormatVersion"> State Format Version
* <span id="left"> Left Child Physical Operator
* <span id="right"> Right Child Physical Operator

`StreamingSymmetricHashJoinExec` is created when:

* [StreamingJoinStrategy](../execution-planning-strategies/StreamingJoinStrategy.md) execution planning strategy is executed (to plan an equi-join `Join` logical operator of two streaming logical query plans)

!!! note "Equi-Join"
    An **Equi-Join** is a join with a join condition containing the following equality operators:

    * `EqualTo` ([Spark SQL]({{ book.spark_sql }}/expressions/EqualTo))
    * `EqualNullSafe` ([Spark SQL]({{ book.spark_sql }}/expressions/EqualNullSafe))

### <span id="joinType"> Join Type

`StreamingSymmetricHashJoinExec` is given a `JoinType` ([Spark SQL]({{ book.spark_sql }}/JoinType)) that can be one of the following:

* `FullOuter`
* `Inner`
* `LeftOuter`
* `LeftSemi`
* `RightOuter`

## <span id="StateStoreWriter"> StateStoreWriter

`StreamingSymmetricHashJoinExec` is a [StateStoreWriter](StateStoreWriter.md).

## <span id="doExecute"> Executing Operator

```scala
doExecute(): RDD[InternalRow]
```

`doExecute` is part of the `SparkPlan` ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan#doExecute)) abstraction.

---

`doExecute` requests the [left](#left) child physical operator to execute (that produces an `RDD[InternalRow]`) that is then [stateStoreAwareZipPartitions](../join/StateStoreAwareZipPartitionsHelper.md#stateStoreAwareZipPartitions) with the following:

* [right](#right) child physical operator's `RDD[InternalRow]`
* [StatefulOperatorStateInfo](#stateInfo) (that at this point is supposed to be defined)
* [Names of the state stores](../join/SymmetricHashJoinStateManager.md#allStateStoreNames) of the left and right side of the join
* [StateStoreCoordinator endpoint](../stateful-stream-processing/StateStoreCoordinatorRef.md#forDriver)
* [processPartitions](#processPartitions) function

### <span id="processPartitions"> Computing Partition

```scala
processPartitions(
  partitionId: Int,
  leftInputIter: Iterator[InternalRow],
  rightInputIter: Iterator[InternalRow]): Iterator[InternalRow]
```

!!! note "processPartitions"
    `processPartitions` is used as the function to compute a partition of [StateStoreAwareZipPartitionsRDD](../join/StateStoreAwareZipPartitionsRDD.md).

`processPartitions`...FIXME

## <span id="shortName"> Short Name

```scala
shortName: String
```

`shortName` is part of the [StateStoreWriter](StateStoreWriter.md#shortName) abstraction.

---

`shortName` is the following text:

```text
symmetricHashJoin
```

## <span id="requiredChildDistribution"> Required Child Output Distribution

```scala
requiredChildDistribution: Seq[Distribution]
```

`requiredChildDistribution` is part of the `SparkPlan` ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan/#requiredChildDistribution)) abstraction.

---

`requiredChildDistribution` is two [StatefulOpClusteredDistribution](StatefulOpClusteredDistribution.md)s for the [left](#leftKeys) and [right](#rightKeys) keys (with the [numPartitions](../stateful-stream-processing/StatefulOperatorStateInfo.md#numPartitions) of the [StatefulOperatorStateInfo](StatefulOperator.md#getStateInfo)).

## <span id="shouldRunAnotherBatch"> Should Run Another Non-Data Batch

```scala
shouldRunAnotherBatch(
  newMetadata: OffsetSeqMetadata): Boolean
```

`shouldRunAnotherBatch` is part of the [StateStoreWriter](StateStoreWriter.md#shouldRunAnotherBatch) abstraction.

---

`shouldRunAnotherBatch` is positive (`true`) when all of the following are positive:

* Either the [left](../join/JoinStateWatermarkPredicates.md#left) or [right](../join/JoinStateWatermarkPredicates.md#right) join state watermark predicate is defined (in the [JoinStateWatermarkPredicates](#stateWatermarkPredicates))

* This `StreamingSymmetricHashJoinExec` operator has [event-time watermark](#eventTimeWatermark) defined and the current [event-time watermark](../OffsetSeqMetadata.md#batchWatermarkMs) threshold of the given `OffsetSeqMetadata` is above (_greater than_) it, i.e. moved above

`shouldRunAnotherBatch` is negative (`false`) otherwise.

## <span id="metrics"> Performance Metrics

`StreamingSymmetricHashJoinExec` uses the same performance metrics as the other [stateful physical operators that write to a state store](StateStoreWriter.md#metrics).

![StreamingSymmetricHashJoinExec in web UI (Details for Query)](../images/StreamingSymmetricHashJoinExec-webui-query-details.png)

<!---
## Review Me

`StreamingSymmetricHashJoinExec` is given execution-specific configuration (i.e. <<stateInfo, StatefulOperatorStateInfo>>, <<eventTimeWatermark, event-time watermark>>, and <<stateWatermarkPredicates, JoinStateWatermarkPredicates>>) when `IncrementalExecution` is requested to plan a streaming query for execution (and uses the [state preparation rule](../IncrementalExecution.md#state)).

`StreamingSymmetricHashJoinExec` uses two [OneSideHashJoiners](../join/OneSideHashJoiner.md) (for the <<processPartitions-leftSideJoiner, left>> and <<processPartitions-rightSideJoiner, right>> sides of the join) to manage join state when <<processPartitions, processing partitions of the left and right sides of a stream-stream join>>.

=== [[output]] Output Schema -- `output` Method

[source, scala]
----
output: Seq[Attribute]
----

NOTE: `output` is part of the `QueryPlan` Contract to describe the attributes of (the schema of) the output.

`output` schema depends on the <<joinType, join type>>:

* For `Cross` and `Inner` (`InnerLike`) joins, it is the output schema of the <<left, left>> and <<right, right>> operators

* For `LeftOuter` joins, it is the output schema of the <<left, left>> operator with the attributes of the <<right, right>> operator with `nullability` flag enabled (`true`)

* For `RightOuter` joins, it is the output schema of the <<right, right>> operator with the attributes of the <<left, left>> operator with `nullability` flag enabled (`true`)

`output` throws an `IllegalArgumentException` for other join types:

```
[className] should not take [joinType] as the JoinType
```

=== [[outputPartitioning]] Output Partitioning -- `outputPartitioning` Method

[source, scala]
----
outputPartitioning: Partitioning
----

NOTE: `outputPartitioning` is part of the `SparkPlan` Contract to specify how data should be partitioned across different nodes in the cluster.

`outputPartitioning` depends on the <<joinType, join type>>:

* For `Cross` and `Inner` (`InnerLike`) joins, it is a `PartitioningCollection` of the output partitioning of the <<left, left>> and <<right, right>> operators

* For `LeftOuter` joins, it is a `PartitioningCollection` of the output partitioning of the <<left, left>> operator

* For `RightOuter` joins, it is a `PartitioningCollection` of the output partitioning of the <<right, right>> operator

`outputPartitioning` throws an `IllegalArgumentException` for other join types:

```text
[className] should not take [joinType] as the JoinType
```

=== [[eventTimeWatermark]] Event-Time Watermark -- `eventTimeWatermark` Internal Property

[source, scala]
----
eventTimeWatermark: Option[Long]
----

When <<creating-instance, created>>, `StreamingSymmetricHashJoinExec` can be given the [event-time watermark](../OffsetSeqMetadata.md#batchWatermarkMs) of the current streaming micro-batch.

`eventTimeWatermark` is an optional property that is specified only after [IncrementalExecution](../IncrementalExecution.md) was requested to apply the [state preparation rule](../IncrementalExecution.md#state) to a physical query plan of a streaming query (to [optimize (prepare) the physical plan of the streaming query](../IncrementalExecution.md#executedPlan) once for [ContinuousExecution](../continuous-execution/ContinuousExecution.md) and every trigger for [MicroBatchExecution](../micro-batch-execution/MicroBatchExecution.md) in the **queryPlanning** phase).

`eventTimeWatermark` is used when:

* `StreamingSymmetricHashJoinExec` is requested to [check out whether the last batch execution requires another non-data batch or not](#shouldRunAnotherBatch)
* `OneSideHashJoiner` is requested to [storeAndJoinWithOtherSide](../join/OneSideHashJoiner.md#storeAndJoinWithOtherSide)

## <span id="stateWatermarkPredicates"> Watermark Predicates for State Removal

```scala
stateWatermarkPredicates: JoinStateWatermarkPredicates
```

When <<creating-instance, created>>, `StreamingSymmetricHashJoinExec` is given a <<JoinStateWatermarkPredicates.md#, JoinStateWatermarkPredicates>> for the <<left, left>> and <<right, right>> join sides (using the [StreamingSymmetricHashJoinHelper](../join/StreamingSymmetricHashJoinHelper.md#getStateWatermarkPredicates) utility).

`stateWatermarkPredicates` contains the left and right predicates only when [IncrementalExecution](../IncrementalExecution.md) is requested to apply the [state preparation rule](../IncrementalExecution.md#state) to a physical query plan of a streaming query (to [optimize (prepare) the physical plan of the streaming query](../IncrementalExecution.md#executedPlan) once for [ContinuousExecution](../continuous-execution/ContinuousExecution.md) and every trigger for [MicroBatchExecution](../micro-batch-execution/MicroBatchExecution.md) in the **queryPlanning** phase).

`stateWatermarkPredicates` is used when `StreamingSymmetricHashJoinExec` is requested for the following:

* [Process partitions of the left and right sides of the stream-stream join](#processPartitions) (and creating [OneSideHashJoiner](../join/OneSideHashJoiner.md)s)

* [Checking out whether the last batch execution requires another non-data batch or not](#shouldRunAnotherBatch)

## <span id="doExecute"> Executing Physical Operator

```scala
doExecute(): RDD[InternalRow]
```

`doExecute` is part of the `SparkPlan` abstraction ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan/)).

`doExecute` first requests the `StreamingQueryManager` for the [StateStoreCoordinatorRef](../StreamingQueryManager.md#stateStoreCoordinator) to the `StateStoreCoordinator` RPC endpoint (for the driver).

`doExecute` then uses `SymmetricHashJoinStateManager` utility to [get the names of the state stores](../join/SymmetricHashJoinStateManager.md#allStateStoreNames) for the [left](../join/SymmetricHashJoinStateManager.md#LeftSide) and [right](../join/SymmetricHashJoinStateManager.md#RightSide) sides of the streaming join.

In the end, `doExecute` requests the <<left, left>> and <<right, right>> child physical operators to execute (generate an RDD) and then <<spark-sql-streaming-StateStoreAwareZipPartitionsHelper.md#stateStoreAwareZipPartitions, stateStoreAwareZipPartitions>> with <<processPartitions, processPartitions>> (and with the `StateStoreCoordinatorRef` and the state stores).

=== [[processPartitions]] Processing Partitions of Left and Right Sides of Stream-Stream Join -- `processPartitions` Internal Method

[source, scala]
----
processPartitions(
  leftInputIter: Iterator[InternalRow],
  rightInputIter: Iterator[InternalRow]): Iterator[InternalRow]
----

[[processPartitions-updateStartTimeNs]]
`processPartitions` records the current time (as _updateStartTimeNs_ for the <<allUpdatesTimeMs, total time to update rows>> performance metric in <<onOutputCompletion, onOutputCompletion>>).

[[processPartitions-postJoinFilter]]
`processPartitions` creates a new predicate (_postJoinFilter_) based on the `bothSides` of the <<condition, JoinConditionSplitPredicates>> if defined or `true` literal.

[[processPartitions-leftSideJoiner]]
`processPartitions` creates a [OneSideHashJoiner](../join/OneSideHashJoiner.md) for the [LeftSide](../join/SymmetricHashJoinStateManager.md#LeftSide) and all other properties for the left-hand join side (`leftSideJoiner`).

[[processPartitions-rightSideJoiner]]
`processPartitions` creates a [OneSideHashJoiner](../join/OneSideHashJoiner.md) for the [RightSide](../join/SymmetricHashJoinStateManager.md#RightSide) and all other properties for the right-hand join side (`rightSideJoiner`).

[[processPartitions-leftOutputIter]][[processPartitions-rightOutputIter]]
`processPartitions` requests the `OneSideHashJoiner` for the left-hand join side to [storeAndJoinWithOtherSide](../join/OneSideHashJoiner.md#storeAndJoinWithOtherSide) with the right-hand side one (that creates a `leftOutputIter` row iterator) and the `OneSideHashJoiner` for the right-hand join side to do the same with the left-hand side one (and creates a `rightOutputIter` row iterator).

[[processPartitions-innerOutputCompletionTimeNs]]
`processPartitions` records the current time (as _innerOutputCompletionTimeNs_ for the <<allRemovalsTimeMs, total time to remove rows>> performance metric in <<onOutputCompletion, onOutputCompletion>>).

[[processPartitions-innerOutputIter]]
`processPartitions` creates a `CompletionIterator` with the left and right output iterators (with the rows of the `leftOutputIter` first followed by `rightOutputIter`). When no rows are left to process, the `CompletionIterator` records the completion time.

[[processPartitions-outputIter]]
`processPartitions` creates a join-specific output `Iterator[InternalRow]` of the output rows based on the <<joinType, join type>> (of the `StreamingSymmetricHashJoinExec`):

* For `Inner` joins, `processPartitions` simply uses the <<processPartitions-innerOutputIter, output iterator of the left and right rows>>

* For `LeftOuter` joins, `processPartitions`...

* For `RightOuter` joins, `processPartitions`...

* For other joins, `processPartitions` simply throws an `IllegalArgumentException`.

[[processPartitions-outputIterWithMetrics]]
`processPartitions` creates an `UnsafeProjection` for the <<output, output>> (and the output of the <<left, left>> and <<right, right>> child operators) that counts all the rows of the <<processPartitions-outputIter, join-specific output iterator>> (as the <<numOutputRows, numOutputRows>> metric) and generate an output projection.

In the end, `processPartitions` returns a `CompletionIterator` with with the <<processPartitions-outputIterWithMetrics, output iterator with the rows counted (as numOutputRows metric)>> and <<processPartitions-onOutputCompletion, onOutputCompletion>> completion function.

NOTE: `processPartitions` is used exclusively when `StreamingSymmetricHashJoinExec` physical operator is requested to <<doExecute, execute>>.

==== [[processPartitions-onOutputCompletion]][[onOutputCompletion]] Calculating Performance Metrics (Output Completion Callback) -- `onOutputCompletion` Internal Method

[source, scala]
----
onOutputCompletion: Unit
----

`onOutputCompletion` calculates the <<allUpdatesTimeMs, total time to update rows>> performance metric (that is the time since the <<processPartitions-updateStartTimeNs, processPartitions>> was executed).

`onOutputCompletion` adds the time for the inner join to complete (since <<processPartitions-innerOutputCompletionTimeNs, innerOutputCompletionTimeNs>> time marker) to the <<allRemovalsTimeMs, total time to remove rows>> performance metric.

`onOutputCompletion` records the time to [remove old state](../join/OneSideHashJoiner.md#removeOldState) (per the [join state watermark predicate](../join/OneSideHashJoiner.md#stateWatermarkPredicate) for the <<left, left>> and the <<right, right>> streaming queries) and adds it to the <<allRemovalsTimeMs, total time to remove rows>> performance metric.

NOTE: `onOutputCompletion` triggers the [old state removal](../join/OneSideHashJoiner.md#removeOldState) eagerly by iterating over the state rows to be deleted.

`onOutputCompletion` records the time for the <<processPartitions-leftSideJoiner, left>> and <<processPartitions-rightSideJoiner, right>> `OneSideHashJoiners` to [commit any state changes](../join/OneSideHashJoiner.md#commitStateAndGetMetrics) that becomes the <<commitTimeMs, time to commit changes>> performance metric.

`onOutputCompletion` calculates the <<numUpdatedStateRows, number of updated state rows>> performance metric (as the [number of updated state rows](../join/OneSideHashJoiner.md#numUpdatedStateRows) of the <<processPartitions-leftSideJoiner, left>> and <<processPartitions-rightSideJoiner, right>> streaming queries).

`onOutputCompletion` calculates the <<numTotalStateRows, number of total state rows>> performance metric (as the sum of the [number of keys](../stateful-stream-processing/StateStoreMetrics.md#numKeys) in the [KeyWithIndexToValueStore](../join/SymmetricHashJoinStateManager.md#keyWithIndexToValue) of the <<processPartitions-leftSideJoiner, left>> and <<processPartitions-rightSideJoiner, right>> streaming queries).

`onOutputCompletion` calculates the <<stateMemory, memory used by state>> performance metric (as the sum of the [memory used](../stateful-stream-processing/StateStoreMetrics.md#memoryUsedBytes) by the [KeyToNumValuesStore](../join/SymmetricHashJoinStateManager.md#keyToNumValues) and [KeyWithIndexToValueStore](../join/SymmetricHashJoinStateManager.md#keyWithIndexToValue) of the <<processPartitions-leftSideJoiner, left>> and <<processPartitions-rightSideJoiner, right>> streams).

In the end, `onOutputCompletion` calculates the [custom metrics](../stateful-stream-processing/StateStoreMetrics.md#customMetrics).
-->
