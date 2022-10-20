# SourceProgress

`SourceProgress` is a `Serializable` representation of progress of a [SparkDataStream](../SparkDataStream.md) (in the execution of a [StreamingQuery](../StreamingQuery.md) during a trigger).

## Creating Instance

`SourceProgress` takes the following to be created:

* <span id="description"> `description`
* <span id="startOffset"> `startOffset`
* <span id="endOffset"> `endOffset`
* <span id="latestOffset"> `latestOffset`
* <span id="numInputRows"> `numInputRows`
* <span id="inputRowsPerSecond"> `inputRowsPerSecond`
* <span id="processedRowsPerSecond"> `processedRowsPerSecond`
* <span id="metrics"> `metrics`

`SourceProgress` is created when:

* `ProgressReporter` is requested to [finishTrigger](ProgressReporter.md#finishTrigger) (and [update stream progress](ProgressReporter.md#updateProgress) with a new [StreamingQueryProgress](StreamingQueryProgress.md#sources))
