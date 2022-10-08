# FlatMapGroupsWithStateExecHelper

`FlatMapGroupsWithStateExecHelper` utility is mainly used to [creating a StateManager](#createStateManager) for [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md) physical operator.

=== [[createStateManager]] Creating StateManager

[source, scala]
----
createStateManager(
  stateEncoder: ExpressionEncoder[Any],
  shouldStoreTimestamp: Boolean,
  stateFormatVersion: Int): StateManager
----

`createStateManager` simply creates a <<StateManager.md#, StateManager>> (with the `stateEncoder` and `shouldStoreTimestamp` flag) based on `stateFormatVersion`:

* <<spark-sql-streaming-StateManagerImplV1.md#, StateManagerImplV1>> for `1`

* <<spark-sql-streaming-StateManagerImplV2.md#, StateManagerImplV2>> for `2`

`createStateManager` throws an `IllegalArgumentException` for `stateFormatVersion` not `1` or `2`:

```text
Version [stateFormatVersion] is invalid
```

`createStateManager` is used for the [StateManager](../physical-operators/FlatMapGroupsWithStateExec.md#stateManager) for [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md) physical operator.
