# MetricsReporter

`MetricsReporter` is a Metrics Source for [streaming queries](../StreamExecution.md#streamMetrics).

`MetricsReporter` uses the [last StreamingQueryProgress](ProgressReporter.md#lastProgress) (of the [StreamExecution](#stream)) if available or simply defaults to a "zero" value.

## Creating Instance

`MetricsReporter` takes the following to be created:

* <span id="stream"> [StreamExecution](../StreamExecution.md)
* <span id="sourceName"> Source Name

`MetricsReporter` is created for [stream execution engines](../StreamExecution.md#streamMetrics).

## Gauges

### inputRate-total

Reports [inputRowsPerSecond](StreamingQueryProgress.md#inputRowsPerSecond) (across all streaming sources)

### processingRate-total

Reports [processedRowsPerSecond](StreamingQueryProgress.md#processedRowsPerSecond) (across all streaming sources)

### latency

Reports `triggerExecution` duration of the last [StreamingQueryProgress](StreamingQueryProgress.md#durationMs)

### eventTime-watermark

Reports `watermark` of the last [StreamingQueryProgress](StreamingQueryProgress.md#eventTime)

Format: `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`

### states-rowsTotal

Reports the total of [numRowsTotal](StateOperatorProgress.md#numRowsTotal) of all [StateOperatorProgress](StreamingQueryProgress.md#stateOperators)es of the last [StreamingQueryProgress](StreamingQueryProgress.md)

### states-usedBytes

Reports the total of [memoryUsedBytes](StateOperatorProgress.md#memoryUsedBytes) of all [StateOperatorProgress](StreamingQueryProgress.md#stateOperators)es of the last [StreamingQueryProgress](StreamingQueryProgress.md)
