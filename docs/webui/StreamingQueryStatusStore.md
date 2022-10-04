# StreamingQueryStatusStore

## Creating Instance

`StreamingQueryStatusStore` takes the following to be created:

* <span id="store"> `KVStore` ([Spark Core]({{ book.spark_core }}/core/KVStore))

`StreamingQueryStatusStore` is created when:

* `StreamingQueryHistoryServerPlugin` is requested to `setupUI`
* `SharedState` ([Spark SQL]({{ book.spark_sql }}/SharedState)) is created (with [spark.sql.streaming.ui.enabled](../configuration-properties.md#spark.sql.streaming.ui.enabled) enabled)

## <span id="allQueryUIData"> allQueryUIData

```scala
allQueryUIData: Seq[StreamingQueryUIData]
```

`allQueryUIData` creates a view of `StreamingQueryData`s (in the [KVStore](#store)) indexed by `startTimestamp` to [makeUIData](#makeUIData).

---

`allQueryUIData` is used when:

* `StreamingQueryHistoryServerPlugin` is requested to `setupUI`
* `StreamingQueryPage` is requested to [generateStreamingQueryTable](StreamingQueryPage.md#generateStreamingQueryTable)
* `StreamingQueryStatisticsPage` is requested to [render](StreamingQueryStatisticsPage.md#render)

### <span id="makeUIData"> makeUIData

```scala
makeUIData(
  summary: StreamingQueryData): StreamingQueryUIData
```

`makeUIData`...FIXME
