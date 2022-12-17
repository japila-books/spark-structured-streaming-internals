# OneSideHashJoiner

`OneSideHashJoiner` manages join state of one side of a <<join/index.md#stream-stream-joins, stream-stream join>> (using <<joinStateManager, SymmetricHashJoinStateManager>>).

`OneSideHashJoiner` is <<creating-instance, created>> exclusively for <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>> physical operator (when requested to <<physical-operators/StreamingSymmetricHashJoinExec.md#processPartitions, process partitions of the left and right sides of a stream-stream join>>).

.OneSideHashJoiner and StreamingSymmetricHashJoinExec
image::images/OneSideHashJoiner.png[align="center"]

`StreamingSymmetricHashJoinExec` physical operator uses two `OneSideHashJoiners` per side of the stream-stream join (<<physical-operators/StreamingSymmetricHashJoinExec.md#processPartitions-leftSideJoiner, left>> and <<physical-operators/StreamingSymmetricHashJoinExec.md#processPartitions-rightSideJoiner, right>> sides).

`OneSideHashJoiner` uses an <<stateWatermarkPredicate, optional join state watermark predicate>> to <<removeOldState, remove old state>>.

NOTE: `OneSideHashJoiner` is a Scala private internal class of <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>> and so has full access to `StreamingSymmetricHashJoinExec` properties.

## Creating OneSideHashJoiner Instance

`OneSideHashJoiner` takes the following to be created:

* [[joinSide]] [JoinSide](SymmetricHashJoinStateManager.md#joinSide-internals)
* [[inputAttributes]] Input attributes (`Seq[Attribute]`)
* [[joinKeys]] Join keys (`Seq[Expression]`)
* [[inputIter]] Input rows (`Iterator[InternalRow]`)
* [[preJoinFilterExpr]] Optional pre-join filter Catalyst expression
* [[postJoinFilter]] Post-join filter (`(InternalRow) => Boolean`)
* <<stateWatermarkPredicate, JoinStateWatermarkPredicate>>

`OneSideHashJoiner` initializes the <<internal-registries, internal registries and counters>>.

=== [[joinStateManager]] SymmetricHashJoinStateManager -- `joinStateManager` Internal Property

[source, scala]
----
joinStateManager: SymmetricHashJoinStateManager
----

`joinStateManager` is a [SymmetricHashJoinStateManager](SymmetricHashJoinStateManager.md) that is created for a `OneSideHashJoiner` (with the <<joinSide, join side>>, the <<inputAttributes, input attributes>>, the <<joinKeys, join keys>>, and the <<stateInfo, StatefulOperatorStateInfo>> of the owning <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>>).

`joinStateManager` is used when `OneSideHashJoiner` is requested for the following:

* <<storeAndJoinWithOtherSide, storeAndJoinWithOtherSide>>

* <<get, Get the values for a given key>>

* <<removeOldState, Remove an old state>>

* <<commitStateAndGetMetrics, commitStateAndGetMetrics>>

=== [[updatedStateRowsCount]] Number of Updated State Rows -- `updatedStateRowsCount` Internal Counter

`updatedStateRowsCount` is the number the join keys and associated rows that were persisted as a join state, i.e. how many times <<storeAndJoinWithOtherSide, storeAndJoinWithOtherSide>> requested the <<joinStateManager, SymmetricHashJoinStateManager>> to [append](SymmetricHashJoinStateManager.md#append) the join key and the input row (to a join state).

`updatedStateRowsCount` is then used (via <<numUpdatedStateRows, numUpdatedStateRows>> method) for the <<physical-operators/StreamingSymmetricHashJoinExec.md#numUpdatedStateRows, numUpdatedStateRows>> performance metric.

`updatedStateRowsCount` is available via `numUpdatedStateRows` method.

[[numUpdatedStateRows]]
[source, scala]
----
numUpdatedStateRows: Long
----

NOTE: `numUpdatedStateRows` is used exclusively when `StreamingSymmetricHashJoinExec` physical operator is requested to <<physical-operators/StreamingSymmetricHashJoinExec.md#processPartitions process partitions of the left and right sides of a stream-stream join>> (and <<physical-operators/StreamingSymmetricHashJoinExec.md#processPartitions, completes>>).

=== [[stateWatermarkPredicate]] Optional Join State Watermark Predicate -- `stateWatermarkPredicate` Internal Property

[source, scala]
----
stateWatermarkPredicate: Option[JoinStateWatermarkPredicate]
----

When <<creating-instance, created>>, `OneSideHashJoiner` is given a <<JoinStateWatermarkPredicate.md#, JoinStateWatermarkPredicate>>.

`stateWatermarkPredicate` is used for the <<stateKeyWatermarkPredicateFunc, stateKeyWatermarkPredicateFunc>> (when a <<JoinStateWatermarkPredicate.md#JoinStateKeyWatermarkPredicate, JoinStateKeyWatermarkPredicate>>) and the <<stateValueWatermarkPredicateFunc, stateValueWatermarkPredicateFunc>> (when a <<JoinStateWatermarkPredicate.md#JoinStateValueWatermarkPredicate, JoinStateValueWatermarkPredicate>>) that are both used when `OneSideHashJoiner` is requested to <<removeOldState, removeOldState>>.

=== [[storeAndJoinWithOtherSide]] `storeAndJoinWithOtherSide` Method

[source, scala]
----
storeAndJoinWithOtherSide(
  otherSideJoiner: OneSideHashJoiner)(
  generateJoinedRow: (InternalRow, InternalRow) => JoinedRow): Iterator[InternalRow]
----

`storeAndJoinWithOtherSide` tries to find the [watermark attribute](../logical-operators/EventTimeWatermark.md#delayKey) among the [input attributes](#inputAttributes).

`storeAndJoinWithOtherSide` creates a [watermark expression](../physical-operators/WatermarkSupport.md#watermarkExpression) (for the watermark attribute and the current [event-time watermark](../physical-operators/StreamingSymmetricHashJoinExec.md#eventTimeWatermark)).

[[storeAndJoinWithOtherSide-nonLateRows]]
With the watermark attribute found, `storeAndJoinWithOtherSide` generates a new predicate for the watermark expression and the <<inputAttributes, input attributes>> that is then used to filter out (_exclude_) late rows from the <<inputIter, input>>. Otherwise, the input rows are left unchanged (i.e. no rows are considered late and excluded).

[[storeAndJoinWithOtherSide-nonLateRows-flatMap]]
For every <<inputIter, input row>> (possibly <<storeAndJoinWithOtherSide-nonLateRows, watermarked>>), `storeAndJoinWithOtherSide` applies the <<preJoinFilter, preJoinFilter>> predicate and branches off per result (<<preJoinFilter-true, true>> or <<preJoinFilter-false, false>>).

NOTE: `storeAndJoinWithOtherSide` is used when `StreamingSymmetricHashJoinExec` physical operator is requested to <<physical-operators/StreamingSymmetricHashJoinExec.md#processPartitions, process partitions of the left and right sides of a stream-stream join>>.

==== [[preJoinFilter-true]] `preJoinFilter` Predicate Positive (`true`)

When the <<preJoinFilter, preJoinFilter>> predicate succeeds on an input row, `storeAndJoinWithOtherSide` extracts the join key (using the <<keyGenerator, keyGenerator>>) and requests the given `OneSideHashJoiner` (`otherSideJoiner`) for the <<joinStateManager, SymmetricHashJoinStateManager>> that is in turn requested for the state values for the extracted join key. The values are then processed (_mapped over_) using the given `generateJoinedRow` function and then filtered by the <<postJoinFilter, post-join filter>>.

`storeAndJoinWithOtherSide` uses the <<stateKeyWatermarkPredicateFunc, stateKeyWatermarkPredicateFunc>> (on the extracted join key) and the <<stateValueWatermarkPredicateFunc, stateValueWatermarkPredicateFunc>> (on the current input row) to determine whether to request the <<joinStateManager, SymmetricHashJoinStateManager>> to [append](SymmetricHashJoinStateManager.md#append) the key and the input row (to a join state). If so, `storeAndJoinWithOtherSide` increments the <<updatedStateRowsCount, updatedStateRowsCount>> counter.

==== [[preJoinFilter-false]] `preJoinFilter` Predicate Negative (`false`)

When the <<preJoinFilter, preJoinFilter>> predicate fails on an input row, `storeAndJoinWithOtherSide` creates a new `Iterator[InternalRow]` of joined rows per <<joinSide, join side>> and <<physical-operators/StreamingSymmetricHashJoinExec.md#joinType, type>>:

* For [LeftSide](SymmetricHashJoinStateManager.md#LeftSide) and `LeftOuter`, the join row is the current row with the values of the right side all `null` (`nullRight`)

* For [RightSide](SymmetricHashJoinStateManager.md#RightSide) and `RightOuter`, the join row is the current row with the values of the left side all `null` (`nullLeft`)

* For all other combinations, the iterator is simply empty (that will be removed from the output by the outer <<storeAndJoinWithOtherSide-nonLateRows-flatMap, nonLateRows.flatMap>>).

=== [[removeOldState]] Removing Old State -- `removeOldState` Method

[source, scala]
----
removeOldState(): Iterator[UnsafeRowPair]
----

`removeOldState` branches off per the <<stateWatermarkPredicate, JoinStateWatermarkPredicate>>:

* For <<JoinStateWatermarkPredicate.md#JoinStateKeyWatermarkPredicate, JoinStateKeyWatermarkPredicate>>, `removeOldState` requests the <<joinStateManager, SymmetricHashJoinStateManager>> to [removeByKeyCondition](SymmetricHashJoinStateManager.md#removeByKeyCondition) (with the <<stateKeyWatermarkPredicateFunc, stateKeyWatermarkPredicateFunc>>)

* For <<JoinStateWatermarkPredicate.md#JoinStateValueWatermarkPredicate, JoinStateValueWatermarkPredicate>>, `removeOldState` requests the <<joinStateManager, SymmetricHashJoinStateManager>> to [removeByValueCondition](SymmetricHashJoinStateManager.md#removeByValueCondition) (with the <<stateValueWatermarkPredicateFunc, stateValueWatermarkPredicateFunc>>)

* For any other predicates, `removeOldState` returns an empty iterator (no rows to process)

NOTE: `removeOldState` is used exclusively when `StreamingSymmetricHashJoinExec` physical operator is requested to <<physical-operators/StreamingSymmetricHashJoinExec.md#processPartitions, process partitions of the left and right sides of a stream-stream join>>.

=== [[get]] Retrieving Value Rows For Key -- `get` Method

[source, scala]
----
get(key: UnsafeRow): Iterator[UnsafeRow]
----

`get` simply requests the <<joinStateManager, SymmetricHashJoinStateManager>> to [retrieve value rows for the key](SymmetricHashJoinStateManager.md#get).

NOTE: `get` is used exclusively when `StreamingSymmetricHashJoinExec` physical operator is requested to <<physical-operators/StreamingSymmetricHashJoinExec.md#processPartitions, process partitions of the left and right sides of a stream-stream join>>.

=== [[commitStateAndGetMetrics]] Committing State (Changes) and Requesting Performance Metrics -- `commitStateAndGetMetrics` Method

[source, scala]
----
commitStateAndGetMetrics(): StateStoreMetrics
----

`commitStateAndGetMetrics` simply requests the <<joinStateManager, SymmetricHashJoinStateManager>> to [commit](SymmetricHashJoinStateManager.md#commit) followed by requesting for the [performance metrics](SymmetricHashJoinStateManager.md#metrics).

`commitStateAndGetMetrics` is used when `StreamingSymmetricHashJoinExec` physical operator is requested to <<physical-operators/StreamingSymmetricHashJoinExec.md#processPartitions, process partitions of the left and right sides of a stream-stream join>>.

## Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| keyGenerator
a| [[keyGenerator]]

[source, scala]
----
keyGenerator: UnsafeProjection
----

Function to project (_extract_) join keys from an input row

Used when...FIXME

| preJoinFilter
a| [[preJoinFilter]]

[source, scala]
----
preJoinFilter: InternalRow => Boolean
----

Used when...FIXME

| stateKeyWatermarkPredicateFunc
a| [[stateKeyWatermarkPredicateFunc]]

[source, scala]
----
stateKeyWatermarkPredicateFunc: InternalRow => Boolean
----

Predicate for late rows based on the <<stateWatermarkPredicate, stateWatermarkPredicate>>

Used for the following:

* <<storeAndJoinWithOtherSide, storeAndJoinWithOtherSide>> (and check out whether to [append a row](SymmetricHashJoinStateManager.md#append) to the [SymmetricHashJoinStateManager](#joinStateManager))

* <<removeOldState, removeOldState>>

| stateValueWatermarkPredicateFunc
a| [[stateValueWatermarkPredicateFunc]]

[source, scala]
----
stateValueWatermarkPredicateFunc: InternalRow => Boolean
----

Predicate for late rows based on the <<stateWatermarkPredicate, stateWatermarkPredicate>>

Used for the following:

* <<storeAndJoinWithOtherSide, storeAndJoinWithOtherSide>> (and check out whether to [append a row](SymmetricHashJoinStateManager.md#append) to the <<joinStateManager, SymmetricHashJoinStateManager>>)

* <<removeOldState, removeOldState>>

|===
