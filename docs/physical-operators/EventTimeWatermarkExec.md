# EventTimeWatermarkExec Unary Physical Operator

`EventTimeWatermarkExec` is a unary physical operator that represents [EventTimeWatermark](../logical-operators/EventTimeWatermark.md) logical operator at execution time.

!!! tip
    A unary physical operator (`UnaryExecNode`) is a physical operator with a single [child](#child) physical operator.

    Learn more about [Unary Physical Operators]({{ book.spark_sql }}/physical-operators/UnaryExecNode) (and physical operators in general) in [The Internals of Spark SQL]({{ book.spark_sql }}) online book.

`EventTimeWatermarkExec` operator is used to extract (_project_) the values of the [event-time watermark column](#eventTime) and add them all to the [EventTimeStatsAccum](#eventTimeStats) accumulator (and produce a [EventTimeStats](../EventTimeStats.md)).

## Creating Instance

`EventTimeWatermarkExec` takes the following to be created:

* <span id="eventTime"> Catalyst `Attribute` for event time ([Spark SQL]({{ book.spark_sql }}/expressions/Attribute))
* <span id="delay"> Delay Interval ([Spark SQL]({{ book.spark_sql }}/CalendarInterval))
* <span id="child"> Child Physical Operator ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan))

When created, `EventTimeWatermarkExec` registers the [EventTimeStatsAccum](#eventTimeStats) accumulator (with the current `SparkContext`).

`EventTimeWatermarkExec` is created when [StatefulAggregationStrategy](../StatefulAggregationStrategy.md) execution planning strategy is executed (requested to plan a [EventTimeWatermark](../logical-operators/EventTimeWatermark.md) logical operator for execution).

## <span id="eventTimeStats"> EventTimeStats Accumulator

```scala
eventTimeStats: EventTimeStatsAccum
```

`EventTimeWatermarkExec` creates an [EventTimeStatsAccum](../EventTimeStatsAccum.md) accumulator when [created](#creating-instance).

When [executed](#doExecute), `EventTimeWatermarkExec` uses the `EventTimeStatsAccum` to extract and accumulate [eventTime](#eventTime) values (as `Long`s) from every row in a streaming batch.

!!! note
    Since the execution (data processing) happens on Spark executors, the only way to establish communication between the tasks (on the executors) and the driver is to use accumulator facility.

    Learn more about [Accumulators]({{ book.spark_core }}/accumulators) in [The Internals of Apache Spark]({{ book.spark_core }}) online book.

`eventTimeStats` is registered (with the current `SparkContext`) when `EventTimeWatermarkExec` is [created](#creating-instance). `eventTimeStats` uses no name (_unnamed accumulator_).

`eventTimeStats` is used to transfer the statistics (maximum, minimum, average and update count) of the long values in the [event-time watermark column](#eventTime) to be used for the following:

* `ProgressReporter` is requested for the most recent [execution statistics](../monitoring/ProgressReporter.md#extractExecutionStats) (for `max`, `min`, `avg`, and `watermark` event-time watermark statistics)

* `WatermarkTracker` is requested to [updateWatermark](../WatermarkTracker.md#updateWatermark)

## <span id="doExecute"> Executing Physical Operator

```scala
doExecute(): RDD[InternalRow]
```

`doExecute` is part of the `SparkPlan` ([Spark SQL]({{ book.spark_sql }}/physical-operators/SparkPlan)) abstraction.

`doExecute` executes the [child](#child) physical operator and maps over the partitions (using `RDD.mapPartitions`).

`doExecute` creates an unsafe projection (per partition) for the [column with the event time](#eventTime) in the output schema of the [child](#child) physical operator. The unsafe projection is to extract event times from the (stream of) internal rows of the child physical operator.

For every row in a partition, `doExecute` requests the [eventTimeStats](#eventTimeStats) accumulator to [accumulate the event time](../EventTimeStatsAccum.md#add).

!!! note
    The event time value is in seconds (not millis as the value is divided by `1000` ).

## <span id="output"> Output Attributes

```scala
output: Seq[Attribute]
```

`output` is part of the `QueryPlan` ([Spark SQL]({{ book.spark_sql }}/catalyst/QueryPlan)) abstraction.

`output` requests the [child](#child) physical operator for the output attributes to find the [event time attribute](#eventTime) and any other column with metadata that contains [spark.watermarkDelayMs](../EventTimeWatermark.md#delayKey) key.

For the event time attribute, `output` updates the metadata to include the [delay interval](#delayMs) for the [spark.watermarkDelayMs](../EventTimeWatermark.md#delayKey) key.

For any other column (not the [event time attribute](#eventTime)) with the [spark.watermarkDelayMs](../EventTimeWatermark.md#delayKey) key, `output` removes the key from the attribute metadata.

## Demo

Check out [Demo: Streaming Watermark with Aggregation in Append Output Mode](../demo/watermark-aggregation-append.md) to deep dive into the internals of [Streaming Watermark](../spark-sql-streaming-watermark.md).
