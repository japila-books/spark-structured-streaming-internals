== [[FlatMapGroupsWithStateExecHelper]] FlatMapGroupsWithStateExecHelper

`FlatMapGroupsWithStateExecHelper` is a utility with the main purpose of <<createStateManager, creating a StateManager>> for [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator.

=== [[createStateManager]] Creating StateManager -- `createStateManager` Method

[source, scala]
----
createStateManager(
  stateEncoder: ExpressionEncoder[Any],
  shouldStoreTimestamp: Boolean,
  stateFormatVersion: Int): StateManager
----

`createStateManager` simply creates a <<spark-sql-streaming-StateManager.adoc#, StateManager>> (with the `stateEncoder` and `shouldStoreTimestamp` flag) based on `stateFormatVersion`:

* <<spark-sql-streaming-StateManagerImplV1.adoc#, StateManagerImplV1>> for `1`

* <<spark-sql-streaming-StateManagerImplV2.adoc#, StateManagerImplV2>> for `2`

`createStateManager` throws an `IllegalArgumentException` for `stateFormatVersion` not `1` or `2`:

```
Version [stateFormatVersion] is invalid
```

`createStateManager` is used for the [StateManager](physical-operators/FlatMapGroupsWithStateExec.md#stateManager) for [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator.
