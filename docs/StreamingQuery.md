# StreamingQuery

`StreamingQuery` is an [abstraction](#contract) of [handles to streaming queries](#implementations) (that are executed continuously and concurrently on a [separate thread](StreamExecution.md#queryExecutionThread)).

## Creating StreamingQuery

`StreamingQuery` is created when a streaming query is started using [DataStreamWriter.start](DataStreamWriter.md#start) operator.

??? tip "Demo: Deep Dive into FileStreamSink"
    Learn more in [Demo: Deep Dive into FileStreamSink](demo/deep-dive-into-filestreamsink.md).

## Managing Active StreamingQueries

[StreamingQueryManager](StreamingQueryManager.md) manages active `StreamingQuery` instances and allows to access one (by [id](#id)) or all active queries (using [StreamingQueryManager.get](StreamingQueryManager.md#get) or [StreamingQueryManager.active](StreamingQueryManager.md#active) operators, respectively).

## States

`StreamingQuery` can be in [two states](#isActive):

* Active (started)
* Inactive (stopped)

If inactive, `StreamingQuery` may have stopped due to an [StreamingQueryException](#exception).

## Implementations

* [StreamExecution](StreamExecution.md)
* [StreamingQueryWrapper](spark-sql-streaming-StreamingQueryWrapper.md)

## Contract

### <span id="awaitTermination"> awaitTermination

```scala
awaitTermination(): Unit
awaitTermination(
  timeoutMs: Long): Boolean
```

Used when...FIXME

### <span id="exception"> StreamingQueryException

```scala
exception: Option[StreamingQueryException]
```

`StreamingQueryException` if the streaming query has finished due to an exception

Used when...FIXME

### <span id="explain"> Explaining Streaming Query

```scala
explain(): Unit
explain(
  extended: Boolean): Unit
```

Used when...FIXME

### <span id="id"> Id

```scala
id: UUID
```

Unique identifier of the streaming query (that does not change across restarts unlike [runId](#runId))

Used when...FIXME

### <span id="isActive"> isActive

```scala
isActive: Boolean
```

Indicates whether the streaming query is active (`true`) or not (`false`)

Used when...FIXME

### <span id="lastProgress"> StreamingQueryProgress

```scala
lastProgress: StreamingQueryProgress
```

The latest [StreamingQueryProgress](monitoring/StreamingQueryProgress.md) of the streaming query

Used when...FIXME

### <span id="name"> Query Name

```scala
name: String
```

Name of the streaming query (unique across all active queries in [SparkSession](#sparkSession))

Used when...FIXME

### <span id="processAllAvailable"> Processing All Available Data

```scala
processAllAvailable(): Unit
```

Pauses (_blocks_) the current thread until the streaming query has no more data to be processed or has been [stopped](#stop).

Intended for testing

Used when...FIXME

### <span id="recentProgress"> Recent StreamingQueryProgresses

```scala
recentProgress: Array[StreamingQueryProgress]
```

Recent [StreamingQueryProgress](monitoring/StreamingQueryProgress.md) updates.

Used when...FIXME

### <span id="runId"> Run Id

```scala
runId: UUID
```

Unique identifier of the current execution of the streaming query (that is different every restart unlike [id](#id))

Used when...FIXME

### <span id="sparkSession"> SparkSession

```scala
sparkSession: SparkSession
```

Used when...FIXME

### <span id="status"> StreamingQueryStatus

```scala
status: StreamingQueryStatus
```

[StreamingQueryStatus](monitoring/StreamingQueryStatus.md) of the streaming query (as `StreamExecution` [has accumulated](monitoring/ProgressReporter.md#currentStatus) being a `ProgressReporter` while running the streaming query)

Used when...FIXME

### <span id="stop"> Stopping Streaming Query

```scala
stop(): Unit
```

Stops the streaming query

Used when...FIXME
