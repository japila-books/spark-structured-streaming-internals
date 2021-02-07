== [[EpochCoordinatorRef]] EpochCoordinatorRef

`EpochCoordinatorRef` is...FIXME

=== [[create]] Creating Remote Reference to EpochCoordinator RPC Endpoint -- `create` Factory Method

[source, scala]
----
create(
  writer: StreamWriter,
  reader: ContinuousReader,
  query: ContinuousExecution,
  epochCoordinatorId: String,
  startEpoch: Long,
  session: SparkSession,
  env: SparkEnv): RpcEndpointRef
----

`create`...FIXME

NOTE: `create` is used exclusively when `ContinuousExecution` is requested to <<ContinuousExecution.md#runContinuous, run a streaming query in continuous mode>>.

=== [[get]] Getting Remote Reference to EpochCoordinator RPC Endpoint -- `get` Factory Method

[source, scala]
----
get(id: String, env: SparkEnv): RpcEndpointRef
----

`get`...FIXME

[NOTE]
====
`get` is used when:

* `DataSourceV2ScanExec` leaf physical operator is requested for the input RDDs (and creates a <<ContinuousDataSourceRDD.md#, ContinuousDataSourceRDD>> for a [ContinuousReader](continuous-execution/ContinuousReader.md))

* `ContinuousQueuedDataReader` is created (and initializes the <<ContinuousQueuedDataReader.md#epochCoordEndpoint, epochCoordEndpoint>>)

* `EpochMarkerGenerator` is created (and initializes the <<EpochMarkerGenerator.md#epochCoordEndpoint, epochCoordEndpoint>>)

* `ContinuousWriteRDD` is requested to <<ContinuousWriteRDD.md#compute, compute a partition>>

* `WriteToContinuousDataSourceExec` is requested to <<WriteToContinuousDataSourceExec.md#doExecute, execute and generate a recipe for a distributed computation (as an RDD[InternalRow])>>
====
