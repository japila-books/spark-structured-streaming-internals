# StreamingQueryStatusStore

## Creating Instance

`StreamingQueryStatusStore` takes the following to be created:

* <span id="store"> `KVStore` (Spark Core)

`StreamingQueryStatusStore` is created when:

* `StreamingQueryHistoryServerPlugin` is requested to `setupUI`
* `SharedState` ([Spark SQL]({{ book.spark_sql }}/SharedState)) is created (with [spark.sql.streaming.ui.enabled](../configuration-properties.md#spark.sql.streaming.ui.enabled) enabled)
