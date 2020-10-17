# ManifestFileCommitProtocol

`ManifestFileCommitProtocol` is...FIXME

=== [[commitJob]] `commitJob` Method

[source, scala]
----
commitJob(
  jobContext: JobContext,
  taskCommits: Seq[TaskCommitMessage]): Unit
----

NOTE: `commitJob` is part of the `FileCommitProtocol` contract to...FIXME.

`commitJob`...FIXME

=== [[commitTask]] `commitTask` Method

[source, scala]
----
commitTask(
  taskContext: TaskAttemptContext): TaskCommitMessage
----

NOTE: `commitTask` is part of the `FileCommitProtocol` contract to...FIXME.

`commitTask`...FIXME
