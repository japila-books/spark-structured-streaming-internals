# StateStoreCoordinatorRef &mdash; RPC Endpoint Reference to StateStoreCoordinator

`StateStoreCoordinatorRef` is used to (let the tasks on Spark executors to) send <<messages, messages>> to the <<rpcEndpointRef, StateStoreCoordinator>> (that lives on the driver).

[[creating-instance]]
[[rpcEndpointRef]]
`StateStoreCoordinatorRef` is given the `RpcEndpointRef` to the <<spark-sql-streaming-StateStoreCoordinator.md#, StateStoreCoordinator>> RPC endpoint when created.

`StateStoreCoordinatorRef` is <<creating-instance, created>> through `StateStoreCoordinatorRef` helper object when requested to create one for the <<forDriver, driver>> (when [StreamingQueryManager](StreamingQueryManager.md#stateStoreCoordinator) is created) or an <<forExecutor, executor>> (when `StateStore` helper object is requested for the <<spark-sql-streaming-StateStore.md#coordinatorRef, RPC endpoint reference to StateStoreCoordinator for Executors>>).

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

Requests the <<rpcEndpointRef, RpcEndpointRef>> to send a <<spark-sql-streaming-StateStoreCoordinator.md#DeactivateInstances, DeactivateInstances>> synchronous message with the given `runId` and waits for a `true` / `false` response

Used exclusively when `StreamingQueryManager` is requested to [handle termination of a streaming query](StreamingQueryManager.md#notifyQueryTermination) (when `StreamExecution` is requested to [run a streaming query](StreamExecution.md#runStream) and the query [has finished (running streaming batches)](StreamExecution.md#runStream-finally)).

| getLocation
a| [[getLocation]]

[source, scala]
----
getLocation(
  stateStoreProviderId: StateStoreProviderId): Option[String]
----

Requests the <<rpcEndpointRef, RpcEndpointRef>> to send a <<spark-sql-streaming-StateStoreCoordinator.md#GetLocation, GetLocation>> synchronous message with the given <<spark-sql-streaming-StateStoreProviderId.md#, StateStoreProviderId>> and waits for the location

Used when:

* `StateStoreAwareZipPartitionsRDD` is requested for the <<spark-sql-streaming-StateStoreAwareZipPartitionsRDD.md#getPreferredLocations, preferred locations of a partition>> (when `StreamingSymmetricHashJoinExec` physical operator is requested to <<physical-operators/StreamingSymmetricHashJoinExec.md#doExecute, execute and generate a recipe for a distributed computation (as an RDD[InternalRow])>>)

* `StateStoreRDD` is requested for <<spark-sql-streaming-StateStoreRDD.md#getPreferredLocations, preferred locations for a task for a partition>>

| reportActiveInstance
a| [[reportActiveInstance]]

[source, scala]
----
reportActiveInstance(
  stateStoreProviderId: StateStoreProviderId,
  host: String,
  executorId: String): Unit
----

Requests the <<rpcEndpointRef, RpcEndpointRef>> to send a <<spark-sql-streaming-StateStoreCoordinator.md#ReportActiveInstance, ReportActiveInstance>> one-way asynchronous (fire-and-forget) message with the given <<spark-sql-streaming-StateStoreProviderId.md#, StateStoreProviderId>>, `host` and `executorId`

Used exclusively when `StateStore` utility is requested for <<spark-sql-streaming-StateStore.md#reportActiveStoreInstance, reportActiveStoreInstance>> (when `StateStore` utility is requested to <<spark-sql-streaming-StateStore.md#get-StateStore, look up the StateStore by StateStoreProviderId>>)

| stop
a| [[stop]]

[source, scala]
----
stop(): Unit
----

Requests the <<rpcEndpointRef, RpcEndpointRef>> to send a <<spark-sql-streaming-StateStoreCoordinator.md#StopCoordinator, StopCoordinator>> synchronous message

Used exclusively for unit testing

| verifyIfInstanceActive
a| [[verifyIfInstanceActive]]

[source, scala]
----
verifyIfInstanceActive(
  stateStoreProviderId: StateStoreProviderId,
  executorId: String): Boolean
----

Requests the <<rpcEndpointRef, RpcEndpointRef>> to send a <<spark-sql-streaming-StateStoreCoordinator.md#VerifyIfInstanceActive, VerifyIfInstanceActive>> synchronous message with the given <<spark-sql-streaming-StateStoreProviderId.md#, StateStoreProviderId>> and `executorId`, and waits for a `true` / `false` response

Used exclusively when `StateStore` helper object is requested for <<spark-sql-streaming-StateStore.md#verifyIfStoreInstanceActive, verifyIfStoreInstanceActive>> (when requested to <<spark-sql-streaming-StateStore.md#doMaintenance, doMaintenance>> from a running <<spark-sql-streaming-StateStore.md#MaintenanceTask, MaintenanceTask daemon thread>>)

|===

=== [[forDriver]] Creating StateStoreCoordinatorRef to StateStoreCoordinator RPC Endpoint for Driver -- `forDriver` Factory Method

[source, scala]
----
forDriver(env: SparkEnv): StateStoreCoordinatorRef
----

`forDriver`...FIXME

`forDriver` is used when `StreamingQueryManager` is [created](StreamingQueryManager.md#stateStoreCoordinator).

=== [[forExecutor]] Creating StateStoreCoordinatorRef to StateStoreCoordinator RPC Endpoint for Executor -- `forExecutor` Factory Method

[source, scala]
----
forExecutor(env: SparkEnv): StateStoreCoordinatorRef
----

`forExecutor`...FIXME

NOTE: `forExecutor` is used exclusively when `StateStore` helper object is requested for the <<spark-sql-streaming-StateStore.md#coordinatorRef, RPC endpoint reference to StateStoreCoordinator for Executors>>.
