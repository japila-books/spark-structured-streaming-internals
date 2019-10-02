package demo

import java.util.concurrent.TimeUnit

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger

object Main extends App {

  val spark = SparkSession
    .builder
    .master("local[*]")
    .appName("Demo Source (DSv1 / Micro-Batch)")
    .getOrCreate

  println(s"This is Spark v${spark.version}")

  val data = spark
    .readStream
    .format("demo") // <-- DataSourceRegister + META-INF/services
    .load

  data.explain

  val stream = data
    .writeStream
    .queryName("demo")
    .format("console")
    .trigger(Trigger.ProcessingTime(5, TimeUnit.SECONDS))
    .start
  stream.awaitTermination()
}
