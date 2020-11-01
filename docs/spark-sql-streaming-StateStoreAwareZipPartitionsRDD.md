== [[StateStoreAwareZipPartitionsRDD]] StateStoreAwareZipPartitionsRDD

`StateStoreAwareZipPartitionsRDD` is a `ZippedPartitionsRDD2` with the <<rdd1, left>> and <<rdd2, right>> parent RDDs.

`StateStoreAwareZipPartitionsRDD` is <<creating-instance, created>> exclusively when `StreamingSymmetricHashJoinExec` physical operator is requested to <<physical-operators/StreamingSymmetricHashJoinExec.md#doExecute, execute and generate a recipe for a distributed computation (as an RDD[InternalRow])>> (and requests <<spark-sql-streaming-StateStoreAwareZipPartitionsHelper.md#, StateStoreAwareZipPartitionsHelper>> for one).

=== [[creating-instance]] Creating StateStoreAwareZipPartitionsRDD Instance

`StateStoreAwareZipPartitionsRDD` takes the following to be created:

* [[sc]] `SparkContext`
* [[f]] Function (`(Iterator[A], Iterator[B]) => Iterator[V]`, e.g. <<physical-operators/StreamingSymmetricHashJoinExec.md#processPartitions, processPartitions>>)
* [[rdd1]] *Left RDD* - the RDD of the left side of a join (`RDD[A]`)
* [[rdd2]] *Right RDD* - the RDD of the right side of a join (`RDD[B]`)
* [[stateInfo]] [StatefulOperatorStateInfo](StatefulOperatorStateInfo.md)
* [[stateStoreNames]] Names of the <<spark-sql-streaming-StateStore.md#, state stores>>
* [[storeCoordinator]] <<spark-sql-streaming-StateStoreCoordinatorRef.md#, StateStoreCoordinatorRef>>

=== [[getPreferredLocations]] Placement Preferences of Partition (Preferred Locations) -- `getPreferredLocations` Method

[source, scala]
----
getPreferredLocations(partition: Partition): Seq[String]
----

NOTE: `getPreferredLocations` is a part of the RDD Contract to specify placement preferences (aka _preferred task locations_), i.e. where tasks should be executed to be as close to the data as possible.

`getPreferredLocations` simply requests the <<storeCoordinator, StateStoreCoordinatorRef>> for <<spark-sql-streaming-StateStoreCoordinatorRef.md#getLocation, the location>> of every <<stateStoreNames, state store>> (with the <<stateInfo, StatefulOperatorStateInfo>> and the partition ID) and returns unique executor IDs (so that processing a partition happens on the executor with the proper state store for the operator and the partition).
