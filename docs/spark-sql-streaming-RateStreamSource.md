# RateStreamSource

`RateStreamSource` is a [streaming source](Source.md) that generates <<schema, consecutive numbers with timestamp>> that can be useful for testing and PoCs.

`RateStreamSource` <<creating-instance, is created>> for *rate* format (that is registered by spark-sql-streaming-RateSourceProvider.md[RateSourceProvider]).

[source, scala]
----
val rates = spark
  .readStream
  .format("rate") // <-- use RateStreamSource
  .option("rowsPerSecond", 1)
  .load
----

[[options]]
.RateStreamSource's Options
[cols="1m,1,2",options="header",width="100%"]
|===
| Name
| Default Value
| Description

| numPartitions
| (default parallelism)
| [[numPartitions]] Number of partitions to use

| rampUpTime
| `0` (seconds)
| [[rampUpTime]]

| rowsPerSecond
| `1`
| [[rowsPerSecond]] Number of rows to generate per second (has to be greater than `0`)

|===

[[schema]]
`RateStreamSource` uses a predefined schema that cannot be changed.

[source, scala]
----
val schema = rates.schema
scala> println(schema.treeString)
root
 |-- timestamp: timestamp (nullable = true)
 |-- value: long (nullable = true)
----

.RateStreamSource's Dataset Schema (in the positional order)
[cols="1m,1m",options="header",width="100%"]
|===
| Name
| Type

| timestamp
| TimestampType

| value
| LongType

|===

[[internal-registries]]
.RateStreamSource's Internal Registries and Counters
[cols="1m,2",options="header",width="100%"]
|===
| Name
| Description

| clock
| [[clock]]

| lastTimeMs
| [[lastTimeMs]]

| maxSeconds
| [[maxSeconds]]

| startTimeMs
| [[startTimeMs]]

|===

[[logging]]
[TIP]
====
Enable `INFO` or `DEBUG` logging levels for `org.apache.spark.sql.execution.streaming.RateStreamSource` to see what happens inside.

Add the following line to `conf/log4j.properties`:

```
log4j.logger.org.apache.spark.sql.execution.streaming.RateStreamSource=DEBUG
```

Refer to spark-sql-streaming-logging.md[Logging].
====

=== [[getBatch]] Generating DataFrame for Streaming Batch -- `getBatch` Method

[source, scala]
----
getBatch(start: Option[Offset], end: Offset): DataFrame
----

`getBatch` is a part of the [Source](Source.md#getBatch) abstraction.

Internally, `getBatch` calculates the seconds to start from and end at (from the input `start` and `end` offsets) or assumes `0`.

`getBatch` then calculates the values to generate for the start and end seconds.

You should see the following DEBUG message in the logs:

```text
DEBUG RateStreamSource: startSeconds: [startSeconds], endSeconds: [endSeconds], rangeStart: [rangeStart], rangeEnd: [rangeEnd]
```

If the start and end ranges are equal, `getBatch` creates an empty `DataFrame` (with the <<schema, schema>>) and returns.

Otherwise, when the ranges are different, `getBatch` creates a `DataFrame` using `SparkContext.range` operator (for the start and end ranges and <<numPartitions, numPartitions>> partitions).

=== [[creating-instance]] Creating RateStreamSource Instance

`RateStreamSource` takes the following when created:

* [[sqlContext]] `SQLContext`
* [[metadataPath]] Path to the metadata
* [[rowsPerSecond]] Rows per second
* [[rampUpTimeSeconds]] RampUp time in seconds
* [[numPartitions]] Number of partitions
* [[useManualClock]] Flag to whether to use `ManualClock` (`true`) or `SystemClock` (`false`)

`RateStreamSource` initializes the <<internal-registries, internal registries and counters>>.
