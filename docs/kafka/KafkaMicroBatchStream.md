# KafkaMicroBatchStream

`KafkaMicroBatchStream` is a [MicroBatchStream](../MicroBatchStream.md) for [Kafka Data Source](index.md) for [Micro-Batch Stream Processing](../micro-batch-execution/index.md).

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

## <span id="SupportsTriggerAvailableNow"> SupportsTriggerAvailableNow

`KafkaMicroBatchStream` is a [SupportsTriggerAvailableNow](../SupportsTriggerAvailableNow.md).

### <span id="prepareForTriggerAvailableNow"> prepareForTriggerAvailableNow

```scala
prepareForTriggerAvailableNow(): Unit
```

`prepareForTriggerAvailableNow` is part of the [SupportsTriggerAvailableNow](../SupportsTriggerAvailableNow.md#prepareForTriggerAvailableNow) abstraction.

---

`prepareForTriggerAvailableNow` sets the [allDataForTriggerAvailableNow](#allDataForTriggerAvailableNow) internal registry to [fetchLatestOffsets](KafkaOffsetReader.md#fetchLatestOffsets) (of the [KafkaOffsetReader](#kafkaOffsetReader)) for [getOrCreateInitialPartitionOffsets](#getOrCreateInitialPartitionOffsets).

## <span id="ReportsSourceMetrics"> ReportsSourceMetrics

`KafkaMicroBatchStream` is a [ReportsSourceMetrics](../ReportsSourceMetrics.md).

### <span id="metrics"> Performance Metrics

```scala
metrics(
  latestConsumedOffset: Optional[Offset]): Map[String, String]
```

`metrics` is part of the [ReportsSourceMetrics](../ReportsSourceMetrics.md#metrics) abstraction.

---

`metrics` returns the [metrics](#metrics-util) for the given `latestConsumedOffset` and the [latestPartitionOffsets](#latestPartitionOffsets).

#### <span id="metrics-util"> metrics

```scala
metrics(
  latestConsumedOffset: Optional[Offset],
  latestAvailablePartitionOffsets: Map[TopicPartition, Long]): Map[String, String]
```

`metrics` converts the given `latestConsumedOffset` to a [KafkaSourceOffset](KafkaSourceOffset.md) when defined.

`metrics` returns the following performance metrics for the offsets in the given `latestAvailablePartitionOffsets` behind the latest [partitionToOffsets](KafkaSourceOffset.md#partitionToOffsets) (of the given `latestConsumedOffset`):

* `minOffsetsBehindLatest`
* `maxOffsetsBehindLatest`
* `avgOffsetsBehindLatest`

## <span id="getDefaultReadLimit"> Default Read Limit

```scala
getDefaultReadLimit: ReadLimit
```

`getDefaultReadLimit` is part of the [SupportsAdmissionControl](../SupportsAdmissionControl.md#getDefaultReadLimit) abstraction.

---

`getDefaultReadLimit` uses [minOffsetsPerTrigger](#minOffsetPerTrigger) and [maxOffsetsPerTrigger](#maxOffsetsPerTrigger) options to determine [ReadLimit](../ReadLimit.md).

`getDefaultReadLimit` uses [maxTriggerDelayMs](#maxTriggerDelayMs) option, too, but it has a default value so it is always available.

ReadLimit | Condition
----------|----------
 [CompositeReadLimit](../ReadLimit.md#CompositeReadLimit) | Both [minOffsetsPerTrigger](#minOffsetPerTrigger) and [maxOffsetsPerTrigger](#maxOffsetsPerTrigger) defined
 [ReadMinRows](../ReadLimit.md#ReadMinRows) | Only [minOffsetPerTrigger](#minOffsetPerTrigger) defined
 [ReadMaxRows](../ReadLimit.md#ReadMaxRows) | Only [maxOffsetsPerTrigger](#maxOffsetsPerTrigger) defined
 [ReadAllAvailable](../ReadLimit.md#allAvailable) |

---

In other words, with [minOffsetsPerTrigger](#minOffsetPerTrigger) and [maxOffsetsPerTrigger](#maxOffsetsPerTrigger) defined, `getDefaultReadLimit` [creates a CompositeReadLimit](../ReadLimit.md#compositeLimit) with the following:

* [ReadMinRows](../ReadLimit.md#minRows) with [minOffsetsPerTrigger](#minOffsetPerTrigger) (and [maxTriggerDelayMs](#maxTriggerDelayMs))
* [ReadMaxRows](../ReadLimit.md#maxRows) with [maxOffsetsPerTrigger](#maxOffsetsPerTrigger)

With only [minOffsetPerTrigger](#minOffsetPerTrigger) defined (with no [maxOffsetsPerTrigger](#maxOffsetsPerTrigger)), `getDefaultReadLimit` [creates a ReadMinRows](../ReadLimit.md#minRows) with [minOffsetsPerTrigger](#minOffsetPerTrigger) (and [maxTriggerDelayMs](#maxTriggerDelayMs)).

Otherwise, `getDefaultReadLimit` takes the [maxOffsetsPerTrigger](#maxOffsetsPerTrigger), if defined, and creates a `ReadMaxRows` (with the approximate maximum rows to scan) or defaults to [ReadAllAvailable](../ReadLimit.md#allAvailable).

### <span id="maxOffsetsPerTrigger"> maxOffsetsPerTrigger

```scala
maxOffsetsPerTrigger: Option[Long]
```

`KafkaMicroBatchStream` takes the value of [maxOffsetsPerTrigger](options.md#maxOffsetsPerTrigger) option (in the [options](#options)), if available, when [created](#creating-instance). Otherwise, `maxOffsetsPerTrigger` is `None` (undefined).

### <span id="minOffsetPerTrigger"> minOffsetPerTrigger

```scala
minOffsetPerTrigger: Option[Long]
```

When [created](#creating-instance), `KafkaMicroBatchStream` takes the value of [minOffsetsPerTrigger](options.md#minOffsetsPerTrigger) option (in the [options](#options)), if available, or defaults to `None` (undefined).

`minOffsetPerTrigger` is used to determine the [default limit on the number of records to read](#getDefaultReadLimit).

### <span id="maxTriggerDelayMs"> maxTriggerDelayMs

`KafkaMicroBatchStream` reads the value of [maxTriggerDelay](options.md#maxTriggerDelay) option (in the [options](#options)) when [created](#creating-instance).

## <span id="latestOffset"> Latest Offset

```scala
latestOffset(
  start: Offset,
  readLimit: ReadLimit): Offset
```

`latestOffset` is part of the [SupportsAdmissionControl](../SupportsAdmissionControl.md#latestOffset) abstraction.

---

`latestOffset` converts the given `start` offset to a [KafkaSourceOffset](KafkaSourceOffset.md) to request for the [partitionToOffsets](KafkaSourceOffset.md#partitionToOffsets).

`latestOffset` sets the [latestPartitionOffsets](#latestPartitionOffsets) internal registry to be as follows:

* [allDataForTriggerAvailableNow](#allDataForTriggerAvailableNow), if available
* [fetchLatestOffsets](KafkaOffsetReader.md#fetchLatestOffsets) of the [KafkaOffsetReader](#kafkaOffsetReader) (for the [partitionToOffsets](KafkaSourceOffset.md#partitionToOffsets) of the given [KafkaSourceOffset](KafkaSourceOffset.md)), otherwise

!!! note "FIXME"
    When is [allDataForTriggerAvailableNow](#allDataForTriggerAvailableNow) available?

`latestOffset` requests the given [ReadLimit](../ReadLimit.md) for read limits if it is a [CompositeReadLimit](../ReadLimit.md#CompositeReadLimit). Otherwise, `latestOffset` uses the given [ReadLimit](../ReadLimit.md) as the only read limit.

`latestOffset` determines the offsets to read based on the read limits.

* With [ReadAllAvailable](../ReadLimit.md#ReadAllAvailable) among the read limits, `latestOffset` uses the [latestPartitionOffsets](#latestPartitionOffsets) registry.

    `ReadAllAvailable` has the highest priority as it is necessary for `Trigger.Once` to work properly.

* With [ReadMinRows](../ReadLimit.md#ReadMinRows) among the read limits, `latestOffset` [checks whether to skip this trigger or not](#delayBatch) (using the `minRows` and `maxTriggerDelayMs` of this `ReadMinRows` as well as the [latestPartitionOffsets](#latestPartitionOffsets) and the [partitionToOffsets](KafkaSourceOffset.md#partitionToOffsets) of the given [KafkaSourceOffset](KafkaSourceOffset.md)).

    If there is not enough rows available (based on `minRows`) or `maxTriggerDelayMs` has not elapsed yet, `latestOffset` prints out the following DEBUG message to the logs:

    ```text
    Delaying batch as number of records available is less than minOffsetsPerTrigger
    ```

* With [ReadMaxRows](../ReadLimit.md#ReadMaxRows) among the read limits, `latestOffset` [rateLimit](#rateLimit) (with the `maxRows` as well as the [latestPartitionOffsets](#latestPartitionOffsets) and the [partitionToOffsets](KafkaSourceOffset.md#partitionToOffsets) of the given [KafkaSourceOffset](KafkaSourceOffset.md)).

* With neither [ReadMinRows](../ReadLimit.md#ReadMinRows) nor [ReadMaxRows](../ReadLimit.md#ReadMaxRows) among the read limits, `latestOffset` uses the [latestPartitionOffsets](#latestPartitionOffsets) registry (as if [ReadAllAvailable](../ReadLimit.md#ReadAllAvailable) were among the read limits).

In the end, `latestOffset` records the offsets in the [endPartitionOffsets](#endPartitionOffsets) registry.

!!! note "Summary"
    [endPartitionOffsets](#endPartitionOffsets) can be as follows based on the read limits:

    * [latestPartitionOffsets](#latestPartitionOffsets) for `ReadAllAvailable`
    * [partitionToOffsets](KafkaSourceOffset.md#partitionToOffsets) of the given [KafkaSourceOffset](KafkaSourceOffset.md) for `ReadMinRows` and a [batch delayed](#delayBatch)
    * [rateLimit](#rateLimit) for `ReadMaxRows`
    * [latestPartitionOffsets](#latestPartitionOffsets)

### <span id="delayBatch"> delayBatch

```scala
delayBatch(
  minLimit: Long,
  latestOffsets: Map[TopicPartition, Long],
  currentOffsets: Map[TopicPartition, Long],
  maxTriggerDelayMs: Long): Boolean
```

!!! note "Summary"
    `delayBatch` is `false` (_no delay_) when either holds:

    * The given `maxTriggerDelayMs` has passed since [lastTriggerMillis](#lastTriggerMillis)
    * The total of new records (across all topics and partitions given `latestOffsets` and `currentOffsets`) is at least the given `minLimit`

    Otherwise, `delayBatch` is `true` (_delay_).

Input Parameter | Value
----------------|------
 `minLimit` | [minOffsetPerTrigger](#minOffsetPerTrigger)
 `latestOffsets` | [latestPartitionOffsets](#latestPartitionOffsets)
 `currentOffsets` | [Offsets by Partitions](KafkaSourceOffset.md#partitionToOffsets) of the [start offset](KafkaSourceOffset.md) (as given to [latestOffset](#latestOffset))
 `maxTriggerDelayMs` | [maxTriggerDelayMs](#maxTriggerDelayMs)

---

If the given `maxTriggerDelayMs` has passed (since [lastTriggerMillis](#lastTriggerMillis)), `delayBatch` prints out the following DEBUG message to the logs, records the current timestamp in [lastTriggerMillis](#lastTriggerMillis) registry and returns `false` (_no delay_).

```text
Maximum wait time is passed, triggering batch
```

Otherwise, `delayBatch` calculates the number of new records (based on the given `latestOffsets` and `currentOffsets`).

If the number of new records is below the given `minLimit`, `delayBatch` returns `true` (_delay_). Otherwise, `delayBatch` records the current timestamp in [lastTriggerMillis](#lastTriggerMillis) registry and returns `false` (_no delay_).

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

## <span id="toString"> String Representation

```scala
toString(): String
```

`toString` is part of the `Object` ([Java]({{ java.api }}/java/lang/Object.html#toString())) abstraction.

---

`toString` is the following (with the string representation of the [KafkaOffsetReader](#kafkaOffsetReader)):

```text
KafkaV2[[kafkaOffsetReader]]
```

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.kafka010.KafkaMicroBatchStream` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.kafka010.KafkaMicroBatchStream=ALL
```

Refer to [Logging](../spark-logging.md).
