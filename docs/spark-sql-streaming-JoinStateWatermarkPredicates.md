== [[JoinStateWatermarkPredicates]] JoinStateWatermarkPredicates -- Watermark Predicates for State Removal

[[creating-instance]]
`JoinStateWatermarkPredicates` contains watermark predicates for state removal of the children of a <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>> physical operator:

* [[left]] <<spark-sql-streaming-JoinStateWatermarkPredicate.md#, JoinStateWatermarkPredicate>> for the left-hand side of a join (default: `None`)

* [[right]] <<spark-sql-streaming-JoinStateWatermarkPredicate.md#, JoinStateWatermarkPredicate>> for the right-hand side of a join (default: `None`)

`JoinStateWatermarkPredicates` is <<creating-instance, created>> for the following:

* <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>> physical operator is created (with the optional properties undefined, including <<physical-operators/StreamingSymmetricHashJoinExec.md#stateWatermarkPredicates, JoinStateWatermarkPredicates>>)

* `StreamingSymmetricHashJoinHelper` utility is requested for <<spark-sql-streaming-StreamingSymmetricHashJoinHelper.md#getStateWatermarkPredicates, one>> (for `IncrementalExecution` for the <<spark-sql-streaming-IncrementalExecution.md#state, state preparation rule>> to optimize and specify the execution-specific configuration for a query plan with <<physical-operators/StreamingSymmetricHashJoinExec.md#, StreamingSymmetricHashJoinExec>> physical operators)

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
