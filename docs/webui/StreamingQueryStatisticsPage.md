# StreamingQueryStatisticsPage

`StreamingQueryStatisticsPage` is a `WebUIPage` ([Spark Core]({{ book.spark_core }}/webui/WebUIPage)) with `statistics` URL prefix.

`StreamingQueryStatisticsPage` [renders a Streaming Query Statistics page](render) of the statistics of a streaming query (by a given `runId` as `id` request parameter) with the following sections:

* [Basic Info](#generateBasicInfo)
* [Statistics](#generateStatTable)

## Global Watermark Gap

<figure markdown>
  ![Global Watermark Gap](../images/webui-statistics-global-watermark-gap.png)
</figure>

Only displayed when there is a global watermark in a streaming query.

!!! note "Global Watermark of Batch"
    **Global Watermark** of a batch is the value of the `watermark` entry in the [Event Time Statistics](../monitoring/StreamingQueryProgress.md#eventTime) of a [StreamingQueryProgress](../monitoring/StreamingQueryProgress.md).

    `watermark` entry will only be included in the [Event Time Statistics](../monitoring/StreamingQueryProgress.md#eventTime) for a streaming query with [EventTimeWatermark](../logical-operators/EventTimeWatermark.md) logical operator.

A time series of the gaps (_differences_) between the [batch timestamp](../monitoring/StreamingQueryProgress.md#timestamp) and the global watermark (in secs).

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

### <span id="generateStatTable"> generateStatTable

```scala
generateStatTable(
  query: StreamingQueryUIData): Seq[Node]
```

`generateStatTable`...FIXME

### <span id="generateWatermark"> generateWatermark

```scala
generateWatermark(
  query: StreamingQueryUIData,
  minBatchTime: Long,
  maxBatchTime: Long,
  jsCollector: JsCollector): Seq[Node]
```

`generateWatermark` finds the global watermark (`watermark` entry in the [eventTime](../monitoring/StreamingQueryProgress.md#eventTime)) of the last [StreamingQueryProgress](../monitoring/StreamingQueryProgress.md) of a streaming query.

Unless found, `generateWatermark` returns _nothing_ (an empty collection).

`generateWatermark` scans [StreamingQueryProgresses](../monitoring/StreamingQueryProgress.md) for which the global watermark is greater than `0` and collects a time series (data points) with the following:

* [Batch timestamp](../monitoring/StreamingQueryProgress.md#timestamp)
* The gap (_difference_) between the batch timestamp and the global watermark (in secs)

`generateWatermark` creates a `GraphUIData` to generate a timeline and a histogram for `watermark-gap-timeline` and `watermark-gap-histogram` HTML `div`s, respectively.

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.streaming.ui.StreamingQueryStatisticsPage` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.streaming.ui.StreamingQueryStatisticsPage=ALL
```

Refer to [Logging](../spark-logging.md).
