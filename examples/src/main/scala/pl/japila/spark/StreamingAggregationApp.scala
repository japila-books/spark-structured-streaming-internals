package pl.japila.spark

import org.apache.spark.sql.execution.streaming.LongOffset

object StreamingAggregationApp extends SparkStreamsApp {

  // FIXME Compare to FlatMapGroupsWithStateApp
  // FIXME It should also kick GroupbyAppendWatermarkExample out

  // FIXME Configurable from the command line
  import org.apache.spark.sql.streaming.OutputMode
  val queryOutputMode = OutputMode.Append

  println(
    s"""
       |Demo: Streaming Aggregation (Micro-Batch Stream Processing)
       |https://jaceklaskowski.gitbooks.io/spark-structured-streaming/spark-sql-streaming-aggregation.html
       |
       |Output Mode: $queryOutputMode
       |
       |Observe $checkpointLocation/state directory that gets populated with delta and snapshot files
       |- The directories are per operator and partitions IDs, e.g. 0/0 for 0th op and 0th partition
       |- The numbers in the names of the state files are state versions (and micro-batch IDs actually)
       |- The directory is available only after the first micro-batch finishes
       |
       |Just for more logs (and fun!) logging levels of the MVPs are at ALL level
       |(MVPs = most valuable players)
     """.stripMargin)

  pause()

  // FIXME Make it configurable from the command line
  spark.sparkContext.setLogLevel("OFF")

  // Define event "format"
  // Event time must be defined on a window or a timestamp
  import java.sql.Timestamp
  case class Event(time: Timestamp, value: Long, batch: Long)
  import scala.concurrent.duration._
  object Event {
    def apply(secs: Long, value: Long, batch: Long): Event = {
      Event(new Timestamp(secs.seconds.toMillis), value, batch)
    }
  }

  // Using memory data source for full control of the input
  import org.apache.spark.sql.SQLContext
  implicit val sqlCtx: SQLContext = spark.sqlContext
  import spark.implicits._
  import org.apache.spark.sql.execution.streaming.MemoryStream
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

  import org.apache.spark.sql.functions._
  val windowDuration = 5.seconds
  import org.apache.spark.sql.functions.window
  val countsPer5secWindow = valuesWatermarked
    .groupBy(window(col(eventTime), windowDuration.toString) as "sliding_window")
    .agg(collect_list("batch") as "batches", collect_list("value") as "values")

  // FIXME Configurable from the command line
  deleteCheckpointLocation()

  import org.apache.spark.sql.streaming.OutputMode
  val streamingQuery = countsPer5secWindow
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

  // Sorry, it's simply to copy and paste event sections
  // and track the batches :)
  // FIXME Create batch generator (to read data from a directory?)
  // Batch number would then be assigned automatically
  // the batch ID would be whatever this generator tracks internally
  var batchNo: Int = 0

  {
    println(
      s"""
         |Batch $batchNo
      """.stripMargin)

    println(
      s"""
         |There's actually batch 0
         |- unless resumed from checkpoint which is not the case here
         |- the app always starts from scratch (no earlier state)
         |
         |The batch 0 is started immediately after a streaming query is started
         |It has no data (and hence no Spark jobs ran and in web UI)
         |- Don't get confused with the one Spark job that did get run
         |- This is for the show operator to display the in-memory table (of the memory sink)
         |
         |Note that "stateOperators" entry in the progress report that follows is empty
         |- No stateful operator was actually executed
         |- Can't explain why (FIXME)
         |
         |You should also notice that after you started the query with memory sink
         |The only completed query in SQL tab in web UI is "Execute CreateViewCommand"
         |That's how the memory sink (DataStreamWriter actually) makes sure that the temporary table is registered
         |
         |Let's analyze the stats
         |""".stripMargin)

    val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
    val currentWatermarkMs = toMillis(currentWatermark)
    println(s"Current watermark: $currentWatermarkMs ms")
    val progressCount = streamingQuery.recentProgress.length
    println(s"Number of the recent progress reports: $progressCount")
    println()
    println(streamingQuery.lastProgress.prettyJson)
  }

