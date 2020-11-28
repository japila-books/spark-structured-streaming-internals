# StateStoreProviderId

[[creating-instance]]
`StateStoreProviderId` is a unique identifier of a [StateStoreProvider](spark-sql-streaming-StateStoreProvider.md) with the following properties:

* [[storeId]] [StateStoreId](spark-sql-streaming-StateStoreId.md)
* [[queryRunId]] Run ID of a streaming query ([java.util.UUID]({{ java.api }}/java/util/UUID.html))

In other words, `StateStoreProviderId` is a <<storeId, StateStoreId>> with the <<queryRunId, run ID>> that is different every restart.

`StateStoreProviderId` is used by the following execution components:

* `StateStoreCoordinator` to track the [executors of state store providers](StateStoreCoordinator.md#instances) (on the driver)

* `StateStore` object to manage [state store providers](StateStore.md#loadedProviders) (on executors)

`StateStoreProviderId` is <<creating-instance, created>> (directly or using <<apply, apply>> factory method) when:

* `StateStoreRDD` is requested for the [placement preferences of a partition](StateStoreRDD.md#getPreferredLocations) and to [compute a partition](StateStoreRDD.md#compute)

* `StateStoreAwareZipPartitionsRDD` is requested for the [preferred locations of a partition](StateStoreAwareZipPartitionsRDD.md#getPreferredLocations)

* `StateStoreHandler` is requested to [look up a state store](spark-sql-streaming-StateStoreHandler.md#getStateStore)

## <span id="apply"> Creating StateStoreProviderId

```scala
apply(
  stateInfo: StatefulOperatorStateInfo,
  partitionIndex: Int,
  storeName: String): StateStoreProviderId
```

`apply` simply creates a <<creating-instance, new StateStoreProviderId>> for the [StatefulOperatorStateInfo](StatefulOperatorStateInfo.md), the partition and the store name.

Internally, `apply` requests the `StatefulOperatorStateInfo` for the [checkpoint directory](StatefulOperatorStateInfo.md#checkpointLocation) (_checkpointLocation_) and the [stateful operator ID](StatefulOperatorStateInfo.md#operatorId) and creates a new [StateStoreId](spark-sql-streaming-StateStoreId.md) (with the `partitionIndex` and `storeName`).

In the end, `apply` requests the `StatefulOperatorStateInfo` for the [run ID of a streaming query](StatefulOperatorStateInfo.md#queryRunId) and creates a <<creating-instance, new StateStoreProviderId>> (together with the run ID).

`apply` is used when:

* `StateStoreAwareZipPartitionsRDD` is requested for the [preferred locations of a partition](StateStoreAwareZipPartitionsRDD.md#getPreferredLocations)

* `StateStoreHandler` is requested to [look up a state store](spark-sql-streaming-StateStoreHandler.md#getStateStore)
