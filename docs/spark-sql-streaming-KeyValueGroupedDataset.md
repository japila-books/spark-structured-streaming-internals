== [[KeyValueGroupedDataset]] KeyValueGroupedDataset -- Streaming Aggregation

`KeyValueGroupedDataset` represents a *grouped dataset* as a result of <<spark-sql-streaming-Dataset-operators.adoc#groupByKey, Dataset.groupByKey>> operator (that aggregates records by a grouping function).

[source, scala]
----
// Dataset[T]
groupByKey(func: T => K): KeyValueGroupedDataset[K, T]
----

[source, scala]
----
import java.sql.Timestamp
val numGroups = spark.
  readStream.
  format("rate").
  load.
  as[(Timestamp, Long)].
  groupByKey { case (time, value) => value % 2 }

scala> :type numGroups
org.apache.spark.sql.KeyValueGroupedDataset[Long,(java.sql.Timestamp, Long)]
----

`KeyValueGroupedDataset` is also <<creating-instance, created>> for <<keyAs, KeyValueGroupedDataset.keyAs>> and <<mapValues, KeyValueGroupedDataset.mapValues>> operators.

[source, scala]
----
scala> :type numGroups
org.apache.spark.sql.KeyValueGroupedDataset[Long,(java.sql.Timestamp, Long)]

scala> :type numGroups.keyAs[String]
org.apache.spark.sql.KeyValueGroupedDataset[String,(java.sql.Timestamp, Long)]
----

[source, scala]
----
scala> :type numGroups
org.apache.spark.sql.KeyValueGroupedDataset[Long,(java.sql.Timestamp, Long)]

val mapped = numGroups.mapValues { case (ts, n) => s"($ts, $n)" }
scala> :type mapped
org.apache.spark.sql.KeyValueGroupedDataset[Long,String]
----

`KeyValueGroupedDataset` works for batch and streaming aggregations, but shines the most when used for <<spark-sql-streaming-aggregation.adoc#, Streaming Aggregation>>.

[source, scala]
----
scala> :type numGroups
org.apache.spark.sql.KeyValueGroupedDataset[Long,(java.sql.Timestamp, Long)]

import org.apache.spark.sql.streaming.Trigger
import scala.concurrent.duration._
numGroups.
  mapGroups { case(group, values) => values.size }.
  writeStream.
  format("console").
  trigger(Trigger.ProcessingTime(10.seconds)).
  start

-------------------------------------------
Batch: 0
-------------------------------------------
+-----+
|value|
+-----+
+-----+

-------------------------------------------
Batch: 1
-------------------------------------------
+-----+
|value|
+-----+
|    3|
|    2|
+-----+

-------------------------------------------
Batch: 2
-------------------------------------------
+-----+
|value|
+-----+
|    5|
|    5|
+-----+

// Eventually...
spark.streams.active.foreach(_.stop)
----

The most prestigious use case of `KeyValueGroupedDataset` however is <<spark-sql-arbitrary-stateful-streaming-aggregation.adoc#, Arbitrary Stateful Streaming Aggregation>> that allows for accumulating *streaming state* (by means of [GroupState](GroupState.md)) using <<mapGroupsWithState, mapGroupsWithState>> and the more advanced <<flatMapGroupsWithState, flatMapGroupsWithState>> operators.

[[operators]]
.KeyValueGroupedDataset's Operators
[cols="1m,2",options="header",width="100%"]
|===
| Operator
| Description

| agg
a| [[agg]]

[source, scala]
----
agg[U1](col1: TypedColumn[V, U1]): Dataset[(K, U1)]
agg[U1, U2](
  col1: TypedColumn[V, U1],
  col2: TypedColumn[V, U2]): Dataset[(K, U1, U2)]
agg[U1, U2, U3](
  col1: TypedColumn[V, U1],
  col2: TypedColumn[V, U2],
  col3: TypedColumn[V, U3]): Dataset[(K, U1, U2, U3)]
agg[U1, U2, U3, U4](
  col1: TypedColumn[V, U1],
  col2: TypedColumn[V, U2],
  col3: TypedColumn[V, U3],
  col4: TypedColumn[V, U4]): Dataset[(K, U1, U2, U3, U4)]
----

| cogroup
a| [[cogroup]]

[source, scala]
----
cogroup[U, R : Encoder](
  other: KeyValueGroupedDataset[K, U])(
  f: (K, Iterator[V], Iterator[U]) => TraversableOnce[R]): Dataset[R]
----

| count
a| [[count]]

[source, scala]
----
count(): Dataset[(K, Long)]
----

| flatMapGroups
a| [[flatMapGroups]]

[source, scala]
----
flatMapGroups[U : Encoder](f: (K, Iterator[V]) => TraversableOnce[U]): Dataset[U]
----

| <<spark-sql-streaming-KeyValueGroupedDataset-flatMapGroupsWithState.adoc#, flatMapGroupsWithState>>
a| [[flatMapGroupsWithState]]

[source, scala]
----
flatMapGroupsWithState[S: Encoder, U: Encoder](
  outputMode: OutputMode,
  timeoutConf: GroupStateTimeout)(
  func: (K, Iterator[V], GroupState[S]) => Iterator[U]): Dataset[U]
----

<<spark-sql-arbitrary-stateful-streaming-aggregation.adoc#, Arbitrary Stateful Streaming Aggregation>> - streaming aggregation with explicit state and state timeout

NOTE: The difference between this `flatMapGroupsWithState` and <<mapGroupsWithState, mapGroupsWithState>> operators is the state function that generates zero or more elements (that are in turn the rows in the result streaming `Dataset`).

| keyAs
a| [[keyAs]]

[source, scala]
----
keys: Dataset[K]
keyAs[L : Encoder]: KeyValueGroupedDataset[L, V]
----

| mapGroups
a| [[mapGroups]]

[source, scala]
----
mapGroups[U : Encoder](f: (K, Iterator[V]) => U): Dataset[U]
----

| link:spark-sql-streaming-KeyValueGroupedDataset-mapGroupsWithState.adoc[mapGroupsWithState]
a| [[mapGroupsWithState]]

[source, scala]
----
mapGroupsWithState[S: Encoder, U: Encoder](
  func: (K, Iterator[V], GroupState[S]) => U): Dataset[U]
mapGroupsWithState[S: Encoder, U: Encoder](
  timeoutConf: GroupStateTimeout)(
  func: (K, Iterator[V], GroupState[S]) => U): Dataset[U]
----

Creates a new `Dataset` with link:spark-sql-streaming-FlatMapGroupsWithState.adoc#apply[FlatMapGroupsWithState] logical operator

NOTE: The difference between `mapGroupsWithState` and <<flatMapGroupsWithState, flatMapGroupsWithState>> is the state function that generates exactly one element (that is in turn the row in the result `Dataset`).

| mapValues
a| [[mapValues]]

[source, scala]
----
mapValues[W : Encoder](func: V => W): KeyValueGroupedDataset[K, W]
----

| reduceGroups
a| [[reduceGroups]]

[source, scala]
----
reduceGroups(f: (V, V) => V): Dataset[(K, V)]
----

|===

=== [[creating-instance]] Creating KeyValueGroupedDataset Instance

`KeyValueGroupedDataset` takes the following when created:

* [[kEncoder]] `Encoder` for keys
* [[vEncoder]] `Encoder` for values
* [[queryExecution]] `QueryExecution`
* [[dataAttributes]] Data attributes
* [[groupingAttributes]] Grouping attributes
