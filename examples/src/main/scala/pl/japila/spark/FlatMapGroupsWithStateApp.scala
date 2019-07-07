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
    .as[(Timestamp, Session.UserId)] // convert DataFrame to Dataset to make groupByKey easier to write
    .groupByKey { case (_, userId) => userId }
    .flatMapGroupsWithState(flatMapOutputMode, timeoutConf)(Session.countClicksPerSession)
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

  // delayThreshold "delays" event-time watermark
  // so whatever events are generated
  // their timestamp is delayThreshold earlier to calculate watermark
  // Use the following logger to learn how watermark is computed
  // log4j.logger.org.apache.spark.sql.execution.streaming.WatermarkTracker=ALL
  val eventGen = new EventGenerator(startTime = delayThreshold.toMillis + 1)
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
