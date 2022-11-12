# KafkaScan

!!! note "The Internals of Spark SQL"
    Head over to [The Internals of Spark SQL]({{ book.spark_sql }}/kafka/KafkaScan) to learn more.

`KafkaScan` is a logical `Scan` ([Spark SQL]({{ book.spark_sql }}/connector/Scan)).

## Creating Instance

`KafkaScan` takes the following to be created:

* <span id="options"> Options

`KafkaScan` is created when:

* `KafkaTable` ([Spark SQL]({{ book.spark_sql }}/kafka/KafkaTable)) is requested for a `ScanBuilder` ([Spark SQL]({{ book.spark_sql }}/kafka/KafkaTable#newScanBuilder))

## <span id="toMicroBatchStream"> MicroBatchStream

```scala
toMicroBatchStream(
  checkpointLocation: String): MicroBatchStream
```

`toMicroBatchStream` is part of the `Scan` ([Spark SQL]({{ book.spark_sql }}/connector/Scan#toMicroBatchStream)) abstraction.

---

`toMicroBatchStream` [validates](#validateStreamOptions) the streaming part of the [options](#options).

`toMicroBatchStream` [generate a unique group ID](#streamingUniqueGroupId).

`toMicroBatchStream` [removes kafka prefix](#convertToSpecifiedParams) from the keys in the [options](#options).

In the end, `toMicroBatchStream` creates a [KafkaMicroBatchStream](KafkaMicroBatchStream.md) with the following:

* A new [KafkaOffsetReader](KafkaOffsetReader.md#build) with the [strategy](#strategy) (from the [options](#options)) and the [kafkaParamsForDriver](#kafkaParamsForDriver)
* [kafkaParamsForExecutors](#kafkaParamsForExecutors) with the [options](#options) with the `kafka.` prefix removed and an [unique group ID](#streamingUniqueGroupId)
* The given `checkpointLocation`
* [Starting offsets](KafkaSourceProvider.md#getKafkaOffsetRangeLimit) using the [options](#options) (with [startingtimestamp](options.md#startingtimestamp), [startingoffsetsbytimestamp](options.md#startingoffsetsbytimestamp) and [startingOffsets](options.md#startingoffsets)); [LatestOffsetRangeLimit](KafkaOffsetRangeLimit.md#LatestOffsetRangeLimit) is the default
* [failOnDataLoss](options.md#failOnDataLoss) option

## <span id="supportedCustomMetrics"> supportedCustomMetrics

```scala
supportedCustomMetrics(): Array[CustomMetric]
```

`supportedCustomMetrics` is part of the `Scan` ([Spark SQL]({{ book.spark_sql }}/connector/Scan#supportedCustomMetrics)) abstraction.

---

`supportedCustomMetrics` is the following metrics:

* `dataLoss`
* `offsetOutOfRange`
