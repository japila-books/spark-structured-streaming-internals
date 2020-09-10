== [[StatefulOperator]] StatefulOperator Contract -- Physical Operators That Read or Write to StateStore

`StatefulOperator` is the <<contract, base>> of <<extensions, physical operators>> that <<StateStoreReader, read>> or <<StateStoreWriter, write>> state (described by <<stateInfo, stateInfo>>).

[[contract]]
.StatefulOperator Contract
[cols="1m,2",options="header",width="100%"]
|===
| Method
| Description

| stateInfo
a| [[stateInfo]]

[source, scala]
----
stateInfo: Option[StatefulOperatorStateInfo]
----

The <<spark-sql-streaming-StatefulOperatorStateInfo.md#, StatefulOperatorStateInfo>> of the physical operator
|===

[[extensions]]
.StatefulOperators (Direct Implementations)
[cols="1,2",options="header",width="100%"]
|===
| StatefulOperator
| Description

| <<spark-sql-streaming-StateStoreReader.md#, StateStoreReader>>
| [[StateStoreReader]]

| <<spark-sql-streaming-StateStoreWriter.md#, StateStoreWriter>>
| [[StateStoreWriter]] Physical operator that writes to a state store and collects the write metrics for execution progress reporting
|===
