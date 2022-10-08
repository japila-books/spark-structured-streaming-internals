# FlatMapGroupsWithStateExec Physical Operator

`FlatMapGroupsWithStateExec` is a unary physical operator ([Spark SQL]({{ book.spark_sql }}/physical-operators/UnaryExecNode/)) that represents [FlatMapGroupsWithState](../logical-operators/FlatMapGroupsWithState.md) logical operator at execution time.

`FlatMapGroupsWithStateExec` is an `ObjectProducerExec` ([Spark SQL]({{ book.spark_sql }}/physical-operators/ObjectProducerExec/)) physical operator that produces a [single output object](#outputObjAttr).

!!! tip
    Check out [Demo: Internals of FlatMapGroupsWithStateExec Physical Operator](../demo/spark-sql-streaming-demo-FlatMapGroupsWithStateExec.md).

!!! note
    `FlatMapGroupsWithStateExec` is given an [OutputMode](#outputMode) when created, but it does not seem to be used at all. Check out the question [What's the purpose of OutputMode in flatMapGroupsWithState? How/where is it used?](https://stackoverflow.com/q/56921772/1305344) on StackOverflow.

## Creating Instance

`FlatMapGroupsWithStateExec` takes the following to be created:

* <span id="func"> **User-defined state function** that is applied to every group (of type `(Any, Iterator[Any], LogicalGroupState[Any]) => Iterator[Any]`)
* <span id="keyDeserializer"> Deserializer expression for keys
* <span id="valueDeserializer"> Deserializer expression for values
* <span id="groupingAttributes"> Grouping attributes (as used for grouping in [KeyValueGroupedDataset](../KeyValueGroupedDataset.md#groupingAttributes) for `mapGroupsWithState` or `flatMapGroupsWithState` operators)
* <span id="dataAttributes"> Data attributes
* <span id="outputObjAttr"> Output object attribute (that is the reference to the single object field this operator outputs)
* <span id="stateInfo"> Optional [StatefulOperatorStateInfo](../stateful-stream-processing/StatefulOperatorStateInfo.md)
* <span id="stateEncoder"> State encoder (`ExpressionEncoder[Any]`)
* <span id="stateFormatVersion"> State format version
* <span id="outputMode"> [OutputMode](../OutputMode.md)
* <span id="timeoutConf"> [GroupStateTimeout](../GroupStateTimeout.md)
* <span id="batchTimestampMs"> Optional [Batch Processing Time](../spark-structured-streaming-batch-processing-time.md)
* <span id="eventTimeWatermark"> Optional [Event-Time Watermark](../streaming-watermark/index.md)
* <span id="child"> Child physical operator

`FlatMapGroupsWithStateExec` is created when [FlatMapGroupsWithStateStrategy](../execution-planning-strategies/FlatMapGroupsWithStateStrategy.md) execution planning strategy is executed (and plans a [FlatMapGroupsWithState](../logical-operators/FlatMapGroupsWithState.md) logical operator for execution).

## <span id="doExecute"> Executing Physical Operator

```scala
doExecute(): RDD[InternalRow]
```

`doExecute` first initializes the [metrics](StateStoreWriter.md#metrics) (which happens on the driver).

`doExecute` then requests the [child](#child) physical operator to execute (and generate an `RDD[InternalRow]`).

`doExecute` uses [StateStoreOps](../stateful-stream-processing/StateStoreOps.md) to [create a StateStoreRDD](../stateful-stream-processing/StateStoreOps.md#mapPartitionsWithStateStore) with a `storeUpdateFunction` that does the following (for a partition):

1. Creates an [InputProcessor](../arbitrary-stateful-streaming-aggregation/InputProcessor.md) for a given [StateStore](../stateful-stream-processing/StateStore.md)

1. (only when the [GroupStateTimeout](#timeoutConf) is [EventTimeTimeout](../GroupStateTimeout.md#EventTimeTimeout)) Filters out late data based on the [event-time watermark](WatermarkSupport.md#watermarkPredicateForData), i.e. rows from a given `Iterator[InternalRow]` that are older than the [event-time watermark](WatermarkSupport.md#watermarkPredicateForData) are excluded from the steps that follow

1. Requests the `InputProcessor` to [create an iterator of a new data processed](../arbitrary-stateful-streaming-aggregation/InputProcessor.md#processNewData) from the (possibly filtered) iterator

1. Requests the `InputProcessor` to [create an iterator of a timed-out state data](../arbitrary-stateful-streaming-aggregation/InputProcessor.md#processTimedOutState)

1. Creates an iterator by concatenating the above iterators (with the new data processed first)

1. In the end, creates a `CompletionIterator` that executes a completion function (`completionFunction`) after it has successfully iterated through all the elements (i.e. when a client has consumed all the rows). The completion method requests the given `StateStore` to [commit changes](../stateful-stream-processing/StateStore.md#commit) followed by [setting the store-specific metrics](StateStoreWriter.md#setStoreMetrics)

`doExecute` is part of Spark SQL's `SparkPlan` abstraction.

## <span id="metrics"> Performance Metrics

`FlatMapGroupsWithStateExec` uses the performance metrics of [StateStoreWriter](StateStoreWriter.md#metrics).

![FlatMapGroupsWithStateExec in web UI (Details for Query)](../images/FlatMapGroupsWithStateExec-webui-query-details.png)

## <span id="StateStoreWriter"> StateStoreWriter

`FlatMapGroupsWithStateExec` is a [stateful physical operator that can write to a state store](StateStoreWriter.md) (and `MicroBatchExecution` requests [whether to run another batch or not](#shouldRunAnotherBatch) based on the [GroupStateTimeout](#timeoutConf)).

`FlatMapGroupsWithStateExec` uses the [GroupStateTimeout](#timeoutConf) (and possibly the updated [metadata](../OffsetSeqMetadata.md)) when asked [whether to run another batch or not](#shouldRunAnotherBatch) (when `MicroBatchExecution` is requested to [construct the next streaming micro-batch](../micro-batch-execution/MicroBatchExecution.md#constructNextBatch) when requested to [run the activated streaming query](../micro-batch-execution/MicroBatchExecution.md#runActivatedStream)).

## <span id="WatermarkSupport"> Streaming Event-Time Watermark Support

`FlatMapGroupsWithStateExec` is a [physical operator that supports streaming event-time watermark](WatermarkSupport.md).

`FlatMapGroupsWithStateExec` is given the [optional event time watermark](#eventTimeWatermark) when created.

The [event-time watermark](#eventTimeWatermark) is initially undefined (`None`) when planned for execution (in [FlatMapGroupsWithStateStrategy](../execution-planning-strategies/FlatMapGroupsWithStateStrategy.md) execution planning strategy).

!!! note
    `FlatMapGroupsWithStateStrategy` converts [FlatMapGroupsWithState](../logical-operators/FlatMapGroupsWithState.md) unary logical operator to `FlatMapGroupsWithStateExec` physical operator with undefined [StatefulOperatorStateInfo](#stateInfo), [batchTimestampMs](#batchTimestampMs), and [eventTimeWatermark](#eventTimeWatermark).

The [event-time watermark](#eventTimeWatermark) (with the [StatefulOperatorStateInfo](#stateInfo) and the [batchTimestampMs](#batchTimestampMs)) is only defined to the [current event-time watermark](../OffsetSeqMetadata.md#batchWatermarkMs) of the given [OffsetSeqMetadata](../IncrementalExecution.md#offsetSeqMetadata) when `IncrementalExecution` query execution pipeline is requested to apply the [state](../IncrementalExecution.md#state) preparation rule (as part of the [preparations](../IncrementalExecution.md#preparations) rules).

!!! note
    The [preparations](../IncrementalExecution.md#preparations) rules are executed (applied to a physical query plan) at the `executedPlan` phase of Structured Query Execution Pipeline to generate an optimized physical query plan ready for execution).

    Read up on [Structured Query Execution Pipeline]({{ book.spark_sql }}/QueryExecution/) in [The Internals of Spark SQL]({{ book.spark_sql }}/) online book.

`IncrementalExecution` is used as the [lastExecution](../StreamExecution.md#lastExecution) of the available [streaming query execution engines](../StreamExecution.md#extensions). It is created in the **queryPlanning** phase (of the [MicroBatchExecution](../micro-batch-execution/MicroBatchExecution.md#runBatch-queryPlanning) and [ContinuousExecution](../continuous-execution/ContinuousExecution.md#runContinuous-queryPlanning) execution engines) based on the current [OffsetSeqMetadata](../StreamExecution.md#offsetSeqMetadata).

!!! note
    The [optional event-time watermark](#eventTimeWatermark) can only be defined when the [state](../IncrementalExecution.md#state) preparation rule is executed which is at the `executedPlan` phase of Structured Query Execution Pipeline which is also part of the **queryPlanning** phase.

## <span id="stateManager"> StateManager

```scala
stateManager: StateManager
```

While being created, `FlatMapGroupsWithStateExec` creates a [StateManager](../arbitrary-stateful-streaming-aggregation/StateManager.md) (with the [state encoder](#stateEncoder) and the [isTimeoutEnabled](#isTimeoutEnabled) flag).

A `StateManager` is [created](../arbitrary-stateful-streaming-aggregation/FlatMapGroupsWithStateExecHelper.md#createStateManager) per [state format version](#stateFormatVersion) that is given while creating a `FlatMapGroupsWithStateExec` (to choose between the [available implementations](../arbitrary-stateful-streaming-aggregation/StateManagerImplBase.md#implementations)).

The [state format version](#stateFormatVersion) is controlled by [spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion](../configuration-properties.md#spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion) internal configuration property.

The `StateManager` is used exclusively when `FlatMapGroupsWithStateExec` physical operator is [executed](#doExecute) for the following:

* [State schema](../arbitrary-stateful-streaming-aggregation/StateManager.md#stateSchema) (for the [value schema](../stateful-stream-processing/StateStoreRDD.md#valueSchema) of a [StateStoreRDD](../stateful-stream-processing/StateStoreRDD.md))

* [State data for a key in a StateStore](../arbitrary-stateful-streaming-aggregation/StateManager.md#getState) while [processing new data](../arbitrary-stateful-streaming-aggregation/InputProcessor.md#processNewData)

* [All state data (for all keys) in a StateStore](../arbitrary-stateful-streaming-aggregation/StateManager.md#getAllState) while [processing timed-out state data](../arbitrary-stateful-streaming-aggregation/InputProcessor.md#processTimedOutState)

* [Removing the state for a key from a StateStore](../arbitrary-stateful-streaming-aggregation/StateManager.md#removeState) when [all rows have been processed](../arbitrary-stateful-streaming-aggregation/InputProcessor.md#onIteratorCompletion)

* [Persisting the state for a key in a StateStore](../arbitrary-stateful-streaming-aggregation/StateManager.md#putState) when [all rows have been processed](../arbitrary-stateful-streaming-aggregation/InputProcessor.md#onIteratorCompletion)

## <span id="keyExpressions"> keyExpressions Method

```scala
keyExpressions: Seq[Attribute]
```

`keyExpressions` simply returns the [grouping attributes](#groupingAttributes).

`keyExpressions` is part of the [WatermarkSupport](WatermarkSupport.md#keyExpressions) abstraction.

## <span id="shouldRunAnotherBatch"> Checking Out Whether Last Batch Execution Requires Another Non-Data Batch or Not

```scala
shouldRunAnotherBatch(
  newMetadata: OffsetSeqMetadata): Boolean
```

`shouldRunAnotherBatch` uses the [GroupStateTimeout](#timeoutConf) as follows:

* With [EventTimeTimeout](../GroupStateTimeout.md#EventTimeTimeout), `shouldRunAnotherBatch` is `true` only when the [event-time watermark](#eventTimeWatermark) is defined and is older (below) the [event-time watermark](../OffsetSeqMetadata.md#batchWatermarkMs) of the given `OffsetSeqMetadata`

* With [NoTimeout](../GroupStateTimeout.md#NoTimeout) (and other [GroupStateTimeouts](../GroupStateTimeout.md#extensions) if there were any), `shouldRunAnotherBatch` is always `false`

* With [ProcessingTimeTimeout](../GroupStateTimeout.md#ProcessingTimeTimeout), `shouldRunAnotherBatch` is always `true`

`shouldRunAnotherBatch` is part of the [StateStoreWriter](StateStoreWriter.md#shouldRunAnotherBatch) abstraction.

## Internal Properties

### <span id="isTimeoutEnabled"> isTimeoutEnabled Flag

Flag that says whether the [GroupStateTimeout](#timeoutConf) is not [NoTimeout](../GroupStateTimeout.md#NoTimeout)

Used when:

* `FlatMapGroupsWithStateExec` is created (and creates the internal [StateManager](#stateManager))
* `InputProcessor` is requested to [processTimedOutState](../arbitrary-stateful-streaming-aggregation/InputProcessor.md#processTimedOutState)

### <span id="watermarkPresent"> watermarkPresent Flag

Flag that says whether the [child](#child) physical operator has a [watermark attribute](../logical-operators/EventTimeWatermark.md#delayKey) (among the output attributes).

Used when:

* `InputProcessor` is requested to [callFunctionAndUpdateState](../arbitrary-stateful-streaming-aggregation/InputProcessor.md#callFunctionAndUpdateState)

## <span id="requiredChildDistribution"> Required Child Output Distribution

```scala
requiredChildDistribution: Seq[Distribution]
```

`requiredChildDistribution` is part of the `SparkPlan` ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan/#requiredChildDistribution)) abstraction.

---

`requiredChildDistribution`...FIXME

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.FlatMapGroupsWithStateExec` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.execution.streaming.FlatMapGroupsWithStateExec=ALL
```

Refer to [Logging](../spark-logging.md).
