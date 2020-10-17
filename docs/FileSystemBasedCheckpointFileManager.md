# FileSystemBasedCheckpointFileManager

[[CheckpointFileManager]]
`FileSystemBasedCheckpointFileManager` is a [CheckpointFileManager](CheckpointFileManager.md) that uses Hadoop's https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html[FileSystem] API for managing checkpoint files:

* [list](CheckpointFileManager.md#list) uses ++https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html#listStatus(org.apache.hadoop.fs.Path[],%20org.apache.hadoop.fs.PathFilter)++[FileSystem.listStatus]

* [mkdirs](CheckpointFileManager.md#mkdirs) uses ++https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html#mkdirs(org.apache.hadoop.fs.Path,%20org.apache.hadoop.fs.permission.FsPermission)++[FileSystem.mkdirs]

* [createTempFile](CheckpointFileManager.md#createTempFile) uses ++https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html#create(org.apache.hadoop.fs.Path,%20boolean)++[FileSystem.create] (with overwrite enabled)

* [[createAtomic]] [createAtomic](CheckpointFileManager.md#createAtomic) uses `RenameBasedFSDataOutputStream`

* [open](CheckpointFileManager.md#open) uses ++https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html#open(org.apache.hadoop.fs.Path)++[FileSystem.open]

* [exists](CheckpointFileManager.md#exists) uses ++https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html#getFileStatus(org.apache.hadoop.fs.Path)++[FileSystem.getFileStatus]

* [renameTempFile](CheckpointFileManager.md#renameTempFile) uses ++https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html#rename(org.apache.hadoop.fs.Path,%20org.apache.hadoop.fs.Path)++[FileSystem.rename]

* [delete](CheckpointFileManager.md#delete) uses ++https://hadoop.apache.org/docs/r2.8.3/api/org/apache/hadoop/fs/FileSystem.html#delete(org.apache.hadoop.fs.Path,%20boolean)++[FileSystem.delete] (with recursive enabled)

* [isLocal](CheckpointFileManager.md#isLocal) is `true` for the <<fs, FileSystem>> being `LocalFileSystem` or `RawLocalFileSystem`

`FileSystemBasedCheckpointFileManager` is <<creating-instance, created>> exclusively when `CheckpointFileManager` helper object is requested for a [CheckpointFileManager](CheckpointFileManager.md#create) (for [HDFSMetadataLog](HDFSMetadataLog.md), [StreamMetadata](StreamMetadata.md) and [HDFSBackedStateStoreProvider](HDFSBackedStateStoreProvider.md)).

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
