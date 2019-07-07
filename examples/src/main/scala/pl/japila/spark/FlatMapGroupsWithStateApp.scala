package pl.japila.spark

import org.apache.spark.sql.execution.streaming.LongOffset

object FlatMapGroupsWithStateApp extends SparkStreamsApp {

  // FIXME Make it configurable from the command line
  spark.sparkContext.setLogLevel("OFF")

  // Define event "format"
  // Event time must be defined on a window or a timestamp
  import java.sql.Timestamp
  case class Event(time: Timestamp, userId: Long)
  import scala.concurrent.duration._
  class EventGenerator(val startTime: Long = System.currentTimeMillis()) {
    def generate(userId: Long, offset: Duration = 0.seconds): Event = {
      val time = new Timestamp(startTime + offset.toMillis)
      Event(time, userId)
    }
  }

  // Using memory data source for full control of the input
  import org.apache.spark.sql.execution.streaming.MemoryStream
  implicit val sqlCtx = spark.sqlContext
  import spark.implicits._
  val events = MemoryStream[Event]
  val sessions = events.toDS
  assert(sessions.isStreaming, "sessions must be a streaming Dataset")

  // FIXME Make it configurable from the command line
  import scala.concurrent.duration._
  val delayThreshold = 10.seconds
  val eventTime = "time"

  println(
    s"""
      |Setting watermark column and delay for EventTimeTimeout
      |... eventTime column: $eventTime
      |... delayThreshold:   $delayThreshold
      |""".stripMargin)
  val valuesWatermarked = sessions
    .withWatermark(eventTime, delayThreshold.toString)

  // FIXME Make it configurable from the command line
  val stateDurationMs = 1.second.toMillis

  case class Session(
      userId: Long,
      beginTimestamp: Long,
      var clicks: Long = 0,
      var endTimestamp: Long = 0) {
    def duration: Long = endTimestamp - beginTimestamp
    def recordClick(time: Timestamp): Unit = {
      clicks = clicks + 1
      val ms = time.getTime
      if (ms > endTimestamp) {
        endTimestamp = ms
      }
    }
    def expire(time: Long): Unit = {
      endTimestamp = time
    }
  }

  type UserId = Long
  import org.apache.spark.sql.streaming.GroupState
  def countClicksPerSession(
      userId: UserId,
      values: Iterator[(Timestamp, UserId)],
      state: GroupState[Session]): Iterator[Session] = {
    val s = state.getOption.getOrElse("<undefined>")
    val currentProcessingTimeMs = state.getCurrentProcessingTimeMs
    val currentWatermarkMs = state.getCurrentWatermarkMs
    val hasTimedOut = state.hasTimedOut
    println(
      s"""
         |... countClicksPerSession(userId=$userId, state=$s)
         |... ... Batch Processing Time: $currentProcessingTimeMs ms
         |... ... Event-Time Watermark:  $currentWatermarkMs ms
         |... ... hasTimedOut:           $hasTimedOut
       """.stripMargin)
    val result = if (hasTimedOut) {
      val userSession = state.get
      userSession.expire(currentProcessingTimeMs)
      println(
        s"""
           |... countClicksPerSession: State expired for userId=$userId
           |... ... userSession: $userSession
           |... ... Goes to the output
           |... ... Removing state (to keep memory usage low)
         """.stripMargin)
      state.remove()
      Iterator(userSession)
    } else {
      val session = if (state.exists) {
        val session = state.get
        for {
          (time, _) <- values
        } session.recordClick(time)
        println(
          s"""
             |... countClicksPerSession: State exists for userId=$userId
             |... ... userSession: $session
           """.stripMargin)
        session
      } else {
        // FIXME How to record events for a new session?
        // Avoid two passes over values
        // Iterator is a one-off data structure
        // Once an element is accessed, it is gone forever
        val vs = values.toSeq
        val beginTimestamp = vs.maxBy { case (_, time) => time }._1
        val session = Session(userId, beginTimestamp.getTime)
        for {
          (time, _) <- vs
        } session.recordClick(time)
        println(
          s"""
             |... countClicksPerSession: State does not exist for userId=$userId
             |... ... userSession: $session
         """.stripMargin)
        session
      }
      state.update(session)

      // timeout makes a session expired when no data received for stateDurationMs
      val timeoutMs = currentWatermarkMs + stateDurationMs
      println(
        s"""
           |... ... Setting state timeout to $timeoutMs ms
           |... ... $stateDurationMs ms later after event-time watermark: $currentWatermarkMs ms
         """.stripMargin)
      state.setTimeoutTimestamp(timeoutMs)

      Iterator.empty
    }
    println("")
    result
  }

