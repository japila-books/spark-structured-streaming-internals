# StateStoreConf

## <span id="minDeltasForSnapshot"> minDeltasForSnapshot

[spark.sql.streaming.stateStore.minDeltasForSnapshot](../configuration-properties.md#spark.sql.streaming.stateStore.minDeltasForSnapshot)

## <span id="maxVersionsToRetainInMemory"> maxVersionsToRetainInMemory

[spark.sql.streaming.maxBatchesToRetainInMemory](../configuration-properties.md#spark.sql.streaming.maxBatchesToRetainInMemory)

## <span id="minVersionsToRetain"> minVersionsToRetain

[spark.sql.streaming.minBatchesToRetain](../configuration-properties.md#spark.sql.streaming.minBatchesToRetain)

Used when `HDFSBackedStateStoreProvider` is requested for [cleanup](HDFSBackedStateStoreProvider.md#cleanup).

## <span id="providerClass"> providerClass

[spark.sql.streaming.stateStore.providerClass](../configuration-properties.md#spark.sql.streaming.stateStore.providerClass)

Used when `StateStoreProvider` helper object is requested to [create and initialize the StateStoreProvider](StateStoreProvider.md#createAndInit).
