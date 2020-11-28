# StreamingAggregationStateManager

`StreamingAggregationStateManager` is the <<contract, abstraction>> of <<implementations, state managers>> that act as _middlemen_ between [state stores](spark-sql-streaming-StateStore.md) and the physical operators used in [Streaming Aggregation](streaming-aggregation.md) (e.g. [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md) and [StateStoreRestoreExec](physical-operators/StateStoreRestoreExec.md)).

[[contract]]
.StreamingAggregationStateManager Contract
[cols="1m,2",options="header",width="100%"]
|===
| Method
| Description

| commit
a| [[commit]]

[source, scala]
----
commit(
  store: StateStore): Long
----

Commits all updates (_changes_) to the given [StateStore](spark-sql-streaming-StateStore.md) and returns the new version

Used when [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md) physical operator is executed.

| get
a| [[get]]

[source, scala]
----
get(store: StateStore, key: UnsafeRow): UnsafeRow
----

Looks up the value of the key from the [StateStore](spark-sql-streaming-StateStore.md) (the key is non-``null``)

Used exclusively when [StateStoreRestoreExec](physical-operators/StateStoreRestoreExec.md) physical operator is executed.

| getKey
a| [[getKey]]

[source, scala]
----
getKey(row: UnsafeRow): UnsafeRow
----

Extracts the columns for the key from the input row

Used when:

* [StateStoreRestoreExec](physical-operators/StateStoreRestoreExec.md) physical operator is executed

* `StreamingAggregationStateManagerImplV1` legacy state manager is requested to [put a row to a state store](StreamingAggregationStateManagerImplV1.md#put)

| getStateValueSchema
a| [[getStateValueSchema]]

[source, scala]
----
getStateValueSchema: StructType
----

Gets the schema of the values in [StateStore](spark-sql-streaming-StateStore.md)s

Used when [StateStoreRestoreExec](physical-operators/StateStoreRestoreExec.md) and [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md) physical operators are executed

| iterator
a| [[iterator]]

[source, scala]
----
iterator(
  store: StateStore): Iterator[UnsafeRowPair]
----

Returns all `UnsafeRow` key-value pairs in the given [StateStore](spark-sql-streaming-StateStore.md)

Used exclusively when [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md) physical operator is executed.

| keys
a| [[keys]]

[source, scala]
----
keys(store: StateStore): Iterator[UnsafeRow]
----

Returns all the keys in the given [StateStore](spark-sql-streaming-StateStore.md)

Used exclusively when physical operators with `WatermarkSupport` are requested to [removeKeysOlderThanWatermark](WatermarkSupport.md#removeKeysOlderThanWatermark-StreamingAggregationStateManager-store) (when [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md) physical operator is executed).

| put
a| [[put]]

[source, scala]
----
put(
  store: StateStore,
  row: UnsafeRow): Unit
----

Stores (_puts_) the given row in the given [StateStore](spark-sql-streaming-StateStore.md)

Used exclusively when [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md) physical operator is executed.

| remove
a| [[remove]]

[source, scala]
----
remove(
  store: StateStore,
  key: UnsafeRow): Unit
----

Removes the key-value pair from the given [StateStore](spark-sql-streaming-StateStore.md) per key

Used exclusively when [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md) physical operator is executed (directly or indirectly as a [WatermarkSupport](WatermarkSupport.md#removeKeysOlderThanWatermark-StreamingAggregationStateManager-store))

| values
a| [[values]]

[source, scala]
----
values(
  store: StateStore): Iterator[UnsafeRow]
----

All values in the given [StateStore](spark-sql-streaming-StateStore.md)

Used exclusively when [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md) physical operator is executed.

|===

[[supportedVersions]]
`StreamingAggregationStateManager` supports <<createStateManager, two versions of state managers for streaming aggregations>> (per the [spark.sql.streaming.aggregation.stateFormatVersion](configuration-properties.md#spark.sql.streaming.aggregation.stateFormatVersion) internal configuration property):

* [[legacyVersion]] `1` (for the legacy [StreamingAggregationStateManagerImplV1](StreamingAggregationStateManagerBaseImpl.md#StreamingAggregationStateManagerImplV1))

* [[default]] `2` (for the default [StreamingAggregationStateManagerImplV2](StreamingAggregationStateManagerBaseImpl.md#StreamingAggregationStateManagerImplV2))

[[implementations]]
NOTE: [StreamingAggregationStateManagerBaseImpl](StreamingAggregationStateManagerBaseImpl.md) is the one and only known direct implementation of the <<contract, StreamingAggregationStateManager Contract>> in Spark Structured Streaming.

NOTE: `StreamingAggregationStateManager` is a Scala *sealed trait* which means that all the <<implementations, implementations>> are in the same compilation unit (a single file).

=== [[createStateManager]] Creating StreamingAggregationStateManager Instance -- `createStateManager` Factory Method

[source, scala]
----
createStateManager(
  keyExpressions: Seq[Attribute],
  inputRowAttributes: Seq[Attribute],
  stateFormatVersion: Int): StreamingAggregationStateManager
----

`createStateManager` creates a new `StreamingAggregationStateManager` for a given `stateFormatVersion`:

* [StreamingAggregationStateManagerImplV1](StreamingAggregationStateManagerImplV1.md) for `stateFormatVersion` being `1`

* [StreamingAggregationStateManagerImplV2](StreamingAggregationStateManagerImplV2.md) for `stateFormatVersion` being `2`

`createStateManager` throws a `IllegalArgumentException` for any other `stateFormatVersion`:

```text
Version [stateFormatVersion] is invalid
```

`createStateManager` is used when [StateStoreRestoreExec](physical-operators/StateStoreRestoreExec.md#stateManager) and [StateStoreSaveExec](physical-operators/StateStoreSaveExec.md#stateManager) physical operators are created.
