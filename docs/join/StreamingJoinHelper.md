# StreamingJoinHelper

## <span id="getStateValueWatermark"> Calculating State Watermark for Values

```scala
getStateValueWatermark(
  attributesToFindStateWatermarkFor: AttributeSet,
  attributesWithEventWatermark: AttributeSet,
  joinCondition: Option[Expression],
  eventWatermark: Option[Long]): Option[Long]
```

`getStateValueWatermark` returns `None` when one of the following holds:

* Either `joinCondition` or `eventWatermark` is empty
* There are no attributes with [watermark delay metadata marker](../logical-operators/EventTimeWatermark.md#delayKey) among the given `attributesWithEventWatermark`

`getStateValueWatermark` splits `And` predicates in the given `joinCondition` expression into a series of the following expressions (and skips the others):

* `LessThan`
* `LessThanOrEqual`
* `GreaterThan`
* `GreaterThanOrEqual`

`getStateValueWatermark`...FIXME

---

`getStateValueWatermark` is used when:

* `UnsupportedOperationChecker` is requested to [checkForStreamStreamJoinWatermark](../UnsupportedOperationChecker.md#checkForStreamStreamJoinWatermark)
* `StreamingSymmetricHashJoinHelper` is requested for [state watermark predicates](StreamingSymmetricHashJoinHelper.md#getStateWatermarkPredicates)

### <span id="getStateWatermarkSafely"> getStateWatermarkSafely

```scala
getStateWatermarkSafely(
  l: Expression,
  r: Expression): Option[Long]
```

`getStateWatermarkSafely` [getStateWatermarkFromLessThenPredicate](#getStateWatermarkFromLessThenPredicate).

In case of a non-fatal exception, `getStateWatermarkSafely` prints out the following WARN message to the logs and returns `None` (and hence the "Safely" suffix):

```text
Error trying to extract state constraint from condition [joinCondition]
```

### <span id="getStateWatermarkFromLessThenPredicate"> getStateWatermarkFromLessThenPredicate

```scala
getStateWatermarkFromLessThenPredicate(
  leftExpr: Expression,
  rightExpr: Expression,
  attributesToFindStateWatermarkFor: AttributeSet,
  attributesWithEventWatermark: AttributeSet,
  eventWatermark: Option[Long]): Option[Long]
```

`getStateWatermarkFromLessThenPredicate`...FIXME

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.catalyst.analysis.StreamingJoinHelper` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.catalyst.analysis.StreamingJoinHelper=ALL
```

Refer to [Logging](../spark-logging.md).
