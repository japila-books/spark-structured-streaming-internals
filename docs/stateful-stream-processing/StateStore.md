# StateStore

`StateStore` is an [extension](#contract) of the [ReadStateStore](ReadStateStore.md) abstraction for [versioned key-value stores](#implementations) for writing and reading state for [Stateful Stream Processing](index.md) (e.g., for persisting running aggregates in [Streaming Aggregation](../streaming-aggregation/index.md)).

!!! note
    `StateStore` was introduced in [\[SPARK-13809\]\[SQL\] State store for streaming aggregations]({{ spark.commit }}/8c826880f5eaa3221c4e9e7d3fece54e821a0b98).

    Read the motivation and design in [State Store for Streaming Aggregations](https://docs.google.com/document/d/1-ncawFx8JS5Zyfq1HAEGBx56RDet9wfVp_hDM8ZL254/edit).

## Contract

### <span id="commit"> commit

```scala
commit(): Long
```

Commits all updates ([puts](#put) and [removes](#remove)) and returns a new version

Used when:

* `FlatMapGroupsWithStateExec` physical operator is requested to [processDataWithPartition](../physical-operators/FlatMapGroupsWithStateExec.md#processDataWithPartition)
* `SessionWindowStateStoreSaveExec` physical operator is [executed](../physical-operators/SessionWindowStateStoreSaveExec.md#doExecute)
* `StreamingDeduplicateExec` physical operator is [executed](../physical-operators/StreamingDeduplicateExec.md#doExecute)
* `StreamingGlobalLimitExec` physical operator is [executed](../physical-operators/StreamingGlobalLimitExec.md#doExecute)
* `StreamingAggregationStateManagerBaseImpl` is requested to [commit](../streaming-aggregation/StreamingAggregationStateManagerBaseImpl.md#commit)
* `StreamingSessionWindowStateManagerImplV1` is requested to `commit`
* `StateStoreHandler` is requested to [commit](../streaming-join/StateStoreHandler.md#commit)

### <span id="metrics"> metrics

```scala
metrics: StateStoreMetrics
```

[StateStoreMetrics](StateStoreMetrics.md) of this state store

Used when:

* `StateStoreWriter` physical operator is requested to [setStoreMetrics](../physical-operators/StateStoreWriter.md#setStoreMetrics)
* `StateStoreHandler` is requested for the [metrics](../streaming-join/StateStoreHandler.md#metrics)

### <span id="put"> put

```scala
put(
  key: UnsafeRow,
  value: UnsafeRow): Unit
```

Puts a new non-`null` value for a non-`null` key

Used when:

* `StreamingDeduplicateExec` physical operator is [executed](../physical-operators/StreamingDeduplicateExec.md#doExecute)
* `StreamingGlobalLimitExec` physical operator is [executed](../physical-operators/StreamingGlobalLimitExec.md#doExecute)
* `StateManagerImplBase` is requested to [putState](../arbitrary-stateful-streaming-aggregation/StateManagerImplBase.md#putState)
* `StreamingAggregationStateManagerImplV2`(and `StreamingAggregationStateManagerImplV1`) is requested to [put a row](../streaming-aggregation/StreamingAggregationStateManagerImplV2.md#put)
* `StreamingSessionWindowStateManagerImplV1` is requested to `putRows`
* `KeyToNumValuesStore` is requested to [put the number of values of a key](../streaming-join/KeyToNumValuesStore.md#put)
* `KeyWithIndexToValueStore` is requested to [put a new value of a key](../streaming-join/KeyWithIndexToValueStore.md#put)

### <span id="remove"> remove

```scala
remove(
  key: UnsafeRow): Unit
```

Removes a non-`null` key

Used when:

* `WatermarkSupport` physical operator is requested to [removeKeysOlderThanWatermark](../physical-operators/WatermarkSupport.md#removeKeysOlderThanWatermark)
* `StateManagerImplBase` is requested to [removeState](../arbitrary-stateful-streaming-aggregation/StateManagerImplBase.md#removeState)
* `StreamingAggregationStateManagerBaseImpl` is requested to [remove a key](../streaming-aggregation/StreamingAggregationStateManagerBaseImpl.md#remove)
* `StreamingSessionWindowStateManagerImplV1` is requested to `removeByValueCondition` and `putRows`
* `KeyToNumValuesStore` is requested to [remove a key](../streaming-join/KeyToNumValuesStore.md#remove)
* `KeyWithIndexToValueStore` is requested to [remove a key](../streaming-join/KeyWithIndexToValueStore.md#remove)

## Implementations

* [HDFSBackedStateStore](HDFSBackedStateStore.md)
* [RocksDBStateStore](RocksDBStateStore.md)

## Review Me

`StateStore` supports **incremental checkpointing** in which only the key-value "Row" pairs that changed are <<commit, committed>> or <<abort, aborted>> (without touching other key-value pairs).

`StateStore` is identified with the <<id, aggregating operator id and the partition id>> (among other properties for identification).

[[logging]]
[TIP]
====
Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.state.StateStore$` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.execution.streaming.state.StateStore$=ALL
```

Refer to <<spark-sql-streaming-spark-logging.md#, Logging>>.
====

=== [[coordinatorRef]] Creating (and Caching) RPC Endpoint Reference to StateStoreCoordinator for Executors

[source, scala]
----
coordinatorRef: Option[StateStoreCoordinatorRef]
----

`coordinatorRef` requests the `SparkEnv` helper object for the current `SparkEnv`.

If the `SparkEnv` is available and the <<_coordRef, _coordRef>> is not assigned yet, `coordinatorRef` prints out the following DEBUG message to the logs followed by requesting the `StateStoreCoordinatorRef` for the [StateStoreCoordinator endpoint](StateStoreCoordinatorRef.md#forExecutor).

```
Getting StateStoreCoordinatorRef
```

If the `SparkEnv` is available, `coordinatorRef` prints out the following INFO message to the logs:

```
Retrieved reference to StateStoreCoordinator: [_coordRef]
```

NOTE: `coordinatorRef` is used when `StateStore` helper object is requested to <<reportActiveStoreInstance, reportActiveStoreInstance>> (when `StateStore` object helper is requested to <<get-StateStore, find the StateStore by StateStoreProviderId>>) and <<verifyIfStoreInstanceActive, verifyIfStoreInstanceActive>> (when `StateStore` object helper is requested to <<doMaintenance, doMaintenance>>).

=== [[reportActiveStoreInstance]] Announcing New StateStoreProvider

[source, scala]
----
reportActiveStoreInstance(
  storeProviderId: StateStoreProviderId): Unit
----

`reportActiveStoreInstance` takes the current host and `executorId` (from the `BlockManager` on the Spark executor) and requests the <<coordinatorRef, StateStoreCoordinatorRef>> to [reportActiveInstance](StateStoreCoordinatorRef.md#reportActiveInstance).

NOTE: `reportActiveStoreInstance` uses `SparkEnv` to access the `BlockManager`.

In the end, `reportActiveStoreInstance` prints out the following INFO message to the logs:

```
Reported that the loaded instance [storeProviderId] is active
```

NOTE: `reportActiveStoreInstance` is used exclusively when `StateStore` utility is requested to <<get-StateStore, find the StateStore by StateStoreProviderId>>.

=== [[MaintenanceTask]] `MaintenanceTask` Daemon Thread

`MaintenanceTask` is a daemon thread that <<doMaintenance, triggers maintenance work of registered StateStoreProviders>>.

When an error occurs, `MaintenanceTask` clears <<loadedProviders, loadedProviders>> internal registry.

`MaintenanceTask` is scheduled on *state-store-maintenance-task* thread pool that runs periodically every [spark.sql.streaming.stateStore.maintenanceInterval](../configuration-properties.md#spark.sql.streaming.stateStore.maintenanceInterval).

## <span id="get-StateStore"> Looking Up StateStore by Provider ID

```scala
get(
  storeProviderId: StateStoreProviderId,
  keySchema: StructType,
  valueSchema: StructType,
  indexOrdinal: Option[Int],
  version: Long,
  storeConf: StateStoreConf,
  hadoopConf: Configuration): StateStore
```

`get` finds `StateStore` for the specified [StateStoreProviderId](StateStoreProviderId.md) and version.

NOTE: The version is either the <<EpochTracker.md#getCurrentEpoch, current epoch>> (in [Continuous Stream Processing](../continuous-execution/index.md)) or the [current batch ID](StatefulOperatorStateInfo.md#storeVersion) (in [Micro-Batch Stream Processing](../micro-batch-execution/index.md)).

Internally, `get` looks up the <<StateStoreProvider.md#, StateStoreProvider>> (by `storeProviderId`) in the <<loadedProviders, loadedProviders>> internal cache. If unavailable, `get` uses the `StateStoreProvider` utility to <<StateStoreProvider.md#createAndInit, create and initialize one>>.

`get` will also <<startMaintenanceIfNeeded, start the periodic maintenance task>> (unless already started) and <<reportActiveStoreInstance, announce the new StateStoreProvider>>.

In the end, `get` requests the `StateStoreProvider` to <<StateStoreProvider.md#getStore, look up the StateStore by the specified version>>.

`get` is used when:

* `StateStoreRDD` is requested to [compute a partition](StateStoreRDD.md#compute)

* `StateStoreHandler` (of [SymmetricHashJoinStateManager](../streaming-join/SymmetricHashJoinStateManager.md)) is requested to <<StateStoreHandler.md#getStateStore, look up a StateStore (by key and value schemas)>>

==== [[startMaintenanceIfNeeded]] Starting Periodic Maintenance Task (Unless Already Started) -- `startMaintenanceIfNeeded` Internal Object Method

[source, scala]
----
startMaintenanceIfNeeded(): Unit
----

`startMaintenanceIfNeeded` schedules <<MaintenanceTask, MaintenanceTask>> to start after and every [spark.sql.streaming.stateStore.maintenanceInterval](../configuration-properties.md#spark.sql.streaming.stateStore.maintenanceInterval) (defaults to `60s`).

NOTE: `startMaintenanceIfNeeded` does nothing when the maintenance task has already been started and is still running.

NOTE: `startMaintenanceIfNeeded` is used exclusively when `StateStore` is requested to <<get, find the StateStore by StateStoreProviderId>>.

==== [[doMaintenance]] Doing State Maintenance of Registered State Store Providers -- `doMaintenance` Internal Object Method

[source, scala]
----
doMaintenance(): Unit
----

Internally, `doMaintenance` prints the following DEBUG message to the logs:

```
Doing maintenance
```

`doMaintenance` then requests every StateStoreProvider.md[StateStoreProvider] (registered in <<loadedProviders, loadedProviders>>) to StateStoreProvider.md#doMaintenance[do its own internal maintenance] (only when a `StateStoreProvider` <<verifyIfStoreInstanceActive, is still active>>).

When a `StateStoreProvider` is <<verifyIfStoreInstanceActive, inactive>>, `doMaintenance` <<unload, removes it from the provider registry>> and prints the following INFO message to the logs:

```
Unloaded [provider]
```

NOTE: `doMaintenance` is used exclusively in <<MaintenanceTask, MaintenanceTask daemon thread>>.