  import org.apache.spark.sql.streaming.OutputMode

  // FIXME Configurable from the command line
  val queryOutputMode = OutputMode.Update

  // FIXME Configurable from the command line
  val flatMapOutputMode = OutputMode.Update

  // FIXME Configurable from the command line
  import org.apache.spark.sql.streaming.GroupStateTimeout
  val timeoutConf = GroupStateTimeout.EventTimeTimeout

  import org.apache.spark.sql.functions.current_timestamp
  val valuesCounted = valuesWatermarked
    .as[(Timestamp, UserId)] // convert DataFrame to Dataset to make groupByKey easier to write
    .groupByKey { case (_, userId) => userId }
    .flatMapGroupsWithState(flatMapOutputMode, timeoutConf)(countClicksPerSession)
    .select($"userId", $"clicks", $"endTimestamp" - $"beginTimestamp" as "duration")
    .withColumn("Batch Processing Time", current_timestamp())

  // FIXME Configurable from the command line
  deleteCheckpointLocation()

  val streamingQuery = valuesCounted
    .writeStream
    .format("memory")
    .queryName(queryName)
    .option("checkpointLocation", checkpointLocation)
    .outputMode(queryOutputMode)
    .start
  val currentStatus = streamingQuery.status.message
  val expectedStatus = "Waiting for data to arrive"
  assert(
    currentStatus == expectedStatus,
    s"""Current status: $currentStatus not $expectedStatus""")

  println(
    s"""
       |Demo: Stateful Counter
       |
       |Counting the number of clicks across batches (globally)
       |User clicks (visits) pages generating events (who, when)
       |As long as it uses the website actively (clicks within $stateDurationMs)
       |Session (state) does not time-out => every update advances when it happens
       |
       |State key: userId
       |State value: number of clicks (across batches to create a logical session)
       |
       |Every batch gives the number of clicks
       |In order to have a global count across batches
       |flatMap... gives us access to the state (per unique value) with the global count
       |the global count (across batches) is updated with the batch-only count
     """.stripMargin)

  val eventGen = new EventGenerator
  println(s"EventGenerator.startTime: ${eventGen.startTime}")

  // Sorry, it's simply to copy and paste event sections
  // and track the batches :)
  // FIXME Create batch generator (to read data from a directory?)
  var batchNo: Int = 0

  {
    batchNo = batchNo + 1
    println(
      s"""
        |Batch $batchNo
      """.stripMargin)
    val batch =
      eventGen.generate(userId = 1, offset = 1.second) ::
      Nil
    val currentOffset = events.addData(batch)
    streamingQuery.processAllAvailable()
    // the following is memory data source-specific
    events.commit(currentOffset.asInstanceOf[LongOffset])

    val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
    val currentWatermarkMs = toMillis(currentWatermark)
    println(s"Current watermark: $currentWatermarkMs ms")
    println()
    println(streamingQuery.lastProgress.prettyJson)

    spark
      .table(queryName)
      .orderBy("userId", "Batch Processing Time")
      .select($"userId", $"clicks", $"duration", $"Batch Processing Time")
      .show(truncate = false)
  }

  pause()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
      """.stripMargin)
    val batch =
      eventGen.generate(userId = 2, offset = 3.seconds) ::
      Nil
    val currentOffset = events.addData(batch)
    streamingQuery.processAllAvailable()
    // the following is memory data source-specific
    events.commit(currentOffset.asInstanceOf[LongOffset])

    spark
      .table(queryName)
      .orderBy("userId", "Batch Processing Time")
      .select($"userId", $"clicks", $"duration", $"Batch Processing Time")
      .show(truncate = false)
  }

  pause()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
      """.stripMargin)
    val batch =
      eventGen.generate(userId = 2, offset = 4.seconds) ::
      Nil
    val currentOffset = events.addData(batch)
    streamingQuery.processAllAvailable()
    // the following is memory data source-specific
    events.commit(currentOffset.asInstanceOf[LongOffset])

    spark
      .table(queryName)
      .orderBy("userId", "Batch Processing Time")
      .select($"userId", $"clicks", $"duration", $"Batch Processing Time")
      .show(truncate = false)
  }

  pause()

}