  pause()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
      """.stripMargin)
    val batch = Seq(
      Event(1,  1, batch = batchNo),
      Event(15, 2, batch = batchNo))
    val currentOffset = events.addData(batch)
    streamingQuery.processAllAvailable()
    events.commit(currentOffset.asInstanceOf[LongOffset])

    val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
    val currentWatermarkMs = toMillis(currentWatermark)
    println(s"Current watermark: $currentWatermarkMs ms")
    val progressCount = streamingQuery.recentProgress.length
    println(s"Number of the recent progress reports: $progressCount")
    println()
    println(streamingQuery.lastProgress.prettyJson)

    spark
      .table(queryName)
      .orderBy("sliding_window")
      .show(truncate = false)
  }

  pause()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
      """.stripMargin)
    val batch = Seq(
      Event(1,  1, batch = batchNo),
      Event(15, 2, batch = batchNo),
      Event(35, 3, batch = batchNo))
    val currentOffset = events.addData(batch)
    streamingQuery.processAllAvailable()
    events.commit(currentOffset.asInstanceOf[LongOffset])

    val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
    val currentWatermarkMs = toMillis(currentWatermark)
    println(s"Current watermark: $currentWatermarkMs ms")
    val progressCount = streamingQuery.recentProgress.length
    println(s"Number of the recent progress reports: $progressCount")
    println()
    println(streamingQuery.lastProgress.prettyJson)

    spark
      .table(queryName)
      .orderBy("sliding_window")
      .show(truncate = false)
  }

  pause()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
      """.stripMargin)
    val batch = Seq(
      Event(15,1, batch = batchNo),
      Event(15,2, batch = batchNo),
      Event(20,3, batch = batchNo),
      Event(26,4, batch = batchNo))
    val currentOffset = events.addData(batch)
    streamingQuery.processAllAvailable()
    events.commit(currentOffset.asInstanceOf[LongOffset])

    val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
    val currentWatermarkMs = toMillis(currentWatermark)
    println(s"Current watermark: $currentWatermarkMs ms")
    val progressCount = streamingQuery.recentProgress.length
    println(s"Number of the recent progress reports: $progressCount")
    println()
    println(streamingQuery.lastProgress.prettyJson)

    spark
      .table(queryName)
      .orderBy("sliding_window")
      .show(truncate = false)
  }

  pause()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
      """.stripMargin)
    val batch = Seq(
      Event(36, 1, batch = batchNo))
    val currentOffset = events.addData(batch)
    streamingQuery.processAllAvailable()
    events.commit(currentOffset.asInstanceOf[LongOffset])

    val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
    val currentWatermarkMs = toMillis(currentWatermark)
    println(s"Current watermark: $currentWatermarkMs ms")
    val progressCount = streamingQuery.recentProgress.length
    println(s"Number of the recent progress reports: $progressCount")
    println()
    println(streamingQuery.lastProgress.prettyJson)

    spark
      .table(queryName)
      .orderBy("sliding_window")
      .show(truncate = false)
  }

  pause()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
      """.stripMargin)
    val batch = Seq(
      Event(50, 1, batch = batchNo))
    val currentOffset = events.addData(batch)
    streamingQuery.processAllAvailable()
    events.commit(currentOffset.asInstanceOf[LongOffset])

    val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
    val currentWatermarkMs = toMillis(currentWatermark)
    println(s"Current watermark: $currentWatermarkMs ms")
    val progressCount = streamingQuery.recentProgress.length
    println(s"Number of the recent progress reports: $progressCount")
    println()
    println(streamingQuery.lastProgress.prettyJson)

    spark
      .table(queryName)
      .orderBy("sliding_window")
      .show(truncate = false)
  }

  pause()

}
