# StateStoreId

[[creating-instance]]
`StateStoreId` is a unique identifier of a [StateStore](StateStore.md) with the following attributes:

* [[checkpointRootLocation]] *Checkpoint Root Location* - the root directory for state checkpointing
* [[operatorId]] *Operator ID* - a unique ID of the stateful operator
* [[partitionId]] *Partition ID* - the index of the partition
* [[storeName]] *Store Name* - the name of the [state store](StateStore.md) (default: <<DEFAULT_STORE_NAME, default>>)

`StateStoreId` is <<creating-instance, created>> when:

* `StateStoreRDD` is requested for the [preferred locations of a partition](StateStoreRDD.md#getPreferredLocations) (executed on the driver) and to [compute it](StateStoreRDD.md#compute) (later on an executor)

* `StateStoreProviderId` helper object is requested to create a <<StateStoreProviderId.md#, StateStoreProviderId>> (with a <<StateStoreId, StateStoreId>> and the run ID of a streaming query) that is then used for the [preferred locations of a partition](../join/StateStoreAwareZipPartitionsRDD.md#getPreferredLocations) of a `StateStoreAwareZipPartitionsRDD` (executed on the driver) and to...FIXME

[[DEFAULT_STORE_NAME]]
The name of the *default state store* (for reading state store data that was generated before store names were used, i.e. in Spark 2.2 and earlier) is *default*.

=== [[storeCheckpointLocation]] State Checkpoint Base Directory of Stateful Operator -- `storeCheckpointLocation` Method

[source, scala]
----
storeCheckpointLocation(): Path
----

`storeCheckpointLocation` is Hadoop DFS's https://hadoop.apache.org/docs/r2.7.3/api/org/apache/hadoop/fs/Path.html[Path] of the checkpoint location (for the stateful operator by <<operatorId, operator ID>>, the partition by the <<partitionId, partition ID>> in the <<checkpointRootLocation, checkpoint root location>>).

If the <<DEFAULT_STORE_NAME, default store name>> is used (for Spark 2.2 and earlier), the <<storeName, storeName>> is not included in the path.

`storeCheckpointLocation` is used when `HDFSBackedStateStoreProvider` is requested for the [state checkpoint base directory](HDFSBackedStateStoreProvider.md#baseDir).
