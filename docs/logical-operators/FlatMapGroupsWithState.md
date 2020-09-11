# FlatMapGroupsWithState Unary Logical Operator

`FlatMapGroupsWithState` is a unary logical operator that represents the following operators in a logical query plan of a streaming query:

* [KeyValueGroupedDataset.mapGroupsWithState](../spark-sql-streaming-KeyValueGroupedDataset.md#mapGroupsWithState)

* [KeyValueGroupedDataset.flatMapGroupsWithState](../spark-sql-streaming-KeyValueGroupedDataset.md#flatMapGroupsWithState)

!!! note
    A unary logical operator (`UnaryNode`) is a logical operator with a single <<child, child>> logical operator.

    Read up on [UnaryNode](https://jaceklaskowski.github.io/mastering-spark-sql-book/logical-operators/LogicalPlan#UnaryNode) (and logical operators in general) in [The Internals of Spark SQL](https://jaceklaskowski.github.io/mastering-spark-sql-book/) online book.

## Execution Planning

`FlatMapGroupsWithState` is resolved (_planned_) to:

* [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md) unary physical operator for streaming datasets (in [FlatMapGroupsWithStateStrategy](../spark-sql-streaming-FlatMapGroupsWithStateStrategy.md) execution planning strategy)

* `MapGroupsExec` physical operator for batch datasets (in `BasicOperators` execution planning strategy)

## Creating Instance

`FlatMapGroupsWithState` takes the following to be created:

* <span id="func"> State function (`(Any, Iterator[Any], LogicalGroupState[Any]) => Iterator[Any]`)
* <span id="keyDeserializer"> Catalyst Expression for keys
* <span id="valueDeserializer"> Catalyst Expression for values
* <span id="groupingAttributes"> Grouping Attributes
* <span id="dataAttributes"> Data Attributes
* <span id="outputObjAttr"> Output Object Attribute
* <span id="stateEncoder"> State `ExpressionEncoder`
* <span id="outputMode"> [OutputMode](../spark-sql-streaming-OutputMode.md)
* <span id="isMapGroupsWithState"> `isMapGroupsWithState` flag (default: `false`)
* <span id="timeout"> [GroupStateTimeout](../spark-sql-streaming-GroupStateTimeout.md)
* <span id="child"> Child logical operator

`FlatMapGroupsWithState` is created (using [apply](#apply) factory method) for [KeyValueGroupedDataset.mapGroupsWithState](../spark-sql-streaming-KeyValueGroupedDataset.md#mapGroupsWithState) and [KeyValueGroupedDataset.flatMapGroupsWithState](../spark-sql-streaming-KeyValueGroupedDataset.md#flatMapGroupsWithState) operators.

## <span id="apply"> Creating SerializeFromObject with FlatMapGroupsWithState

```scala
apply[K: Encoder, V: Encoder, S: Encoder, U: Encoder](
  func: (Any, Iterator[Any], LogicalGroupState[Any]) => Iterator[Any],
  groupingAttributes: Seq[Attribute],
  dataAttributes: Seq[Attribute],
  outputMode: OutputMode,
  isMapGroupsWithState: Boolean,
  timeout: GroupStateTimeout,
  child: LogicalPlan): LogicalPlan
```

`apply` creates a `SerializeFromObject` logical operator with a `FlatMapGroupsWithState` as its child logical operator.

Internally, `apply` creates `SerializeFromObject` object consumer (aka unary logical operator) with `FlatMapGroupsWithState` logical plan.

Internally, `apply` finds `ExpressionEncoder` for the type `S` and creates a `FlatMapGroupsWithState` with `UnresolvedDeserializer` for the types `K` and `V`.

In the end, `apply` creates a `SerializeFromObject` object consumer with the `FlatMapGroupsWithState`.

`apply` is used for [KeyValueGroupedDataset.mapGroupsWithState](../spark-sql-streaming-KeyValueGroupedDataset.md#mapGroupsWithState) and [KeyValueGroupedDataset.flatMapGroupsWithState](../spark-sql-streaming-KeyValueGroupedDataset.md#flatMapGroupsWithState) operators.
