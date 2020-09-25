# CommitLog &mdash; HDFSMetadataLog for Offset Commit Log

`CommitLog` is an <<spark-sql-streaming-HDFSMetadataLog.md#, HDFSMetadataLog>> with <<CommitMetadata, CommitMetadata>> metadata.

`CommitLog` is <<creating-instance, created>> exclusively for the [offset commit log](StreamExecution.md#commitLog) (of [StreamExecution](StreamExecution.md)).

[[CommitMetadata]][[nextBatchWatermarkMs]]
`CommitLog` uses `CommitMetadata` for the metadata with *nextBatchWatermarkMs* attribute (of type `Long` and the default `0`).

`CommitLog` <<serialize, writes>> commit metadata to files with names that are offsets.

```
$ ls -tr [checkpoint-directory]/commits
0 1 2 3 4 5 6 7 8 9

$ cat [checkpoint-directory]/commits/8
v1
{"nextBatchWatermarkMs": 0}
```

[[VERSION]]
`CommitLog` uses *1* for the version.

[[creating-instance]]
`CommitLog` (like the parent <<spark-sql-streaming-HDFSMetadataLog.md#creating-instance, HDFSMetadataLog>>) takes the following to be created:

* [[sparkSession]] `SparkSession`
* [[path]] Path of the metadata log directory

=== [[serialize]] Serializing Metadata (Writing Metadata to Persistent Storage) -- `serialize` Method

[source, scala]
----
serialize(
  metadata: CommitMetadata,
  out: OutputStream): Unit
----

NOTE: `serialize` is part of <<spark-sql-streaming-HDFSMetadataLog.md#serialize, HDFSMetadataLog Contract>> to write a metadata in serialized format.

`serialize` writes out the <<VERSION, version>> prefixed with `v` on a single line (e.g. `v1`) followed by the given `CommitMetadata` in JSON format.

=== [[deserialize]] Deserializing Metadata -- `deserialize` Method

[source, scala]
----
deserialize(in: InputStream): CommitMetadata
----

NOTE: `deserialize` is part of <<spark-sql-streaming-HDFSMetadataLog.md#deserialize, HDFSMetadataLog Contract>> to deserialize a metadata (from an `InputStream`).

`deserialize` simply reads (_deserializes_) two lines from the given `InputStream` for <<spark-sql-streaming-HDFSMetadataLog.md#parseVersion, version>> and the <<nextBatchWatermarkMs, nextBatchWatermarkMs>> attribute.

=== [[add-batchId]] `add` Method

[source, scala]
----
add(batchId: Long): Unit
----

`add`...FIXME

NOTE: `add` is used when...FIXME

=== [[add-batchId-metadata]] `add` Method

[source, scala]
----
add(batchId: Long, metadata: String): Boolean
----

NOTE: `add` is part of <<spark-sql-streaming-MetadataLog.md#add, MetadataLog Contract>> to...FIXME.

`add`...FIXME
