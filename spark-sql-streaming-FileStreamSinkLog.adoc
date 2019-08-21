== [[FileStreamSinkLog]] FileStreamSinkLog

`FileStreamSinkLog` is a concrete <<spark-sql-streaming-CompactibleFileStreamLog.adoc#, CompactibleFileStreamLog>> (of <<spark-sql-streaming-SinkFileStatus.adoc#, SinkFileStatuses>>) for <<spark-sql-streaming-FileStreamSink.adoc#, FileStreamSink>> and <<spark-sql-streaming-MetadataLogFileIndex.adoc#, MetadataLogFileIndex>>.

[[VERSION]]
`FileStreamSinkLog` uses *1* for the version.

[[ADD_ACTION]]
`FileStreamSinkLog` uses *add* action to create new <<spark-sql-streaming-SinkFileStatus.adoc#, metadata logs>>.

[[DELETE_ACTION]]
`FileStreamSinkLog` uses *delete* action to mark <<spark-sql-streaming-SinkFileStatus.adoc#, metadata logs>> that should be excluded from <<compactLogs, compaction>>.

=== [[creating-instance]] Creating FileStreamSinkLog Instance

`FileStreamSinkLog` (like the parent <<spark-sql-streaming-CompactibleFileStreamLog.adoc#, CompactibleFileStreamLog>>) takes the following to be created:

* [[metadataLogVersion]] Metadata version
* [[sparkSession]] `SparkSession`
* [[path]] Path of the metadata log directory

=== [[compactLogs]] `compactLogs` Method

[source, scala]
----
compactLogs(logs: Seq[SinkFileStatus]): Seq[SinkFileStatus]
----

NOTE: `compactLogs` is part of the <<spark-sql-streaming-CompactibleFileStreamLog.adoc#compactLogs, CompactibleFileStreamLog Contract>> to...FIXME.

`compactLogs`...FIXME
