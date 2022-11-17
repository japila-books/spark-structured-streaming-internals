# MetricsReporter

`MetricsReporter` is a metrics `Source` ([Spark Core]({{ book.spark_core }}/metrics/Source/)) to report [metrics](#gauges) of a [streaming query](../StreamExecution.md#streamMetrics).

`MetricsReporter` uses the [last StreamingQueryProgress](../ProgressReporter.md#lastProgress) (of the [StreamExecution](#stream)) if available or simply defaults to a "zero" value.

!!! note "spark.sql.streaming.metricsEnabled"
    `MetricsReporter` is registered when [spark.sql.streaming.metricsEnabled](../configuration-properties.md#spark.sql.streaming.metricsEnabled) is enabled.

## Creating Instance

`MetricsReporter` takes the following to be created:

* <span id="stream"> [StreamExecution](../StreamExecution.md)
* [Source Name](#sourceName)

`MetricsReporter` is created when:

* `StreamExecution` is requested for the [MetricsReporter](../StreamExecution.md#streamMetrics)

### <span id="sourceName"> Source Name

`MetricsReporter` is given a source name when [created](#creating-instance).

The source name is of the format (with the [name](../StreamExecution.md#name) if available or [id](../StreamExecution.md#id)):

```text
spark.streaming.[name or id]
```

## Gauges

### <span id="inputRate-total"> inputRate-total

Reports [inputRowsPerSecond](StreamingQueryProgress.md#inputRowsPerSecond) (across all streaming sources)

### <span id="processingRate-total"> processingRate-total

Reports [processedRowsPerSecond](StreamingQueryProgress.md#processedRowsPerSecond) (across all streaming sources)

### <span id="latency"> latency

Reports `triggerExecution` duration of the last [StreamingQueryProgress](StreamingQueryProgress.md#durationMs)

### <span id="eventTime-watermark"> eventTime-watermark

Reports `watermark` of the last [StreamingQueryProgress](StreamingQueryProgress.md#eventTime)

Format: `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`

### <span id="states-rowsTotal"> states-rowsTotal

Reports the total of [numRowsTotal](StateOperatorProgress.md#numRowsTotal) of all [StateOperatorProgress](StreamingQueryProgress.md#stateOperators)es of the last [StreamingQueryProgress](StreamingQueryProgress.md)

### <span id="states-usedBytes"> states-usedBytes

Reports the total of [memoryUsedBytes](StateOperatorProgress.md#memoryUsedBytes) of all [StateOperatorProgress](StreamingQueryProgress.md#stateOperators)es of the last [StreamingQueryProgress](StreamingQueryProgress.md)
