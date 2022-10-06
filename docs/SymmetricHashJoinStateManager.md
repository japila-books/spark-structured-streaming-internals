# SymmetricHashJoinStateManager

`SymmetricHashJoinStateManager` is <<creating-instance, created>> for the left and right [OneSideHashJoiners](OneSideHashJoiner.md#joinStateManager) of a [StreamingSymmetricHashJoinExec](physical-operators/StreamingSymmetricHashJoinExec.md) physical operator (one for each side when `StreamingSymmetricHashJoinExec` is requested to [process partitions of the left and right sides of a stream-stream join](physical-operators/StreamingSymmetricHashJoinExec.md#processPartitions)).

![SymmetricHashJoinStateManager and Stream-Stream Join](images/SymmetricHashJoinStateManager.png)

`SymmetricHashJoinStateManager` manages join state using the <<keyToNumValues, KeyToNumValuesStore>> and the <<keyWithIndexToValue, KeyWithIndexToValueStore>> state store handlers (and simply acts like their facade).

## Creating Instance

`SymmetricHashJoinStateManager` takes the following to be created:

* [[joinSide]] [JoinSide](#joinSide-internals)
* [[inputValueAttributes]] Attributes of input values
* [[joinKeys]] Join keys (`Seq[Expression]`)
* [[stateInfo]] [StatefulOperatorStateInfo](StatefulOperatorStateInfo.md)
* [[storeConf]] [StateStoreConf](StateStoreConf.md)
* [[hadoopConf]] Hadoop [Configuration]({{ hadoop.api }}/org/apache/hadoop/conf/Configuration.html)

=== [[keyToNumValues]][[keyWithIndexToValue]] KeyToNumValuesStore and KeyWithIndexToValueStore State Store Handlers -- `keyToNumValues` and `keyWithIndexToValue` Internal Properties

`SymmetricHashJoinStateManager` uses a <<spark-sql-streaming-KeyToNumValuesStore.md#, KeyToNumValuesStore>> (`keyToNumValues`) and a <<spark-sql-streaming-KeyWithIndexToValueStore.md#, KeyWithIndexToValueStore>> (`keyWithIndexToValue`) internally that are created immediately when `SymmetricHashJoinStateManager` is <<creating-instance, created>> (for a [OneSideHashJoiner](OneSideHashJoiner.md#joinStateManager)).

`keyToNumValues` and `keyWithIndexToValue` are used when `SymmetricHashJoinStateManager` is requested for the following:

* <<get, Retrieving the value rows by key>>

* <<append, Append a new value row to a given key>>

* <<removeByKeyCondition, removeByKeyCondition>>

* <<removeByValueCondition, removeByValueCondition>>

* <<commit, Commit state changes>>

* <<abortIfNeeded, Abort state changes>>

* <<metrics, Performance metrics>>

=== [[joinSide-internals]] Join Side Marker -- `JoinSide` Internal Enum

`JoinSide` can be one of the two possible values:

* [[LeftSide]][[left]] `LeftSide` (alias: `left`)

* [[RightSide]][[right]] `RightSide` (alias: `right`)

They are both used exclusively when `StreamingSymmetricHashJoinExec` binary physical operator is requested to <<physical-operators/StreamingSymmetricHashJoinExec.md#doExecute, execute>> (and <<physical-operators/StreamingSymmetricHashJoinExec.md#processPartitions, process partitions of the left and right sides of a stream-stream join>> with an [OneSideHashJoiner](OneSideHashJoiner.md)).

=== [[metrics]] Performance Metrics -- `metrics` Method

[source, scala]
----
metrics: StateStoreMetrics
----

`metrics` returns the combined [StateStoreMetrics](StateStoreMetrics.md) of the <<keyToNumValues, KeyToNumValuesStore>> and the <<keyWithIndexToValue, KeyWithIndexToValueStore>> state store handlers.

`metrics` is used when `OneSideHashJoiner` is requested to [commitStateAndGetMetrics](OneSideHashJoiner.md#commitStateAndGetMetrics).

=== [[removeByKeyCondition]] `removeByKeyCondition` Method

[source, scala]
----
removeByKeyCondition(
  removalCondition: UnsafeRow => Boolean): Iterator[UnsafeRowPair]
----

`removeByKeyCondition` creates an `Iterator` of `UnsafeRowPairs` that <<removeByKeyCondition-getNext, removes keys (and associated values)>> for which the given `removalCondition` predicate holds.

[[removeByKeyCondition-allKeyToNumValues]]
`removeByKeyCondition` uses the <<keyToNumValues, KeyToNumValuesStore>> for <<spark-sql-streaming-KeyToNumValuesStore.md#iterator, all state keys and values (in the underlying state store)>>.

`removeByKeyCondition` is used when `OneSideHashJoiner` is requested to [remove an old state](OneSideHashJoiner.md#removeOldState) (for [JoinStateKeyWatermarkPredicate](spark-sql-streaming-JoinStateWatermarkPredicate.md#JoinStateKeyWatermarkPredicate)).

==== [[removeByKeyCondition-getNext]] `getNext` Internal Method (of `removeByKeyCondition` Method)

[source, scala]
----
getNext(): UnsafeRowPair
----

`getNext` goes over the keys and values in the <<removeByKeyCondition-allKeyToNumValues, allKeyToNumValues>> sequence and <<spark-sql-streaming-KeyToNumValuesStore.md#remove, removes keys>> (from the <<keyToNumValues, KeyToNumValuesStore>>) and the <<spark-sql-streaming-KeyWithIndexToValueStore.md#, corresponding values>> (from the <<keyWithIndexToValue, KeyWithIndexToValueStore>>) for which the given `removalCondition` predicate holds.

=== [[removeByValueCondition]] `removeByValueCondition` Method

[source, scala]
----
removeByValueCondition(
  removalCondition: UnsafeRow => Boolean): Iterator[UnsafeRowPair]
----

`removeByValueCondition` creates an `Iterator` of `UnsafeRowPairs` that <<removeByValueCondition-getNext, removes values (and associated keys if needed)>> for which the given `removalCondition` predicate holds.

`removeByValueCondition` is used when `OneSideHashJoiner` is requested to [remove an old state](OneSideHashJoiner.md#removeOldState) (when [JoinStateValueWatermarkPredicate](spark-sql-streaming-JoinStateWatermarkPredicate.md#JoinStateValueWatermarkPredicate) is used).

==== [[removeByValueCondition-getNext]] `getNext` Internal Method (of `removeByValueCondition` Method)

[source, scala]
----
getNext(): UnsafeRowPair
----

`getNext`...FIXME

=== [[append]] Appending New Value Row to Key -- `append` Method

[source, scala]
----
append(
  key: UnsafeRow,
  value: UnsafeRow): Unit
----

`append` requests the <<keyToNumValues, KeyToNumValuesStore>> for the <<spark-sql-streaming-KeyToNumValuesStore.md#get, number of value rows for the given key>>.

In the end, `append` requests the stores for the following:

* <<keyWithIndexToValue, KeyWithIndexToValueStore>> to <<spark-sql-streaming-KeyWithIndexToValueStore.md#put, store the given value row>>

* <<keyToNumValues, KeyToNumValuesStore>> to <<spark-sql-streaming-KeyToNumValuesStore.md#put, store the given key with the number of value rows incremented>>.

`append` is used when `OneSideHashJoiner` is requested to [storeAndJoinWithOtherSide](OneSideHashJoiner.md#storeAndJoinWithOtherSide).

=== [[get]] Retrieving Value Rows By Key -- `get` Method

[source, scala]
----
get(key: UnsafeRow): Iterator[UnsafeRow]
----

`get` requests the <<keyToNumValues, KeyToNumValuesStore>> for the <<spark-sql-streaming-KeyToNumValuesStore.md#get, number of value rows for the given key>>.

In the end, `get` requests the <<keyWithIndexToValue, KeyWithIndexToValueStore>> to <<spark-sql-streaming-KeyWithIndexToValueStore.md#getAll, retrieve that number of value rows for the given key>> and leaves value rows only.

`get` is used when `OneSideHashJoiner` is requested to [storeAndJoinWithOtherSide](OneSideHashJoiner.md#storeAndJoinWithOtherSide) and [retrieving value rows for a key](OneSideHashJoiner.md#get).

=== [[commit]] Committing State (Changes) -- `commit` Method

[source, scala]
----
commit(): Unit
----

`commit` simply requests the <<keyToNumValues, keyToNumValues>> and <<keyWithIndexToValue, keyWithIndexToValue>> state store handlers to <<spark-sql-streaming-StateStoreHandler.md#commit, commit state changes>>.

`commit` is used when `OneSideHashJoiner` is requested to [commit state changes and get performance metrics](OneSideHashJoiner.md#commitStateAndGetMetrics).

=== [[abortIfNeeded]] Aborting State (Changes) -- `abortIfNeeded` Method

[source, scala]
----
abortIfNeeded(): Unit
----

`abortIfNeeded`...FIXME

NOTE: `abortIfNeeded` is used when...FIXME

=== [[allStateStoreNames]] `allStateStoreNames` Object Method

[source, scala]
----
allStateStoreNames(joinSides: JoinSide*): Seq[String]
----

`allStateStoreNames` simply returns the <<getStateStoreName, names of the state stores>> for all possible combinations of the given `JoinSides` and the two possible store types (e.g. <<spark-sql-streaming-StateStoreHandler.md#KeyToNumValuesType, keyToNumValues>> and <<spark-sql-streaming-StateStoreHandler.md#KeyWithIndexToValueType, keyWithIndexToValue>>).

NOTE: `allStateStoreNames` is used exclusively when `StreamingSymmetricHashJoinExec` physical operator is requested to <<physical-operators/StreamingSymmetricHashJoinExec.md#doExecute, execute and generate the runtime representation>> (as a `RDD[InternalRow]`).

=== [[getStateStoreName]] `getStateStoreName` Object Method

[source, scala]
----
getStateStoreName(
  joinSide: JoinSide,
  storeType: StateStoreType): String
----

`getStateStoreName` simply returns a string of the following format:

```
[joinSide]-[storeType]
```

[NOTE]
====
`getStateStoreName` is used when:

* `StateStoreHandler` is requested to <<spark-sql-streaming-StateStoreHandler.md#getStateStore, load a state store>>

* `SymmetricHashJoinStateManager` utility is requested for <<allStateStoreNames, allStateStoreNames>> (for `StreamingSymmetricHashJoinExec` physical operator to <<physical-operators/StreamingSymmetricHashJoinExec.md#doExecute, execute and generate the runtime representation>>)
====

=== [[updateNumValueForCurrentKey]] `updateNumValueForCurrentKey` Internal Method

[source, scala]
----
updateNumValueForCurrentKey(): Unit
----

`updateNumValueForCurrentKey`...FIXME

NOTE: `updateNumValueForCurrentKey` is used exclusively when `SymmetricHashJoinStateManager` is requested to <<removeByValueCondition, removeByValueCondition>>.

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| keyAttributes
a| [[keyAttributes]] Key attributes, i.e. `AttributeReferences` of the <<keySchema, key schema>>

Used exclusively in `KeyWithIndexToValueStore` when requested for the <<spark-sql-streaming-KeyWithIndexToValueStore.md#keyWithIndexExprs, keyWithIndexExprs>>, <<spark-sql-streaming-KeyWithIndexToValueStore.md#indexOrdinalInKeyWithIndexRow, indexOrdinalInKeyWithIndexRow>>, <<spark-sql-streaming-KeyWithIndexToValueStore.md#keyWithIndexRowGenerator, keyWithIndexRowGenerator>> and <<spark-sql-streaming-KeyWithIndexToValueStore.md#keyRowGenerator, keyRowGenerator>>

| keySchema
a| [[keySchema]] Key schema (`StructType`) based on the <<joinKeys, join keys>> with the names in the format of *field* and their ordinals (index)

Used when:

* `SymmetricHashJoinStateManager` is requested for the <<keyAttributes, key attributes>> (for <<spark-sql-streaming-KeyWithIndexToValueStore.md#, KeyWithIndexToValueStore>>)

* `KeyToNumValuesStore` is requested for the <<spark-sql-streaming-KeyToNumValuesStore.md#stateStore, state store>>

* `KeyWithIndexToValueStore` is requested for the <<spark-sql-streaming-KeyWithIndexToValueStore.md#keyWithIndexSchema, keyWithIndexSchema>> (for the internal <<spark-sql-streaming-KeyWithIndexToValueStore.md#stateStore, state store>>)

|===
