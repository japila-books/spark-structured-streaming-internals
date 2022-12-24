# StateStoreAwareZipPartitionsRDD

`StateStoreAwareZipPartitionsRDD` is a `ZippedPartitionsBaseRDD` ([Spark Core]({{ book.spark_core }}/rdd/ZippedPartitionsBaseRDD)) with the [rdd1](#rdd1) and [rdd2](#rdd2) parent `RDD`s.

```scala
class StateStoreAwareZipPartitionsRDD[A: ClassTag, B: ClassTag, V: ClassTag]
extends ZippedPartitionsBaseRDD[V]
```

`StateStoreAwareZipPartitionsRDD` is used to execute the following physical operators (using [StateStoreAwareZipPartitionsHelper](StateStoreAwareZipPartitionsHelper.md) implicit class):

* [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md) (with [hasInitialState](../physical-operators/FlatMapGroupsWithStateExec.md#hasInitialState) enabled)
* [StreamingSymmetricHashJoinExec](../physical-operators/StreamingSymmetricHashJoinExec.md)

## Creating Instance

`StateStoreAwareZipPartitionsRDD` takes the following to be created:

* <span id="sc"> `SparkContext` ([Spark Core]({{ book.spark_core }}/SparkContext))
* [Process Partitions Function](#f)
* <span id="rdd1"> Left `RDD[A]`
* <span id="rdd2"> Right `RDD[B]`
* <span id="stateInfo"> [StatefulOperatorStateInfo](../stateful-stream-processing/StatefulOperatorStateInfo.md)
* <span id="stateStoreNames"> Names of [StateStore](../stateful-stream-processing/StateStore.md)s
* <span id="storeCoordinator"> [StateStoreCoordinatorRef](../stateful-stream-processing/StateStoreCoordinatorRef.md)

`StateStoreAwareZipPartitionsRDD` is created when:

* `StateStoreAwareZipPartitionsHelper` is requested to [stateStoreAwareZipPartitions](StateStoreAwareZipPartitionsHelper.md#stateStoreAwareZipPartitions)

### <span id="f"> Process Partitions Function

```scala
f: (Int, Iterator[A], Iterator[B]) => Iterator[V]
```

`StateStoreAwareZipPartitionsRDD` is given a function when [created](#creating-instance) that is used to process (_join_) rows of two partitions of the [left](#rdd1) and [right](#rdd2) RDDs.

Physical Operator | Function
------------------|---------
 [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md) | [processDataWithPartition](../physical-operators/FlatMapGroupsWithStateExec.md#processDataWithPartition)
 [StreamingSymmetricHashJoinExec](../physical-operators/StreamingSymmetricHashJoinExec.md) | [processPartitions](../physical-operators/StreamingSymmetricHashJoinExec.md#processPartitions)

## <span id="compute"> Computing Partition

```scala
compute(
  s: Partition,
  context: TaskContext): Iterator[V]
```

`compute` is part of the `RDD` ([Spark Core]({{ book.spark_core }}/rdd/RDD#compute)) abstraction.

---

`compute` executes the [process partitions function](#f) with the following:

* Partition ID
* Partition records from the [rdd1](#rdd1) for the given partition ID
* Partition records from the [rdd2](#rdd2) for the given partition ID

<!---
=== [[getPreferredLocations]] Placement Preferences of Partition (Preferred Locations) -- `getPreferredLocations` Method

[source, scala]
----
getPreferredLocations(partition: Partition): Seq[String]
----

NOTE: `getPreferredLocations` is a part of the RDD Contract to specify placement preferences (aka _preferred task locations_), i.e. where tasks should be executed to be as close to the data as possible.

`getPreferredLocations` simply requests the [StateStoreCoordinatorRef](#storeCoordinator) for the [location](../stateful-stream-processing/StateStoreCoordinatorRef.md#getLocation) of every <<stateStoreNames, state store>> (with the <<stateInfo, StatefulOperatorStateInfo>> and the partition ID) and returns unique executor IDs (so that processing a partition happens on the executor with the proper state store for the operator and the partition).
-->
