package pl.japila.spark

import org.apache.spark.sql.execution.streaming.LongOffset

object FlatMapGroupsWithStateApp extends SparkStreamsApp {

  // FIXME Make it configurable from the command line
  spark.sparkContext.setLogLevel("OFF")

  // Define event "format"
  // Event time must be defined on a window or a timestamp
  import java.sql.Timestamp
  case class Event(time: Timestamp, value: Long)
  import scala.concurrent.duration._
  class EventGenerator(startTime: Long = System.currentTimeMillis()) {
    def generate(value: Long, offset: Duration = 0.seconds): Event = {
      val time = new Timestamp(startTime + offset.toMillis)
      Event(time, value)
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

  // FIXME Make it configurable from the command line
  val stateDurationMs = 1.second.toMillis

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
         |... ... Event-Time Watermark:  $currentWatermarkMs ms
         |... ... hasTimedOut: ${state.hasTimedOut}
       """.stripMargin)
    val count = values.length
    val result = if (state.exists && state.hasTimedOut) {
      val s = state.get
      val cnt = s.count
      println(
        s"""
           |... counter: State expired for key=$key with count=$cnt
           |... ... (key=$key, count=$cnt) goes to the output
           |... ... Removing state (to keep memory usage low)
         """.stripMargin)
      state.remove()
      Iterator((key, cnt))
    } else if (state.exists) {
      val s = state.get
      val oldCount = s.count
      val newCount = oldCount + count
      println(
        s"""
           |... counter: State exists for key=$key with count=$oldCount
           |... ... Updating state with count=$newCount
         """.stripMargin)
      val newState = State(newCount)
      state.update(newState)
      Iterator.empty
    } else {
      println(
        s"""
           |... counter: State does not exist for key=$key
           |... ... Creating new state with count=$count
         """.stripMargin)
      val newState = State(count)
      state.update(newState)

      // FIXME Why not simply setTimeoutDuration since it does the same calculation?!
      val timeoutMs = currentWatermarkMs + stateDurationMs
      println(
        s"""
           |... ... Setting state timeout to $timeoutMs ms
           |... ... ($stateDurationMs ms later from now)
         """.stripMargin)
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

  val eventGen = new EventGenerator

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
        |Event-Time Watermark advances (to 2 seconds) so there will be extra non-data batch
        |Session/state timeout is 1 second so the states expire
      """.stripMargin)
    val batch = Seq(
      eventGen.generate(value = 1, offset = 1.second),
      eventGen.generate(value = 2, offset = 2.seconds))
    val currentOffset = events.addData(batch)
    streamingQuery.processAllAvailable()
    // the following is memory data source-specific
    events.commit(currentOffset.asInstanceOf[LongOffset])

    val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
    println(s"Current watermark: $currentWatermark")
    println()
    println(streamingQuery.lastProgress.prettyJson)

    spark
      .table(queryName)
      .orderBy("value", "Batch Processing Time")
      .show(truncate = false)
  }

  pause()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
         |Some counts are not 1
         |State available and expired (key 1)
         |Event-Time Watermark same (at 2 seconds) so no extra non-data batch
      """.stripMargin)
    val batch = Seq(
      eventGen.generate(value = 1, offset = 0.seconds),
      eventGen.generate(value = 1, offset = 1.seconds),
      eventGen.generate(value = 3, offset = 2.seconds))
    val currentOffset = events.addData(batch)
    streamingQuery.processAllAvailable()
    // the following is memory data source-specific
    events.commit(currentOffset.asInstanceOf[LongOffset])

    spark
      .table(queryName)
      .orderBy("value", "Batch Processing Time")
      .show(truncate = false)
  }

  pause()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
         |Watermark should advance
      """.stripMargin)
    val batch = Seq(
      eventGen.generate(value = 3, offset = 3.seconds),
      eventGen.generate(value = 3, offset = 4.seconds),
      eventGen.generate(value = 4, offset = 1.seconds))
    val currentOffset = events.addData(batch)
    streamingQuery.processAllAvailable()
    // the following is memory data source-specific
    events.commit(currentOffset.asInstanceOf[LongOffset])

    spark
      .table(queryName)
      .orderBy("value", "Batch Processing Time")
      .show(truncate = false)
  }

  pause()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
         |Watermark should advance
      """.stripMargin)
    val batch = Seq(
      eventGen.generate(value = 3, offset = 1.seconds))
    val currentOffset = events.addData(batch)
    streamingQuery.processAllAvailable()
    // the following is memory data source-specific
    events.commit(currentOffset.asInstanceOf[LongOffset])

    spark
      .table(queryName)
      .orderBy("value", "Batch Processing Time")
      .show(truncate = false)
  }

  pause()
}
