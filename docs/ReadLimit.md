# ReadLimit

`ReadLimit` is an abstraction of [limits](#implementations) on how many records to read from a [MicroBatchStream](MicroBatchStream.md) that [SupportsAdmissionControl](SupportsAdmissionControl.md).

`ReadLimit` is used by [stream execution engine](StreamExecution.md)s when they are requested for unique streaming sources:

* [MicroBatchExecution](micro-batch-execution/MicroBatchExecution.md#uniqueSources)
* [ContinuousExecution](continuous-execution/ContinuousExecution.md#uniqueSources)

## Factory Methods (Subset)

### <span id="allAvailable"> ReadAllAvailable

```java
ReadLimit allAvailable()
```

`allAvailable` returns `ReadAllAvailable`.

---

`allAvailable` is used when:

* `SupportsAdmissionControl` is requested for the [default ReadLimit](SupportsAdmissionControl.md#getDefaultReadLimit)
* `KafkaMicroBatchStream` is requested for the [default ReadLimit](kafka/KafkaMicroBatchStream.md#getDefaultReadLimit)
* `KafkaSource` is requested for the [default ReadLimit](kafka/KafkaSource.md#getDefaultReadLimit)
* `AvailableNowDataStreamWrapper` is requested to `prepareForTriggerAvailableNow`, `getDefaultReadLimit`
* `MicroBatchExecution` is requested for the [uniqueSources](micro-batch-execution/MicroBatchExecution.md#uniqueSources)
* `ContinuousExecution` is requested for the [uniqueSources](continuous-execution/ContinuousExecution.md#uniqueSources)

### <span id="compositeLimit"> Creating CompositeReadLimit

```java
ReadLimit compositeLimit(
  ReadLimit[] readLimits)
```

`compositeLimit` creates a [CompositeReadLimit](#CompositeReadLimit) with the given [ReadLimit](ReadLimit.md)s.

!!! note
    `compositeLimit` is used only in [Kafka Data Source](kafka/index.md) when [minOffsetPerTrigger](kafka/options.md#minOffsetPerTrigger) and [maxOffsetsPerTrigger](kafka/options.md#maxOffsetsPerTrigger) options are specified.

---

`compositeLimit` is used when:

* `KafkaMicroBatchStream` is requested for the [default read limit](kafka/KafkaMicroBatchStream.md#getDefaultReadLimit)
* `KafkaSource` is requested for the [default read limit](kafka/KafkaSource.md#getDefaultReadLimit)

### <span id="maxRows"> Creating ReadMaxRows

```java
ReadLimit maxRows(
  long rows)
```

`maxRows` creates a `ReadMaxRows` with the given `rows`.

---

`maxRows` is used when:

* `KafkaMicroBatchStream` is requested for the [default ReadLimit](kafka/KafkaMicroBatchStream.md#getDefaultReadLimit)
* `KafkaSource` is requested for the [default ReadLimit](kafka/KafkaSource.md#getDefaultReadLimit)
* `RatePerMicroBatchStream` is requested for the [default ReadLimit](datasources/rate-micro-batch//RatePerMicroBatchStream.md#getDefaultReadLimit)

### <span id="minRows"> Creating ReadMinRows

```java
ReadLimit minRows(
  long rows,
  long maxTriggerDelayMs)
```

`minRows` creates a [ReadMinRows](#ReadMinRows) with the given `rows` and `maxTriggerDelayMs`.

---

`minRows` is used when:

* `KafkaMicroBatchStream` is requested for the [default ReadLimit](kafka/KafkaMicroBatchStream.md#getDefaultReadLimit)
* `KafkaSource` is requested for the [default ReadLimit](kafka/KafkaSource.md#getDefaultReadLimit)

## Implementations

* [CompositeReadLimit](#CompositeReadLimit)
* `ReadAllAvailable`
* `ReadMaxFiles`
* [ReadMaxRows](#ReadMaxRows)
* [ReadMinRows](#ReadMinRows)

### <span id="CompositeReadLimit"> CompositeReadLimit

`CompositeReadLimit` is a [ReadLimit](ReadLimit.md) that holds other [ReadLimit](ReadLimit.md)s.

`CompositeReadLimit` is created using [ReadLimit.compositeLimit](#compositeLimit) utility.

Used when:

* `KafkaMicroBatchStream` is requested for the [latest offset](kafka/KafkaMicroBatchStream.md#latestOffset)
* `KafkaSource` is requested for the [latest offset](kafka/KafkaSource.md#latestOffset)

### <span id="ReadMaxRows"> ReadMaxRows

`ReadMaxRows` is a [ReadLimit](ReadLimit.md) that is described by the following:

Attribute | Description
----------|------------
 `rows` | Approximate maximum rows to scan (_maxRows_)

`ReadMaxRows` is created using [ReadLimit.maxRows](#maxRows) utility.

Used when:

* `KafkaMicroBatchStream` is requested for the [latestOffset](kafka/KafkaMicroBatchStream.md#latestOffset)
* `KafkaSource` is requested for the [latestOffset](kafka/KafkaSource.md#latestOffset)

### <span id="ReadMinRows"> ReadMinRows

`ReadMinRows` is a [ReadLimit](ReadLimit.md) that is described by the following:

Attribute | Description
----------|------------
 `rows` | Approximate minimum rows to scan (_minRows_)
 `maxTriggerDelayMs` | Approximate maximum trigger delay

`ReadMinRows` is created using [ReadLimit.minRows](#minRows) utility.

Used when:

* `KafkaMicroBatchStream` is requested for the [latestOffset](kafka/KafkaMicroBatchStream.md#latestOffset)
* `KafkaSource` is requested for the [latestOffset](kafka/KafkaSource.md#latestOffset)
