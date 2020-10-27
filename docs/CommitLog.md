# CommitLog &mdash; HDFSMetadataLog for Offset Commit Log

`CommitLog` is an [HDFSMetadataLog](HDFSMetadataLog.md) with [CommitMetadata](#CommitMetadata) metadata.

`CommitLog` is the [offset commit log](StreamExecution.md#commitLog) of [streaming query execution engines](StreamExecution.md).

[[CommitMetadata]][[nextBatchWatermarkMs]]
`CommitLog` uses `CommitMetadata` for the metadata with *nextBatchWatermarkMs* attribute (of type `Long` and the default `0`).

`CommitLog` <<serialize, writes>> commit metadata to files with names that are offsets.

```text
$ ls -tr [checkpoint-directory]/commits
0 1 2 3 4 5 6 7 8 9

$ cat [checkpoint-directory]/commits/8
v1
{"nextBatchWatermarkMs": 0}
```

[[VERSION]]
`CommitLog` uses *1* for the version.

[[creating-instance]]
`CommitLog` (like the parent [HDFSMetadataLog](HDFSMetadataLog.md#creating-instance)) takes the following to be created:

* [[sparkSession]] `SparkSession`
* [[path]] Path of the metadata log directory

=== [[serialize]] Serializing Metadata (Writing Metadata to Persistent Storage) -- `serialize` Method

[source, scala]
----
serialize(
  metadata: CommitMetadata,
  out: OutputStream): Unit
----

`serialize` writes out the <<VERSION, version>> prefixed with `v` on a single line (e.g. `v1`) followed by the given `CommitMetadata` in JSON format.

`serialize` is part of [HDFSMetadataLog](HDFSMetadataLog.md#serialize) abstraction.

=== [[deserialize]] Deserializing Metadata -- `deserialize` Method

[source, scala]
----
deserialize(in: InputStream): CommitMetadata
----

`deserialize` simply reads (_deserializes_) two lines from the given `InputStream` for [version](HDFSMetadataLog.md#parseVersion) and the <<nextBatchWatermarkMs, nextBatchWatermarkMs>> attribute.

`deserialize` is part of [HDFSMetadataLog](HDFSMetadataLog.md#deserialize) abstraction.

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

`add`...FIXME

`add` is part of [MetadataLog](MetadataLog.md#add) abstraction.
