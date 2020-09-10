== [[StreamingJoinHelper]] StreamingJoinHelper Utility

`StreamingJoinHelper` is a Scala object with the following utility methods:

* <<getStateValueWatermark, getStateValueWatermark>>

[[logging]]
[TIP]
====
Enable `ALL` logging level for `org.apache.spark.sql.catalyst.analysis.StreamingJoinHelper` to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.catalyst.analysis.StreamingJoinHelper=ALL
```

Refer to <<spark-sql-streaming-logging.md#, Logging>>.
====

=== [[getStateValueWatermark]] State Value Watermark -- `getStateValueWatermark` Object Method

[source, scala]
----
getStateValueWatermark(
  attributesToFindStateWatermarkFor: AttributeSet,
  attributesWithEventWatermark: AttributeSet,
  joinCondition: Option[Expression],
  eventWatermark: Option[Long]): Option[Long]
----

`getStateValueWatermark`...FIXME

[NOTE]
====
`getStateValueWatermark` is used when:

* `UnsupportedOperationChecker` utility is used to <<spark-sql-streaming-UnsupportedOperationChecker.md#checkForStreaming, checkForStreaming>>

* `StreamingSymmetricHashJoinHelper` utility is used to <<spark-sql-streaming-StreamingSymmetricHashJoinHelper.md#getStateWatermarkPredicates, create a JoinStateWatermarkPredicates>>
====
