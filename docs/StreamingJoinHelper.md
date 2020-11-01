# StreamingJoinHelper Utility

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

Refer to <<spark-sql-streaming-spark-logging.md#, Logging>>.
====

## <span id="getStateValueWatermark"> State Value Watermark

```scala
getStateValueWatermark(
  attributesToFindStateWatermarkFor: AttributeSet,
  attributesWithEventWatermark: AttributeSet,
  joinCondition: Option[Expression],
  eventWatermark: Option[Long]): Option[Long]
```

`getStateValueWatermark`...FIXME

`getStateValueWatermark` is used when:

* `UnsupportedOperationChecker` utility is used to [checkForStreaming](spark-sql-streaming-UnsupportedOperationChecker.md#checkForStreaming)

* `StreamingSymmetricHashJoinHelper` utility is used to [create a JoinStateWatermarkPredicates](StreamingSymmetricHashJoinHelper.md#getStateWatermarkPredicates)
