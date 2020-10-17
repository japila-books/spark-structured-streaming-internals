# MetadataLog

`MetadataLog` is the <<contract, abstraction>> of <<implementations, metadata storage>> that can <<add, persist>>, <<get, retrieve>>, and <<purge, remove>> metadata (of type `T`).

[[contract]]
.MetadataLog Contract
[cols="30m,70",options="header",width="100%"]
|===
| Method
| Description

| add
a| [[add]]

[source, scala]
----
add(
  batchId: Long,
  metadata: T): Boolean
----

Persists (_adds_) metadata of a streaming batch

Used when:

* `KafkaMicroBatchReader` is requested to [getOrCreateInitialPartitionOffsets](datasources/kafka/KafkaMicroBatchReader.md#getOrCreateInitialPartitionOffsets)

* `KafkaSource` is requested for the [initialPartitionOffsets](datasources/kafka/KafkaSource.md#initialPartitionOffsets)

* `CompactibleFileStreamLog` is requested for the [store metadata of a streaming batch](datasources/file/CompactibleFileStreamLog.md#add) and to [compact](datasources/file/CompactibleFileStreamLog.md#compact)

* `FileStreamSource` is requested to [fetchMaxOffset](datasources/file/FileStreamSource.md#fetchMaxOffset)

* `FileStreamSourceLog` is requested to [store (add) metadata of a streaming batch](datasources/file/FileStreamSourceLog.md#add)

* `ManifestFileCommitProtocol` is requested to [commitJob](datasources/file/ManifestFileCommitProtocol.md#commitJob)

* `MicroBatchExecution` stream execution engine is requested to <<MicroBatchExecution.md#constructNextBatch, construct a next streaming micro-batch>> and <<MicroBatchExecution.md#runBatch, run a single streaming micro-batch>>

* `ContinuousExecution` stream execution engine is requested to <<ContinuousExecution.md#addOffset, addOffset>> and <<ContinuousExecution.md#commit, commit an epoch>>

* `RateStreamMicroBatchReader` is created (`creationTimeMs`)

| get
a| [[get]]

[source, scala]
----
get(
  batchId: Long): Option[T]
get(
  startId: Option[Long],
  endId: Option[Long]): Array[(Long, T)]
----

Retrieves (_gets_) metadata of one or more batches

Used when...FIXME

| getLatest
a| [[getLatest]]

[source, scala]
----
getLatest(): Option[(Long, T)]
----

Retrieves the latest-committed metadata (if available)

Used when...FIXME

| purge
a| [[purge]]

[source, scala]
----
purge(thresholdBatchId: Long): Unit
----

Used when...FIXME

|===

[[implementations]]
NOTE: [HDFSMetadataLog](HDFSMetadataLog.md) is the only direct implementation of the <<contract, MetadataLog Contract>> in Spark Structured Streaming.
