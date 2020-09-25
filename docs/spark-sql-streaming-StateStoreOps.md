== [[StateStoreOps]] StateStoreOps -- Extension Methods for Creating StateStoreRDD

[[dataRDD]]
`StateStoreOps` is a *Scala implicit class* of a data RDD (of type `RDD[T]`) to <<mapPartitionsWithStateStore, create a StateStoreRDD>> for the following physical operators:

* [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md)

* <<spark-sql-streaming-StateStoreRestoreExec.md#, StateStoreRestoreExec>>

* <<StateStoreSaveExec.md#, StateStoreSaveExec>>

* <<physical-operators/StreamingDeduplicateExec.md#, StreamingDeduplicateExec>>

NOTE: http://docs.scala-lang.org/overviews/core/implicit-classes.html[Implicit Classes] are a language feature in Scala for *implicit conversions* with *extension methods* for existing types.

=== [[mapPartitionsWithStateStore]] Creating StateStoreRDD (with storeUpdateFunction Aborting StateStore When Task Fails) -- `mapPartitionsWithStateStore` Method

[source, scala]
----
mapPartitionsWithStateStore[U](
  stateInfo: StatefulOperatorStateInfo,
  keySchema: StructType,
  valueSchema: StructType,
  indexOrdinal: Option[Int],
  sessionState: SessionState,
  storeCoordinator: Option[StateStoreCoordinatorRef])(
  storeUpdateFunction: (StateStore, Iterator[T]) => Iterator[U]): StateStoreRDD[T, U]
// Used for testing only
mapPartitionsWithStateStore[U](
  sqlContext: SQLContext,
  stateInfo: StatefulOperatorStateInfo,
  keySchema: StructType,
  valueSchema: StructType,
  indexOrdinal: Option[Int])(
  storeUpdateFunction: (StateStore, Iterator[T]) => Iterator[U]): StateStoreRDD[T, U] // <1>
----
<1> Uses `sqlContext.streams.stateStoreCoordinator` to access `StateStoreCoordinator`

Internally, `mapPartitionsWithStateStore` requests `SparkContext` to clean `storeUpdateFunction` function.

NOTE: `mapPartitionsWithStateStore` uses the <<dataRDD, enclosing RDD>> to access the current `SparkContext`.

NOTE: *Function Cleaning* is to clean a closure from unreferenced variables before it is serialized and sent to tasks. `SparkContext` reports a `SparkException` when the closure is not serializable.

`mapPartitionsWithStateStore` then creates a (wrapper) function to spark-sql-streaming-StateStore.md#abort[abort] the `StateStore` if spark-sql-streaming-StateStore.md#hasCommitted[state updates had not been committed] before a task finished (which is to make sure that the `StateStore` has been spark-sql-streaming-StateStore.md#commit[committed] or spark-sql-streaming-StateStore.md##abort[aborted] in the end to follow the contract of `StateStore`).

NOTE: `mapPartitionsWithStateStore` uses `TaskCompletionListener` to be notified when a task has finished.

In the end, `mapPartitionsWithStateStore` creates a spark-sql-streaming-StateStoreRDD.md[StateStoreRDD] (with the wrapper function, `SessionState` and spark-sql-streaming-StateStoreCoordinatorRef.md[StateStoreCoordinatorRef]).

[NOTE]
====
`mapPartitionsWithStateStore` is used when the following physical operators are executed:

* [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md)
* <<spark-sql-streaming-StateStoreRestoreExec.md#, StateStoreRestoreExec>>
* <<StateStoreSaveExec.md#, StateStoreSaveExec>>
* <<physical-operators/StreamingDeduplicateExec.md#, StreamingDeduplicateExec>>
* <<physical-operators/StreamingGlobalLimitExec.md#, StreamingGlobalLimitExec>>
====
