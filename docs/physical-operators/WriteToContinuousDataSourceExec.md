# WriteToContinuousDataSourceExec Unary Physical Operator

[[children]]
`WriteToContinuousDataSourceExec` is a unary physical operator that <<doExecute, creates a ContinuousWriteRDD for continuous write>>.

[NOTE]
====
A unary physical operator (`UnaryExecNode`) is a physical operator with a single <<child, child>> physical operator.

Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-SparkPlan.html[UnaryExecNode] (and physical operators in general) in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.
====

`WriteToContinuousDataSourceExec` is <<creating-instance, created>> exclusively when `DataSourceV2Strategy` execution planning strategy is requested to plan a [WriteToContinuousDataSource](../logical-operators/WriteToContinuousDataSource.md) unary logical operator.

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-SparkStrategy-DataSourceV2Strategy.html[DataSourceV2Strategy Execution Planning Strategy] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

[[creating-instance]]
`WriteToContinuousDataSourceExec` takes the following to be created:

* [[query]][[child]] Child physical operator (`SparkPlan`)

[[output]]
`WriteToContinuousDataSourceExec` uses empty output schema (which is exactly to say that no output is expected whatsoever).

[[logging]]
[TIP]
====
Enable `ALL` logging level for `org.apache.spark.sql.execution.streaming.continuous.WriteToContinuousDataSourceExec` to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.execution.streaming.continuous.WriteToContinuousDataSourceExec=ALL
```

Refer to <<spark-sql-streaming-spark-logging.md#, Logging>>.
====

=== [[doExecute]] Executing Physical Operator (Generating RDD[InternalRow]) -- `doExecute` Method

[source, scala]
----
doExecute(): RDD[InternalRow]
----

NOTE: `doExecute` is part of `SparkPlan` Contract to generate the runtime representation of an physical operator as a distributed computation over internal binary rows on Apache Spark (i.e. `RDD[InternalRow]`).

`doExecute` requests the <<writer, StreamWriter>> to create a `DataWriterFactory`.

`doExecute` then requests the <<query, child physical operator>> to execute (that gives a `RDD[InternalRow]`) and uses the `RDD[InternalRow]` and the `DataWriterFactory` to create a <<ContinuousWriteRDD.md#, ContinuousWriteRDD>>.

`doExecute` prints out the following INFO message to the logs:

```
Start processing data source writer: [writer]. The input RDD has [partitions] partitions.
```

`doExecute` requests the `EpochCoordinatorRef` helper for a <<EpochCoordinatorRef.md#get, remote reference to the EpochCoordinator RPC endpoint>> (using the <<ContinuousExecution.md#EPOCH_COORDINATOR_ID_KEY, __epoch_coordinator_id local property>>).

NOTE: The <<EpochCoordinator.md#, EpochCoordinator RPC endpoint>> runs on the driver as the single point to coordinate epochs across partition tasks.

`doExecute` requests the EpochCoordinator RPC endpoint reference to send out a <<EpochCoordinator.md#SetWriterPartitions, SetWriterPartitions>> message synchronously.

In the end, `doExecute` requests the `ContinuousWriteRDD` to collect (which simply runs a Spark job on all partitions in an RDD and returns the results in an array).

NOTE: Requesting the `ContinuousWriteRDD` to collect is how a Spark job is ran that in turn runs tasks (one per partition) that are described by the <<ContinuousWriteRDD.md#compute, ContinuousWriteRDD.compute>> method. Since executing `collect` is meant to run a Spark job (with tasks on executors), it's in the discretion of the tasks themselves to decide when to finish (so if they want to run indefinitely, so be it). _What a clever trick!_
