package pl.japila.spark

object FlatMapGroupsWithStateApp extends SparkStreamsApp {

  // FIXME Make it configurable from the command line
  spark.sparkContext.setLogLevel("OFF")

  // Define event "format"
  // Event time must be defined on a window or a timestamp
  import java.sql.Timestamp
  case class Event(time: Timestamp, value: Long)
  import scala.concurrent.duration._
  object Event {
    def apply(value: Long): Event = {
      val ms = System.currentTimeMillis()
      Event(new Timestamp(ms), value)
    }
    def apply(time: Duration, value: Long): Event = {
      Event(new Timestamp(time.toMillis), value)
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
      values: Iterator[(Long, Long)],
      state: GroupState[State]): Iterator[(AggregationKey, StateValue)] = {
    val s = state.getOption.getOrElse("<undefined>")
    val currentProcessingTimeMs = state.getCurrentProcessingTimeMs
    val currentWatermarkMs = state.getCurrentWatermarkMs
    println(
      s"""
         |... counter(key=$key, state=$s)
         |... ... Batch Processing Time: $currentProcessingTimeMs ms
         |... ... currentWatermarkMs: $currentWatermarkMs ms
         |... ... hasTimedOut: ${state.hasTimedOut}
       """.stripMargin)
    val count = values.length
    val result = if (state.exists && state.hasTimedOut) {
      val s = state.get
      println(
        s"""
           |... counter: State expired for key=$key with count=${s.count}
           |... ... Removing state
         """.stripMargin)
      state.remove()
      val cnt = s.count
      println(s">>> >>> (key=$key, count=$cnt) goes to the output")
      Iterator((key, cnt))
    } else if (state.exists /** and not state.hasTimedOut */ ) {
      val s = state.get
      val oldCount = s.count
      val newCount = oldCount + count
      println(s">>> counter: Updating state for key=$key with count=$newCount (old: $oldCount)")
      val newState = State(newCount)
      state.update(newState)
      Iterator.empty
    } else {
      println(s">>> counter: Creating new state for key=$key with value=$count")
      val newState = State(count)
      state.update(newState)

      // FIXME Why not simply setTimeoutDuration since it does the same calculation?!
      val timeoutMs = state.getCurrentWatermarkMs + 1.second.toMillis
      println(s">>> >>> Setting timeout to $timeoutMs ms")
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
    .as[(Long, Long)] // convert DataFrame to Dataset to make groupByKey easier to write
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

  println(
    s"""
       |Demo: Stateful Counter
       |
       |Counting the number of values across batches (globally)
       |
       |State key: event value
       |State value: number of values (across all batches)
       |
       |Every batch gives the occurrences per value (state key) only
       |In order to have a global count across batches
       |flatMap... gives us access to the state (per unique value) with the global count
       |the global count (across batches) is updated with the batch-only count
     """.stripMargin)

  // Sorry, it's simply to copy and paste event sections
  // and track the batches :)
  var batchNo: Int = 0

  {
    batchNo = batchNo + 1
    println(
      s"""
        |Batch $batchNo: Unique values only
        |All counts are 1s
        |No state available
      """.stripMargin)
    val batch = Seq(
      Event(value = 1),
      Event(value = 2))
    events.addData(batch)
    streamingQuery.processAllAvailable()

    spark.table(queryName).show(truncate = false)
  }

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo: Multiple values
         |Some counts are not 1
         |State available
      """.stripMargin)
    val batch = Seq(
      Event(value = 1), // state available
      Event(value = 1), // non-unique value
      Event(value = 3)) // unique value, no state
    events.addData(batch)
    streamingQuery.processAllAvailable()

    spark.table(queryName).show(truncate = false)
  }

  {
    val batch = Seq(
      Event(value = 3))
    events.addData(batch)
    streamingQuery.processAllAvailable()

    spark.table(queryName).show(truncate = false)
  }

  // Not really needed, and exclusively so web UI is available
  // http://localhost:4040/jobs/
  streamingQuery.awaitTermination()
}
