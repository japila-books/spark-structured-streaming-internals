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
  val joinedEvents = leftEvents.join(rightEvents, Seq("value"), "inner")

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

  var batchNo: Int = 0

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
         |- 1 row in the output
         |- 3 keys in the state store (for the left and right side)
      """.stripMargin)

    val leftBatch = Seq(
      Event(0,  value = 11, batch = batchNo),
      Event(15, value = 12, batch = batchNo)
    )
    val leftOffset = leftEventStream.addData(leftBatch)

    val rightBatch = Seq(
      Event(0, value = 12, batch = batchNo)
    )
    val rightOffset = rightEventStream.addData(rightBatch)
    streamingQuery.processAllAvailable()

    import org.apache.spark.sql.execution.streaming.LongOffset
    leftEventStream.commit(leftOffset.asInstanceOf[LongOffset])
    rightEventStream.commit(rightOffset.asInstanceOf[LongOffset])

    println(streamingQuery.lastProgress.prettyJson)
  }

  pause()

  // This should show execution-specific properties, e.g. checkpointLocation, event-time watermark
  streamingQuery.explain()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
         |- 2 rows in the output
         |- 5 keys in the state store (old state + the left and right side)
      """.stripMargin)

    val rightBatch = Seq(
      Event(batchNo * 10, value = 11, batch = batchNo),
      Event(batchNo * 10, value = 12, batch = batchNo)
    )
    val rightOffset = rightEventStream.addData(rightBatch)
    streamingQuery.processAllAvailable()

    import org.apache.spark.sql.execution.streaming.LongOffset
    rightEventStream.commit(rightOffset.asInstanceOf[LongOffset])

    println(streamingQuery.lastProgress.prettyJson)
  }

  pause()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
         |- ??? rows in the output
         |- ??? keys in the state store (old state + the left and right side)
      """.stripMargin)

    val rightBatch = Seq(
      Event(batchNo * 10, value = 12, batch = batchNo),
      Event(batchNo * 10, value = 13, batch = batchNo)
    )
    val rightOffset = rightEventStream.addData(rightBatch)
    streamingQuery.processAllAvailable()

    import org.apache.spark.sql.execution.streaming.LongOffset
    rightEventStream.commit(rightOffset.asInstanceOf[LongOffset])

    println(streamingQuery.lastProgress.prettyJson)
  }

  pause()

}
