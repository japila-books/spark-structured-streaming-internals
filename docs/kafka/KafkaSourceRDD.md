# KafkaSourceRDD

`KafkaSourceRDD` is an `RDD` of Kafka's [ConsumerRecords]({{ kafka.api }}/org/apache/kafka/clients/consumer/ConsumerRecords.html) (`RDD[ConsumerRecord[Array[Byte], Array[Byte]]]`) and no parent RDDs.

`KafkaSourceRDD` is <<creating-instance, created>> when:

* `KafkaRelation` is requested to [build a distributed data scan with column pruning](KafkaRelation.md#buildScan)

* `KafkaSource` is requested to [generate a streaming DataFrame with records from Kafka for a streaming micro-batch](KafkaSource.md#getBatch)

## Creating Instance

`KafkaSourceRDD` takes the following when created:

* [[sc]] `SparkContext`
* [[executorKafkaParams]] Collection of key-value settings for executors reading records from Kafka topics
* [[offsetRanges]] Collection of `KafkaSourceRDDOffsetRange` offsets
* [[pollTimeoutMs]] Timeout (in milliseconds) to poll data from Kafka
+
Used when `KafkaSourceRDD` <<compute, is requested for records>> (for given offsets) and in turn requests the `CachedKafkaConsumer` to [poll for records](CachedKafkaConsumer.md#poll).
* [[failOnDataLoss]] Flag to...FIXME
* [[reuseKafkaConsumer]] Flag to...FIXME

=== [[getPreferredLocations]] Placement Preferences of Partition (Preferred Locations) -- `getPreferredLocations` Method

[source, scala]
----
getPreferredLocations(
  split: Partition): Seq[String]
----

NOTE: `getPreferredLocations` is part of the `RDD` contract to specify placement preferences.

`getPreferredLocations` converts the given `Partition` to a `KafkaSourceRDDPartition` and...FIXME

=== [[compute]] Computing Partition -- `compute` Method

[source, scala]
----
compute(
  thePart: Partition,
  context: TaskContext
): Iterator[ConsumerRecord[Array[Byte], Array[Byte]]]
----

NOTE: `compute` is part of the `RDD` contract to compute a given partition.

`compute` uses `KafkaDataConsumer` utility to [acquire a cached KafkaDataConsumer](KafkaDataConsumer.md#acquire) (for a partition).

`compute` <<resolveRange, resolves the range>> (based on the `offsetRange` of the given partition that is assumed a `KafkaSourceRDDPartition`).

`compute` returns a `NextIterator` so that `getNext` uses the `KafkaDataConsumer` to [get a record](KafkaDataConsumer.md#get).

When the beginning and ending offsets (of the offset range) are equal, `compute` prints out the following INFO message to the logs, requests the `KafkaDataConsumer` to [release](KafkaDataConsumer.md#release) and returns an empty iterator.

```text
Beginning offset [fromOffset] is the same as ending offset skipping [topic] [partition]
```

`compute` throws an `AssertionError` when the beginning offset (`fromOffset`) is after the ending offset (`untilOffset`):

[options="wrap"]
----
Beginning offset [fromOffset] is after the ending offset [untilOffset] for topic [topic] partition [partition]. You either provided an invalid fromOffset, or the Kafka topic has been damaged
----

=== [[getPartitions]] `getPartitions` Method

[source, scala]
----
getPartitions: Array[Partition]
----

NOTE: `getPartitions` is part of the `RDD` contract to...FIXME.

`getPartitions`...FIXME

=== [[persist]] Persisting RDD -- `persist` Method

[source, scala]
----
persist: Array[Partition]
----

NOTE: `persist` is part of the `RDD` contract to persist an RDD.

`persist`...FIXME

=== [[resolveRange]] `resolveRange` Internal Method

[source, scala]
----
resolveRange(
  consumer: KafkaDataConsumer,
  range: KafkaSourceRDDOffsetRange
): KafkaSourceRDDOffsetRange
----

`resolveRange`...FIXME

NOTE: `resolveRange` is used when...FIXME
