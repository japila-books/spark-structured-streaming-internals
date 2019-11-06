package pl.japila.spark

object StreamStreamJoinApp extends SparkStreamsApp {

  // FIXME Compare to StreamingAggregationApp

  // FIXME Configurable from the command line
  import org.apache.spark.sql.streaming.OutputMode
  val queryOutputMode = OutputMode.Append

  println(
    s"""
       |Demo: Stream-Stream Join (Micro-Batch Stream Processing)
       |https://jaceklaskowski.gitbooks.io/spark-structured-streaming/spark-sql-streaming-join.html
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

  // Using memory data source for full control of the input
  import org.apache.spark.sql.SQLContext
  implicit val sqlCtx: SQLContext = spark.sqlContext
  import spark.implicits._
  import org.apache.spark.sql.execution.streaming.MemoryStream

  val leftEventStream = MemoryStream[Event]
  val rightEventStream = MemoryStream[Event]

  val leftEvents = leftEventStream.toDS.as("left")
  val rightEvents = rightEventStream.toDS.as("right")

  assert(leftEvents.isStreaming && rightEvents.isStreaming, "events must both be streaming Datasets")

  // FIXME Configurable from the command line
  deleteCheckpointLocation()

  println("Case 1: Stream-Stream INNER JOIN")

  // FIXME Configurable from the command line
  import scala.concurrent.duration._
  val watermark = 5.seconds.toString

  import org.apache.spark.sql.functions.expr
  val joinCondition =
    leftEvents("value") === rightEvents("value") &&
    (
      leftEvents("time") >= rightEvents("time") &&
      leftEvents("time") < rightEvents("time") + expr("INTERVAL 1 HOUR")
    )
  val joinedEvents = leftEvents
    .withWatermark("time", watermark)
    .join(rightEvents, Seq.empty, "INNER")
    .where(joinCondition)

  joinedEvents.explain(extended = true)

  val streamingQuery = joinedEvents
    .writeStream
    .format("console")
    .queryName(queryName)
    .option("checkpointLocation", checkpointLocation)
    .outputMode(queryOutputMode)
    .start

  val expectedStatus = "Waiting for data to arrive"
  var currentStatus = streamingQuery.status.message
  do {
    currentStatus = streamingQuery.status.message
  } while (currentStatus == expectedStatus)

  var batchNo: Int = -1

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
         |- number of total state rows: 2 (stateOperators.numRowsTotal)
         |- number of output rows: 0
         |- number of updated state rows: 2 (stateOperators.numRowsUpdated?)
      """.stripMargin)

    val leftBatch = Seq(
      Event(value = 1, batch = batchNo),
      Event(value = 2, batch = batchNo, secs = 5),
      Event(value = 11, batch = batchNo),
      Event(value = 12, batch = batchNo, secs = 10)
    )
    val leftOffset = leftEventStream.addData(leftBatch)
    streamingQuery.processAllAvailable()

    import org.apache.spark.sql.execution.streaming.LongOffset
    leftEventStream.commit(leftOffset.asInstanceOf[LongOffset])

    println(streamingQuery.lastProgress.prettyJson)
  }

  // This should show execution-specific properties, e.g. checkpointLocation, event-time watermark
  streamingQuery.explain()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
         |- number of total state rows: 4 (stateOperators.numRowsTotal)
         |- number of output rows: 1
         |- number of updated state rows: 2 (stateOperators.numRowsUpdated?)
      """.stripMargin)

    val leftBatch = Seq(
      Event(value = 30, batch = batchNo)
    )
    val leftOffset = leftEventStream.addData(leftBatch)

    val rightBatch = Seq(
      Event(value = 12, batch = batchNo, secs = 0),
    )
    val rightOffset = rightEventStream.addData(rightBatch)
    streamingQuery.processAllAvailable()

    import org.apache.spark.sql.execution.streaming.LongOffset
    leftEventStream.commit(leftOffset.asInstanceOf[LongOffset])
    rightEventStream.commit(rightOffset.asInstanceOf[LongOffset])

    println(streamingQuery.lastProgress.prettyJson)
  }

  pause()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |- number of total state rows: 5 (stateOperators.numRowsTotal)
         |- number of output rows: 1
         |- number of updated state rows: 1 (stateOperators.numRowsUpdated?)
      """.stripMargin)

    val leftBatch = Seq(
      // this secs is important
      // the match is on time column with the watermark less
      // (time#127-T5000ms >= time#124)) && (time#127-T5000ms < time#124 + interval 1 hours))
      // See the logs
      Event(value = 30, batch = batchNo, secs = 5)
    )
    val leftOffset = leftEventStream.addData(leftBatch)

    val rightBatch = Seq(
      Event(value = 30, batch = batchNo, secs = 5)
    )
    val rightOffset = rightEventStream.addData(rightBatch)
    streamingQuery.processAllAvailable()

    import org.apache.spark.sql.execution.streaming.LongOffset
    leftEventStream.commit(leftOffset.asInstanceOf[LongOffset])
    rightEventStream.commit(rightOffset.asInstanceOf[LongOffset])

    println(streamingQuery.lastProgress.prettyJson)
  }

  pause()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
         |- number of total state rows: 7 (stateOperators.numRowsTotal)
         |- number of output rows: 0
         |- number of updated state rows: 2 (stateOperators.numRowsUpdated?)
      """.stripMargin)

    val rightBatch = Seq(
      Event(value = 12, batch = batchNo),
      Event(value = 13, batch = batchNo)
    )
    val rightOffset = rightEventStream.addData(rightBatch)
    streamingQuery.processAllAvailable()

    import org.apache.spark.sql.execution.streaming.LongOffset
    rightEventStream.commit(rightOffset.asInstanceOf[LongOffset])

    println(streamingQuery.lastProgress.prettyJson)
  }

  pause()

}
