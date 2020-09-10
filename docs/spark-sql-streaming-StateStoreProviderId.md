== [[StateStoreProviderId]] StateStoreProviderId -- Unique Identifier of State Store Provider

[[creating-instance]]
`StateStoreProviderId` is a unique identifier of a <<spark-sql-streaming-StateStoreProvider.md#, state store provider>> with the following properties:

* [[storeId]] <<spark-sql-streaming-StateStoreId.md#, StateStoreId>>
* [[queryRunId]] Run ID of a streaming query (https://docs.oracle.com/javase/8/docs/api/java/util/UUID.html[java.util.UUID])

In other words, `StateStoreProviderId` is a <<storeId, StateStoreId>> with the <<queryRunId, run ID>> that is different every restart.

`StateStoreProviderId` is used by the following execution components:

* `StateStoreCoordinator` to track the <<spark-sql-streaming-StateStoreCoordinator.md#instances, executors of state store providers>> (on the driver)

* `StateStore` object to manage <<spark-sql-streaming-StateStore.md#loadedProviders, state store providers>> (on executors)

`StateStoreProviderId` is <<creating-instance, created>> (directly or using <<apply, apply>> factory method) when:

* `StateStoreRDD` is requested for the <<spark-sql-streaming-StateStoreRDD.md#getPreferredLocations, placement preferences of a partition>> and to <<spark-sql-streaming-StateStoreRDD.md#compute, compute a partition>>

* `StateStoreAwareZipPartitionsRDD` is requested for the <<spark-sql-streaming-StateStoreAwareZipPartitionsRDD.md#getPreferredLocations, preferred locations of a partition>>

* `StateStoreHandler` is requested to <<spark-sql-streaming-StateStoreHandler.md#getStateStore, look up a state store>>

=== [[apply]] Creating StateStoreProviderId -- `apply` Factory Method

[source, scala]
----
apply(
  stateInfo: StatefulOperatorStateInfo,
  partitionIndex: Int,
  storeName: String): StateStoreProviderId
----

`apply` simply creates a <<creating-instance, new StateStoreProviderId>> for the <<spark-sql-streaming-StatefulOperatorStateInfo.md#, StatefulOperatorStateInfo>>, the partition and the store name.

Internally, `apply` requests the `StatefulOperatorStateInfo` for the <<spark-sql-streaming-StatefulOperatorStateInfo.md#checkpointLocation, checkpoint directory>> (aka _checkpointLocation_) and the <<spark-sql-streaming-StatefulOperatorStateInfo.md#operatorId, stateful operator ID>> and creates a new <<spark-sql-streaming-StateStoreId.md#, StateStoreId>> (with the `partitionIndex` and `storeName`).

In the end, `apply` requests the `StatefulOperatorStateInfo` for the <<spark-sql-streaming-StatefulOperatorStateInfo.md#queryRunId, run ID of a streaming query>> and creates a <<creating-instance, new StateStoreProviderId>> (together with the run ID).

[NOTE]
====
`apply` is used when:

* `StateStoreAwareZipPartitionsRDD` is requested for the <<spark-sql-streaming-StateStoreAwareZipPartitionsRDD.md#getPreferredLocations, preferred locations of a partition>>

* `StateStoreHandler` is requested to <<spark-sql-streaming-StateStoreHandler.md#getStateStore, look up a state store>>
====
