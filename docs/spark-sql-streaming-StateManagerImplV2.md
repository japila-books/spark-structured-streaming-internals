== [[StateManagerImplV2]] StateManagerImplV2 -- Default StateManager of FlatMapGroupsWithStateExec Physical Operator

`StateManagerImplV2` is a concrete <<spark-sql-streaming-StateManager.adoc#, StateManager>> (as a <<spark-sql-streaming-StateManagerImplBase.adoc#, StateManagerImplBase>>) that is used by default in <<spark-sql-streaming-FlatMapGroupsWithStateExec.adoc#, FlatMapGroupsWithStateExec>> physical operator (per <<spark-sql-streaming-properties.adoc#spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion, spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion>> internal configuration property).

`StateManagerImplV2` is <<creating-instance, created>> exclusively when `FlatMapGroupsWithStateExecHelper` utility is requested for a <<spark-sql-streaming-FlatMapGroupsWithStateExecHelper.adoc#createStateManager, StateManager>> (when the `stateFormatVersion` is `2`).

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

NOTE: `stateSchema` is part of the <<spark-sql-streaming-StateManager.adoc#stateSchema, StateManager Contract>> for the schema of the state.

`stateSchema`...FIXME

=== [[stateSerializerExprs]] State Serializer -- `stateSerializerExprs` Value

[source, scala]
----
stateSerializerExprs: Seq[Expression]
----

NOTE: `stateSerializerExprs` is part of the <<spark-sql-streaming-StateManagerImplBase.adoc#stateSerializerExprs, StateManager Contract>> for the state serializer, i.e. Catalyst expressions to serialize a state object to a row (`UnsafeRow`).

`stateSerializerExprs`...FIXME

=== [[stateDeserializerExpr]] State Deserializer -- `stateDeserializerExpr` Value

[source, scala]
----
stateDeserializerExpr: Expression
----

NOTE: `stateDeserializerExpr` is part of the <<spark-sql-streaming-StateManagerImplBase.adoc#stateDeserializerExpr, StateManager Contract>> for the state deserializer, i.e. a Catalyst expression to deserialize a state object from a row (`UnsafeRow`).

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
