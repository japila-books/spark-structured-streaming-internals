== [[SinkFileStatus]] SinkFileStatus

[[creating-instance]]
`SinkFileStatus` represents the status of files of <<spark-sql-streaming-FileStreamSink.md#, FileStreamSink>> (and the type of the metadata of <<spark-sql-streaming-FileStreamSinkLog.md#, FileStreamSinkLog>>):

* [[path]] Path
* [[size]] Size
* [[isDir]] `isDir` flag
* [[modificationTime]] Modification time
* [[blockReplication]] Block replication
* [[blockSize]] Block size
* [[action]] Action (either <<spark-sql-streaming-FileStreamSinkLog.md#ADD_ACTION, add>> or <<spark-sql-streaming-FileStreamSinkLog.md#DELETE_ACTION, delete>>)

=== [[toFileStatus]] `toFileStatus` Method

[source, scala]
----
toFileStatus: FileStatus
----

`toFileStatus` simply creates a new Hadoop https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileStatus.html[FileStatus].

NOTE: `toFileStatus` is used exclusively when `MetadataLogFileIndex` is <<spark-sql-streaming-MetadataLogFileIndex.md#, created>>.

=== [[apply]] Creating SinkFileStatus Instance -- `apply` Object Method

[source, scala]
----
apply(f: FileStatus): SinkFileStatus
----

`apply` simply creates a new <<SinkFileStatus, SinkFileStatus>> (with <<spark-sql-streaming-FileStreamSinkLog.md#ADD_ACTION, add>> action).

NOTE: `apply` is used exclusively when `ManifestFileCommitProtocol` is requested to <<spark-sql-streaming-ManifestFileCommitProtocol.md#commitTask, commitTask>>.
