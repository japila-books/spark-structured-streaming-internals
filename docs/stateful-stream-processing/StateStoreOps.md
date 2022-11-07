# StateStoreOps

`StateStoreOps` is a **Scala implicit class** of a data RDD (of type `RDD[T]`) to [create a StateStoreRDD](#mapPartitionsWithStateStore) for the following physical operators:

* [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md)
* [SessionWindowStateStoreSaveExec](../physical-operators/SessionWindowStateStoreSaveExec.md)
* [StateStoreRestoreExec](../physical-operators/StateStoreRestoreExec.md)
* [StateStoreSaveExec](../physical-operators/StateStoreSaveExec.md)
* [StreamingDeduplicateExec](../physical-operators/StreamingDeduplicateExec.md)
* [StreamingGlobalLimitExec](../physical-operators/StreamingGlobalLimitExec.md)

!!! note
    [Implicit Classes](http://docs.scala-lang.org/overviews/core/implicit-classes.html) are a language feature in Scala for **implicit conversions** with **extension methods** for existing types.

## <span id="mapPartitionsWithStateStore"> Creating StateStoreRDD (with storeUpdateFunction Aborting StateStore When Task Fails)

```scala
mapPartitionsWithStateStore[U: ClassTag](
  sqlContext: SQLContext,
  stateInfo: StatefulOperatorStateInfo,
  keySchema: StructType,
  valueSchema: StructType,
  numColsPrefixKey: Int)(
  storeUpdateFunction: (StateStore, Iterator[T]) => Iterator[U]): StateStoreRDD[T, U]
mapPartitionsWithStateStore[U: ClassTag](
  stateInfo: StatefulOperatorStateInfo,
  keySchema: StructType,
  valueSchema: StructType,
  numColsPrefixKey: Int,
  sessionState: SessionState,
  storeCoordinator: Option[StateStoreCoordinatorRef],
  extraOptions: Map[String, String] = Map.empty)(
  storeUpdateFunction: (StateStore, Iterator[T]) => Iterator[U]): StateStoreRDD[T, U]
```

??? note "numColsPrefixKey"

    Physical Operator | numColsPrefixKey
    ------------------|-----------------
     [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md#doExecute) | 0
     [SessionWindowStateStoreSaveExec](../physical-operators/SessionWindowStateStoreSaveExec.md#doExecute) | [numColsForPrefixKey](StreamingSessionWindowStateManager.md#getNumColsForPrefixKey)
     [StateStoreSaveExec](../physical-operators/StateStoreSaveExec.md#doExecute) | 0
     [StreamingDeduplicateExec](../physical-operators/StreamingDeduplicateExec.md#doExecute) | 0
     [StreamingGlobalLimitExec](../physical-operators/StreamingGlobalLimitExec.md#doExecute) | 0

`mapPartitionsWithStateStore` creates a (wrapper) function to [abort](StateStore.md#abort) the `StateStore` if [state updates had not been committed](StateStore.md#hasCommitted) before a task finished (which is to make sure that the `StateStore` has been [committed](StateStore.md#commit) or [aborted](StateStore.md#abort) in the end to follow the contract of `StateStore`).

!!! note
    `mapPartitionsWithStateStore` uses `TaskCompletionListener` ([Spark Core]({{ book.spark_core }}/TaskCompletionListener)) to be notified when a task has finished.

In the end, `mapPartitionsWithStateStore` creates a [StateStoreRDD](StateStoreRDD.md) (with the wrapper function, `SessionState` and [StateStoreCoordinatorRef](StateStoreCoordinatorRef.md)).

---

`mapPartitionsWithStateStore` is used when the following physical operators are executed:

* [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md)
* [SessionWindowStateStoreSaveExec](../physical-operators/SessionWindowStateStoreSaveExec.md)
* [StateStoreSaveExec](../physical-operators/StateStoreSaveExec.md)
* [StreamingDeduplicateExec](../physical-operators/StreamingDeduplicateExec.md)
* [StreamingGlobalLimitExec](../physical-operators/StreamingGlobalLimitExec.md)
