== [[flatMapGroupsWithState]] flatMapGroupsWithState Operator -- Arbitrary Stateful Streaming Aggregation (with Explicit State Logic)

[source, scala]
----
KeyValueGroupedDataset[K, V].flatMapGroupsWithState[S: Encoder, U: Encoder](
  outputMode: OutputMode,
  timeoutConf: GroupStateTimeout)(
  func: (K, Iterator[V], GroupState[S]) => Iterator[U]): Dataset[U]
----

`flatMapGroupsWithState` operator is used for <<spark-sql-arbitrary-stateful-streaming-aggregation.md#, Arbitrary Stateful Streaming Aggregation (with Explicit State Logic)>>.

`flatMapGroupsWithState` requires that the given <<spark-sql-streaming-OutputMode.md#, OutputMode>> is either <<spark-sql-streaming-OutputMode.md#Append, Append>> or <<spark-sql-streaming-OutputMode.md#Update, Update>> (and reports an `IllegalArgumentException` at runtime).

NOTE: An `OutputMode` is a required argument, but does not seem to be used at all. Check out the question https://stackoverflow.com/q/56921772/1305344[What's the purpose of OutputMode in flatMapGroupsWithState? How/where is it used?] on StackOverflow.

Every time the state function `func` is executed for a key, the state (as `GroupState[S]`) is for this key only.

[NOTE]
====
* `K` is the type of the keys in `KeyValueGroupedDataset`

* `V` is the type of the values (per key) in `KeyValueGroupedDataset`

* `S` is the user-defined type of the state as maintained for each group

* `U` is the type of rows in the result `Dataset`
====

`flatMapGroupsWithState` creates a new `Dataset` with [FlatMapGroupsWithState](logical-operators/FlatMapGroupsWithState.md) unary logical operator.
