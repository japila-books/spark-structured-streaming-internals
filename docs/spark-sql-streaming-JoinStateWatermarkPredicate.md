== [[JoinStateWatermarkPredicate]] JoinStateWatermarkPredicate Contract (Sealed Trait)

`JoinStateWatermarkPredicate` is the <<contract, abstraction>> of <<implementations, join state watermark predicates>> that are described by a <<expr, Catalyst expression>> and <<desc, desc>>.

`JoinStateWatermarkPredicate` is created using <<spark-sql-streaming-StreamingSymmetricHashJoinHelper.md#getOneSideStateWatermarkPredicate, StreamingSymmetricHashJoinHelper>> utility (for <<spark-sql-streaming-IncrementalExecution.md#state, planning a StreamingSymmetricHashJoinExec physical operator for execution with execution-specific configuration>>)

`JoinStateWatermarkPredicate` is used to create a <<spark-sql-streaming-OneSideHashJoiner.md#, OneSideHashJoiner>> (and <<spark-sql-streaming-JoinStateWatermarkPredicates.md#, JoinStateWatermarkPredicates>>).

[[contract]]
.JoinStateWatermarkPredicate Contract
[cols="30m,70",options="header",width="100%"]
|===
| Method
| Description

| desc
a| [[desc]]

[source, scala]
----
desc: String
----

Used exclusively for the <<toString, textual representation>>

| expr
a| [[expr]]

[source, scala]
----
expr: Expression
----

A Catalyst `Expression`

Used for the <<toString, textual representation>> and a <<spark-sql-streaming-StreamingSymmetricHashJoinHelper.md#getStateWatermarkPredicates, JoinStateWatermarkPredicates>> (for <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>> physical operator)

|===

[[implementations]]
.JoinStateWatermarkPredicates
[cols="30,70",options="header",width="100%"]
|===
| JoinStateWatermarkPredicate
| Description

| JoinStateKeyWatermarkPredicate
a| [[JoinStateKeyWatermarkPredicate]] Watermark predicate on state keys (i.e. when the <<spark-sql-streaming-watermark.md#, streaming watermark>> is defined either on the <<physical-operators/StreamingSymmetricHashJoinExec.md#leftKeys, left>> or <<physical-operators/StreamingSymmetricHashJoinExec.md#rightKeys, right>> join keys)

Created when `StreamingSymmetricHashJoinHelper` utility is requested for a <<spark-sql-streaming-StreamingSymmetricHashJoinHelper.md#getStateWatermarkPredicates, JoinStateWatermarkPredicates>> for the left and right side of a stream-stream join (when `IncrementalExecution` is requested to optimize a query plan with a <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>> physical operator)

Used when `OneSideHashJoiner` is requested for the <<spark-sql-streaming-OneSideHashJoiner.md#stateKeyWatermarkPredicateFunc, stateKeyWatermarkPredicateFunc>> and then to <<spark-sql-streaming-OneSideHashJoiner.md#removeOldState, remove an old state>>

| JoinStateValueWatermarkPredicate
| [[JoinStateValueWatermarkPredicate]] Watermark predicate on state values

|===

NOTE: `JoinStateWatermarkPredicate` is a Scala *sealed trait* which means that all the <<implementations, implementations>> are in the same compilation unit (a single file).

=== [[toString]] Textual Representation -- `toString` Method

[source, scala]
----
toString: String
----

NOTE: `toString` is part of the ++https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#toString--++[java.lang.Object] contract for the string representation of the object.

`toString` uses the <<desc, desc>> and <<expr, expr>> for the string representation:

```
[desc]: [expr]
```
