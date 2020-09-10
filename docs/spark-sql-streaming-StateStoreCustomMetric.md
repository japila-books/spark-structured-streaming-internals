== [[StateStoreCustomMetric]] StateStoreCustomMetric Contract

`StateStoreCustomMetric` is the <<contract, abstraction>> of <<implementations, metrics>> that a state store may wish to expose (as <<spark-sql-streaming-StateStoreMetrics.md#, StateStoreMetrics>> or <<spark-sql-streaming-StateStoreProvider.md#supportedCustomMetrics, supportedCustomMetrics>>).

`StateStoreCustomMetric` is used when:

* `StateStoreProvider` is requested for the <<spark-sql-streaming-StateStoreProvider.md#supportedCustomMetrics, custom metrics>>

* `StateStoreMetrics` is <<spark-sql-streaming-StateStoreMetrics.md#customMetrics, created>>

[[contract]]
.StateStoreCustomMetric Contract
[cols="1m,2",options="header",width="100%"]
|===
| Method
| Description

| desc
a| [[desc]]

[source, scala]
----
desc: String
----

Description of the custom metrics

| name
a| [[name]]

[source, scala]
----
name: String
----

Name of the custom metrics

|===

[[implementations]]
.StateStoreCustomMetrics
[cols="1m,2",options="header",width="100%"]
|===
| StateStoreCustomMetric
| Description

| StateStoreCustomSizeMetric
| [[StateStoreCustomSizeMetric]]

| StateStoreCustomSumMetric
| [[StateStoreCustomSumMetric]]

| StateStoreCustomTimingMetric
| [[StateStoreCustomTimingMetric]]
|===
