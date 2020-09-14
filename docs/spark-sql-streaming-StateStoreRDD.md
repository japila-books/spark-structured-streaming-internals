== [[StateStoreRDD]] StateStoreRDD -- RDD for Updating State (in StateStores Across Spark Cluster)

`StateStoreRDD` is an `RDD` for <<compute, executing storeUpdateFunction>> with spark-sql-streaming-StateStore.md[StateStore] (and data from partitions of the <<dataRDD, data RDD>>).

`StateStoreRDD` is <<creating-instance, created>> for the following stateful physical operators (using <<spark-sql-streaming-StateStoreOps.md#mapPartitionsWithStateStore, StateStoreOps.mapPartitionsWithStateStore>>):

* [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md)
* <<spark-sql-streaming-StateStoreRestoreExec.md#, StateStoreRestoreExec>>
* <<spark-sql-streaming-StateStoreSaveExec.md#, StateStoreSaveExec>>
* <<spark-sql-streaming-StreamingDeduplicateExec.md#, StreamingDeduplicateExec>>
* <<spark-sql-streaming-StreamingGlobalLimitExec.md#, StreamingGlobalLimitExec>>

.StateStoreRDD, Physical and Logical Plans, and operators
image::images/StateStoreRDD-SparkPlans-LogicalPlans-operators.png[align="center"]

`StateStoreRDD` uses `StateStoreCoordinator` for the <<getPreferredLocations, preferred locations of a partition>> for job scheduling.

.StateStoreRDD and StateStoreCoordinator
image::images/StateStoreRDD-StateStoreCoordinator.png[align="center"]

[[getPartitions]]
`getPartitions` is exactly the partitions of the <<dataRDD, data RDD>>.

=== [[compute]] Computing Partition -- `compute` Method

[source, scala]
----
compute(
  partition: Partition,
  ctxt: TaskContext): Iterator[U]
----

NOTE: `compute` is part of the RDD Contract to compute a given partition.

`compute` computes <<dataRDD, dataRDD>> passing the result on to <<storeUpdateFunction, storeUpdateFunction>> (with a configured spark-sql-streaming-StateStore.md[StateStore]).

Internally, (and similarly to <<getPreferredLocations, getPreferredLocations>>) `compute` creates a <<spark-sql-streaming-StateStoreProviderId.md#, StateStoreProviderId>> with `StateStoreId` (using <<checkpointLocation, checkpointLocation>>, <<operatorId, operatorId>> and the index of the input `partition`) and <<queryRunId, queryRunId>>.

`compute` then requests `StateStore` for spark-sql-streaming-StateStore.md#get[the store for the StateStoreProviderId].

In the end, `compute` computes <<dataRDD, dataRDD>> (using the input `partition` and `ctxt`) followed by executing <<storeUpdateFunction, storeUpdateFunction>> (with the store and the result).

=== [[getPreferredLocations]] Placement Preferences of Partition (Preferred Locations) -- `getPreferredLocations` Method

[source, scala]
----
getPreferredLocations(partition: Partition): Seq[String]
----

NOTE: `getPreferredLocations` is a part of the RDD Contract to specify placement preferences (aka _preferred task locations_), i.e. where tasks should be executed to be as close to the data as possible.

`getPreferredLocations` creates a <<spark-sql-streaming-StateStoreProviderId.md#, StateStoreProviderId>> with `StateStoreId` (using <<checkpointLocation, checkpointLocation>>, <<operatorId, operatorId>> and the index of the input `partition`) and <<queryRunId, queryRunId>>.

NOTE: <<checkpointLocation, checkpointLocation>> and <<operatorId, operatorId>> are shared across different partitions and so the only difference in <<spark-sql-streaming-StateStoreProviderId.md#, StateStoreProviderIds>> is the partition index.

In the end, `getPreferredLocations` requests <<storeCoordinator, StateStoreCoordinatorRef>> for the spark-sql-streaming-StateStoreCoordinatorRef.md#getLocation[location of the state store] for the <<spark-sql-streaming-StateStoreProviderId.md#, StateStoreProviderId>>.

NOTE: spark-sql-streaming-StateStoreCoordinator.md[StateStoreCoordinator] coordinates instances of `StateStores` across Spark executors in the cluster, and tracks their locations for job scheduling.

=== [[creating-instance]] Creating StateStoreRDD Instance

`StateStoreRDD` takes the following to be created:

* [[dataRDD]] Data RDD (`RDD[T]` to update the aggregates in a state store)
* [[storeUpdateFunction]] Store update function (`(StateStore, Iterator[T]) => Iterator[U]` where `T` is the type of rows in the <<dataRDD, data RDD>>)
* [[checkpointLocation]] Checkpoint directory
* [[queryRunId]] Run ID of the streaming query
* [[operatorId]] Operator ID
* [[storeVersion]] Version of the store
* [[keySchema]] *Key schema* - schema of the keys
* [[valueSchema]] *Value schema* - schema of the values
* [[indexOrdinal]] Index
* [[sessionState]] `SessionState`
* [[storeCoordinator]] Optional <<spark-sql-streaming-StateStoreCoordinatorRef.md#, StateStoreCoordinatorRef>>

`StateStoreRDD` initializes the <<internal-properties, internal properties>>.

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| hadoopConfBroadcast
| [[hadoopConfBroadcast]]

| storeConf
| [[storeConf]] Configuration parameters (as `StateStoreConf`) using the current `SQLConf` (from `SessionState`)
|===
