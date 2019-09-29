package demo

import org.apache.spark.sql.SparkSession

object Main extends App {

  val spark = SparkSession
    .builder
    .master("local[*]")
    .getOrCreate

  println(s"This is Spark v${spark.version}")

  val stream = spark.readStream.format("demo").load
  stream.explain

}
