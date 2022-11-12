# ReportsSourceMetrics

`ReportsSourceMetrics` is an [extension](#contract) of the [SparkDataStream](SparkDataStream.md) abstraction for [data streams](#implementations) with [metrics](#metrics).

## Contract

### <span id="metrics"> Performance Metrics

```java
Map<String, String> metrics(
  Optional<Offset> latestConsumedOffset)
```

Metrics of this [SparkDataStream](SparkDataStream.md) for the latest consumed offset (to create a [SourceProgress](monitoring/SourceProgress.md#metrics) for a [StreamingQueryProgress](monitoring/StreamingQueryProgress.md#sources))

See [KafkaMicroBatchStream](kafka/KafkaMicroBatchStream.md#metrics)

Used when:

* `ProgressReporter` is requested to [finishTrigger](monitoring/ProgressReporter.md#finishTrigger)

## Implementations

* [KafkaMicroBatchStream](kafka/KafkaMicroBatchStream.md)
