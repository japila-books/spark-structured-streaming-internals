# MetadataLog

`MetadataLog` is an [abstraction](#contract) of [metadata logs](#implementations) that can [add](#add), [get](#get), [getLatest](#getLatest) and [purge](#purge) metadata (of type `T`).

??? note "Type Constructor"
    `MetadataLog[T]` is a Scala type constructor with the type parameter `T`

## Contract

### <span id="add"> Storing Metadata of Streaming Batch

```scala
add(
  batchId: Long,
  metadata: T): Boolean
```

Stores (_adds_) metadata of a streaming batch

Used when:

* `KafkaSource` is requested for the [initialPartitionOffsets](datasources/kafka/KafkaSource.md#initialPartitionOffsets)

* `CompactibleFileStreamLog` is requested for the [store metadata of a streaming batch](datasources/file/CompactibleFileStreamLog.md#add) and to [compact](datasources/file/CompactibleFileStreamLog.md#compact)

* `FileStreamSource` is requested to [fetchMaxOffset](datasources/file/FileStreamSource.md#fetchMaxOffset)

* `FileStreamSourceLog` is requested to [store (add) metadata of a streaming batch](datasources/file/FileStreamSourceLog.md#add)

* `ManifestFileCommitProtocol` is requested to [commitJob](datasources/file/ManifestFileCommitProtocol.md#commitJob)

* `MicroBatchExecution` stream execution engine is requested to <<MicroBatchExecution.md#constructNextBatch, construct a next streaming micro-batch>> and <<MicroBatchExecution.md#runBatch, run a single streaming micro-batch>>

* `ContinuousExecution` stream execution engine is requested to <<ContinuousExecution.md#addOffset, addOffset>> and <<ContinuousExecution.md#commit, commit an epoch>>

### <span id="get"> get

```scala
get(
  batchId: Long): Option[T]
get(
  startId: Option[Long],
  endId: Option[Long]): Array[(Long, T)]
```

Looks up (_gets_) metadata of one or more streaming batches

Used when...FIXME

### <span id="getLatest"> getLatest

```scala
getLatest(): Option[(Long, T)]
```

Looks up the latest-committed metadata (if available)

Used when...FIXME

### <span id="purge"> purge

```scala
purge(
  thresholdBatchId: Long): Unit
```

Purging (_removing_) metadata older than the given threshold

Used when...FIXME

## Implementations

* [HDFSMetadataLog](HDFSMetadataLog.md)
