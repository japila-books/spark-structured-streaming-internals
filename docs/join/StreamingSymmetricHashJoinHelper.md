---
title: StreamingSymmetricHashJoinHelper
---

# StreamingSymmetricHashJoinHelper Utility

`StreamingSymmetricHashJoinHelper` is a Scala object with the following utility methods:

* [getStateWatermarkPredicates](#getStateWatermarkPredicates)

=== [[getStateWatermarkPredicates]] Creating JoinStateWatermarkPredicates -- `getStateWatermarkPredicates` Object Method

[source, scala]
----
getStateWatermarkPredicates(
  leftAttributes: Seq[Attribute],
  rightAttributes: Seq[Attribute],
  leftKeys: Seq[Expression],
  rightKeys: Seq[Expression],
  condition: Option[Expression],
  eventTimeWatermark: Option[Long]): JoinStateWatermarkPredicates
----

[[getStateWatermarkPredicates-joinKeyOrdinalForWatermark]]
`getStateWatermarkPredicates` tries to find the index of the [watermark attribute](../logical-operators/EventTimeWatermark.md#delayKey) among the left keys first, and if not found, the right keys.

`getStateWatermarkPredicates` <<getOneSideStateWatermarkPredicate, determines the state watermark predicate>> for the left side of a join (for the given `leftAttributes`, the `leftKeys` and the `rightAttributes`).

`getStateWatermarkPredicates` <<getOneSideStateWatermarkPredicate, determines the state watermark predicate>> for the right side of a join (for the given `rightAttributes`, the `rightKeys` and the `leftAttributes`).

In the end, `getStateWatermarkPredicates` creates a [JoinStateWatermarkPredicates](JoinStateWatermarkPredicates.md) with the left- and right-side state watermark predicates.

NOTE: `getStateWatermarkPredicates` is used exclusively when `IncrementalExecution` is requested to [apply the state preparation rule for batch-specific configuration](../IncrementalExecution.md#state) (while optimizing query plans with [StreamingSymmetricHashJoinExec](../physical-operators/StreamingSymmetricHashJoinExec.md) physical operators).

==== [[getOneSideStateWatermarkPredicate]] Join State Watermark Predicate (for One Side of Join) -- `getOneSideStateWatermarkPredicate` Internal Method

[source, scala]
----
getOneSideStateWatermarkPredicate(
  oneSideInputAttributes: Seq[Attribute],
  oneSideJoinKeys: Seq[Expression],
  otherSideInputAttributes: Seq[Attribute]): Option[JoinStateWatermarkPredicate]
----

`getOneSideStateWatermarkPredicate` finds what attributes were used to define the [watermark attribute](../logical-operators/EventTimeWatermark.md#delayKey) (the `oneSideInputAttributes` attributes, the <<getStateWatermarkPredicates-joinKeyOrdinalForWatermark, left or right join keys>>) and creates a <<JoinStateWatermarkPredicate.md#, JoinStateWatermarkPredicate>> as follows:

* <<JoinStateWatermarkPredicate.md#JoinStateKeyWatermarkPredicate, JoinStateKeyWatermarkPredicate>> if the watermark was defined on a join key (with the watermark expression for the index of the join key expression)

* <<JoinStateWatermarkPredicate.md#JoinStateValueWatermarkPredicate, JoinStateValueWatermarkPredicate>> if the watermark was defined among the `oneSideInputAttributes` (with the [state value watermark](StreamingJoinHelper.md#getStateValueWatermark) based on the given `oneSideInputAttributes` and `otherSideInputAttributes`)

NOTE: `getOneSideStateWatermarkPredicate` creates no <<JoinStateWatermarkPredicate.md#, JoinStateWatermarkPredicate>> (`None`) for no watermark found.

NOTE: `getStateWatermarkPredicates` is used exclusively to <<getStateWatermarkPredicates, create a JoinStateWatermarkPredicates>>.
