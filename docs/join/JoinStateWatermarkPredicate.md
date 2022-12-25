# JoinStateWatermarkPredicate

`JoinStateWatermarkPredicate` is an [abstraction](#contract) of [join state watermark predicates](#implementations).

A [concrete JoinStateWatermarkPredicate](#implementations) is created using [StreamingSymmetricHashJoinHelper](StreamingSymmetricHashJoinHelper.md#getOneSideStateWatermarkPredicate) utility (for [planning a StreamingSymmetricHashJoinExec physical operator for execution with execution-specific configuration](../IncrementalExecution.md#state)).

`JoinStateWatermarkPredicate` can be used to create a [OneSideHashJoiner](OneSideHashJoiner.md#stateWatermarkPredicate) (and [JoinStateWatermarkPredicates](JoinStateWatermarkPredicates.md)).

??? note "Sealed Trait"
    `JoinStateWatermarkPredicate` is a Scala **sealed trait** which means that all of the implementations are in the same compilation unit (a single file).

    Learn more in the [Scala Language Specification]({{ scala.spec }}/05-classes-and-objects.html#sealed).

## Contract

### <span id="desc"> Description

```scala
desc: String
```

JoinStateWatermarkPredicate | Description
----------------------------|------------
 [JoinStateKeyWatermarkPredicate](#JoinStateKeyWatermarkPredicate) | key predicate
 [JoinStateValueWatermarkPredicate](#JoinStateValueWatermarkPredicate) | value predicate

Used when:

* `JoinStateWatermarkPredicate` is requested for [string representation](#toString)

### <span id="expr"> Watermark Expression

```scala
expr: Expression
```

A Catalyst `Expression` ([Spark SQL]({{ book.spark_sql }}/expressions/Expression))

Used when:

* `JoinStateWatermarkPredicate` is requested for [string representation](#toString)
* `OneSideHashJoiner` is [created](OneSideHashJoiner.md#stateKeyWatermarkPredicateFunc)

## Implementations

### <span id="JoinStateKeyWatermarkPredicate"> JoinStateKeyWatermarkPredicate

Watermark predicate on state keys (i.e., when the [streaming watermark](../watermark/index.md) is defined either on the [left](../physical-operators/StreamingSymmetricHashJoinExec.md#leftKeys) or [right](../physical-operators/StreamingSymmetricHashJoinExec.md#rightKeys) join keys)

Created with a watermark expression (on left or right join keys) when:

* `StreamingSymmetricHashJoinHelper` is requested for a [JoinStateWatermarkPredicates](StreamingSymmetricHashJoinHelper.md#getStateWatermarkPredicates) for the left and right side of a stream-stream join (when `IncrementalExecution` is requested to optimize a query plan with a [StreamingSymmetricHashJoinExec](../physical-operators/StreamingSymmetricHashJoinExec.md) physical operator)

Used when:

* `OneSideHashJoiner` is requested for the [stateKeyWatermarkPredicateFunc](OneSideHashJoiner.md#stateKeyWatermarkPredicateFunc) (to [remove an old state](OneSideHashJoiner.md#removeOldState))

### <span id="JoinStateValueWatermarkPredicate"> JoinStateValueWatermarkPredicate

Watermark predicate on state values

Created with a watermark expression (on left or right join keys) when:

* `StreamingSymmetricHashJoinHelper` is requested for a [JoinStateWatermarkPredicates](StreamingSymmetricHashJoinHelper.md#getStateWatermarkPredicates) for the left and right side of a stream-stream join (when `IncrementalExecution` is requested to optimize a query plan with a [StreamingSymmetricHashJoinExec](../physical-operators/StreamingSymmetricHashJoinExec.md) physical operator)

Used when:

* `OneSideHashJoiner` is requested for the [stateValueWatermarkPredicateFunc](OneSideHashJoiner.md#stateValueWatermarkPredicateFunc) (to [remove an old state](OneSideHashJoiner.md#removeOldState))

## <span id="toString"> String Representation

```scala
toString: String
```

`toString` is part of the `java.lang.Object` abstraction.

---

`toString` uses the [desc](#desc) and [expr](#expr) to build the following string representation:

```text
[desc]: [expr]
```
