== [[StateStoreMetrics]] StateStoreMetrics

[[creating-instance]]
`StateStoreMetrics` holds the performance metrics of a <<spark-sql-streaming-StateStore.adoc#, state store>>:

* [[numKeys]] Number of keys
* [[memoryUsedBytes]] Memory used (in bytes)
* [[customMetrics]] <<spark-sql-streaming-StateStoreCustomMetric.adoc#, StateStoreCustomMetrics>> with their current values (`Map[StateStoreCustomMetric, Long]`)

`StateStoreMetrics` is used (and <<creating-instance, created>>) when the following are requested for the performance metrics:

* <<spark-sql-streaming-StateStore.adoc#metrics, StateStore>>

* <<spark-sql-streaming-StateStoreHandler.adoc#metrics, StateStoreHandler>>

* <<spark-sql-streaming-SymmetricHashJoinStateManager.adoc#metrics, SymmetricHashJoinStateManager>>
