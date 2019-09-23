== [[KafkaOffsetRangeCalculator]] KafkaOffsetRangeCalculator

`KafkaOffsetRangeCalculator` is <<apply, created>> for <<spark-sql-streaming-KafkaMicroBatchReader.adoc#rangeCalculator, KafkaMicroBatchReader>> to <<getRanges, calculate offset ranges>> (when `KafkaMicroBatchReader` is requested to <<spark-sql-streaming-KafkaMicroBatchReader.adoc#planInputPartitions, planInputPartitions>>).

[[minPartitions]][[creating-instance]]
`KafkaOffsetRangeCalculator` takes an optional *minimum number of partitions per executor* (`minPartitions`) to be created (that can either be undefined or greater than `0`).

[[apply]]
When created with a `DataSourceOptions`, `KafkaOffsetRangeCalculator` uses <<spark-sql-streaming-kafka-data-source.adoc#minPartitions, minPartitions>> option for the <<minPartitions, minimum number of partitions per executor>>.

=== [[getRanges]] Offset Ranges -- `getRanges` Method

[source, scala]
----
getRanges(
  fromOffsets: PartitionOffsetMap,
  untilOffsets: PartitionOffsetMap,
  executorLocations: Seq[String] = Seq.empty): Seq[KafkaOffsetRange]
----

`getRanges` finds the common `TopicPartitions` that are the keys that are used in the `fromOffsets` and `untilOffsets` collections (_intersection_).

For every common `TopicPartition`, `getRanges` creates a <<KafkaOffsetRange, KafkaOffsetRange>> with the from and until offsets from the `fromOffsets` and `untilOffsets` collections (and the <<preferredLoc, preferredLoc>> undefined). `getRanges` filters out the `TopicPartitions` that <<size, have no records to consume>> (i.e. the difference between until and from offsets is not greater than `0`).

At this point, `getRanges` knows the `TopicPartitions` with records to consume.

`getRanges` branches off based on the defined <<minPartitions, minimum number of partitions per executor>> and the number of `KafkaOffsetRanges` (`TopicPartitions` with records to consume).

For the <<minPartitions, minimum number of partitions per executor>> undefined or smaller than the number of `KafkaOffsetRanges` (`TopicPartitions` to consume records from), `getRanges` updates every `KafkaOffsetRange` with the <<getLocation, preferred executor>> based on the `TopicPartition` and the `executorLocations`).

Otherwise (with the <<minPartitions, minimum number of partitions per executor>> defined and greater than the number of `KafkaOffsetRanges`), `getRanges` splits `KafkaOffsetRanges` into smaller ones.

NOTE: `getRanges` is used exclusively when `KafkaMicroBatchReader` is requested to <<spark-sql-streaming-KafkaMicroBatchReader.adoc#planInputPartitions, planInputPartitions>>.

=== [[KafkaOffsetRange]] KafkaOffsetRange -- TopicPartition with From and Until Offsets and Optional Preferred Location

`KafkaOffsetRange` is a case class with the following attributes:

* [[topicPartition]] `TopicPartition`
* [[fromOffset]] `fromOffset` offset
* [[untilOffset]] `untilOffset` offset
* [[preferredLoc]] Optional preferred location

[[size]]
`KafkaOffsetRange` knows the size, i.e. the number of records between the <<untilOffset, untilOffset>> and <<fromOffset, fromOffset>> offsets.

=== [[getLocation]] Selecting Preferred Executor for TopicPartition -- `getLocation` Internal Method

[source, scala]
----
getLocation(
  tp: TopicPartition,
  executorLocations: Seq[String]): Option[String]
----

`getLocation`...FIXME

NOTE: `getLocation` is used exclusively when `KafkaOffsetRangeCalculator` is requested to <<getRanges, calculate offset ranges>>.
