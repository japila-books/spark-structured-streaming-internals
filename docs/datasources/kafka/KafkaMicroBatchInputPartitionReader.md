# KafkaMicroBatchInputPartitionReader

`KafkaMicroBatchInputPartitionReader` is an `InputPartitionReader` (of `InternalRows`) that is <<creating-instance, created>> exclusively when `KafkaMicroBatchInputPartition` is requested for [one](KafkaMicroBatchInputPartition.md#createPartitionReader) (as a part of the `InputPartition` contract).

## Creating Instance

`KafkaMicroBatchInputPartitionReader` takes the following to be created:

* [[offsetRange]] [KafkaOffsetRange](KafkaOffsetRangeCalculator.md#KafkaOffsetRange)
* [[executorKafkaParams]] Kafka parameters used for Kafka clients on executors (`Map[String, Object]`)
* [[pollTimeoutMs]] Poll timeout (in ms)
* [[failOnDataLoss]] `failOnDataLoss` flag
* [[reuseKafkaConsumer]] `reuseKafkaConsumer` flag

NOTE: All the input arguments to create a `KafkaMicroBatchInputPartitionReader` are exactly the input arguments used to create a [KafkaMicroBatchInputPartition](KafkaMicroBatchInputPartition.md).

`KafkaMicroBatchInputPartitionReader` initializes the <<internal-properties, internal properties>>.

=== [[next]] `next` Method

[source, scala]
----
next(): Boolean
----

NOTE: `next` is part of the `InputPartitionReader` contract to proceed to next record if available (`true`).

`next` checks whether the <<consumer, KafkaDataConsumer>> should <<next-poll, poll records>> or <<next-no-poll, not>> (i.e. <<nextOffset, nextOffset>> is smaller than the [untilOffset](KafkaOffsetRangeCalculator.md#untilOffset) of the <<rangeToRead, KafkaOffsetRange>>).

==== [[next-poll]] `next` Method -- KafkaDataConsumer Polls Records

If so, `next` requests the <<consumer, KafkaDataConsumer>> to get (_poll_) records in the range of <<nextOffset, nextOffset>> and the [untilOffset](KafkaOffsetRangeCalculator.md#untilOffset) (of the <<rangeToRead, KafkaOffsetRange>>) with the given <<pollTimeoutMs, pollTimeoutMs>> and <<failOnDataLoss, failOnDataLoss>>.

With a new record, `next` requests the <<converter, KafkaRecordToUnsafeRowConverter>> to convert (`toUnsafeRow`) the record to be the <<nextRow, next UnsafeRow>>. `next` sets the <<nextOffset, nextOffset>> as the offset of the record incremented. `next` returns `true`.

With no new record, `next` simply returns `false`.

==== [[next-no-poll]] `next` Method -- No Polling

If the <<nextOffset, nextOffset>> is equal or larger than the [untilOffset](KafkaOffsetRangeCalculator.md#untilOffset) (of the <<rangeToRead, KafkaOffsetRange>>), `next` simply returns `false`.

=== [[close]] Closing (Releasing KafkaDataConsumer) -- `close` Method

[source, scala]
----
close(): Unit
----

NOTE: `close` is part of the Java Closeable contract to release resources.

`close` simply requests the <<consumer, KafkaDataConsumer>> to `release`.

=== [[resolveRange]] `resolveRange` Internal Method

[source, scala]
----
resolveRange(
  range: KafkaOffsetRange): KafkaOffsetRange
----

`resolveRange`...FIXME

NOTE: `resolveRange` is used exclusively when `KafkaMicroBatchInputPartitionReader` is <<creating-instance, created>> (and initializes the <<rangeToRead, KafkaOffsetRange>> internal property).

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| consumer
a| [[consumer]] [KafkaDataConsumer](KafkaDataConsumer.md) for the partition (per <<offsetRange, KafkaOffsetRange>>)

Used in <<next, next>>, <<close, close>>, and <<resolveRange, resolveRange>>

| converter
a| [[converter]] `KafkaRecordToUnsafeRowConverter`

| nextOffset
a| [[nextOffset]] Next offset

| nextRow
a| [[nextRow]] Next `UnsafeRow`

| rangeToRead
a| [[rangeToRead]] `KafkaOffsetRange`

|===
