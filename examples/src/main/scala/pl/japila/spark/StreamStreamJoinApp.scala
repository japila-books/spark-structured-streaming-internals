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

  val leftEventStream = MemoryStream[Event]
  val rightEventStream = MemoryStream[Event]

  val leftEvents = leftEventStream.toDS.as("left")
  val rightEvents = rightEventStream.toDS.as("right")

  assert(leftEvents.isStreaming && rightEvents.isStreaming, "events must both be streaming Datasets")

  // FIXME Configurable from the command line
  deleteCheckpointLocation()

  // Stream-Stream INNER JOIN
  val joinedEvents = leftEvents.join(rightEvents)
    .where(leftEvents("value") === rightEvents("value"))
    .where(leftEvents("value") > 10)
    .where(rightEvents("value") % 2 === 0)

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
      """.stripMargin)
    val batch = Seq(
      Event(1,  value = 11, batch = batchNo),
      Event(15, value = 12, batch = batchNo))
    val leftOffset = leftEventStream.addData(batch)
    val rightOffset = rightEventStream.addData(batch)
    streamingQuery.processAllAvailable()

    import org.apache.spark.sql.execution.streaming.LongOffset
    leftEventStream.commit(leftOffset.asInstanceOf[LongOffset])
    rightEventStream.commit(rightOffset.asInstanceOf[LongOffset])

    println(streamingQuery.lastProgress.prettyJson)
  }

  pause()

  streamingQuery.explain()
}
