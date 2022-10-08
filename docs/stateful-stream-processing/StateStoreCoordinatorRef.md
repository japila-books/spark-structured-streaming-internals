# StateStoreCoordinatorRef

`StateStoreCoordinatorRef` is used to (let the tasks on Spark executors to) send <<messages, messages>> to the <<rpcEndpointRef, StateStoreCoordinator>> (that lives on the driver).

[[creating-instance]]
[[rpcEndpointRef]]
`StateStoreCoordinatorRef` is given the `RpcEndpointRef` to the [StateStoreCoordinator](StateStoreCoordinator.md) RPC endpoint when created.

`StateStoreCoordinatorRef` is <<creating-instance, created>> through `StateStoreCoordinatorRef` helper object when requested to create one for the <<forDriver, driver>> (when [StreamingQueryManager](../StreamingQueryManager.md#stateStoreCoordinator) is created) or an <<forExecutor, executor>> (when `StateStore` helper object is requested for the [RPC endpoint reference to StateStoreCoordinator for Executors](StateStore.md#coordinatorRef)).

[[messages]]
.StateStoreCoordinatorRef's Methods and Underlying RPC Messages
[width="100%",cols="1m,3",options="header"]
|===
| Method
| Description

| deactivateInstances
a| [[deactivateInstances]]

[source, scala]
----
deactivateInstances(runId: UUID): Unit
----

Requests the [RpcEndpointRef](#rpcEndpointRef) to send a [DeactivateInstances](StateStoreCoordinator.md#DeactivateInstances) synchronous message with the given `runId` and waits for a `true` / `false` response

Used exclusively when `StreamingQueryManager` is requested to [handle termination of a streaming query](../StreamingQueryManager.md#notifyQueryTermination) (when `StreamExecution` is requested to [run a streaming query](../StreamExecution.md#runStream) and the query [has finished (running streaming batches)](../StreamExecution.md#runStream-finally)).

| getLocation
a| [[getLocation]]

[source, scala]
----
getLocation(
  stateStoreProviderId: StateStoreProviderId): Option[String]
----

Requests the [RpcEndpointRef](#rpcEndpointRef) to send a [GetLocation](StateStoreCoordinator.md#GetLocation) synchronous message with the given [StateStoreProviderId](StateStoreProviderId.md) and waits for the location

Used when:

* `StateStoreAwareZipPartitionsRDD` is requested for the [preferred locations of a partition](../streaming-join/StateStoreAwareZipPartitionsRDD.md#getPreferredLocations) (when [StreamingSymmetricHashJoinExec](../physical-operators/StreamingSymmetricHashJoinExec.md) physical operator is executed

* `StateStoreRDD` is requested for [preferred locations for a task for a partition](StateStoreRDD.md#getPreferredLocations)

| reportActiveInstance
a| [[reportActiveInstance]]

[source, scala]
----
reportActiveInstance(
  stateStoreProviderId: StateStoreProviderId,
  host: String,
  executorId: String): Unit
----

Requests the [RpcEndpointRef](#rpcEndpointRef) to send a [ReportActiveInstance](StateStoreCoordinator.md#ReportActiveInstance) one-way asynchronous (fire-and-forget) message with the given [StateStoreProviderId](StateStoreProviderId.md), `host` and `executorId`

Used when `StateStore` utility is requested for [reportActiveStoreInstance](StateStore.md#reportActiveStoreInstance) (when `StateStore` utility is requested to [look up the StateStore by StateStoreProviderId](StateStore.md#get-StateStore))

| stop
a| [[stop]]

[source, scala]
----
stop(): Unit
----

Requests the [RpcEndpointRef](#rpcEndpointRef) to send a [StopCoordinator](StateStoreCoordinator.md#StopCoordinator) synchronous message

Used exclusively for unit testing

| verifyIfInstanceActive
a| [[verifyIfInstanceActive]]

[source, scala]
----
verifyIfInstanceActive(
  stateStoreProviderId: StateStoreProviderId,
  executorId: String): Boolean
----

Requests the [RpcEndpointRef](#rpcEndpointRef) to send a [VerifyIfInstanceActive](StateStoreCoordinator.md#VerifyIfInstanceActive) synchronous message with the given [StateStoreProviderId](StateStoreProviderId.md) and `executorId`, and waits for a `true` / `false` response

Used when `StateStore` utility is requested for [verifyIfStoreInstanceActive](StateStore.md#verifyIfStoreInstanceActive) (when requested to [doMaintenance](StateStore.md#doMaintenance) from a running [MaintenanceTask daemon thread](StateStore.md#MaintenanceTask))

|===

## <span id="forDriver"> Creating StateStoreCoordinatorRef to StateStoreCoordinator RPC Endpoint for Driver

```scala
forDriver(
  env: SparkEnv): StateStoreCoordinatorRef
```

`forDriver`...FIXME

`forDriver` is used when `StreamingQueryManager` is [created](../StreamingQueryManager.md#stateStoreCoordinator).

## <span id="forExecutor"> Creating StateStoreCoordinatorRef to StateStoreCoordinator RPC Endpoint for Executor

```scala
forExecutor(
  env: SparkEnv): StateStoreCoordinatorRef
```

`forExecutor`...FIXME

`forExecutor` is used when `StateStore` utility is requested for the [RPC endpoint reference to StateStoreCoordinator for Executors](StateStore.md#coordinatorRef).
