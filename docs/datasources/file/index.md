# File Data Source

**File Data Source** comes with the following main abstractions:

* [FileStreamSource](FileStreamSource.md)
* [FileStreamSink](FileStreamSink.md)

[FileStreamSink](FileStreamSink.md) uses [FileStreamSinkLog](FileStreamSinkLog.md) for [tracking valid files per micro-batch](FileStreamSink.md#addBatch) (as part of [ManifestFileCommitProtocol](ManifestFileCommitProtocol.md)).
