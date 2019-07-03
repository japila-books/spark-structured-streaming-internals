package pl.japila.spark

/**
  * Consumes lines from a socket (in the format of "second,group")
  * groups them by group and prints the aggregations to the console
  *
  * Use `nc -l 8888` to open a socket
  */
object GroupbyAppendWatermarkExample extends SparkStreamsApp {

  val source = "socket"
  val sink = "console"

  val host = "localhost"
  val port = "8888"

  val eventTimeCol = "eventTime"

  import spark.implicits._
  import org.apache.spark.sql.DataFrame
  val mytransform: DataFrame => DataFrame = { df =>
    import org.apache.spark.sql.functions._
    df
      .as[String]
      .map { line =>
        val ts = line.split(",")
        (ts(0), ts(1))
      }
      .toDF(eventTimeCol, "value")
      .withColumn(eventTimeCol, col(eventTimeCol) cast "long" cast "timestamp")
      .withWatermark(eventTimeCol, "0 seconds")
      .groupBy($"value", window(col(eventTimeCol), "5 seconds") as "window")
      .agg(collect_list(col(eventTimeCol) cast "long") as "times")
      .select("value", "times", "window")
  }

  val input = spark
    .readStream
    .format(source)
    .option("host", host)
    .option("port", port)
    .load

  val output = input.transform(mytransform)

  import concurrent.duration._
  import org.apache.spark.sql.streaming.{OutputMode, Trigger}
  val sq = output
    .writeStream
    .format(sink)
    .option("truncate", false)
    .queryName(queryName)
    .option("checkpointLocation", checkpointLocation)
    .trigger(Trigger.ProcessingTime(5.seconds))
    .outputMode(OutputMode.Append)
    .start

  sq.awaitTermination()

}
