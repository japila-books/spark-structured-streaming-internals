== [[StateStoreAwareZipPartitionsHelper]] StateStoreAwareZipPartitionsHelper -- Extension Methods for Creating StateStoreAwareZipPartitionsRDD

[[dataRDD]]
`StateStoreAwareZipPartitionsHelper` is a *Scala implicit class* of a data RDD (of type `RDD[T]`) to <<stateStoreAwareZipPartitions, create a StateStoreAwareZipPartitionsRDD>> for <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>> physical operator.

NOTE: http://docs.scala-lang.org/overviews/core/implicit-classes.html[Implicit Classes] are a language feature in Scala for *implicit conversions* with *extension methods* for existing types.

=== [[stateStoreAwareZipPartitions]] Creating StateStoreAwareZipPartitionsRDD -- `stateStoreAwareZipPartitions` Method

[source, scala]
----
stateStoreAwareZipPartitions[U: ClassTag, V: ClassTag](
  dataRDD2: RDD[U],
  stateInfo: StatefulOperatorStateInfo,
  storeNames: Seq[String],
  storeCoordinator: StateStoreCoordinatorRef
)(f: (Iterator[T], Iterator[U]) => Iterator[V]): RDD[V]
----

`stateStoreAwareZipPartitions` simply creates a new <<spark-sql-streaming-StateStoreAwareZipPartitionsRDD.md#, StateStoreAwareZipPartitionsRDD>>.

NOTE: `stateStoreAwareZipPartitions` is used exclusively when `StreamingSymmetricHashJoinExec` physical operator is requested to <<physical-operators/StreamingSymmetricHashJoinExec.md#doExecute, execute and generate a recipe for a distributed computation (as an RDD[InternalRow])>>.
