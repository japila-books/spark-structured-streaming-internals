# StateStoreProvider

`StateStoreProvider` is the <<contract, abstraction>> of <<implementations, state store providers>> that manage <<getStore, state stores>> in <<spark-sql-streaming-stateful-stream-processing.md#, Stateful Stream Processing>> (e.g. for persisting running aggregates in [Streaming Aggregation](streaming-aggregation.md)) in stateful streaming queries.

NOTE: `StateStoreProvider` utility uses <<spark-sql-streaming-properties.md#spark.sql.streaming.stateStore.providerClass, spark.sql.streaming.stateStore.providerClass>> internal configuration property for the name of the class of the default <<implementations, StateStoreProvider implementation>>.

[[implementations]]
NOTE: [HDFSBackedStateStoreProvider](HDFSBackedStateStoreProvider.md) is the default and only known `StateStoreProvider` in Spark Structured Streaming.

[[contract]]
.StateStoreProvider Contract
[cols="30m,70",options="header",width="100%"]
|===
| Method
| Description

| close
a| [[close]]

[source, scala]
----
close(): Unit
----

Closes the state store provider

Used exclusively when `StateStore` helper object is requested to <<spark-sql-streaming-StateStore.md#unload, unload a state store provider>>

| doMaintenance
a| [[doMaintenance]]

[source, scala]
----
doMaintenance(): Unit = {}
----

Optional state maintenance

Used exclusively when `StateStore` utility is requested to <<spark-sql-streaming-StateStore.md#doMaintenance, perform maintenance of registered state store providers>> (on a separate <<spark-sql-streaming-StateStore.md#MaintenanceTask, MaintenanceTask daemon thread>>)

| getStore
a| [[getStore]]

[source, scala]
----
getStore(
  version: Long): StateStore
----

Finds the <<spark-sql-streaming-StateStore.md#, StateStore>> for the specified version

Used exclusively when `StateStore` utility is requested to <<spark-sql-streaming-StateStore.md#get-StateStore, look up the StateStore by a given provider ID>>

| init
a| [[init]]

[source, scala]
----
init(
  stateStoreId: StateStoreId,
  keySchema: StructType,
  valueSchema: StructType,
  keyIndexOrdinal: Option[Int],
  storeConfs: StateStoreConf,
  hadoopConf: Configuration): Unit
----

Initializes the state store provider

Used exclusively when `StateStoreProvider` helper object is requested to <<createAndInit, create and initialize the StateStoreProvider>> for a given <<spark-sql-streaming-StateStoreId.md#, StateStoreId>> (when `StateStore` helper object is requested to <<spark-sql-streaming-StateStore.md#get-StateStore, retrieve a StateStore by ID and version>>)

| stateStoreId
a| [[stateStoreId]]

[source, scala]
----
stateStoreId: StateStoreId
----

<<spark-sql-streaming-StateStoreId.md#, StateStoreId>> associated with the provider (at <<init, initialization>>)

Used when:

* `HDFSBackedStateStore` is requested for the [unique id](HDFSBackedStateStore.md#id)

* `HDFSBackedStateStoreProvider` is [created](HDFSBackedStateStoreProvider.md#baseDir) and requested for the [textual representation](HDFSBackedStateStoreProvider.md#toString)

| supportedCustomMetrics
a| [[supportedCustomMetrics]]

[source, scala]
----
supportedCustomMetrics: Seq[StateStoreCustomMetric]
----

<<spark-sql-streaming-StateStoreCustomMetric.md#, StateStoreCustomMetrics>> of the state store provider

Used when:

* `StateStoreWriter` stateful physical operators are requested for the [stateStoreCustomMetrics](physical-operators/StateStoreWriter.md#stateStoreCustomMetrics) (when requested for the [metrics](physical-operators/StateStoreWriter.md#metrics) and [getProgress](physical-operators/StateStoreWriter.md#getProgress))

* `HDFSBackedStateStore` is requested for the [performance metrics](HDFSBackedStateStore.md#metrics)

|===

=== [[createAndInit]] Creating and Initializing StateStoreProvider -- `createAndInit` Object Method

[source, scala]
----
createAndInit(
  stateStoreId: StateStoreId,
  keySchema: StructType,
  valueSchema: StructType,
  indexOrdinal: Option[Int],
  storeConf: StateStoreConf,
  hadoopConf: Configuration): StateStoreProvider
----

`createAndInit` creates a new <<StateStoreProvider, StateStoreProvider>> (per <<spark-sql-streaming-properties.md#spark.sql.streaming.stateStore.providerClass, spark.sql.streaming.stateStore.providerClass>> internal configuration property).

`createAndInit` requests the `StateStoreProvider` to <<init, initialize>>.

NOTE: `createAndInit` is used exclusively when `StateStore` utility is requested for the <<spark-sql-streaming-StateStore.md#get-StateStore, StateStore by given provider ID and version>>.
