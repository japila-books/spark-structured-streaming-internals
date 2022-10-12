# KafkaOffsetRangeLimit &mdash; Desired Offset Range Limits

`KafkaOffsetRangeLimit` represents the desired offset range limits for starting, ending, and specific offsets in [Kafka Data Source](index.md).

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

`KafkaOffsetRangeLimit` is often used in a text-based representation and is converted to from *latest*, *earliest* or a *JSON-formatted text* using [KafkaSourceProvider.getKafkaOffsetRangeLimit](KafkaSourceProvider.md#getKafkaOffsetRangeLimit) utility.

NOTE: A JSON-formatted text is of the following format `{"topicName":{"partition":offset},...}`, e.g. `{"topicA":{"0":23,"1":-1},"topicB":{"0":-2}}`.

`KafkaOffsetRangeLimit` is used when:

* [KafkaMicroBatchReader](KafkaMicroBatchReader.md) is created (with the [starting offsets](KafkaMicroBatchReader.md#startingOffsets))

* [KafkaRelation](KafkaRelation.md) is created (with the [starting](KafkaRelation.md#startingOffsets) and [ending](KafkaRelation.md#endingOffsets) offsets)

* [KafkaSource](KafkaSource.md) is created (with the [starting offsets](KafkaRelation.md#startingOffsets))

* `KafkaSourceProvider` is requested to [convert configuration options to KafkaOffsetRangeLimits](KafkaSourceProvider.md#getKafkaOffsetRangeLimit)
