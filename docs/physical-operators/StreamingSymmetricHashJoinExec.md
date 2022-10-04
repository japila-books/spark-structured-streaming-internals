# StreamingSymmetricHashJoinExec Binary Physical Operator

`StreamingSymmetricHashJoinExec` is a binary physical operator for [stream-stream equi-join](../streaming-join/index.md) at execution time.

!!! note
    A binary physical operator (`BinaryExecNode`) is a physical operator with <<left, left>> and <<right, right>> child physical operators.

    Learn more about [BinaryExecNode]({{ book.spark_sql }}/physical-operators/SparkPlan) (and physical operators in general) in [The Internals of Spark SQL]({{ book.spark_sql }}) online book.

[[supported-join-types]][[joinType]]
`StreamingSymmetricHashJoinExec` supports `Inner`, `LeftOuter`, and `RightOuter` join types (with the <<leftKeys, left>> and the <<rightKeys, right>> keys using the exact same data types).

`StreamingSymmetricHashJoinExec` is <<creating-instance, created>> exclusively when [StreamingJoinStrategy](../execution-planning-strategies/StreamingJoinStrategy.md) execution planning strategy is requested to plan a logical query plan with a `Join` logical operator of two streaming queries with equality predicates (`EqualTo` and `EqualNullSafe`).

