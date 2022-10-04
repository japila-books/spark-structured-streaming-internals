# FlatMapGroupsWithState Unary Logical Operator

`FlatMapGroupsWithState` is a unary logical operator ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan#UnaryNode)) that represents the following operators in a logical query plan of a streaming query:

* [KeyValueGroupedDataset.mapGroupsWithState](../KeyValueGroupedDataset.md#mapGroupsWithState)

* [KeyValueGroupedDataset.flatMapGroupsWithState](../KeyValueGroupedDataset.md#flatMapGroupsWithState)

## Execution Planning

`FlatMapGroupsWithState` is resolved (_planned_) to:

* [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md) unary physical operator for streaming datasets (in [FlatMapGroupsWithStateStrategy](../execution-planning-strategies/FlatMapGroupsWithStateStrategy.md) execution planning strategy)

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
* <span id="outputMode"> [OutputMode](../OutputMode.md)
* <span id="isMapGroupsWithState"> `isMapGroupsWithState` flag (default: `false`)
* <span id="timeout"> [GroupStateTimeout](../GroupStateTimeout.md)
* <span id="child"> Child logical operator

`FlatMapGroupsWithState` is created (using [apply](#apply) factory method) for [KeyValueGroupedDataset.mapGroupsWithState](../KeyValueGroupedDataset.md#mapGroupsWithState) and [KeyValueGroupedDataset.flatMapGroupsWithState](../KeyValueGroupedDataset.md#flatMapGroupsWithState) operators.

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

`apply` is used for [KeyValueGroupedDataset.mapGroupsWithState](../KeyValueGroupedDataset.md#mapGroupsWithState) and [KeyValueGroupedDataset.flatMapGroupsWithState](../KeyValueGroupedDataset.md#flatMapGroupsWithState) operators.
