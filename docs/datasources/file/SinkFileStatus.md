# SinkFileStatus

[[creating-instance]]
`SinkFileStatus` represents the status of files of [FileStreamSink](FileStreamSink.md) (and the type of the metadata of [FileStreamSinkLog](FileStreamSinkLog.md)):

* [[path]] Path
* [[size]] Size
* [[isDir]] `isDir` flag
* [[modificationTime]] Modification time
* [[blockReplication]] Block replication
* [[blockSize]] Block size
* [[action]] Action (either [add](FileStreamSinkLog.md#ADD_ACTION) or [delete](FileStreamSinkLog.md#DELETE_ACTION))

=== [[toFileStatus]] `toFileStatus` Method

[source, scala]
----
toFileStatus: FileStatus
----

`toFileStatus` simply creates a new Hadoop [FileStatus]({{ hadoop.api }}/org/apache/hadoop/fs/FileStatus.html).

NOTE: `toFileStatus` is used exclusively when `MetadataLogFileIndex` is [created](MetadataLogFileIndex.md).

=== [[apply]] Creating SinkFileStatus Instance

[source, scala]
----
apply(f: FileStatus): SinkFileStatus
----

`apply` simply creates a new <<SinkFileStatus, SinkFileStatus>> (with [add](FileStreamSinkLog.md#ADD_ACTION) action).

`apply` is used when `ManifestFileCommitProtocol` is requested to [commitTask](ManifestFileCommitProtocol.md#commitTask).
