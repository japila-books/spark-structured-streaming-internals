# KafkaMicroBatchStream

`KafkaMicroBatchStream` is a [MicroBatchStream](../../MicroBatchStream.md) that [SupportsTriggerAvailableNow](../../SupportsTriggerAvailableNow.md).

`KafkaMicroBatchStream` is a [ReportsSourceMetrics](../../ReportsSourceMetrics.md).

## Creating Instance

`KafkaMicroBatchStream` takes the following to be created:

* <span id="kafkaOffsetReader"> [KafkaOffsetReader](KafkaOffsetReader.md)
* [Kafka Params For Executors](#executorKafkaParams)
* <span id="options"> Options
* <span id="metadataPath"> Metadata path
* <span id="startingOffsets"> Starting [KafkaOffsetRangeLimit](KafkaOffsetRangeLimit.md)
* <span id="failOnDataLoss"> [failOnDataLoss](options.md#failOnDataLoss) option

`KafkaMicroBatchStream` is created when:

* `KafkaScan` is requested for a [MicroBatchStream](KafkaScan.md#toMicroBatchStream)

### <span id="executorKafkaParams"> Kafka Params For Executors

`KafkaMicroBatchStream` is given Kafka params to use on executors when [created](#creating-instance).

The Kafka params are the [kafkaParamsForExecutors](KafkaSourceProvider.md#kafkaParamsForExecutors) based on the [options](KafkaScan.md#options) of the [KafkaScan](KafkaScan.md) (this `KafkaMicroBatchStream` is created for) that have been [converted](KafkaSourceProvider.md#convertToSpecifiedParams) (`kafka`-prefix removed).
