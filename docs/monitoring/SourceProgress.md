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
* [Metrics](#metrics)

`SourceProgress` is created when:

* `ProgressReporter` is requested to [finishTrigger](../ProgressReporter.md#finishTrigger) (and [update stream progress](../ProgressReporter.md#updateProgress) with a new [StreamingQueryProgress](StreamingQueryProgress.md#sources))

### <span id="metrics"> Metrics

```java
metrics: Map[String, String]
```

`SourceProgress` is given a `metrics` that is the [metrics](../ReportsSourceMetrics.md#metrics) for this progress of [ReportsSourceMetrics](../ReportsSourceMetrics.md) data stream.

`metrics` is used in [jsonValue](#jsonValue).

## <span id="jsonValue"> Converting to JSON Representation

```scala
jsonValue: JValue
```

`jsonValue` converts this `SourceProgress` to JSON representation.

---

`jsonValue` is used when:

* `StreamingQueryProgress` is requested for [jsonValue](StreamingQueryProgress.md#jsonValue)
* `SourceProgress` is requested to [json](#json), [prettyJson](#prettyJson)
