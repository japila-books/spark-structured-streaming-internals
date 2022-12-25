---
title: StreamingSymmetricHashJoinHelper
---

# StreamingSymmetricHashJoinHelper Utility

## <span id="getStateWatermarkPredicates"> Join State Watermark Predicates

```scala
getStateWatermarkPredicates(
  leftAttributes: Seq[Attribute],
  rightAttributes: Seq[Attribute],
  leftKeys: Seq[Expression],
  rightKeys: Seq[Expression],
  condition: Option[Expression],
  eventTimeWatermark: Option[Long]): JoinStateWatermarkPredicates
```

`getStateWatermarkPredicates` creates a [JoinStateWatermarkPredicates](JoinStateWatermarkPredicates.md) with the [JoinStateWatermarkPredicate](JoinStateWatermarkPredicate.md)s for the left and right side of a join (if defined).

---

`getStateWatermarkPredicates` finds the index of the first column (attribute) with the [watermark delay metadata marker](../logical-operators/EventTimeWatermark.md#delayKey) among the given `leftKeys` first, and if not found, among the given `rightKeys`. `getStateWatermarkPredicates` may find no column.

`getStateWatermarkPredicates` [determines the state watermark predicate](#getOneSideStateWatermarkPredicate) (a [JoinStateWatermarkPredicate](JoinStateWatermarkPredicate.md)) for the `leftStateWatermarkPredicate` and `rightStateWatermarkPredicate` sides of a join.

JoinStateWatermarkPredicate | oneSideInputAttributes | oneSideJoinKeys | otherSideInputAttributes
----------------------------|------------------------|-----------------|-------------------------
 `leftStateWatermarkPredicate`  | `leftAttributes`  | `leftKeys`  | `rightAttributes`
 `rightStateWatermarkPredicate` | `rightAttributes` | `rightKeys` | `leftAttributes`

---

`getStateWatermarkPredicates` is used when:

* `IncrementalExecution` is requested for the [state preparations rules](../IncrementalExecution.md#state) (while optimizing query plans with [StreamingSymmetricHashJoinExec](../physical-operators/StreamingSymmetricHashJoinExec.md) physical operators)

## <span id="getOneSideStateWatermarkPredicate"> Join State Watermark Predicate (for One Side of Join)

```scala
getOneSideStateWatermarkPredicate(
  oneSideInputAttributes: Seq[Attribute],
  oneSideJoinKeys: Seq[Expression],
  otherSideInputAttributes: Seq[Attribute]): Option[JoinStateWatermarkPredicate]
```

### <span id="getOneSideStateWatermarkPredicate-isWatermarkDefinedOnJoinKey"> Watermark on Join Keys

With a watermark defined on one of the join keys (`leftKeys` or `rightKeys` of [getStateWatermarkPredicates](#getStateWatermarkPredicates)), `getOneSideStateWatermarkPredicate` creates a [JoinStateKeyWatermarkPredicate](../join/JoinStateWatermarkPredicate.md#JoinStateKeyWatermarkPredicate) with a [watermark (eviction) expression](../physical-operators/WatermarkSupport.md#watermarkExpression) for the following:

* A `BoundReference` ([Spark SQL]({{ book.spark_sql }}/expressions/BoundReference)) for the key with watermark
* The given `eventTimeWatermark` (of [getStateWatermarkPredicates](#getStateWatermarkPredicates))

### <span id="getOneSideStateWatermarkPredicate-isWatermarkDefinedOnInput"> Watermark on Input

With a watermark defined on the given `oneSideInputAttributes`, `getOneSideStateWatermarkPredicate` creates a [JoinStateValueWatermarkPredicate](../join/JoinStateWatermarkPredicate.md#JoinStateValueWatermarkPredicate) with a [watermark (eviction) expression](../physical-operators/WatermarkSupport.md#watermarkExpression) for the following:

* The `Attribute` among the given `oneSideInputAttributes` with the [watermark delay metadata marker](../logical-operators/EventTimeWatermark.md#delayKey)
* [getStateValueWatermark](StreamingJoinHelper.md#getStateValueWatermark)

### <span id="getOneSideStateWatermarkPredicate-no-watermark"> No Watermark

`getOneSideStateWatermarkPredicate` creates no [JoinStateWatermarkPredicate](JoinStateWatermarkPredicate.md) (`None`) when no watermark was found.
