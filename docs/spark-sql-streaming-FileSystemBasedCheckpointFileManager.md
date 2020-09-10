== [[FileSystemBasedCheckpointFileManager]] FileSystemBasedCheckpointFileManager -- CheckpointFileManager on Hadoop's FileSystem API

[[CheckpointFileManager]]
`FileSystemBasedCheckpointFileManager` is a <<spark-sql-streaming-CheckpointFileManager.md#, CheckpointFileManager>> that uses Hadoop's https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html[FileSystem] API for managing checkpoint files:

* <<spark-sql-streaming-CheckpointFileManager.md#list, list>> uses link:++https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html#listStatus(org.apache.hadoop.fs.Path[],%20org.apache.hadoop.fs.PathFilter)++[FileSystem.listStatus]

* <<spark-sql-streaming-CheckpointFileManager.md#mkdirs, mkdirs>> uses link:++https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html#mkdirs(org.apache.hadoop.fs.Path,%20org.apache.hadoop.fs.permission.FsPermission)++[FileSystem.mkdirs]

* <<spark-sql-streaming-CheckpointFileManager.md#createTempFile, createTempFile>> uses link:++https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html#create(org.apache.hadoop.fs.Path,%20boolean)++[FileSystem.create] (with overwrite enabled)

* [[createAtomic]] <<spark-sql-streaming-CheckpointFileManager.md#createAtomic, createAtomic>> uses `RenameBasedFSDataOutputStream`

* <<spark-sql-streaming-CheckpointFileManager.md#open, open>> uses link:++https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html#open(org.apache.hadoop.fs.Path)++[FileSystem.open]

* <<spark-sql-streaming-CheckpointFileManager.md#exists, exists>> uses link:++https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html#getFileStatus(org.apache.hadoop.fs.Path)++[FileSystem.getFileStatus]

* <<spark-sql-streaming-CheckpointFileManager.md#renameTempFile, renameTempFile>> uses link:++https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html#rename(org.apache.hadoop.fs.Path,%20org.apache.hadoop.fs.Path)++[FileSystem.rename]

* <<spark-sql-streaming-CheckpointFileManager.md#delete, delete>> uses link:++https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html#delete(org.apache.hadoop.fs.Path,%20boolean)++[FileSystem.delete] (with recursive enabled)

* <<spark-sql-streaming-CheckpointFileManager.md#isLocal, isLocal>> is `true` for the <<fs, FileSystem>> being `LocalFileSystem` or `RawLocalFileSystem`

`FileSystemBasedCheckpointFileManager` is <<creating-instance, created>> exclusively when `CheckpointFileManager` helper object is requested for a <<spark-sql-streaming-CheckpointFileManager.md#create, CheckpointFileManager>> (for <<spark-sql-streaming-HDFSMetadataLog.md#, HDFSMetadataLog>>, <<spark-sql-streaming-StreamMetadata.md#, StreamMetadata>> and <<spark-sql-streaming-HDFSBackedStateStoreProvider.md#, HDFSBackedStateStoreProvider>>).

[[RenameHelperMethods]]
`FileSystemBasedCheckpointFileManager` is a `RenameHelperMethods` for <<createAtomic, atomicity>> by "write-to-temp-file-and-rename".

=== [[creating-instance]] Creating FileSystemBasedCheckpointFileManager Instance

`FileSystemBasedCheckpointFileManager` takes the following to be created:

* [[path]] Checkpoint directory (Hadoop's https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/Path.html[Path])
* [[hadoopConf]] Configuration (Hadoop's http://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/conf/Configuration.html[Configuration])

`FileSystemBasedCheckpointFileManager` initializes the <<internal-properties, internal properties>>.

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| fs
a| [[fs]] Hadoop's https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html[FileSystem] of the <<path, checkpoint directory>>

|===
