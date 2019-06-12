== [[WriteToContinuousDataSource]] WriteToContinuousDataSource Unary Logical Operator

[[children]]
`WriteToContinuousDataSource` is a unary logical operator (`LogicalPlan`) that is <<creating-instance, created>> exclusively when `ContinuousExecution` is requested to <<spark-sql-streaming-ContinuousExecution.adoc#runContinuous, run a streaming query in continuous mode>> (to create an <<spark-sql-streaming-IncrementalExecution.adoc#, IncrementalExecution>>).

`WriteToContinuousDataSource` is planned (_translated_) to a <<spark-sql-streaming-WriteToContinuousDataSourceExec.adoc#, WriteToContinuousDataSourceExec>> unary physical operator (when `DataSourceV2Strategy` execution planning strategy is requested to plan a logical query).

TIP: Read up on https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-SparkStrategy-DataSourceV2Strategy.html[DataSourceV2Strategy Execution Planning Strategy] in https://bit.ly/spark-sql-internals[The Internals of Spark SQL] book.

[[creating-instance]]
`WriteToContinuousDataSource` takes the following to be created:

* [[writer]] <<spark-sql-streaming-StreamWriter.adoc#, StreamWriter>>
* [[query]] Child logical operator (`LogicalPlan`)

[[output]]
`WriteToContinuousDataSource` uses empty output schema (which is exactly to say that no output is expected whatsoever).
