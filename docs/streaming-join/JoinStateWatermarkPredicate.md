# JoinStateWatermarkPredicate

`JoinStateWatermarkPredicate` is the <<contract, abstraction>> of <<implementations, join state watermark predicates>> that are described by a <<expr, Catalyst expression>> and <<desc, desc>>.

`JoinStateWatermarkPredicate` is created using [StreamingSymmetricHashJoinHelper](StreamingSymmetricHashJoinHelper.md#getOneSideStateWatermarkPredicate) utility (for [planning a StreamingSymmetricHashJoinExec physical operator for execution with execution-specific configuration](../IncrementalExecution.md#state))

`JoinStateWatermarkPredicate` is used to create a [OneSideHashJoiner](OneSideHashJoiner.md) (and [JoinStateWatermarkPredicates](JoinStateWatermarkPredicates.md)).

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

Used for the <<toString, textual representation>> and a [JoinStateWatermarkPredicates](StreamingSymmetricHashJoinHelper.md#getStateWatermarkPredicates) (for [StreamingSymmetricHashJoinExec](../physical-operators/StreamingSymmetricHashJoinExec.md) physical operator)

|===

[[implementations]]
.JoinStateWatermarkPredicates
[cols="30,70",options="header",width="100%"]
|===
| JoinStateWatermarkPredicate
| Description

| JoinStateKeyWatermarkPredicate
a| [[JoinStateKeyWatermarkPredicate]] Watermark predicate on state keys (i.e. when the [streaming watermark](../streaming-watermark/index.md) is defined either on the [left](../physical-operators/StreamingSymmetricHashJoinExec.md#leftKeys) or [right](../physical-operators/StreamingSymmetricHashJoinExec.md#rightKeys) join keys)

Created when `StreamingSymmetricHashJoinHelper` utility is requested for a [JoinStateWatermarkPredicates](StreamingSymmetricHashJoinHelper.md#getStateWatermarkPredicates) for the left and right side of a stream-stream join (when `IncrementalExecution` is requested to optimize a query plan with a [StreamingSymmetricHashJoinExec](../physical-operators/StreamingSymmetricHashJoinExec.md) physical operator)

Used when `OneSideHashJoiner` is requested for the [stateKeyWatermarkPredicateFunc](OneSideHashJoiner.md#stateKeyWatermarkPredicateFunc) and then to [remove an old state](OneSideHashJoiner.md#removeOldState)

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
