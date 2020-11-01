# StatefulOperator Physical Operators

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

The [StatefulOperatorStateInfo](../StatefulOperatorStateInfo.md) of the physical operator
|===

[[extensions]]
.StatefulOperators (Direct Implementations)
[cols="1,2",options="header",width="100%"]
|===
| StatefulOperator
| Description

| <<StateStoreReader.md#, StateStoreReader>>
| [[StateStoreReader]]

| [StateStoreWriter](StateStoreWriter.md)
| [[StateStoreWriter]] Physical operator that writes to a state store and collects the write metrics for execution progress reporting
|===
