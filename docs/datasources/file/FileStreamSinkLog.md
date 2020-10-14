# FileStreamSinkLog

`FileStreamSinkLog` is a concrete [CompactibleFileStreamLog](../../CompactibleFileStreamLog.md) (of [SinkFileStatus](SinkFileStatus.md)es) for [FileStreamSink](FileStreamSink.md) and [MetadataLogFileIndex](MetadataLogFileIndex.md).

[[VERSION]]
`FileStreamSinkLog` uses *1* for the version.

[[ADD_ACTION]]
`FileStreamSinkLog` uses *add* action to create new [metadata logs](SinkFileStatus.md).

[[DELETE_ACTION]]
`FileStreamSinkLog` uses *delete* action to mark [metadata logs](SinkFileStatus.md) that should be excluded from <<compactLogs, compaction>>.

## Creating Instance

`FileStreamSinkLog` (like the parent [CompactibleFileStreamLog](../../CompactibleFileStreamLog.md)) takes the following to be created:

* [[metadataLogVersion]] Metadata version
* [[sparkSession]] `SparkSession`
* [[path]] Path of the metadata log directory

=== [[compactLogs]] `compactLogs` Method

[source, scala]
----
compactLogs(logs: Seq[SinkFileStatus]): Seq[SinkFileStatus]
----

`compactLogs`...FIXME

`compactLogs` is part of the [CompactibleFileStreamLog](../../CompactibleFileStreamLog.md#compactLogs) abstraction.
