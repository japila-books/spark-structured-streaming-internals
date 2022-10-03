# flatMapGroupsWithState Operator -- Arbitrary Stateful Streaming Aggregation (with Explicit State Logic)

```scala
KeyValueGroupedDataset[K, V].flatMapGroupsWithState[S: Encoder, U: Encoder](
  outputMode: OutputMode,
  timeoutConf: GroupStateTimeout)(
  func: (K, Iterator[V], GroupState[S]) => Iterator[U]): Dataset[U]
```

`flatMapGroupsWithState` operator is used for [Arbitrary Stateful Streaming Aggregation](arbitrary-stateful-streaming-aggregation/index.md) (with Explicit State Logic).

`flatMapGroupsWithState` requires that the given [OutputMode](OutputMode.md) is either [Append](OutputMode.md#Append) or [Update](OutputMode.md#Update) (and reports an `IllegalArgumentException` at runtime).

!!! note
    An `OutputMode` is a required argument, but does not seem to be used at all. Check out the question [What's the purpose of OutputMode in flatMapGroupsWithState? How/where is it used?](https://stackoverflow.com/q/56921772/1305344) on StackOverflow.

Every time the state function `func` is executed for a key, the state (as `GroupState[S]`) is for this key only.

!!! note
    * `K` is the type of the keys in `KeyValueGroupedDataset`

    * `V` is the type of the values (per key) in `KeyValueGroupedDataset`

    * `S` is the user-defined type of the state as maintained for each group

    * `U` is the type of rows in the result `Dataset`

`flatMapGroupsWithState` creates a new `Dataset` with [FlatMapGroupsWithState](logical-operators/FlatMapGroupsWithState.md) unary logical operator.
