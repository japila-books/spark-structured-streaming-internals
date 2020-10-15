# WriteToContinuousDataSource Unary Logical Operator

`WriteToContinuousDataSource` is a unary logical operator (`LogicalPlan`) that is created when `ContinuousExecution` is requested to [run a streaming query in continuous mode](ContinuousExecution.md#runContinuous) (to create an [IncrementalExecution](IncrementalExecution.md)).

`WriteToContinuousDataSource` is planned (_translated_) to a [WriteToContinuousDataSourceExec](spark-sql-streaming-WriteToContinuousDataSourceExec.md) unary physical operator (when `DataSourceV2Strategy` execution planning strategy is requested to plan a logical query).

[[output]]
`WriteToContinuousDataSource` uses empty output schema (which is exactly to say that no output is expected whatsoever).

## Creating Instance

`WriteToContinuousDataSource` takes the following to be created:

* [[query]] Child logical operator (`LogicalPlan`)
