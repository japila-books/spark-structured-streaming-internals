---
title: StateStoreAwareZipPartitionsHelper
---

# StateStoreAwareZipPartitionsHelper Implicit Class

`StateStoreAwareZipPartitionsHelper` is a Scala implicit class of a data RDD (of type `RDD[T]`) to [create a StateStoreAwareZipPartitionsRDD](#stateStoreAwareZipPartitions) to execute the following physical operators:

* [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md) (with [hasInitialState](../physical-operators/FlatMapGroupsWithStateExec.md#hasInitialState) enabled)
* [StreamingSymmetricHashJoinExec](../physical-operators/StreamingSymmetricHashJoinExec.md)

??? note "Implicit Class"
    [Implicit Classes](http://docs.scala-lang.org/overviews/core/implicit-classes.html) are a language feature in Scala for **implicit conversions** with **extension methods** for existing types.

## <span id="stateStoreAwareZipPartitions"> Creating StateStoreAwareZipPartitionsRDD

```scala
stateStoreAwareZipPartitions[U: ClassTag, V: ClassTag](
  dataRDD2: RDD[U],
  stateInfo: StatefulOperatorStateInfo,
  storeNames: Seq[String],
  storeCoordinator: StateStoreCoordinatorRef)(
  f: (Iterator[T], Iterator[U]) => Iterator[V]): RDD[V]
```

`stateStoreAwareZipPartitions` creates a new [StateStoreAwareZipPartitionsRDD](StateStoreAwareZipPartitionsRDD.md).

---

`stateStoreAwareZipPartitions` is used when the following physical operators are executed:

* [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md) (with [hasInitialState](../physical-operators/FlatMapGroupsWithStateExec.md#hasInitialState) enabled)
* [StreamingSymmetricHashJoinExec](../physical-operators/StreamingSymmetricHashJoinExec.md)
