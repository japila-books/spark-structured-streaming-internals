# WatermarkSupport Physical Operators

`WatermarkSupport` is an [extension](#contract) of the `SparkPlan` ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan)) abstraction for [physical operators](#implementations) with support for [streaming watermark](../watermark/index.md).

## Contract

### <span id="child"> Child Physical Operator

```scala
child: SparkPlan
```

Used when:

* `WatermarkSupport` is requested for the [watermark expression](#watermarkExpression) and [watermark predicate for data](#watermarkPredicateForData)

### <span id="eventTimeWatermark"> Event-Time Watermark

```scala
eventTimeWatermark: Option[Long]
```

Current value of watermark

Used when:

* `WatermarkSupport` is requested for the [watermark expression](#watermarkExpression)

### <span id="keyExpressions"> Key Expressions

```scala
keyExpressions: Seq[Attribute]
```

Grouping keys (in [FlatMapGroupsWithStateExec](FlatMapGroupsWithStateExec.md#keyExpressions)), duplicate keys (in [StreamingDeduplicateExec](StreamingDeduplicateExec.md#keyExpressions)) or key attributes (in [StateStoreSaveExec](StateStoreSaveExec.md#keyExpressions)) with at most one that may have [spark.watermarkDelayMs](../logical-operators/EventTimeWatermark.md#watermarkDelayMs) watermark attribute in metadata

Used when:

* `WatermarkSupport` is requested for the [watermark predicate for keys](#watermarkPredicateForKeys) (to match rows older than the event time watermark)

## Implementations

* [FlatMapGroupsWithStateExec](FlatMapGroupsWithStateExec.md)
* [SessionWindowStateStoreRestoreExec](SessionWindowStateStoreRestoreExec.md)
* [SessionWindowStateStoreSaveExec](SessionWindowStateStoreSaveExec.md)
* [StateStoreSaveExec](StateStoreSaveExec.md)
* [StreamingDeduplicateExec](StreamingDeduplicateExec.md)

## Watermark Predicates

### <span id="watermarkPredicateForKeys"> For Keys

```scala
watermarkPredicateForKeys: Option[BasePredicate]
```

??? note "Lazy Value"
    `watermarkPredicateForKeys` is a Scala **lazy value** to guarantee that the code to initialize it is executed once only (when accessed for the first time) and the computed value never changes afterwards.

    Learn more in the [Scala Language Specification]({{ scala.spec }}/05-classes-and-objects.html#lazy).

`watermarkPredicateForKeys` is a `Predicate` (Spark SQL) based on the [watermarkExpression](#watermarkExpression) for the [key expressions](#keyExpressions) if there is at least one key expression with [spark.watermarkDelayMs](../logical-operators/EventTimeWatermark.md#delayKey) metadata key.

---

`watermarkPredicateForKeys` is used when:

* `WatermarkSupport` is requested to [removeKeysOlderThanWatermark](#removeKeysOlderThanWatermark)
* `StateStoreSaveExec` is [executed](StateStoreSaveExec.md#doExecute) (with [Append](StateStoreSaveExec.md#doExecute-Append) output mode)

### <span id="watermarkPredicateForData"> For Data

```scala
watermarkPredicateForData: Option[BasePredicate]
```

??? note "Lazy Value"
    `watermarkPredicateForData` is a Scala **lazy value** to guarantee that the code to initialize it is executed once only (when accessed for the first time) and the computed value never changes afterwards.

    Learn more in the [Scala Language Specification]({{ scala.spec }}/05-classes-and-objects.html#lazy).

`watermarkPredicateForData` is a `Predicate` (Spark SQL) based on the [watermarkExpression](#watermarkExpression) and the output schema of the [child operator](#child) (to match rows older than the event-time watermark).

---

`watermarkPredicateForData` is used when:

* `FlatMapGroupsWithStateExec` is requested to [processDataWithPartition](FlatMapGroupsWithStateExec.md#processDataWithPartition)
* `StateStoreSaveExec` is [executed](StateStoreSaveExec.md#doExecute)
* `SessionWindowStateStoreRestoreExec` is [executed](SessionWindowStateStoreRestoreExec.md#doExecute)
* `SessionWindowStateStoreSaveExec` is [executed](SessionWindowStateStoreSaveExec.md#doExecute)
* `StreamingDeduplicateExec` is [executed](StreamingDeduplicateExec.md#doExecute)

## <span id="removeKeysOlderThanWatermark"> removeKeysOlderThanWatermark

```scala
removeKeysOlderThanWatermark(
  store: StateStore): Unit
removeKeysOlderThanWatermark(
  storeManager: StreamingAggregationStateManager,
  store: StateStore): Unit
```

`removeKeysOlderThanWatermark`...FIXME

---

`removeKeysOlderThanWatermark` is used when:

* `StreamingDeduplicateExec` is [executed](StreamingDeduplicateExec.md#doExecute) (just before finishing up)
* `StateStoreSaveExec` is [executed](StateStoreSaveExec.md#doExecute) (just before finishing up in [Update](StateStoreSaveExec.md#doExecute-Update) output mode)

## <span id="watermarkExpression"> Watermark Expression

```scala
watermarkExpression: Option[Expression]
```

??? note "Lazy Value"
    `watermarkExpression` is a Scala **lazy value** to guarantee that the code to initialize it is executed once only (when accessed for the first time) and the computed value never changes afterwards.

    Learn more in the [Scala Language Specification]({{ scala.spec }}/05-classes-and-objects.html#lazy).

`watermarkExpression` is a Catalyst `Expression` ([Spark SQL]({{ book.spark_sql }}/expressions/Expression)) to find rows older than the [event-time watermark](../watermark/index.md).

!!! note
    Use [withWatermark](../operators/withWatermark.md) operator to specify a watermark expression.

`watermarkExpression` [creates a watermark expression](#watermarkExpression-helper) for the following:

* [child operator](#child)'s output expressions (in the output schema of the [child operator](#child)) with [EventTimeWatermark.delayKey](../logical-operators/EventTimeWatermark.md#delayKey) metadata key
* [eventTimeWatermark](#eventTimeWatermark)

---

`watermarkExpression` is used when:

* `FlatMapGroupsWithStateExec` is [executed](FlatMapGroupsWithStateExec.md#doExecute) (with `EventTimeTimeout`)
* `WatermarkSupport` is requested for [watermarkPredicateForKeys](#watermarkPredicateForKeys), [watermarkPredicateForData](#watermarkPredicateForData)
