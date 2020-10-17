== [[StateStoreConf]] StateStoreConf

`StateStoreConf` is...FIXME

[[properties]]
.StateStoreConf's Properties
[cols="1m,3",options="header",width="100%"]
|===
| Name
| Configuration Property

| minDeltasForSnapshot
| [[minDeltasForSnapshot]] <<spark-sql-streaming-properties.md#spark.sql.streaming.stateStore.minDeltasForSnapshot, spark.sql.streaming.stateStore.minDeltasForSnapshot>>

| maxVersionsToRetainInMemory
| [[maxVersionsToRetainInMemory]] <<spark-sql-streaming-properties.md#spark.sql.streaming.maxBatchesToRetainInMemory, spark.sql.streaming.maxBatchesToRetainInMemory>>

| minVersionsToRetain
| [[minVersionsToRetain]] <<spark-sql-streaming-properties.md#spark.sql.streaming.minBatchesToRetain, spark.sql.streaming.minBatchesToRetain>>

Used exclusively when `HDFSBackedStateStoreProvider` is requested for [cleanup](HDFSBackedStateStoreProvider.md#cleanup).

| providerClass
a| [[providerClass]] <<spark-sql-streaming-properties.md#spark.sql.streaming.stateStore.providerClass, spark.sql.streaming.stateStore.providerClass>>

Used exclusively when `StateStoreProvider` helper object is requested to <<spark-sql-streaming-StateStoreProvider.md#createAndInit, create and initialize the StateStoreProvider>>.

|===
