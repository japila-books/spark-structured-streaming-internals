# StreamingQueryStatisticsPage

`StreamingQueryStatisticsPage` is a `WebUIPage` ([Spark Core]({{ book.spark_core }}/webui/WebUIPage)) with `statistics` URL prefix.

`StreamingQueryStatisticsPage` [renders a Streaming Query Statistics page](render) of the statistics of a streaming query (by a given `runId` as `id` request parameter) with the following sections:

* [Basic Info](#generateBasicInfo)
* [Statistics](#generateStatTable)

## Global Watermark Gap

## <span id="render"> Rendering Page

```scala
render(
  request: HttpServletRequest): Seq[Node]
```

`render` is part of the `WebUIPage` ([Spark Core]({{ book.spark_core }}/webui/WebUIPage#render)) abstraction.

---

`render` uses the `id` request parameter for the `runId` of the streaming query to render statistics of.

`render` requests the parent [StreamingQueryTab](#parent) for the [StreamingQueryStatusStore](StreamingQueryTab.md#store) for the [allQueryUIData](StreamingQueryStatusStore.md#allQueryUIData) to find the data of the streaming query (by `runId`).

`render` generates a HTML page with the following sections:

* [generateLoadResources](#generateLoadResources)
* [generateBasicInfo](#generateBasicInfo)
* [generateStatTable](#generateStatTable)

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.streaming.ui.StreamingQueryStatisticsPage` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.streaming.ui.StreamingQueryStatisticsPage=ALL
```

Refer to [Logging](../spark-logging.md).
