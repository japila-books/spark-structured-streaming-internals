# RatePerMicroBatchStream

`RatePerMicroBatchStream` is a [MicroBatchStream](../../MicroBatchStream.md) that [SupportsTriggerAvailableNow](../../SupportsTriggerAvailableNow.md).

## Creating Instance

`RatePerMicroBatchStream` takes the following to be created:

* <span id="rowsPerBatch"> [rowsPerBatch](options.md#rowsPerBatch)
* <span id="numPartitions"> [numPartitions](options.md#numPartitions)
* <span id="startTimestamp"> [startTimestamp](options.md#startTimestamp)
* <span id="advanceMsPerBatch"> [advanceMsPerBatch](options.md#advanceMsPerBatch)
* <span id="options"> [Options](options.md)

`RatePerMicroBatchStream` is created when:

* `RatePerMicroBatchTable` is requested to [create a ScanBuilder](RatePerMicroBatchTable.md#newScanBuilder)

## <span id="isTriggerAvailableNow"> isTriggerAvailableNow Flag

`RatePerMicroBatchStream` defines `isTriggerAvailableNow` flag to determine whether it is executed in [Trigger.AvailableNow](../../Trigger.md#AvailableNow) mode or not (based on [prepareForTriggerAvailableNow](#prepareForTriggerAvailableNow)).

By default, `isTriggerAvailableNow` flag is off (`false`).

`isTriggerAvailableNow` is used when:

* `RatePerMicroBatchStream` is requested for the [latest offset](#latestOffset)

### <span id="prepareForTriggerAvailableNow"> prepareForTriggerAvailableNow

```scala
prepareForTriggerAvailableNow(): Unit
```

`prepareForTriggerAvailableNow` is part of the [SupportsTriggerAvailableNow](../../SupportsTriggerAvailableNow.md#prepareForTriggerAvailableNow) abstraction.

---

`prepareForTriggerAvailableNow` turns the [isTriggerAvailableNow](#isTriggerAvailableNow) flag on (for [Trigger.AvailableNow](../../Trigger.md#AvailableNow) mode).

## <span id="latestOffset"> Latest Offset

```scala
latestOffset(
  startOffset: Offset,
  limit: ReadLimit): Offset
```

`latestOffset` is part of the [SupportsAdmissionControl](../../SupportsAdmissionControl.md#latestOffset) abstraction.

---

`latestOffset` is different whether it is executed in [Trigger.AvailableNow](../../Trigger.md#AvailableNow) mode or not (based on [isTriggerAvailableNow](#isTriggerAvailableNow) flag).

`latestOffset` [calculates next offset](#calculateNextOffset) just once for [Trigger.AvailableNow](#isTriggerAvailableNow) and the [offsetForTriggerAvailableNow](#offsetForTriggerAvailableNow) registry has no value assigned yet.

In other words, for [isTriggerAvailableNow](#isTriggerAvailableNow), `latestOffset` returns the value of the [offsetForTriggerAvailableNow](#offsetForTriggerAvailableNow) registry (that remains the same for every `latestOffset`).

For all the other triggers (when [isTriggerAvailableNow](#isTriggerAvailableNow) is disabled), `latestOffset` [calculates next offset](#calculateNextOffset).

### <span id="calculateNextOffset"> Calculating Next Offset

```scala
calculateNextOffset(
  start: Offset): Offset
```

`calculateNextOffset` [extractOffsetAndTimestamp](#extractOffsetAndTimestamp) from the given start [Offset](../../Offset.md).

`calculateNextOffset` creates a `RatePerMicroBatchStreamOffset` for the end offset (that is [rowsPerBatch](#rowsPerBatch) rows long):

* Increments the start offset by the [rowsPerBatch](#rowsPerBatch) for the end offset
* Increments the start offset timestamp by the [advanceMsPerBatch](#advanceMsPerBatch) for the end offset timestamp
