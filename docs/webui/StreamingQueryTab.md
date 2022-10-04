# StreamingQueryTab

`StreamingQueryTab` is a `SparkUITab` ([Spark Core]({{ book.spark_core }}/webui/SparkUITab)) with `StreamingQuery` URL prefix.

When [created](#creating-instance), `StreamingQueryTab` attaches the following pages:

* [StreamingQueryPage](StreamingQueryPage.md)
* [StreamingQueryStatisticsPage](StreamingQueryStatisticsPage.md)

## Creating Instance

`StreamingQueryTab` takes the following to be created:

* [StreamingQueryStatusStore](#store)
* <span id="sparkUI"> `SparkUI` ([Spark Core]({{ book.spark_core }}/webui/SparkUI))

`StreamingQueryTab` is created when:

* `StreamingQueryHistoryServerPlugin` is requested to `setupUI`
* `SharedState` ([Spark SQL]({{ book.spark_sql }}/SharedState)) is created (with [spark.sql.streaming.ui.enabled](../configuration-properties.md#spark.sql.streaming.ui.enabled) enabled)

### <span id="store"> StreamingQueryStatusStore

`StreamingQueryTab` is given a [StreamingQueryStatusStore](StreamingQueryStatusStore.md) when [created](#creating-instance).

The `StreamingQueryStatusStore` is used to [fetch the streaming query data](StreamingQueryStatusStore.md#allQueryUIData) in the attached pages:

* [StreamingQueryPage](StreamingQueryPage.md#generateStreamingQueryTable)
* [StreamingQueryStatisticsPage](StreamingQueryStatisticsPage.md#render)

## <span id="name"> Tab Name

```scala
name: String
```

`name` is part of the `WebUITab` ([Spark Core]({{ book.spark_core }}/webui/WebUITab/#name)) abstraction.

---

`name` is `Structured Streaming`.
