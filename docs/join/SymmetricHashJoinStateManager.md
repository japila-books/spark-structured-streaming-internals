# SymmetricHashJoinStateManager

`SymmetricHashJoinStateManager` is used for the left and right [OneSideHashJoiner](OneSideHashJoiner.md#joinStateManager)s of a [StreamingSymmetricHashJoinExec](../physical-operators/StreamingSymmetricHashJoinExec.md) physical operator (one for each side when `StreamingSymmetricHashJoinExec` is requested to [process partitions of the left and right sides of a stream-stream join](../physical-operators/StreamingSymmetricHashJoinExec.md#processPartitions)).

`SymmetricHashJoinStateManager` is created and used exclusively by [OneSideHashJoiner](OneSideHashJoiner.md#joinStateManager) (and reflects the arguments of the owning `OneSideHashJoiner`, e.g., [joinSide](#joinSide), [inputValueAttributes](#inputValueAttributes), [joinKeys](#joinKeys), [stateInfo](#stateInfo), [partitionId](#partitionId)).

`SymmetricHashJoinStateManager` manages join state using the [KeyToNumValuesStore](#keyToNumValues) and [KeyWithIndexToValueStore](#keyWithIndexToValue) state store handlers (and acts like their facade).

![SymmetricHashJoinStateManager and Stream-Stream Join](../images/SymmetricHashJoinStateManager.png)

## Creating Instance

`SymmetricHashJoinStateManager` takes the following to be created:

* [JoinSide](#joinSide)
* <span id="inputValueAttributes"> Input Value `Attribute`s
* <span id="joinKeys"> Join Keys (`Seq[Expression]`)
* <span id="stateInfo"> [StatefulOperatorStateInfo](../stateful-stream-processing/StatefulOperatorStateInfo.md)
* <span id="storeConf"> [StateStoreConf](../stateful-stream-processing/StateStoreConf.md)
* <span id="hadoopConf"> Hadoop [Configuration]({{ hadoop.api }}/org/apache/hadoop/conf/Configuration.html)
* <span id="partitionId"> Partition ID
* <span id="stateFormatVersion"> State Format Version

`SymmetricHashJoinStateManager` is created when:

* `OneSideHashJoiner` is [created](OneSideHashJoiner.md#joinStateManager)

### <span id="joinSide"><span id="LeftSide"><span id="RightSide"> JoinSide

`SymmetricHashJoinStateManager` is given a `JoinSide` marker when [created](#creating-instance) that indicates the join side (of the parent [OneSideHashJoiner](OneSideHashJoiner.md#joinSide)).

JoinSide | Alias
---------|------
 `LeftSide` | `left`
 `RightSide` | `right`

### <span id="keyToNumValues"> KeyToNumValuesStore

`SymmetricHashJoinStateManager` creates a [KeyToNumValuesStore](KeyToNumValuesStore.md) when [created](#creating-instance).

### <span id="keyWithIndexToValue"> KeyWithIndexToValueStore

`SymmetricHashJoinStateManager` creates a [KeyWithIndexToValueStore](KeyWithIndexToValueStore.md) (for the [stateFormatVersion](#stateFormatVersion)) when [created](#creating-instance).

## <span id="getJoinedRows"> getJoinedRows

```scala
getJoinedRows(
  key: UnsafeRow,
  generateJoinedRow: InternalRow => JoinedRow,
  predicate: JoinedRow => Boolean,
  excludeRowsAlreadyMatched: Boolean = false): Iterator[JoinedRow]
```

`getJoinedRows`...FIXME

---

`getJoinedRows` is used when:

* `OneSideHashJoiner` is requested to [storeAndJoinWithOtherSide](OneSideHashJoiner.md#storeAndJoinWithOtherSide)

## <span id="append"> append

```scala
append(
  key: UnsafeRow,
  value: UnsafeRow,
  matched: Boolean): Unit
```

`append`...FIXME

---

`append` is used when:

* `OneSideHashJoiner` is requested to [storeAndJoinWithOtherSide](OneSideHashJoiner.md#storeAndJoinWithOtherSide)

## <span id="removeByKeyCondition"> removeByKeyCondition

```scala
removeByKeyCondition(
  removalCondition: UnsafeRow => Boolean): Iterator[KeyToValuePair]
```

`removeByKeyCondition`...FIXME

---

`removeByKeyCondition` is used when:

* `OneSideHashJoiner` is requested to [removeOldState](OneSideHashJoiner.md#removeOldState) (for `JoinStateKeyWatermarkPredicate`)

## <span id="removeByValueCondition"> removeByValueCondition

```scala
removeByValueCondition(
  removalCondition: UnsafeRow => Boolean): Iterator[KeyToValuePair]
```

`removeByValueCondition`...FIXME

---

`removeByValueCondition` is used when:

* `OneSideHashJoiner` is requested to [removeOldState](OneSideHashJoiner.md#removeOldState) (for `JoinStateValueWatermarkPredicate`)

## <span id="get"> get

```scala
get(
  key: UnsafeRow): Iterator[UnsafeRow]
```

`get`...FIXME

---

`get` is used when:

* `OneSideHashJoiner` is requested to [get](OneSideHashJoiner.md#get)

## <span id="commit"> Committing (State) Changes

```scala
commit(): Unit
```

`commit` requests the [keyToNumValues](#keyToNumValues) and [keyWithIndexToValue](#keyWithIndexToValue) to [commit](StateStoreHandler.md#commit).

---

`commit` is used when:

* `OneSideHashJoiner` is requested to [commitStateAndGetMetrics](OneSideHashJoiner.md#commitStateAndGetMetrics)

## <span id="metrics"> Performance Metrics

```scala
metrics: StateStoreMetrics
```

`metrics` requests the [keyToNumValues](#keyToNumValues) and [keyWithIndexToValue](#keyWithIndexToValue) for the [metrics](StateStoreHandler.md#metrics).

`metrics` creates a [StateStoreMetrics](../stateful-stream-processing/StateStoreMetrics.md):

Metric | Value
-------|------
 [Number of Keys](../stateful-stream-processing/StateStoreMetrics.md#numKeys) | [Number of Keys](../stateful-stream-processing/StateStoreMetrics.md#numKeys) of the [keyWithIndexToValue](#keyWithIndexToValue)
 [Memory used (in bytes)](../stateful-stream-processing/StateStoreMetrics.md#memoryUsedBytes) | Total of the [Memory used (in bytes)](../stateful-stream-processing/StateStoreMetrics.md#memoryUsedBytes) of the [keyToNumValues](#keyToNumValues) and [keyWithIndexToValue](#keyWithIndexToValue)
 [Custom Metrics](../stateful-stream-processing/StateStoreMetrics.md#customMetrics) | The description of all the [Custom Metrics](../stateful-stream-processing/StateStoreMetrics.md#customMetrics) of the [keyWithIndexToValue](#keyWithIndexToValue) prefixed with the [JoinSide](#joinSide)

---

`metrics` is used when:

* `OneSideHashJoiner` is requested to [commitStateAndGetMetrics](OneSideHashJoiner.md#commitStateAndGetMetrics)

<!---
## Review Me

=== [[keyToNumValues]][[keyWithIndexToValue]] KeyToNumValuesStore and KeyWithIndexToValueStore State Store Handlers -- `keyToNumValues` and `keyWithIndexToValue` Internal Properties

`SymmetricHashJoinStateManager` uses a <<KeyToNumValuesStore.md#, KeyToNumValuesStore>> (`keyToNumValues`) and a <<KeyWithIndexToValueStore.md#, KeyWithIndexToValueStore>> (`keyWithIndexToValue`) internally that are created immediately when `SymmetricHashJoinStateManager` is <<creating-instance, created>> (for a [OneSideHashJoiner](OneSideHashJoiner.md#joinStateManager)).

`keyToNumValues` and `keyWithIndexToValue` are used when `SymmetricHashJoinStateManager` is requested for the following:

* <<get, Retrieving the value rows by key>>

* <<append, Append a new value row to a given key>>

* <<removeByKeyCondition, removeByKeyCondition>>

* <<removeByValueCondition, removeByValueCondition>>

* <<commit, Commit state changes>>

* <<abortIfNeeded, Abort state changes>>

* <<metrics, Performance metrics>>

=== [[removeByKeyCondition]] `removeByKeyCondition` Method

[source, scala]
----
removeByKeyCondition(
  removalCondition: UnsafeRow => Boolean): Iterator[UnsafeRowPair]
----

`removeByKeyCondition` creates an `Iterator` of `UnsafeRowPairs` that <<removeByKeyCondition-getNext, removes keys (and associated values)>> for which the given `removalCondition` predicate holds.

[[removeByKeyCondition-allKeyToNumValues]]
`removeByKeyCondition` uses the <<keyToNumValues, KeyToNumValuesStore>> for <<KeyToNumValuesStore.md#iterator, all state keys and values (in the underlying state store)>>.

`removeByKeyCondition` is used when `OneSideHashJoiner` is requested to [remove an old state](OneSideHashJoiner.md#removeOldState) (for [JoinStateKeyWatermarkPredicate](JoinStateWatermarkPredicate.md#JoinStateKeyWatermarkPredicate)).

==== [[removeByKeyCondition-getNext]] `getNext` Internal Method (of `removeByKeyCondition` Method)

[source, scala]
----
getNext(): UnsafeRowPair
----

`getNext` goes over the keys and values in the <<removeByKeyCondition-allKeyToNumValues, allKeyToNumValues>> sequence and <<KeyToNumValuesStore.md#remove, removes keys>> (from the <<keyToNumValues, KeyToNumValuesStore>>) and the <<KeyWithIndexToValueStore.md#, corresponding values>> (from the <<keyWithIndexToValue, KeyWithIndexToValueStore>>) for which the given `removalCondition` predicate holds.

=== [[removeByValueCondition]] `removeByValueCondition` Method

[source, scala]
----
removeByValueCondition(
  removalCondition: UnsafeRow => Boolean): Iterator[UnsafeRowPair]
----

`removeByValueCondition` creates an `Iterator` of `UnsafeRowPairs` that <<removeByValueCondition-getNext, removes values (and associated keys if needed)>> for which the given `removalCondition` predicate holds.

`removeByValueCondition` is used when `OneSideHashJoiner` is requested to [remove an old state](OneSideHashJoiner.md#removeOldState) (when [JoinStateValueWatermarkPredicate](JoinStateWatermarkPredicate.md#JoinStateValueWatermarkPredicate) is used).

=== [[append]] Appending New Value Row to Key -- `append` Method

[source, scala]
----
append(
  key: UnsafeRow,
  value: UnsafeRow): Unit
----

`append` requests the <<keyToNumValues, KeyToNumValuesStore>> for the <<KeyToNumValuesStore.md#get, number of value rows for the given key>>.

In the end, `append` requests the stores for the following:

* <<keyWithIndexToValue, KeyWithIndexToValueStore>> to <<KeyWithIndexToValueStore.md#put, store the given value row>>

* <<keyToNumValues, KeyToNumValuesStore>> to <<KeyToNumValuesStore.md#put, store the given key with the number of value rows incremented>>.

`append` is used when `OneSideHashJoiner` is requested to [storeAndJoinWithOtherSide](OneSideHashJoiner.md#storeAndJoinWithOtherSide).

=== [[get]] Retrieving Value Rows By Key -- `get` Method

[source, scala]
----
get(key: UnsafeRow): Iterator[UnsafeRow]
----

`get` requests the <<keyToNumValues, KeyToNumValuesStore>> for the <<KeyToNumValuesStore.md#get, number of value rows for the given key>>.

In the end, `get` requests the <<keyWithIndexToValue, KeyWithIndexToValueStore>> to <<KeyWithIndexToValueStore.md#getAll, retrieve that number of value rows for the given key>> and leaves value rows only.

`get` is used when `OneSideHashJoiner` is requested to [storeAndJoinWithOtherSide](OneSideHashJoiner.md#storeAndJoinWithOtherSide) and [retrieving value rows for a key](OneSideHashJoiner.md#get).

=== [[commit]] Committing State (Changes) -- `commit` Method

[source, scala]
----
commit(): Unit
----

`commit` simply requests the <<keyToNumValues, keyToNumValues>> and <<keyWithIndexToValue, keyWithIndexToValue>> state store handlers to <<StateStoreHandler.md#commit, commit state changes>>.

`commit` is used when `OneSideHashJoiner` is requested to [commit state changes and get performance metrics](OneSideHashJoiner.md#commitStateAndGetMetrics).

=== [[allStateStoreNames]] `allStateStoreNames` Object Method

[source, scala]
----
allStateStoreNames(joinSides: JoinSide*): Seq[String]
----

`allStateStoreNames` simply returns the <<getStateStoreName, names of the state stores>> for all possible combinations of the given `JoinSides` and the two possible store types (e.g. <<StateStoreHandler.md#KeyToNumValuesType, keyToNumValues>> and <<StateStoreHandler.md#KeyWithIndexToValueType, keyWithIndexToValue>>).

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

* `StateStoreHandler` is requested to <<StateStoreHandler.md#getStateStore, load a state store>>

* `SymmetricHashJoinStateManager` utility is requested for <<allStateStoreNames, allStateStoreNames>> (for `StreamingSymmetricHashJoinExec` physical operator to <<physical-operators/StreamingSymmetricHashJoinExec.md#doExecute, execute and generate the runtime representation>>)
====

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| keyAttributes
a| [[keyAttributes]] Key attributes, i.e. `AttributeReferences` of the <<keySchema, key schema>>

Used exclusively in `KeyWithIndexToValueStore` when requested for the <<KeyWithIndexToValueStore.md#keyWithIndexExprs, keyWithIndexExprs>>, <<KeyWithIndexToValueStore.md#indexOrdinalInKeyWithIndexRow, indexOrdinalInKeyWithIndexRow>>, <<KeyWithIndexToValueStore.md#keyWithIndexRowGenerator, keyWithIndexRowGenerator>> and <<KeyWithIndexToValueStore.md#keyRowGenerator, keyRowGenerator>>

| keySchema
a| [[keySchema]] Key schema (`StructType`) based on the <<joinKeys, join keys>> with the names in the format of *field* and their ordinals (index)

Used when:

* `SymmetricHashJoinStateManager` is requested for the <<keyAttributes, key attributes>> (for <<KeyWithIndexToValueStore.md#, KeyWithIndexToValueStore>>)

* `KeyToNumValuesStore` is requested for the <<KeyToNumValuesStore.md#stateStore, state store>>

* `KeyWithIndexToValueStore` is requested for the <<KeyWithIndexToValueStore.md#keyWithIndexSchema, keyWithIndexSchema>> (for the internal <<KeyWithIndexToValueStore.md#stateStore, state store>>)

|===
-->
