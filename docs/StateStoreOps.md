# StateStoreOps

[[dataRDD]]
`StateStoreOps` is a **Scala implicit class** of a data RDD (of type `RDD[T]`) to [create a StateStoreRDD](#mapPartitionsWithStateStore) for the following physical operators:

* [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md)

* [StateStoreRestoreExec](physical-operators/StateStoreRestoreExec.md)

* [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md)

* [StreamingDeduplicateExec](physical-operators/StreamingDeduplicateExec.md)

!!! note
    [Implicit Classes](http://docs.scala-lang.org/overviews/core/implicit-classes.html) are a language feature in Scala for **implicit conversions** with **extension methods** for existing types.

## <span id="mapPartitionsWithStateStore"> Creating StateStoreRDD (with storeUpdateFunction Aborting StateStore When Task Fails)

```scala
mapPartitionsWithStateStore[U](
  stateInfo: StatefulOperatorStateInfo,
  keySchema: StructType,
  valueSchema: StructType,
  indexOrdinal: Option[Int],
  sessionState: SessionState,
  storeCoordinator: Option[StateStoreCoordinatorRef])(
  storeUpdateFunction: (StateStore, Iterator[T]) => Iterator[U]): StateStoreRDD[T, U]
```

Internally, `mapPartitionsWithStateStore` requests `SparkContext` to clean `storeUpdateFunction` function.

NOTE: `mapPartitionsWithStateStore` uses the <<dataRDD, enclosing RDD>> to access the current `SparkContext`.

NOTE: *Function Cleaning* is to clean a closure from unreferenced variables before it is serialized and sent to tasks. `SparkContext` reports a `SparkException` when the closure is not serializable.

`mapPartitionsWithStateStore` then creates a (wrapper) function to spark-sql-streaming-StateStore.md#abort[abort] the `StateStore` if spark-sql-streaming-StateStore.md#hasCommitted[state updates had not been committed] before a task finished (which is to make sure that the `StateStore` has been spark-sql-streaming-StateStore.md#commit[committed] or spark-sql-streaming-StateStore.md##abort[aborted] in the end to follow the contract of `StateStore`).

NOTE: `mapPartitionsWithStateStore` uses `TaskCompletionListener` to be notified when a task has finished.

In the end, `mapPartitionsWithStateStore` creates a [StateStoreRDD](StateStoreRDD.md) (with the wrapper function, `SessionState` and spark-sql-streaming-StateStoreCoordinatorRef.md[StateStoreCoordinatorRef]).

`mapPartitionsWithStateStore` is used when the following physical operators are executed:

* [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md)
* [StateStoreRestoreExec](physical-operators/StateStoreRestoreExec.md)
* [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md)
* [StreamingDeduplicateExec](physical-operators/StreamingDeduplicateExec.md)
* [StreamingGlobalLimitExec](physical-operators/StreamingGlobalLimitExec.md)
