# StateStoreAwareZipPartitionsRDD

`StateStoreAwareZipPartitionsRDD` is a `ZippedPartitionsRDD2` with the <<rdd1, left>> and <<rdd2, right>> parent RDDs.

`StateStoreAwareZipPartitionsRDD` is <<creating-instance, created>> when `StreamingSymmetricHashJoinExec` physical operator is requested to [execute](../physical-operators/StreamingSymmetricHashJoinExec.md#doExecute) (and requests <<spark-sql-streaming-StateStoreAwareZipPartitionsHelper.md#, StateStoreAwareZipPartitionsHelper>> for one).

## Creating Instance

`StateStoreAwareZipPartitionsRDD` takes the following to be created:

* [[sc]] `SparkContext`
* [[f]] Function (`(Iterator[A], Iterator[B]) => Iterator[V]`, e.g. [processPartitions](../physical-operators/StreamingSymmetricHashJoinExec.md#processPartitions))
* [[rdd1]] *Left RDD* - the RDD of the left side of a join (`RDD[A]`)
* [[rdd2]] *Right RDD* - the RDD of the right side of a join (`RDD[B]`)
* [[stateInfo]] [StatefulOperatorStateInfo](../stateful-stream-processing/StatefulOperatorStateInfo.md)
* [[stateStoreNames]] Names of the [state stores](../stateful-stream-processing/StateStore.md)
* [[storeCoordinator]] [StateStoreCoordinatorRef](../stateful-stream-processing/StateStoreCoordinatorRef.md)

=== [[getPreferredLocations]] Placement Preferences of Partition (Preferred Locations) -- `getPreferredLocations` Method

[source, scala]
----
getPreferredLocations(partition: Partition): Seq[String]
----

NOTE: `getPreferredLocations` is a part of the RDD Contract to specify placement preferences (aka _preferred task locations_), i.e. where tasks should be executed to be as close to the data as possible.

`getPreferredLocations` simply requests the [StateStoreCoordinatorRef](#storeCoordinator) for the [location](../stateful-stream-processing/StateStoreCoordinatorRef.md#getLocation) of every <<stateStoreNames, state store>> (with the <<stateInfo, StatefulOperatorStateInfo>> and the partition ID) and returns unique executor IDs (so that processing a partition happens on the executor with the proper state store for the operator and the partition).