`StreamingSymmetricHashJoinExec` is given execution-specific configuration (i.e. <<stateInfo, StatefulOperatorStateInfo>>, <<eventTimeWatermark, event-time watermark>>, and <<stateWatermarkPredicates, JoinStateWatermarkPredicates>>) when `IncrementalExecution` is requested to plan a streaming query for execution (and uses the [state preparation rule](../IncrementalExecution.md#state)).

`StreamingSymmetricHashJoinExec` uses two [OneSideHashJoiners](../OneSideHashJoiner.md) (for the <<processPartitions-leftSideJoiner, left>> and <<processPartitions-rightSideJoiner, right>> sides of the join) to manage join state when <<processPartitions, processing partitions of the left and right sides of a stream-stream join>>.

`StreamingSymmetricHashJoinExec` is a [stateful physical operator that writes to a state store](StateStoreWriter.md).

## Creating Instance

`StreamingSymmetricHashJoinExec` takes the following to be created:

* [[leftKeys]] Left keys (Catalyst expressions of the keys on the left side)
* [[rightKeys]] Right keys (Catalyst expressions of the keys on the right side)
* [Join type](#joinType)
* [[condition]] Join condition (`JoinConditionSplitPredicates`)
* [[stateInfo]] [StatefulOperatorStateInfo](../StatefulOperatorStateInfo.md)
* [Event-Time Watermark](#eventTimeWatermark)
* [Watermark Predicates for State Removal](#stateWatermarkPredicates)
* [[left]] Physical operator on the left side (`SparkPlan`)
* [[right]] Physical operator on the right side (`SparkPlan`)

`StreamingSymmetricHashJoinExec` initializes the <<internal-properties, internal properties>>.

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
* `OneSideHashJoiner` is requested to [storeAndJoinWithOtherSide](../OneSideHashJoiner.md#storeAndJoinWithOtherSide)

## <span id="stateWatermarkPredicates"> Watermark Predicates for State Removal

```scala
stateWatermarkPredicates: JoinStateWatermarkPredicates
```

When <<creating-instance, created>>, `StreamingSymmetricHashJoinExec` is given a <<spark-sql-streaming-JoinStateWatermarkPredicates.md#, JoinStateWatermarkPredicates>> for the <<left, left>> and <<right, right>> join sides (using the [StreamingSymmetricHashJoinHelper](../StreamingSymmetricHashJoinHelper.md#getStateWatermarkPredicates) utility).

`stateWatermarkPredicates` contains the left and right predicates only when [IncrementalExecution](../IncrementalExecution.md) is requested to apply the [state preparation rule](../IncrementalExecution.md#state) to a physical query plan of a streaming query (to [optimize (prepare) the physical plan of the streaming query](../IncrementalExecution.md#executedPlan) once for [ContinuousExecution](../continuous-execution/ContinuousExecution.md) and every trigger for [MicroBatchExecution](../micro-batch-execution/MicroBatchExecution.md) in the **queryPlanning** phase).

`stateWatermarkPredicates` is used when `StreamingSymmetricHashJoinExec` is requested for the following:

* [Process partitions of the left and right sides of the stream-stream join](#processPartitions) (and creating [OneSideHashJoiner](../OneSideHashJoiner.md)s)

* [Checking out whether the last batch execution requires another non-data batch or not](#shouldRunAnotherBatch)

=== [[requiredChildDistribution]] Required Partition Requirements -- `requiredChildDistribution` Method

[source, scala]
----
requiredChildDistribution: Seq[Distribution]
----

[NOTE]
====
`requiredChildDistribution` is part of the `SparkPlan` Contract for the required partition requirements (aka _required child distribution_) of the input data, i.e. how the output of the children physical operators is split across partitions before this operator can be executed.

Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-SparkPlan.html[SparkPlan Contract] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] online book.
====

`requiredChildDistribution` returns two `HashClusteredDistributions` for the <<leftKeys, left>> and <<rightKeys, right>> keys with the required [number of partitions](../StatefulOperatorStateInfo.md#numPartitions) based on the [StatefulOperatorStateInfo](#stateInfo).

[NOTE]
====
`requiredChildDistribution` is used exclusively when `EnsureRequirements` physical query plan optimization is executed (and enforces partition requirements).

Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-EnsureRequirements.html[EnsureRequirements Physical Query Optimization] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] online book.
====

[NOTE]
====
`HashClusteredDistribution` becomes `HashPartitioning` at execution that distributes rows across partitions (generates partition IDs of rows) based on `Murmur3Hash` of the join expressions (separately for the <<leftKeys, left>> and <<rightKeys, right>> keys) modulo the required number of partitions.

Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-Distribution-HashClusteredDistribution.html[HashClusteredDistribution] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] online book.
====

=== [[metrics]] Performance Metrics (SQLMetrics)

`StreamingSymmetricHashJoinExec` uses the performance metrics as [other stateful physical operators that write to a state store](StateStoreWriter.md#metrics).

![StreamingSymmetricHashJoinExec in web UI (Details for Query)](../images/StreamingSymmetricHashJoinExec-webui-query-details.png)

The following table shows how the performance metrics are computed (and so their exact meaning).

[cols="30,70",options="header",width="100%"]
|===
| Name (in web UI)
| Description

| total time to update rows
a| [[allUpdatesTimeMs]] Processing time of all rows

| total time to remove rows
a| [[allRemovalsTimeMs]]

| time to commit changes
a| [[commitTimeMs]]

| number of output rows
a| [[numOutputRows]] Total number of output rows

| number of total state rows
a| [[numTotalStateRows]]

| number of updated state rows
a| [[numUpdatedStateRows]] [Number of updated state rows](../OneSideHashJoiner.md#updatedStateRowsCount) of the [left](#processPartitions-leftSideJoiner) and [right](#processPartitions-rightSideJoiner) `OneSideHashJoiners`

| memory used by state
a| [[stateMemory]]
|===

=== [[shouldRunAnotherBatch]] Checking Out Whether Last Batch Execution Requires Another Non-Data Batch or Not -- `shouldRunAnotherBatch` Method

[source, scala]
----
shouldRunAnotherBatch(
  newMetadata: OffsetSeqMetadata): Boolean
----

`shouldRunAnotherBatch` is positive (`true`) when all of the following are positive:

* Either the <<spark-sql-streaming-JoinStateWatermarkPredicates.md#left, left>> or <<spark-sql-streaming-JoinStateWatermarkPredicates.md#right, right>> join state watermark predicates are defined (in the <<stateWatermarkPredicates, JoinStateWatermarkPredicates>>)

* <<eventTimeWatermark, Event-time watermark>> threshold (of the `StreamingSymmetricHashJoinExec` operator) is defined and the current [event-time watermark](../OffsetSeqMetadata.md#batchWatermarkMs) threshold of the given `OffsetSeqMetadata` is above (_greater than_) it, i.e. moved above

`shouldRunAnotherBatch` is negative (`false`) otherwise.

`shouldRunAnotherBatch` is part of the [StateStoreWriter](StateStoreWriter.md#shouldRunAnotherBatch) abstraction.

## <span id="doExecute"> Executing Physical Operator

```scala
doExecute(): RDD[InternalRow]
```

`doExecute` is part of the `SparkPlan` abstraction ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan/)).

`doExecute` first requests the `StreamingQueryManager` for the [StateStoreCoordinatorRef](../StreamingQueryManager.md#stateStoreCoordinator) to the `StateStoreCoordinator` RPC endpoint (for the driver).

`doExecute` then uses `SymmetricHashJoinStateManager` utility to [get the names of the state stores](../SymmetricHashJoinStateManager.md#allStateStoreNames) for the [left](../SymmetricHashJoinStateManager.md#LeftSide) and [right](../SymmetricHashJoinStateManager.md#RightSide) sides of the streaming join.

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
`processPartitions` creates a [OneSideHashJoiner](../OneSideHashJoiner.md) for the [LeftSide](../SymmetricHashJoinStateManager.md#LeftSide) and all other properties for the left-hand join side (`leftSideJoiner`).

[[processPartitions-rightSideJoiner]]
`processPartitions` creates a [OneSideHashJoiner](../OneSideHashJoiner.md) for the [RightSide](../SymmetricHashJoinStateManager.md#RightSide) and all other properties for the right-hand join side (`rightSideJoiner`).

[[processPartitions-leftOutputIter]][[processPartitions-rightOutputIter]]
`processPartitions` requests the `OneSideHashJoiner` for the left-hand join side to [storeAndJoinWithOtherSide](../OneSideHashJoiner.md#storeAndJoinWithOtherSide) with the right-hand side one (that creates a `leftOutputIter` row iterator) and the `OneSideHashJoiner` for the right-hand join side to do the same with the left-hand side one (and creates a `rightOutputIter` row iterator).

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

`onOutputCompletion` records the time to [remove old state](../OneSideHashJoiner.md#removeOldState) (per the [join state watermark predicate](../OneSideHashJoiner.md#stateWatermarkPredicate) for the <<left, left>> and the <<right, right>> streaming queries) and adds it to the <<allRemovalsTimeMs, total time to remove rows>> performance metric.

NOTE: `onOutputCompletion` triggers the [old state removal](../OneSideHashJoiner.md#removeOldState) eagerly by iterating over the state rows to be deleted.

`onOutputCompletion` records the time for the <<processPartitions-leftSideJoiner, left>> and <<processPartitions-rightSideJoiner, right>> `OneSideHashJoiners` to [commit any state changes](../OneSideHashJoiner.md#commitStateAndGetMetrics) that becomes the <<commitTimeMs, time to commit changes>> performance metric.

`onOutputCompletion` calculates the <<numUpdatedStateRows, number of updated state rows>> performance metric (as the [number of updated state rows](../OneSideHashJoiner.md#numUpdatedStateRows) of the <<processPartitions-leftSideJoiner, left>> and <<processPartitions-rightSideJoiner, right>> streaming queries).

`onOutputCompletion` calculates the <<numTotalStateRows, number of total state rows>> performance metric (as the sum of the <<spark-sql-streaming-StateStoreMetrics.md#numKeys, number of keys>> in the [KeyWithIndexToValueStore](../SymmetricHashJoinStateManager.md#keyWithIndexToValue) of the <<processPartitions-leftSideJoiner, left>> and <<processPartitions-rightSideJoiner, right>> streaming queries).

`onOutputCompletion` calculates the <<stateMemory, memory used by state>> performance metric (as the sum of the <<spark-sql-streaming-StateStoreMetrics.md#memoryUsedBytes, memory used>> by the [KeyToNumValuesStore](../SymmetricHashJoinStateManager.md#keyToNumValues) and [KeyWithIndexToValueStore](../SymmetricHashJoinStateManager.md#keyWithIndexToValue) of the <<processPartitions-leftSideJoiner, left>> and <<processPartitions-rightSideJoiner, right>> streams).

In the end, `onOutputCompletion` calculates the <<spark-sql-streaming-StateStoreMetrics.md#customMetrics, custom metrics>>.

## Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| hadoopConfBcast
a| [[hadoopConfBcast]] Hadoop Configuration broadcast (to the Spark cluster)

Used exclusively to <<joinStateManager, create a SymmetricHashJoinStateManager>>

| joinStateManager
a| [[joinStateManager]] [SymmetricHashJoinStateManager](../SymmetricHashJoinStateManager.md)

Used when `OneSideHashJoiner` is requested to [storeAndJoinWithOtherSide](../OneSideHashJoiner.md#storeAndJoinWithOtherSide), [removeOldState](../OneSideHashJoiner.md#removeOldState), [commitStateAndGetMetrics](../OneSideHashJoiner.md#commitStateAndGetMetrics), and for the [values for a given key](../OneSideHashJoiner.md#get)

| nullLeft
a| [[nullLeft]] `GenericInternalRow` of the size of the output schema of the <<left, left physical operator>>

| nullRight
a| [[nullRight]] `GenericInternalRow` of the size of the output schema of the <<right, right physical operator>>

| storeConf
a| [[storeConf]] [StateStoreConf](../StateStoreConf.md)

Used exclusively to <<joinStateManager, create a SymmetricHashJoinStateManager>>

|===
