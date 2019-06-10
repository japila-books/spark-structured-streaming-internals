package pl.japila.spark

/**
  * Input lines (from socket source) are in the format of "second,group"
  */
object GroupbyAppendWatermarkExample extends App {

  val appName = this.getClass.getSimpleName.replace("$", "")
  val queryName = appName
  val rootDir = "target"
  val checkpointLocation = s"$rootDir/$queryName"
  val numPartitions = 1
  val master = "local[*]"
  val warehouseDir = s"$checkpointLocation-warehouse"

  val eventTimeCol = "eventTime"

  import org.apache.spark.sql.SparkSession
  val spark = SparkSession
    .builder
    .master(master)
    .appName(appName)
    .config("spark.sql.shuffle.partitions", numPartitions)
    .config("spark.sql.warehouse.dir", warehouseDir)
    .getOrCreate
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
    .format("socket")
    .option("host", "localhost")
    .option("port", "8888")
    .load

  val output = input.transform(mytransform)

  import concurrent.duration._
  import org.apache.spark.sql.streaming.{OutputMode, Trigger}
  val sq = output
    .writeStream
    .format("console")
    .option("truncate", false)
    .queryName(queryName)
    .option("checkpointLocation", checkpointLocation)
    .trigger(Trigger.ProcessingTime(5.seconds))
    .outputMode(OutputMode.Append)
    .start

  sq.awaitTermination()

}
