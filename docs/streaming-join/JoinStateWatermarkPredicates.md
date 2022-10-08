# JoinStateWatermarkPredicates

[[creating-instance]]
`JoinStateWatermarkPredicates` contains watermark predicates for state removal of the children of a [StreamingSymmetricHashJoinExec](../physical-operators/StreamingSymmetricHashJoinExec.md) physical operator:

* [[left]] <<JoinStateWatermarkPredicate.md#, JoinStateWatermarkPredicate>> for the left-hand side of a join (default: `None`)

* [[right]] <<JoinStateWatermarkPredicate.md#, JoinStateWatermarkPredicate>> for the right-hand side of a join (default: `None`)

`JoinStateWatermarkPredicates` is <<creating-instance, created>> for the following:

* <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>> physical operator is created (with the optional properties undefined, including <<physical-operators/StreamingSymmetricHashJoinExec.md#stateWatermarkPredicates, JoinStateWatermarkPredicates>>)

* `StreamingSymmetricHashJoinHelper` utility is requested for [one](StreamingSymmetricHashJoinHelper.md#getStateWatermarkPredicates) (for `IncrementalExecution` for the [state preparation rule](../IncrementalExecution.md#state) to optimize and specify the execution-specific configuration for a query plan with [StreamingSymmetricHashJoinExec](../physical-operators/StreamingSymmetricHashJoinExec.md) physical operators)

=== [[toString]] Textual Representation -- `toString` Method

[source, scala]
----
toString: String
----

NOTE: `toString` is part of the ++https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#toString--++[java.lang.Object] contract for the string representation of the object.

`toString` uses the <<left, left>> and <<right, right>> predicates for the string representation:

```
state cleanup [ left [left], right [right] ]
```
