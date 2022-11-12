== [[KafkaDataConsumer]] KafkaDataConsumer

`KafkaDataConsumer` is the <<contract, abstraction>> of <<implementations, Kafka consumers>> that use <<internalConsumer, InternalKafkaConsumer>> that can be <<release, released>>.

[[contract]]
.KafkaDataConsumer Contract (Abstract Methods Only)
[cols="30m,70",options="header",width="100%"]
|===
| Method
| Description

| internalConsumer
a| [[internalConsumer]]

[source, scala]
----
internalConsumer: InternalKafkaConsumer
----

Used when...FIXME

| release
a| [[release]]

[source, scala]
----
release(): Unit
----

Used when...FIXME

|===

[[implementations]]
.KafkaDataConsumers
[cols="30,70",options="header",width="100%"]
|===
| KafkaDataConsumer
| Description

| CachedKafkaDataConsumer
| [[CachedKafkaDataConsumer]]

| NonCachedKafkaDataConsumer
| [[NonCachedKafkaDataConsumer]]

|===

=== [[acquire]] Acquiring Cached KafkaDataConsumer for Partition -- `acquire` Object Method

[source, scala]
----
acquire(
  topicPartition: TopicPartition,
  kafkaParams: ju.Map[String, Object],
  useCache: Boolean
): KafkaDataConsumer
----

`acquire`...FIXME

NOTE: `acquire` is used when...FIXME

=== [[get]] Getting Kafka Record -- `get` Method

[source, scala]
----
get(
  offset: Long,
  untilOffset: Long,
  pollTimeoutMs: Long,
  failOnDataLoss: Boolean
): ConsumerRecord[Array[Byte], Array[Byte]]
----

`get`...FIXME

NOTE: `get` is used when...FIXME
