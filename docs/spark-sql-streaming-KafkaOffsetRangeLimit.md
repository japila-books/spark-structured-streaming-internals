== [[KafkaOffsetRangeLimit]] KafkaOffsetRangeLimit -- Desired Offset Range Limits

`KafkaOffsetRangeLimit` represents the desired offset range limits for starting, ending, and specific offsets in <<spark-sql-streaming-kafka-data-source.md#, Kafka Data Source>>.

[[implementations]]
.KafkaOffsetRangeLimits
[cols="1m,3",options="header",width="100%"]
|===
| KafkaOffsetRangeLimit
| Description

| EarliestOffsetRangeLimit
| [[EarliestOffsetRangeLimit]] Intent to bind to the *earliest* offset

| LatestOffsetRangeLimit
| [[LatestOffsetRangeLimit]] Intent to bind to the *latest* offset

| SpecificOffsetRangeLimit
a| [[SpecificOffsetRangeLimit]] Intent to bind to *specific offsets* with the following special offset "magic" numbers:

* [[LATEST]] `-1` or `KafkaOffsetRangeLimit.LATEST` - the latest offset
* [[EARLIEST]] `-2` or `KafkaOffsetRangeLimit.EARLIEST` - the earliest offset

|===

NOTE: `KafkaOffsetRangeLimit` is a Scala *sealed trait* which means that all the <<implementations, implementations>> are in the same compilation unit (a single file).

`KafkaOffsetRangeLimit` is often used in a text-based representation and is converted to from *latest*, *earliest* or a *JSON-formatted text* using <<spark-sql-streaming-KafkaSourceProvider.md#getKafkaOffsetRangeLimit, KafkaSourceProvider.getKafkaOffsetRangeLimit>> object method.

NOTE: A JSON-formatted text is of the following format `{"topicName":{"partition":offset},...}`, e.g. `{"topicA":{"0":23,"1":-1},"topicB":{"0":-2}}`.

`KafkaOffsetRangeLimit` is used when:

* <<spark-sql-streaming-KafkaContinuousReader.md#, KafkaContinuousReader>> is created (with the <<spark-sql-streaming-KafkaContinuousReader.md#initialOffsets, initial offsets>>)

* <<spark-sql-streaming-KafkaMicroBatchReader.md#, KafkaMicroBatchReader>> is created (with the <<spark-sql-streaming-KafkaMicroBatchReader.md#startingOffsets, starting offsets>>)

* <<spark-sql-streaming-KafkaRelation.md#, KafkaRelation>> is created (with the <<spark-sql-streaming-KafkaRelation.md#startingOffsets, starting>> and <<spark-sql-streaming-KafkaRelation.md#endingOffsets, ending>> offsets)

* <<spark-sql-streaming-KafkaSource.md#, KafkaSource>> is created (with the <<spark-sql-streaming-KafkaRelation.md#startingOffsets, starting offsets>>)

* `KafkaSourceProvider` is requested to <<spark-sql-streaming-KafkaSourceProvider.md#getKafkaOffsetRangeLimit, convert configuration options to KafkaOffsetRangeLimits>>
