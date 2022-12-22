# FlatMapGroupsWithState Logical Operator

`FlatMapGroupsWithState` is a binary logical operator ([Spark SQL]({{ book.spark_sql }}/logical-operators/LogicalPlan#BinaryNode)) that represents the following high-level operators in (a logical query plan of) a structured query:

* [KeyValueGroupedDataset.flatMapGroupsWithState](../operators/flatMapGroupsWithState.md)
* [KeyValueGroupedDataset.mapGroupsWithState](../operators/mapGroupsWithState.md)

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
* <span id="hasInitialState"> `hasInitialState` flag (default: `false`)
* <span id="initialStateGroupAttrs"> Initial State Group Attributes
* <span id="initialStateDataAttrs"> Initial State Data Attributes
* <span id="initialStateDeserializer"> Initial State Deserializer
* <span id="initialState"> `LogicalPlan` of the initial state
* <span id="child"> Child logical operator

## <span id="apply"> Creating FlatMapGroupsWithState (under SerializeFromObject)

```scala
apply[K: Encoder, V: Encoder, S: Encoder, U: Encoder](
  func: (Any, Iterator[Any], LogicalGroupState[Any]) => Iterator[Any],
  groupingAttributes: Seq[Attribute],
  dataAttributes: Seq[Attribute],
  outputMode: OutputMode,
  isMapGroupsWithState: Boolean,
  timeout: GroupStateTimeout,
  child: LogicalPlan): LogicalPlan
apply[K: Encoder, V: Encoder, S: Encoder, U: Encoder](
  func: (Any, Iterator[Any], LogicalGroupState[Any]) => Iterator[Any],
  groupingAttributes: Seq[Attribute],
  dataAttributes: Seq[Attribute],
  outputMode: OutputMode,
  isMapGroupsWithState: Boolean,
  timeout: GroupStateTimeout,
  child: LogicalPlan,
  initialStateGroupAttrs: Seq[Attribute],
  initialStateDataAttrs: Seq[Attribute],
  initialState: LogicalPlan): LogicalPlan
```

`apply` creates a `SerializeFromObject` logical operator with a `FlatMapGroupsWithState` as its child logical operator.

---

Internally, `apply` creates `SerializeFromObject` object consumer (aka unary logical operator) with `FlatMapGroupsWithState` logical plan.

Internally, `apply` finds `ExpressionEncoder` for the type `S` and creates a `FlatMapGroupsWithState` with `UnresolvedDeserializer` for the types `K` and `V`.

In the end, `apply` creates a `SerializeFromObject` object consumer with the `FlatMapGroupsWithState`.

## Execution Planning

`FlatMapGroupsWithState` is resolved (_planned_) to:

* [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md) physical operator for streaming queries (by [FlatMapGroupsWithStateStrategy](../execution-planning-strategies/FlatMapGroupsWithStateStrategy.md) execution planning strategy)

* `CoGroupExec` or `MapGroupsExec` physical operators for batch queries (by `BasicOperators` execution planning strategy)

## <span id="ObjectProducer"> ObjectProducer

`FlatMapGroupsWithState` is an `ObjectProducer` ([Spark SQL]({{ book.spark_sql }}/logical-operators/ObjectProducer)).
