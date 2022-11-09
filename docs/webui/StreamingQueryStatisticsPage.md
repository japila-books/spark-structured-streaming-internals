# StreamingQueryStatisticsPage

`StreamingQueryStatisticsPage` is a `WebUIPage` ([Spark Core]({{ book.spark_core }}/webui/WebUIPage)) that [renders a page](#render) with the following sections as **Streaming Query Statistics**:

* [Basic Info](#generateBasicInfo)
* [Statistics](#generateStatTable)

`StreamingQueryStatisticsPage` is available under `statistics` URL prefix.

`StreamingQueryStatisticsPage` uses `id` request parameter as the `runId` of a streaming query to [render a page](#render) of.
!!! tip "Demo: Streaming Aggregation"
    Use [Demo: Streaming Aggregation](../demo/streaming-aggregation.md) to learn more and monitor the statistics.

## Creating Instance

`StreamingQueryStatisticsPage` takes the following to be created:

* <span id="parent"> Parent [StreamingQueryTab](StreamingQueryTab.md)

`StreamingQueryStatisticsPage` is created when:

* `StreamingQueryTab` is [created](StreamingQueryTab.md) (and attaches this and [StreamingQueryPage](StreamingQueryPage.md) pages)

## Timelines and Histograms

Displayed only when there are [stateful operators](../monitoring/StreamingQueryProgress.md#stateOperators) in a streaming query.

### Aggregated Number Of Rows Dropped By Watermark

<figure markdown>
  ![Aggregated Number Of Rows Dropped By Watermark](../images/webui-statistics-aggregated-number-of-rows-dropped-by-watermark.png)
</figure>

Number of all input rows in stateful operators that were late and hence dropped by watermark

Sum of [numRowsDroppedByWatermark](../monitoring/StateOperatorProgress.md#numRowsDroppedByWatermark) metrics of [stateOperators](../monitoring/StreamingQueryProgress.md#stateOperators) of every [StreamingQueryProgress](../monitoring/StreamingQueryProgress.md)

`StreamingQueryStatisticsPage` uses [generateAggregatedStateOperators](#generateAggregatedStateOperators) to generate a timeline and a histogram for `aggregated-num-rows-dropped-by-watermark-timeline` and `aggregated-num-rows-dropped-by-watermark-histogram` HTML `div`s, respectively.

### Aggregated Number Of Total State Rows

<figure markdown>
  ![Aggregated Number Of Total State Rows](../images/webui-statistics-aggregated-number-of-total-state-rows.png)
</figure>

The number of state rows across all stateful operators

Total (_sum_) of [numRowsTotal](../monitoring/StateOperatorProgress.md#numRowsTotal) metrics of [stateOperators](../monitoring/StreamingQueryProgress.md#stateOperators) of every [StreamingQueryProgress](../monitoring/StreamingQueryProgress.md)

`StreamingQueryStatisticsPage` uses [generateAggregatedStateOperators](#generateAggregatedStateOperators) to generate a timeline and a histogram for `aggregated-num-total-state-rows-timeline` and `aggregated-num-total-state-rows-histogram` HTML `div`s, respectively.

### Aggregated Number Of Updated State Rows

<figure markdown>
  ![Aggregated Number Of Updated State Rows](../images/webui-statistics-aggregated-number-of-updated-state-rows.png)
</figure>

Total (a sum in `records` unit) of [numRowsUpdated](../monitoring/StateOperatorProgress.md#numRowsUpdated) metric of [stateOperators](../monitoring/StreamingQueryProgress.md#stateOperators) of every [StreamingQueryProgress](../monitoring/StreamingQueryProgress.md)

* The X axis is from `minBatchTime` to `maxBatchTime`
* The Y axis is from 0 to `maxNumRowsUpdated`

`StreamingQueryStatisticsPage` uses [generateAggregatedStateOperators](#generateAggregatedStateOperators) (`graphUIDataForNumberUpdatedRows`) to generate a timeline and a histogram for `aggregated-num-updated-state-rows-timeline` and `aggregated-num-updated-state-rows-histogram` HTML `div`s, respectively.

### Aggregated State Memory Used In Bytes

<figure markdown>
  ![Aggregated State Memory Used In Bytes](../images/webui-statistics-aggregated-state-memory-used-in-bytes.png)
</figure>

### Global Watermark Gap

<figure markdown>
  ![Global Watermark Gap](../images/webui-statistics-global-watermark-gap.png)
</figure>

A time series of the gaps (_differences_) between the [batch timestamp](../monitoring/StreamingQueryProgress.md#timestamp) and the global watermark (in secs).

Only displayed when there is a global watermark in a streaming query.

!!! note "Global Watermark of Batch"
    **Global Watermark** of a batch is the value of the `watermark` entry in the [Event Time Statistics](../monitoring/StreamingQueryProgress.md#eventTime) of a [StreamingQueryProgress](../monitoring/StreamingQueryProgress.md).

    `watermark` entry will only be included in the [Event Time Statistics](../monitoring/StreamingQueryProgress.md#eventTime) for a streaming query with [EventTimeWatermark](../logical-operators/EventTimeWatermark.md) logical operator.

`StreamingQueryStatisticsPage` uses [generateWatermark](#generateWatermark) to generate a timeline and a histogram for `watermark-gap-timeline` and `watermark-gap-histogram` HTML `div`s, respectively.

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

### <span id="generateBasicInfo"> Generating Basic Info

```scala
generateBasicInfo(
  uiData: StreamingQueryUIData): Seq[Node]
```

`generateBasicInfo`...FIXME

<figure markdown>
  ![Basic Info](../images/webui-statistics-basic-info.png)
</figure>

### <span id="generateStatTable"> Generating Statistics Table

```scala
generateStatTable(
  query: StreamingQueryUIData): Seq[Node]
```

`generateStatTable` renders the timelines and histograms of the following:

* Input Rate
* Process Rate
* Input Rows
* Batch Duration
* Operation Duration
* [Global Watermark Gap](#generateWatermark)
* [generateAggregatedStateOperators](#generateAggregatedStateOperators)

### <span id="generateWatermark"> Generating Global Watermark Gap

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

### <span id="generateAggregatedStateOperators"> generateAggregatedStateOperators

```scala
generateAggregatedStateOperators(
  query: StreamingQueryUIData,
  minBatchTime: Long,
  maxBatchTime: Long,
  jsCollector: JsCollector): NodeBuffer
```

`generateAggregatedStateOperators` takes [stateOperators](../monitoring/StreamingQueryProgress.md#stateOperators) of the last [StreamingQueryProgress](../monitoring/StreamingQueryProgress.md) of the given streaming query (as `StreamingQueryUIData`).

Unless available, `generateAggregatedStateOperators` returns _nothing_ (an empty collection).

`generateAggregatedStateOperators` generates data points for a timeline and a histogram for `aggregated-num-total-state-rows` as the sum of [numRowsTotal](../monitoring/StateOperatorProgress.md#numRowsTotal) metrics of [stateOperators](../monitoring/StreamingQueryProgress.md#stateOperators).

`generateAggregatedStateOperators`...FIXME

In the end, `generateAggregatedStateOperators` [generateAggregatedCustomMetrics](#generateAggregatedCustomMetrics) based on [spark.sql.streaming.ui.enabledCustomMetricList](#enabledCustomMetrics).

### <span id="generateAggregatedCustomMetrics"> generateAggregatedCustomMetrics

```scala
generateAggregatedCustomMetrics(
  query: StreamingQueryUIData,
  minBatchTime: Long,
  maxBatchTime: Long,
  jsCollector: JsCollector): NodeBuffer
```

`generateAggregatedCustomMetrics` takes the names of the [custom metrics](../monitoring/StateOperatorProgress.md#customMetrics) from the (head) [StateOperatorProgress](../monitoring/StreamingQueryProgress.md#stateOperators) of the latest [StreamingQueryProgress](../monitoring/StreamingQueryProgress.md).

For every custom metric that is included in [spark.sql.streaming.ui.enabledCustomMetricList](#enabledCustomMetrics), `generateAggregatedCustomMetrics`...FIXME

## <span id="enabledCustomMetrics"> Enabled Custom Metric

`StreamingQueryStatisticsPage` uses [spark.sql.streaming.ui.enabledCustomMetricList](../configuration-properties.md#spark.sql.streaming.ui.enabledCustomMetricList) configuration property for the names of the [Supported Custom Metrics](#supportedCustomMetrics) to [generateAggregatedCustomMetrics](#generateAggregatedCustomMetrics) (in addition to [generateAggregatedStateOperators](#generateAggregatedStateOperators)).

`StreamingQueryStatisticsPage` prints out the following `DEBUG` message to the logs with the custom metrics when [created](#creating-instance):

```text
Enabled custom metrics: [enabledCustomMetrics]
```

## <span id="supportedCustomMetrics"> Supported Custom Metrics

`StreamingQueryStatisticsPage` requests the system [StateStoreProvider](../stateful-stream-processing/StateStoreProvider.md) (based on [spark.sql.streaming.stateStore.providerClass](../configuration-properties.md#spark.sql.streaming.stateStore.providerClass) configuration property) for the [supported custom metrics](../stateful-stream-processing/StateStoreProvider.md#supportedCustomMetrics) when [created](#creating-instance).

Only supported custom metrics can be [enabled](#enabledCustomMetrics) to be displayed in the web UI.

`StreamingQueryStatisticsPage` prints out the following `DEBUG` message to the logs with the supported custom metrics when [created](#creating-instance):

```text
Supported custom metrics: [supportedCustomMetrics]
```

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.streaming.ui.StreamingQueryStatisticsPage` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.streaming.ui.StreamingQueryStatisticsPage=ALL
```

Refer to [Logging](../spark-logging.md).
