# StateManagerImplV2

`StateManagerImplV2` is a concrete <<spark-sql-streaming-StateManager.md#, StateManager>> (as a <<spark-sql-streaming-StateManagerImplBase.md#, StateManagerImplBase>>) that is used by default in [FlatMapGroupsWithStateExec](physical-operators/FlatMapGroupsWithStateExec.md) physical operator (per [spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion](configuration-properties.md#spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion) internal configuration property).

`StateManagerImplV2` is <<creating-instance, created>> exclusively when `FlatMapGroupsWithStateExecHelper` utility is requested for a <<spark-sql-streaming-FlatMapGroupsWithStateExecHelper.md#createStateManager, StateManager>> (when the `stateFormatVersion` is `2`).

=== [[creating-instance]] Creating StateManagerImplV2 Instance

`StateManagerImplV2` takes the following to be created:

* [[stateEncoder]] State encoder (`ExpressionEncoder[Any]`)
* [[shouldStoreTimestamp]] `shouldStoreTimestamp` flag

`StateManagerImplV2` initializes the <<internal-properties, internal properties>>.

=== [[stateSchema]] State Schema -- `stateSchema` Value

[source, scala]
----
stateSchema: StructType
----

NOTE: `stateSchema` is part of the <<spark-sql-streaming-StateManager.md#stateSchema, StateManager Contract>> for the schema of the state.

`stateSchema`...FIXME

=== [[stateSerializerExprs]] State Serializer -- `stateSerializerExprs` Value

[source, scala]
----
stateSerializerExprs: Seq[Expression]
----

NOTE: `stateSerializerExprs` is part of the <<spark-sql-streaming-StateManagerImplBase.md#stateSerializerExprs, StateManager Contract>> for the state serializer, i.e. Catalyst expressions to serialize a state object to a row (`UnsafeRow`).

`stateSerializerExprs`...FIXME

=== [[stateDeserializerExpr]] State Deserializer -- `stateDeserializerExpr` Value

[source, scala]
----
stateDeserializerExpr: Expression
----

NOTE: `stateDeserializerExpr` is part of the <<spark-sql-streaming-StateManagerImplBase.md#stateDeserializerExpr, StateManager Contract>> for the state deserializer, i.e. a Catalyst expression to deserialize a state object from a row (`UnsafeRow`).

`stateDeserializerExpr`...FIXME

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| nestedStateOrdinal
a| [[nestedStateOrdinal]] Position of the state in a state row (`0`)

Used when...FIXME

| timeoutTimestampOrdinalInRow
a| [[timeoutTimestampOrdinalInRow]] Position of the timeout timestamp in a state row (`1`)

Used when...FIXME

|===
