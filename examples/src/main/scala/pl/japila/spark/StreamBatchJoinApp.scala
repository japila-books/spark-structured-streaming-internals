package pl.japila.spark

object StreamBatchJoinApp extends SparkStreamsApp {
  import spark.implicits._

  val stream = spark.readStream.format("rate").load

  {
    val batch = spark.read.format("text").load("target/files")
    val joined = stream.join(batch, "value")
  }

  {
    val batch2 = (0 to 10).toDF("value")
    val joined2 = batch2.join(stream, "value")

    joined2.explain
    assert(joined2.isStreaming)
    joined2.writeStream.format("console").option("truncate", false).start
  }

  {
    val batch3 = spark.read.format("text").load("target/files")
    val joined3 = batch3.join(stream, "value")

    joined3.explain
    assert(joined3.isStreaming)
    joined3.writeStream.format("console").option("truncate", false).start
  }

}
