# StreamingQueryStatisticsPage

`StreamingQueryStatisticsPage` is a `WebUIPage` ([Spark Core]({{ book.spark_core }}/webui/WebUIPage)) with `statistics` URL prefix.

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.streaming.ui.StreamingQueryStatisticsPage` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.streaming.ui.StreamingQueryStatisticsPage=ALL
```

Refer to [Logging](../spark-logging.md).
