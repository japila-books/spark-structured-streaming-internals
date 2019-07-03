package pl.japila.spark

object FlatMapGroupsWithStateApp extends SparkStreamsApp {

  // FIXME Make it configurable from the command line
  spark.sparkContext.setLogLevel("OFF")

  // Define event "format"
  import java.sql.Timestamp
  case class Event(time: Timestamp, value: Long)
  import scala.concurrent.duration._
  object Event {
    def apply(secs: Long, value: Long): Event = {
      Event(new Timestamp(secs.seconds.toMillis), value)
    }
  }

  // Using memory data source for full control of the input
  import org.apache.spark.sql.execution.streaming.MemoryStream
  implicit val sqlCtx = spark.sqlContext
  import spark.implicits._
  val events = MemoryStream[Event]
  val values = events.toDS
  assert(values.isStreaming, "values must be a streaming Dataset")

  // FIXME Make it configurable from the command line
  import scala.concurrent.duration._
  val delayThreshold = 10.seconds.toString
  val eventTime = "time"

  val valuesWatermarked = values
    .withWatermark(eventTime, delayThreshold) // required for EventTimeTimeout

  // Could use Long directly, but...
  // Let's use case class to make the demo a bit more advanced
  case class State(count: Long)

  type AggregationKey = Long
  type StateValue = Long
  import org.apache.spark.sql.streaming.GroupState
  def counter(
      key: AggregationKey,
      values: Iterator[(Timestamp, Long)],
      state: GroupState[State]): Iterator[(AggregationKey, StateValue)] = {
    val s = state.getOption.getOrElse("<undefined>")
    val currentProcessingTimeMs = state.getCurrentProcessingTimeMs
    val currentWatermarkMs = state.getCurrentWatermarkMs
    println(s""">>> counter(key = $key, state = $s)""")
    println(s">>> >>> Batch Processing Time = $currentProcessingTimeMs ms")
    println(s">>> >>> currentWatermarkMs: $currentWatermarkMs ms")
    println(s">>> >>> hasTimedOut: ${state.hasTimedOut}")
    val count = values.length
    val result = if (state.exists && state.hasTimedOut) {
      val s = state.get
      println(s">>> counter: State expired for key=$key with count=${s.count}")
      Iterator((key, s.count))
    } else if (state.exists /** and not state.hasTimedOut */ ) {
      val s = state.get
      println(s">>> counter: Updating state for key=$key (current count=${s.count})")
      val newState = State(s.count + count)
      state.update(newState)
      Iterator.empty
    } else {
      println(s">>> counter: Creating new state for key=$key with value=$count")
      val newState = State(count)
      state.update(newState)

      // FIXME Why not simply setTimeoutDuration since it does the same calculation?!
      val timeoutMs = state.getCurrentWatermarkMs + 1.second.toMillis
      println(s">>> >>> timeout=$timeoutMs ms")
      state.setTimeoutTimestamp(timeoutMs)

      Iterator.empty
    }
    println("")
    result
  }

  // FIXME Configurable from the command line
  import org.apache.spark.sql.streaming.GroupStateTimeout
  val timeoutConf = GroupStateTimeout.EventTimeTimeout

  // FIXME Configurable from the command line
  import org.apache.spark.sql.streaming.OutputMode
  val flatMapOutputMode = OutputMode.Update

  import org.apache.spark.sql.functions.current_timestamp
  val valuesCounted = valuesWatermarked
    .as[(Timestamp, Long)] // convert DataFrame to Dataset to make groupByKey easier to write
    .groupByKey { case (_, value) => value }
    .flatMapGroupsWithState(flatMapOutputMode, timeoutConf)(counter)
    .toDF("value", "count")
    .withColumn("Batch Processing Time", current_timestamp())

  // FIXME Configurable from the command line
  deleteCheckpointLocation()

  // FIXME Configurable from the command line
  val queryOutputMode = OutputMode.Update

  val streamingQuery = valuesCounted
    .writeStream
    .format("memory")
    .queryName(queryName)
    .option("checkpointLocation", checkpointLocation)
    .outputMode(queryOutputMode)
    .start
  assert(streamingQuery.status.message == "Waiting for data to arrive")

  {
    val batch = Seq(
      Event(secs = 1, value = 1),
      Event(secs = 15, value = 2))
    events.addData(batch)
    streamingQuery.processAllAvailable()

    spark.table(queryName).show(truncate = false)
  }

  {
    val batch = Seq(
      Event(secs = 1, value = 1),
      Event(secs = 6, value = 3))
    events.addData(batch)
    streamingQuery.processAllAvailable()

    spark.table(queryName).show(truncate = false)
  }

  {
    val batch = Seq(
      Event(secs = 200,  value = 3))
    events.addData(batch)
    streamingQuery.processAllAvailable()

    spark.table(queryName).show(truncate = false)
  }

  // Not really needed, and exclusively so web UI is available
  // http://localhost:4040/jobs/
  streamingQuery.awaitTermination()
}
