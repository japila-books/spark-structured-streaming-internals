# StateManagerImplV2

`StateManagerImplV2` is a concrete [StateManager](StateManager.md) (as a [StateManagerImplBase](StateManagerImplBase.md)) that is used by default in [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md) physical operator (per [spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion](../configuration-properties.md#spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion) internal configuration property).

## Creating Instance

`StateManagerImplV2` takes the following to be created:

* [[stateEncoder]] State encoder (`ExpressionEncoder[Any]`)
* [[shouldStoreTimestamp]] `shouldStoreTimestamp` flag

`StateManagerImplV2` is created when:

* `FlatMapGroupsWithStateExecHelper` utility is requested for a [StateManager](FlatMapGroupsWithStateExecHelper.md#createStateManager) (when the `stateFormatVersion` is `2`)
