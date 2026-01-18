# Real-Time Mode

**Real-Time Mode (RTM)** is a new execution model designed to lower end-to-end data processing latency (to the order of 100 milliseconds).

Real-Time Mode lets streaming queries running in real-time mode for continuous, sub-second latency processing.

Stateless tasks can hit single-digit millisecond latency.

The goal of Real-Time Mode is to enable Apache Spark to power real-time applications (like instant anomaly alerts or live personalization) that today cannot meet their latency requirements with Spark's current streaming engine.

Real-Time Mode is not supported in [AsyncProgressTrackingMicroBatchExecution](../AsyncProgressTrackingMicroBatchExecution.md).

[Adaptive Query Execution]({{ book.spark_sql }}/adaptive-query-execution/) is not supported in Real-time Mode.

[MicroBatchExecution](../micro-batch-execution/MicroBatchExecution.md) stream execution engine reports [new data available](../micro-batch-execution/MicroBatchExecution.md#isNewDataAvailable) constantly.

Real-Time Mode is supported by [streaming sources](../SparkDataStream.md) with [SupportsRealTimeMode](SupportsRealTimeMode.md).

Real-Time Mode does not support [Sink](../Sink.md)s due to API limitations (i.e., no writing outputs row by row).

Real-Time Mode supports [Update](../OutputMode.md#Update) output mode only (that is enforced by [UnsupportedOperationChecker](../UnsupportedOperationChecker.md#checkAdditionalRealTimeModeConstraints)).

??? note "Apache Spark 4.1.0"
    Real-Time Mode was introduced in [Apache Spark 4.1.0]({{ spark.jira }}/SPARK-53736).

??? note "Continuous Mode"
    The experimental [Continuous Mode](../continuous-execution/index.md) shares the same goals but only supports limited queries.

## RealTimeTrigger

[Trigger.RealTimeTrigger](RealTimeTrigger.md)

## Learning Resources

* [Introducing Real-Time Mode in Apache Spark™ Structured Streaming](https://www.databricks.com/blog/introducing-real-time-mode-apache-sparktm-structured-streaming)
