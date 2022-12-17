# flatMapGroupsWithState Operator

`flatMapGroupsWithState` is part of [KeyValueGroupedDataset](../KeyValueGroupedDataset.md) API for [Arbitrary Stateful Streaming Aggregation](../arbitrary-stateful-streaming-aggregation/index.md) with an explicit state logic.

```scala
flatMapGroupsWithState[S: Encoder, U: Encoder](
  outputMode: OutputMode,
  timeoutConf: GroupStateTimeout)(
  func: (K, Iterator[V], GroupState[S]) => Iterator[U]): Dataset[U]
flatMapGroupsWithState[S: Encoder, U: Encoder](
  outputMode: OutputMode,
  timeoutConf: GroupStateTimeout,
  initialState: KeyValueGroupedDataset[K, S])(
  func: (K, Iterator[V], GroupState[S]) => Iterator[U]): Dataset[U] // (1)!
```

1. Since 3.2.0

## Input Arguments

`flatMapGroupsWithState` accepts the following:

* [OutputMode](../OutputMode.md)
* [GroupStateTimeout](../GroupStateTimeout.md)
* A function with the `K` key and the `V` values and the current [GroupState](../GroupState.md) for the given `K` key
* (optionally) [KeyValueGroupedDataset](../KeyValueGroupedDataset.md) for an user-defined initial state

## FlatMapGroupsWithState Logical Operator

`flatMapGroupsWithState` creates a `Dataset` with [FlatMapGroupsWithState](../logical-operators/FlatMapGroupsWithState.md) logical operator with the following:

* `LogicalGroupState`
* [groupingAttributes](../KeyValueGroupedDataset.md#groupingAttributes) of the `KeyValueGroupedDataset`
* [dataAttributes](../KeyValueGroupedDataset.md#dataAttributes) of the `KeyValueGroupedDataset`
* `isMapGroupsWithState` flag disabled (`false`)
* [logicalPlan](../KeyValueGroupedDataset.md#logicalPlan) of the `KeyValueGroupedDataset` as the [child](../logical-operators/FlatMapGroupsWithState.md#child) operator

## Output Modes

`flatMapGroupsWithState` supports [Append](../OutputMode.md#Append) and [Update](../OutputMode.md#Update) output modes only and throws an `IllegalArgumentException` otherwise:

```text
The output mode of function should be append or update
```

!!! note
    An `OutputMode` is a required argument, but does not seem to be used at all. Check out the question [What's the purpose of OutputMode in flatMapGroupsWithState? How/where is it used?](https://stackoverflow.com/q/56921772/1305344) on StackOverflow.
