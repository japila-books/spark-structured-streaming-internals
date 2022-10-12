# KafkaSource

`KafkaSource` is a [streaming source](../../Source.md) that [loads data from Apache Kafka](#getBatch).

!!! note
    Kafka topics are checked for new records every [trigger](../../Trigger.md) and so there is some noticeable delay between when the records have arrived to Kafka topics and when a Spark application processes them.

`KafkaSource` uses the [metadata log directory](#metadataPath) to persist offsets. The directory is the source ID under the `sources` directory in the [checkpointRoot](../../StreamExecution.md#checkpointRoot) (of the [StreamExecution](../../StreamExecution.md)).

!!! note
    The [checkpointRoot](../../StreamExecution.md#checkpointRoot) directory is one of the following:

    * `checkpointLocation` option
    * [spark.sql.streaming.checkpointLocation](../../configuration-properties.md#spark.sql.streaming.checkpointLocation) configuration property

`KafkaSource` <<creating-instance, is created>> for **kafka** format (that is registered by [KafkaSourceProvider](KafkaSourceProvider.md#shortName)).

![KafkaSource and KafkaSourceProvider](../../images/KafkaSource-creating-instance.png)

[[schema]]
`KafkaSource` uses a [predefined (fixed) schema](index.md#schema) (that [cannot be changed](KafkaSourceProvider.md#sourceSchema)).

`KafkaSource` also supports batch Datasets.

## Creating Instance

`KafkaSource` takes the following to be created:

* <span id="sqlContext"> `SQLContext`
* <span id="kafkaReader"> [KafkaOffsetReader](KafkaOffsetReader.md)
* <span id="executorKafkaParams"> Parameters of executors (reading from Kafka)
* <span id="sourceOptions"> Source Options
* <span id="metadataPath"> Path of Metadata Log (where `KafkaSource` persists [KafkaSourceOffset](KafkaSourceOffset.md) offsets in JSON format)
* <span id="startingOffsets"> [Starting offsets](KafkaOffsetRangeLimit.md) (defined using [startingOffsets](index.md#startingOffsets) option)
* <span id="failOnDataLoss"> `failOnDataLoss` flag to create [KafkaSourceRDD](KafkaSourceRDD.md)s every trigger and to [report an IllegalStateException on data loss](#reportDataLoss).

## <span id="maxTriggerDelayMs"> maxTriggerDelayMs

`KafkaSource` reads the value of [maxTriggerDelay](options.md#maxtriggerdelay) option (in the [sourceOptions](#sourceOptions)) when [created](#creating-instance).

`maxTriggerDelayMs` is used in [getDefaultReadLimit](#getDefaultReadLimit) (when [minOffsetsPerTrigger](options.md#minOffsetsPerTrigger) is defined).

## <span id="getBatch"> Loading Kafka Records for Streaming Micro-Batch

```scala
getBatch(
  start: Option[Offset],
  end: Offset): DataFrame
```

`getBatch` is part of the [Source](../../Source.md#getBatch) abstraction.

`getBatch` creates a streaming `DataFrame` with a query plan with `LogicalRDD` logical operator to scan data from a [KafkaSourceRDD](KafkaSourceRDD.md).

Internally, `getBatch` initializes <<initialPartitionOffsets, initial partition offsets>> (unless initialized already).

You should see the following INFO message in the logs:

```text
GetBatch called with start = [start], end = [end]
```

`getBatch` requests `KafkaSourceOffset` for [end partition offsets](KafkaSourceOffset.md#getPartitionOffsets) for the input `end` offset (known as `untilPartitionOffsets`).

`getBatch` requests `KafkaSourceOffset` for [start partition offsets](KafkaSourceOffset.md#getPartitionOffsets) for the input `start` offset (if defined) or uses <<initialPartitionOffsets, initial partition offsets>> (known as `fromPartitionOffsets`).

`getBatch` finds the new partitions (as the difference between the topic partitions in `untilPartitionOffsets` and `fromPartitionOffsets`) and requests <<kafkaReader, KafkaOffsetReader>> to [fetch their earliest offsets](KafkaOffsetReader.md#fetchEarliestOffsets).

`getBatch` <<reportDataLoss, reports a data loss>> if the new partitions don't match to what <<kafkaReader, KafkaOffsetReader>> fetched.

```text
Cannot find earliest offsets of [partitions]. Some data may have been missed
```

You should see the following INFO message in the logs:

```text
Partitions added: [newPartitionOffsets]
```

`getBatch` <<reportDataLoss, reports a data loss>> if the new partitions don't have their offsets `0`.

```text
Added partition [partition] starts from [offset] instead of 0. Some data may have been missed
```

`getBatch` <<reportDataLoss, reports a data loss>> if the `fromPartitionOffsets` partitions differ from `untilPartitionOffsets` partitions.

```text
[partitions] are gone. Some data may have been missed
```

You should see the following DEBUG message in the logs:

```text
TopicPartitions: [topicPartitions]
```

`getBatch` <<getSortedExecutorList, gets the executors>> (sorted by `executorId` and `host` of the registered block managers).

IMPORTANT: That is when `getBatch` goes very low-level to allow for cached `KafkaConsumers` in the executors to be re-used to read the same partition in every batch (aka _location preference_).

You should see the following DEBUG message in the logs:

```text
Sorted executors: [sortedExecutors]
```

`getBatch` creates a `KafkaSourceRDDOffsetRange` per `TopicPartition`.

`getBatch` filters out `KafkaSourceRDDOffsetRanges` for which until offsets are smaller than from offsets. `getBatch` <<reportDataLoss, reports a data loss>> if they are found.

```text
Partition [topicPartition]'s offset was changed from [fromOffset] to [untilOffset], some data may have been missed
```

`getBatch` creates a [KafkaSourceRDD](KafkaSourceRDD.md) (with <<executorKafkaParams, executorKafkaParams>>, <<pollTimeoutMs, pollTimeoutMs>> and `reuseKafkaConsumer` flag enabled) and maps it to an RDD of `InternalRow`.

IMPORTANT: `getBatch` creates a `KafkaSourceRDD` with `reuseKafkaConsumer` flag enabled.

You should see the following INFO message in the logs:

```text
GetBatch generating RDD of offset range: [offsetRanges]
```

`getBatch` sets <<currentPartitionOffsets, currentPartitionOffsets>> if it was empty (which is when...FIXME)

In the end, `getBatch` creates a streaming `DataFrame` for the `KafkaSourceRDD` and the <<schema, schema>>.

=== [[getOffset]] Fetching Offsets (From Metadata Log or Kafka Directly) -- `getOffset` Method

[source, scala]
----
getOffset: Option[Offset]
----

NOTE: `getOffset` is a part of the ../../Source.md#getOffset[Source Contract].

Internally, `getOffset` fetches the <<initialPartitionOffsets, initial partition offsets>> (from the metadata log or Kafka directly).

.KafkaSource Initializing initialPartitionOffsets While Fetching Initial Offsets
image::images/KafkaSource-initialPartitionOffsets.png[align="center"]

NOTE: <<initialPartitionOffsets, initialPartitionOffsets>> is a lazy value and is initialized the very first time `getOffset` is called (which is when `StreamExecution` MicroBatchExecution.md#constructNextBatch-hasNewData[constructs a streaming micro-batch]).

[source, scala]
----
scala> spark.version
res0: String = 2.3.0-SNAPSHOT

// Case 1: Checkpoint directory undefined
// initialPartitionOffsets read from Kafka directly
val records = spark.
  readStream.
  format("kafka").
  option("subscribe", "topic1").
  option("kafka.bootstrap.servers", "localhost:9092").
  load
// Start the streaming query
// dump records to the console every 10 seconds
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import scala.concurrent.duration._
val q = records.
  writeStream.
  format("console").
  option("truncate", false).
  trigger(Trigger.ProcessingTime(10.seconds)).
  outputMode(OutputMode.Update).
  start
// Note the temporary checkpoint directory
17/08/07 11:09:29 INFO StreamExecution: Starting [id = 75dd261d-6b62-40fc-a368-9d95d3cb6f5f, runId = f18a5eb5-ccab-4d9d-8a81-befed41a72bd] with file:///private/var/folders/0w/kb0d3rqn4zb9fcc91pxhgn8w0000gn/T/temporary-d0055630-24e4-4d9a-8f36-7a12a0f11bc0 to store the query checkpoint.
...
INFO KafkaSource: Initial offsets: {"topic1":{"0":1}}

// Stop the streaming query
q.stop

// Case 2: Checkpoint directory defined
// initialPartitionOffsets read from Kafka directly
// since the checkpoint directory is not available yet
// it will be the next time the query is started
val records = spark.
  readStream.
  format("kafka").
  option("subscribe", "topic1").
  option("kafka.bootstrap.servers", "localhost:9092").
  load.
  select($"value" cast "string", $"topic", $"partition", $"offset")
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import scala.concurrent.duration._
val q = records.
  writeStream.
  format("console").
  option("truncate", false).
  option("checkpointLocation", "/tmp/checkpoint"). // <-- checkpoint directory
  trigger(Trigger.ProcessingTime(10.seconds)).
  outputMode(OutputMode.Update).
  start
// Note the checkpoint directory in use
17/08/07 11:21:25 INFO StreamExecution: Starting [id = b8f59854-61c1-4c2f-931d-62bbaf90ee3b, runId = 70d06a3b-f2b1-4fa8-a518-15df4cf59130] with file:///tmp/checkpoint to store the query checkpoint.
...
INFO KafkaSource: Initial offsets: {"topic1":{"0":1}}
...
INFO StreamExecution: Stored offsets for batch 0. Metadata OffsetSeqMetadata(0,1502098526848,Map(spark.sql.shuffle.partitions -> 200, spark.sql.streaming.stateStore.providerClass -> org.apache.spark.sql.execution.streaming.state.HDFSBackedStateStoreProvider))

// Review the checkpoint location
// $ ls -ltr /tmp/checkpoint/offsets
// total 8
// -rw-r--r--  1 jacek  wheel  248  7 sie 11:21 0
// $ tail -2 /tmp/checkpoint/offsets/0 | jq

// Produce messages to Kafka so the latest offset changes
// And more importanly the offset gets stored to checkpoint location
-------------------------------------------
Batch: 1
-------------------------------------------
+---------------------------+------+---------+------+
|value                      |topic |partition|offset|
+---------------------------+------+---------+------+
|testing checkpoint location|topic1|0        |2     |
+---------------------------+------+---------+------+

// and one more
// Note the offset
-------------------------------------------
Batch: 2
-------------------------------------------
+------------+------+---------+------+
|value       |topic |partition|offset|
+------------+------+---------+------+
|another test|topic1|0        |3     |
+------------+------+---------+------+

// See what was checkpointed
// $ ls -ltr /tmp/checkpoint/offsets
// total 24
// -rw-r--r--  1 jacek  wheel  248  7 sie 11:35 0
// -rw-r--r--  1 jacek  wheel  248  7 sie 11:37 1
// -rw-r--r--  1 jacek  wheel  248  7 sie 11:38 2
// $ tail -2 /tmp/checkpoint/offsets/2 | jq

// Stop the streaming query
q.stop

// And start over to see what offset the query starts from
// Checkpoint location should have the offsets
val q = records.
  writeStream.
  format("console").
  option("truncate", false).
  option("checkpointLocation", "/tmp/checkpoint"). // <-- checkpoint directory
  trigger(Trigger.ProcessingTime(10.seconds)).
  outputMode(OutputMode.Update).
  start
// Whoops...console format does not support recovery (!)
// Reported as https://issues.apache.org/jira/browse/SPARK-21667
org.apache.spark.sql.AnalysisException: This query does not support recovering from checkpoint location. Delete /tmp/checkpoint/offsets to start over.;
  at org.apache.spark.sql.streaming.StreamingQueryManager.createQuery(StreamingQueryManager.scala:222)
  at org.apache.spark.sql.streaming.StreamingQueryManager.startQuery(StreamingQueryManager.scala:278)
  at org.apache.spark.sql.streaming.DataStreamWriter.start(DataStreamWriter.scala:284)
  ... 61 elided

// Change the sink (= output format) to JSON
val q = records.
  writeStream.
  format("json").
  option("path", "/tmp/json-sink").
  option("checkpointLocation", "/tmp/checkpoint"). // <-- checkpoint directory
  trigger(Trigger.ProcessingTime(10.seconds)).
  start
// Note the checkpoint directory in use
17/08/07 12:09:02 INFO StreamExecution: Starting [id = 02e00924-5f0d-4501-bcb8-80be8a8be385, runId = 5eba2576-dad6-4f95-9031-e72514475edc] with file:///tmp/checkpoint to store the query checkpoint.
...
17/08/07 12:09:02 INFO KafkaSource: GetBatch called with start = Some({"topic1":{"0":3}}), end = {"topic1":{"0":4}}
17/08/07 12:09:02 INFO KafkaSource: Partitions added: Map()
17/08/07 12:09:02 DEBUG KafkaSource: TopicPartitions: topic1-0
17/08/07 12:09:02 DEBUG KafkaSource: Sorted executors:
17/08/07 12:09:02 INFO KafkaSource: GetBatch generating RDD of offset range: KafkaSourceRDDOffsetRange(topic1-0,3,4,None)
17/08/07 12:09:03 DEBUG KafkaOffsetReader: Partitions assigned to consumer: [topic1-0]. Seeking to the end.
17/08/07 12:09:03 DEBUG KafkaOffsetReader: Got latest offsets for partition : Map(topic1-0 -> 4)
17/08/07 12:09:03 DEBUG KafkaSource: GetOffset: ArrayBuffer((topic1-0,4))
17/08/07 12:09:03 DEBUG StreamExecution: getOffset took 122 ms
17/08/07 12:09:03 DEBUG StreamExecution: Resuming at batch 3 with committed offsets {KafkaSource[Subscribe[topic1]]: {"topic1":{"0":4}}} and available offsets {KafkaSource[Subscribe[topic1]]: {"topic1":{"0":4}}}
17/08/07 12:09:03 DEBUG StreamExecution: Stream running from {KafkaSource[Subscribe[topic1]]: {"topic1":{"0":4}}} to {KafkaSource[Subscribe[topic1]]: {"topic1":{"0":4}}}
----

`getOffset` requests <<kafkaReader, KafkaOffsetReader>> to [fetchLatestOffsets](KafkaOffsetReader.md#fetchLatestOffsets) (known later as `latest`).

NOTE: (Possible performance degradation?) It is possible that `getOffset` will request the latest offsets from Kafka twice, i.e. while initializing <<initialPartitionOffsets, initialPartitionOffsets>> (when no metadata log is available and KafkaSource's <<startingOffsets, KafkaOffsetRangeLimit>> is `LatestOffsetRangeLimit`) and always as part of `getOffset` itself.

`getOffset` then calculates <<currentPartitionOffsets, currentPartitionOffsets>> based on the  <<maxOffsetsPerTrigger, maxOffsetsPerTrigger>> option.

.getOffset's Offset Calculation per maxOffsetsPerTrigger
[cols="1,1",options="header",width="100%"]
|===
| maxOffsetsPerTrigger
| Offsets

| Unspecified (i.e. `None`)
| `latest`

| Defined (but <<currentPartitionOffsets, currentPartitionOffsets>> is empty)
| <<rateLimit, rateLimit>> with `limit` limit, <<initialPartitionOffsets, initialPartitionOffsets>> as `from`, `until` as `latest`

| Defined (and <<currentPartitionOffsets, currentPartitionOffsets>> contains partitions and offsets)
| <<rateLimit, rateLimit>> with `limit` limit, <<currentPartitionOffsets, currentPartitionOffsets>> as `from`, `until` as `latest`
|===

You should see the following DEBUG message in the logs:

```text
GetOffset: [offsets]
```

In the end, `getOffset` creates a [KafkaSourceOffset](KafkaSourceOffset.md) with `offsets` (as `Map[TopicPartition, Long]`).

=== [[fetchAndVerify]] Fetching and Verifying Specific Offsets -- `fetchAndVerify` Internal Method

[source, scala]
----
fetchAndVerify(specificOffsets: Map[TopicPartition, Long]): KafkaSourceOffset
----

`fetchAndVerify` requests <<kafkaReader, KafkaOffsetReader>> to [fetchSpecificOffsets](KafkaOffsetReader.md#fetchSpecificOffsets) for the given `specificOffsets`.

`fetchAndVerify` makes sure that the starting offsets in `specificOffsets` are the same as in Kafka and <<reportDataLoss, reports a data loss>> otherwise.

```
startingOffsets for [tp] was [off] but consumer reset to [result(tp)]
```

In the end, `fetchAndVerify` creates a [KafkaSourceOffset](KafkaSourceOffset.md) (with the result of <<kafkaReader, KafkaOffsetReader>>).

NOTE: `fetchAndVerify` is used exclusively when `KafkaSource` initializes <<initialPartitionOffsets, initial partition offsets>>.

=== [[initialPartitionOffsets]] Initial Partition Offsets (of 0th Batch) -- `initialPartitionOffsets` Internal Lazy Property

[source, scala]
----
initialPartitionOffsets: Map[TopicPartition, Long]
----

`initialPartitionOffsets` is the *initial partition offsets* for the batch `0` that were already persisted in the <<metadataPath, streaming metadata log directory>> or persisted on demand.

As the very first step, `initialPartitionOffsets` creates a custom [HDFSMetadataLog](../../HDFSMetadataLog.md) (of [KafkaSourceOffsets](KafkaSourceOffset.md) metadata) in the <<metadataPath, streaming metadata log directory>>.

`initialPartitionOffsets` requests the `HDFSMetadataLog` for the [metadata](../../HDFSMetadataLog.md#get) of the ``0``th batch (as `KafkaSourceOffset`).

If the metadata is available, `initialPartitionOffsets` requests the metadata for the [collection of TopicPartitions and their offsets](KafkaSourceOffset.md#partitionToOffsets).

If the metadata could not be found, `initialPartitionOffsets` creates a new `KafkaSourceOffset` per <<startingOffsets, KafkaOffsetRangeLimit>>:

* For `EarliestOffsetRangeLimit`, `initialPartitionOffsets` requests the <<kafkaReader, KafkaOffsetReader>> to [fetchEarliestOffsets](KafkaOffsetReader.md#fetchEarliestOffsets)

* For `LatestOffsetRangeLimit`, `initialPartitionOffsets` requests the <<kafkaReader, KafkaOffsetReader>> to [fetchLatestOffsets](KafkaOffsetReader.md#fetchLatestOffsets)

* For `SpecificOffsetRangeLimit`, `initialPartitionOffsets` requests the <<kafkaReader, KafkaOffsetReader>> to [fetchSpecificOffsets](KafkaOffsetReader.md#fetchSpecificOffsets) (and report a data loss per the <<failOnDataLoss, failOnDataLoss>> flag)

`initialPartitionOffsets` requests the custom `HDFSMetadataLog` to [add the offsets to the metadata log](../../HDFSMetadataLog.md#add) (as the metadata of the ``0``th batch).

`initialPartitionOffsets` prints out the following INFO message to the logs:

```text
Initial offsets: [offsets]
```

!!! note
    `initialPartitionOffsets` is used when `KafkaSource` is requested for the following:

    * <<getOffset, Fetch offsets (from metadata log or Kafka directly)>>

    * <<getBatch, Generate a DataFrame with records from Kafka for a streaming batch>> (when the start offsets are not defined, i.e. before `StreamExecution` [commits the first streaming batch](../../StreamExecution.md#runStream) and so nothing is in [committedOffsets](../../StreamExecution.md#committedOffsets) registry for a `KafkaSource` data source yet)

==== [[initialPartitionOffsets-HDFSMetadataLog-serialize]] `HDFSMetadataLog.serialize`

[source, scala]
----
serialize(
  metadata: KafkaSourceOffset,
  out: OutputStream): Unit
----

`serialize` requests the `OutputStream` to write a zero byte (to support Spark 2.1.0 as per SPARK-19517).

`serialize` creates a `BufferedWriter` over a `OutputStreamWriter` over the `OutputStream` (with `UTF_8` charset encoding).

`serialize` requests the `BufferedWriter` to write the *v1* version indicator followed by a new line.

`serialize` then requests the `KafkaSourceOffset` for a JSON-serialized representation and the `BufferedWriter` to write it out.

In the end, `serialize` requests the `BufferedWriter` to flush (the underlying stream).

`serialize` is part of the [HDFSMetadataLog](../../HDFSMetadataLog.md#serialize) abstraction.

=== [[rateLimit]] `rateLimit` Internal Method

[source, scala]
----
rateLimit(
  limit: Long,
  from: Map[TopicPartition, Long],
  until: Map[TopicPartition, Long]): Map[TopicPartition, Long]
----

`rateLimit` requests <<kafkaReader, KafkaOffsetReader>> to [fetchEarliestOffsets](KafkaOffsetReader.md#fetchEarliestOffsets).

CAUTION: FIXME

NOTE: `rateLimit` is used exclusively when `KafkaSource` <<getOffset, gets available offsets>> (when <<maxOffsetsPerTrigger, maxOffsetsPerTrigger>> option is specified).

=== [[getSortedExecutorList]] `getSortedExecutorList` Method

CAUTION: FIXME

=== [[reportDataLoss]] `reportDataLoss` Internal Method

CAUTION: FIXME

[NOTE]
====
`reportDataLoss` is used when `KafkaSource` does the following:

* <<fetchAndVerify, fetches and verifies specific offsets>>
* <<getBatch, generates a DataFrame with records from Kafka for a batch>>
====

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| currentPartitionOffsets
| [[currentPartitionOffsets]] Current partition offsets (as `Map[TopicPartition, Long]`)

Initially `NONE` and set when `KafkaSource` is requested to <<getOffset, get the maximum available offsets>> or <<getBatch, generate a DataFrame with records from Kafka for a batch>>.

| pollTimeoutMs
a| [[pollTimeoutMs]]

| sc
a| [[sc]] Spark Core's `SparkContext` (of the <<sqlContext, SQLContext>>)

Used when:

* <<getBatch, Generating a DataFrame with records from Kafka for a streaming micro-batch>> (and creating a [KafkaSourceRDD](KafkaSourceRDD.md))

* Initializing the [pollTimeoutMs](#pollTimeoutMs) internal property

|===

## Logging

Enable `ALL` logging level for `org.apache.spark.sql.kafka010.KafkaSource` logger to see what happens inside.

Add the following line to `conf/log4j.properties`:

```text
log4j.logger.org.apache.spark.sql.kafka010.KafkaSource=ALL
```

Refer to [Logging](../../spark-logging.md).
