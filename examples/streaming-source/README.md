# Demo Streaming Source (Structured Streaming)

The project contains the sources of a `demo` streaming data source with the following features:

* Reading (loading) data
* Data Source V1 ([Source](https://jaceklaskowski.gitbooks.io/spark-structured-streaming/spark-sql-streaming-Source.html)) 
* [Micro-Batch Stream Processing](https://jaceklaskowski.gitbooks.io/spark-structured-streaming/spark-sql-streaming-micro-batch-stream-processing.html)

Among the other features are:

* Resuming execution from `checkpointLocation`
    * Restarting query preserves the latest batch and offset processed (committed)
* `SparkContext.runJob` to simulate requesting remote data for `Source.getBatch`
    * Use web UI to see the jobs with the same description as the micro-batch (cf. `StreamExecution.getBatchDescriptionString`)

## Logging

The demo uses the custom logging configuration to learn the innerworkings of Structured Streaming and the features from the logs:

```
log4j.logger.org.apache.spark.sql.execution.streaming.MicroBatchExecution=ALL
```