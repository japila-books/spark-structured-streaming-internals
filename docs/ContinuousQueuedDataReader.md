== [[ContinuousQueuedDataReader]] ContinuousQueuedDataReader

`ContinuousQueuedDataReader` is <<creating-instance, created>> exclusively when `ContinuousDataSourceRDD` is requested to <<ContinuousDataSourceRDD.md#compute, compute a partition>>.

[[ContinuousRecord]]
`ContinuousQueuedDataReader` uses two types of *continuous records*:

* [[EpochMarker]] `EpochMarker`
* [[ContinuousRow]] `ContinuousRow` (with the `InternalRow` at `PartitionOffset`)

=== [[next]] Fetching Next Row -- `next` Method

[source, scala]
----
next(): InternalRow
----

`next`...FIXME

NOTE: `next` is used when...FIXME

=== [[close]] Closing ContinuousQueuedDataReader -- `close` Method

[source, scala]
----
close(): Unit
----

NOTE: `close` is part of the https://docs.oracle.com/javase/8/docs/api/java/io/Closeable.html[java.io.Closeable] to close this stream and release any system resources associated with it.

`close`...FIXME

=== [[creating-instance]] Creating ContinuousQueuedDataReader Instance

`ContinuousQueuedDataReader` takes the following to be created:

* [[partition]] `ContinuousDataSourceRDDPartition`
* [[context]] `TaskContext`
* [[dataQueueSize]] Size of the <<queue, data queue>>
* [[epochPollIntervalMs]] `epochPollIntervalMs`

`ContinuousQueuedDataReader` initializes the <<internal-properties, internal properties>>.

=== [[internal-properties]] Internal Properties

[cols="30m,70",options="header",width="100%"]
|===
| Name
| Description

| coordinatorId
a| [[coordinatorId]] *Epoch Coordinator Identifier*

Used when...FIXME

| currentOffset
a| [[currentOffset]] `PartitionOffset`

Used when...FIXME

| dataReaderThread
a| [[dataReaderThread]] <<DataReaderThread.md#, DataReaderThread>> daemon thread that is created and started immediately when `ContinuousQueuedDataReader` is <<creating-instance, created>>

Used when...FIXME

| epochCoordEndpoint
a| [[epochCoordEndpoint]] `RpcEndpointRef` of the <<EpochCoordinator.md#, EpochCoordinator>> per <<coordinatorId, coordinatorId>>

Used when...FIXME

| epochMarkerExecutor
a| [[epochMarkerExecutor]] https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ScheduledExecutorService.html[java.util.concurrent.ScheduledExecutorService]

Used when...FIXME

| epochMarkerGenerator
a| [[epochMarkerGenerator]] <<EpochMarkerGenerator.md#, EpochMarkerGenerator>>

Used when...FIXME

| reader
a| [[reader]] `InputPartitionReader`

Used when...FIXME

| queue
a| [[queue]] https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ArrayBlockingQueue.html[java.util.concurrent.ArrayBlockingQueue] of <<ContinuousRecord, ContinuousRecords>> (of the given <<dataQueueSize, data size>>)

Used when...FIXME

|===
