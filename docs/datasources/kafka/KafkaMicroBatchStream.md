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
* [failOnDataLoss](#failOnDataLoss)

`KafkaMicroBatchStream` is created when:

* `KafkaScan` is requested for a [MicroBatchStream](KafkaScan.md#toMicroBatchStream)

### <span id="executorKafkaParams"> Kafka Params For Executors

`KafkaMicroBatchStream` is given Kafka params to use on executors when [created](#creating-instance).

The Kafka params are the [kafkaParamsForExecutors](KafkaSourceProvider.md#kafkaParamsForExecutors) based on the [options](KafkaScan.md#options) of the [KafkaScan](KafkaScan.md) (this `KafkaMicroBatchStream` is created for) that have been [converted](KafkaSourceProvider.md#convertToSpecifiedParams) (`kafka`-prefix removed).

### <span id="failOnDataLoss"> failOnDataLoss

```scala
failOnDataLoss: Boolean
```

`KafkaMicroBatchStream` is given `failOnDataLoss` flag when [created](#creating-instance).

`failOnDataLoss` is the value of [failOnDataLoss](options.md#failOnDataLoss) option.

`failOnDataLoss` flag is used for the following:

* [planInputPartitions](#planInputPartitions) (to create [KafkaBatchInputPartition](KafkaBatchInputPartition.md#failOnDataLoss))
* [reportDataLoss](#reportDataLoss)

## <span id="getDefaultReadLimit"> Default ReadLimit

```scala
getDefaultReadLimit: ReadLimit
```

`getDefaultReadLimit` is part of the [SupportsAdmissionControl](../../SupportsAdmissionControl.md#getDefaultReadLimit) abstraction.

---

With [minOffsetsPerTrigger](#minOffsetPerTrigger) and [maxOffsetsPerTrigger](#maxOffsetsPerTrigger) defined, `getDefaultReadLimit` [creates a CompositeReadLimit](../../ReadLimit.md#compositeLimit) with the following:

* [ReadMinRows](../../ReadLimit.md#minRows) with [minOffsetsPerTrigger](#minOffsetPerTrigger) (and [maxTriggerDelayMs](#maxTriggerDelayMs))
* [ReadMaxRows](../../ReadLimit.md#maxRows) with [maxOffsetsPerTrigger](#maxOffsetsPerTrigger)

With only [minOffsetPerTrigger](#minOffsetPerTrigger) defined (with no [maxOffsetsPerTrigger](#maxOffsetsPerTrigger)), `getDefaultReadLimit` [creates a ReadMinRows](../../ReadLimit.md#minRows) with [minOffsetsPerTrigger](#minOffsetPerTrigger) (and [maxTriggerDelayMs](#maxTriggerDelayMs)).

Otherwise, `getDefaultReadLimit` takes the [maxOffsetsPerTrigger](#maxOffsetsPerTrigger), if defined, and creates a `ReadMaxRows` (with the approximate maximum rows to scan) or defaults to [ReadAllAvailable](../../ReadLimit.md#allAvailable).

### <span id="maxOffsetsPerTrigger"> maxOffsetsPerTrigger

```scala
maxOffsetsPerTrigger: Option[Long]
```

`KafkaMicroBatchStream` takes the value of [maxOffsetsPerTrigger](options.md#maxOffsetsPerTrigger) option (in the [options](#options)), if available, when [created](#creating-instance).

### <span id="minOffsetPerTrigger"> minOffsetPerTrigger

```scala
minOffsetPerTrigger: Option[Long]
```

`KafkaMicroBatchStream` takes the value of [minOffsetsPerTrigger](options.md#minOffsetsPerTrigger) option (in the [options](#options)), if available, when [created](#creating-instance).

### <span id="maxTriggerDelayMs"> maxTriggerDelayMs

`KafkaMicroBatchStream` reads the value of [maxTriggerDelay](options.md#maxTriggerDelay) option (in the [options](#options)) when [created](#creating-instance).

## <span id="latestOffset"> latestOffset

```scala
latestOffset(
  start: Offset,
  readLimit: ReadLimit): Offset
```

`latestOffset` is part of the [SupportsAdmissionControl](../../SupportsAdmissionControl.md#latestOffset) abstraction.

---

`latestOffset`...FIXME

## <span id="reportDataLoss"> reportDataLoss

```scala
reportDataLoss(message: String): Unit
```

With [failOnDataLoss](#failOnDataLoss) enabled, `reportDataLoss` throws an `IllegalStateException` (with the given `message`):

```text
[message]. Some data may have been lost because they are not available in Kafka any more;
either the data was aged out by Kafka or the topic may have been deleted before all the data in the topic was processed.
If you don't want your streaming query to fail on such cases, set the source option "failOnDataLoss" to "false"
```

Otherwise, `reportDataLoss` prints out the following WARN message (with the given `message`) to the logs:

```text
[message]. Some data may have been lost because they are not available in Kafka any more;
either the data was aged out by Kafka or the topic may have been deleted before all the data in the
topic was processed.
If you want your streaming query to fail on such cases, set the source option "failOnDataLoss" to "true"
```

---

`reportDataLoss` is used when:

* `KafkaMicroBatchStream` is requested to [planInputPartitions](#planInputPartitions) and [getOrCreateInitialPartitionOffsets](#getOrCreateInitialPartitionOffsets)

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.kafka010.KafkaMicroBatchStream` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.kafka010.KafkaMicroBatchStream=ALL
```

Refer to [Logging](../../spark-logging.md).
