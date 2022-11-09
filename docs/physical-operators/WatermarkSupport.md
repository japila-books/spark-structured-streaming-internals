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

## <span id="watermarkExpression"> Watermark Expression

```scala
watermarkExpression: Option[Expression]
```

??? note "Lazy Value"
    `watermarkExpression` is a Scala **lazy value** to guarantee that the code to initialize it is executed once only (when accessed for the first time) and the computed value never changes afterwards.

    Learn more in the [Scala Language Specification]({{ scala.spec }}/05-classes-and-objects.html#lazy).

`watermarkExpression` is a `LessThanOrEqual` ([Spark SQL]({{ book.spark_sql }}/expressions/LessThanOrEqual)) expression.

!!! note
    Use [Dataset.withWatermark](../operators/withWatermark.md) operator to specify a watermark expression.

`watermarkExpression` [creates a watermark expression](#watermarkExpression-utility) for the following:

* [child operator](#child)'s output expressions (from the output schema of the [child operator](#child)) with [spark.watermarkDelayMs](../logical-operators/EventTimeWatermark.md#delayKey) metadata key
* [eventTimeWatermark](#eventTimeWatermark)

---

`watermarkExpression` is used when:

* `WatermarkSupport` is requested for the [watermark predicates](#watermark-predicates) (for [keys](#watermarkPredicateForKeys) and [data](#watermarkPredicateForData))

## Watermark Predicates

### <span id="watermarkPredicateForKeys"> For Keys

```scala
watermarkPredicateForKeys: Option[BasePredicate]
```

??? note "Lazy Value"
    `watermarkPredicateForKeys` is a Scala **lazy value** to guarantee that the code to initialize it is executed once only (when accessed for the first time) and the computed value never changes afterwards.

    Learn more in the [Scala Language Specification]({{ scala.spec }}/05-classes-and-objects.html#lazy).

`watermarkPredicateForKeys` is a `BasePredicate` ([Spark SQL]({{ book.spark_sql }}/expressions/BasePredicate)) for the [watermark expression](#watermarkExpression) (if defined) and the [key expressions](#keyExpressions) with [spark.watermarkDelayMs](../logical-operators/EventTimeWatermark.md#delayKey) metadata key.

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

`watermarkPredicateForData` is a `BasePredicate` ([Spark SQL]({{ book.spark_sql }}/expressions/BasePredicate)) for the [watermark expression](#watermarkExpression) (if defined).

!!! note
    `watermarkPredicateForData` is created only when the [watermark expression](#watermarkExpression) is defined.

---

`watermarkPredicateForData` is used when:

* `FlatMapGroupsWithStateExec` is requested to [processDataWithPartition](FlatMapGroupsWithStateExec.md#processDataWithPartition)
* `StateStoreSaveExec` is [executed](StateStoreSaveExec.md#doExecute) (with [Append](StateStoreSaveExec.md#doExecute-Append) output mode)
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

## <span id="watermarkExpression-utility"> watermarkExpression

```scala
watermarkExpression(
  optionalWatermarkExpression: Option[Expression],
  optionalWatermarkMs: Option[Long]): Option[Expression]
```

`watermarkExpression` is `None` (undefined) when neither the given `optionalWatermarkExpression` nor the `optionalWatermarkMs` is undefined.

In other words, `watermarkExpression` creates an `Expression` ([LessThanOrEqual]({{ book.spark_sql }}/expressions/LessThanOrEqual), precisely) when both `optionalWatermarkExpression` and `optionalWatermarkMs` are specified.

`watermarkExpression` takes the watermark attribute from the given `optionalWatermarkExpression`.

For the watermark attribute of `StructType` (which means it is a window), `watermarkExpression` uses the end of the window to `LessThanOrEqual` with the `optionalWatermarkMs`.

For the watermark attribute of non-`StructType`, `watermarkExpression` uses it to `LessThanOrEqual` with the `optionalWatermarkMs`.

??? note "FIXME Demo"

    ```scala
    import org.apache.spark.sql.execution.streaming.WatermarkSupport

    import org.apache.spark.sql.functions.window
    val w = window(timeColumn = $"time", windowDuration = "5 seconds")

    val optionalWatermarkExpression = Some(w.expr)
    val optionalWatermarkMs = Some(5L)

    WatermarkSupport.watermarkExpression(optionalWatermarkExpression, optionalWatermarkMs)

    // FIXME Resolve Catalyst expressions
    // org.apache.spark.sql.catalyst.analysis.UnresolvedException: Invalid call to dataType on unresolved object
    //   at org.apache.spark.sql.catalyst.analysis.UnresolvedAttribute.dataType(unresolved.scala:137)
    //   at org.apache.spark.sql.catalyst.expressions.TimeWindow.dataType(TimeWindow.scala:101)
    //   at org.apache.spark.sql.catalyst.expressions.Alias.dataType(namedExpressions.scala:166)
    //   at org.apache.spark.sql.execution.streaming.WatermarkSupport$.watermarkExpression(statefulOperators.scala:276)
    //   ... 52 elided
    ```

---

`watermarkExpression` is used when:

* `WatermarkSupport` is requested for the [watermark expression](#watermarkExpression)
* `StreamingSymmetricHashJoinExec.OneSideHashJoiner` is requested to [storeAndJoinWithOtherSide](../streaming-join/OneSideHashJoiner.md#storeAndJoinWithOtherSide)
* `StreamingSymmetricHashJoinHelper` is requested to [getStateWatermarkPredicates](../streaming-join/StreamingSymmetricHashJoinHelper.md#getStateWatermarkPredicates)
