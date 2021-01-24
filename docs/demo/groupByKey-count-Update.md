---
hide:
  - navigation
---

# Demo: groupByKey Streaming Aggregation in Update Mode

The example shows [Dataset.groupByKey](../operators/groupByKey.md) streaming operator to count rows in [Update](../OutputMode.md#Update) output mode.

In other words, it is an example of using `Dataset.groupByKey` with `count` aggregation function to count customer orders (`T`) per zip code (`K`).

```text
package pl.japila.spark.examples

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.{OutputMode, Trigger}

object GroupByKeyStreamingApp extends App {

  val inputTopic = "GroupByKeyApp-input"
  val appName = this.getClass.getSimpleName.replace("$", "")

  val spark = SparkSession.builder
    .master("local[*]")
    .appName(appName)
    .getOrCreate
  import spark.implicits._

  case class Order(id: Long, zipCode: String)

  // Input (source node)
  val orders = spark
    .readStream
    .format("kafka")
    .option("startingOffsets", "latest")
    .option("subscribe", inputTopic)
    .option("kafka.bootstrap.servers", ":9092")
    .load
    .select($"offset" as "id", $"value" as "zipCode") // FIXME Use csv, json, avro
    .as[Order]

  // Processing logic
  // groupByKey + count
  val byZipCode = (o: Order) => o.zipCode
  val ordersByZipCode = orders.groupByKey(byZipCode)

  import org.apache.spark.sql.functions.count
  val typedCountCol = (count("zipCode") as "count").as[String]
  val counts = ordersByZipCode
    .agg(typedCountCol)
    .select($"value" as "zip_code", $"count")

  // Output (sink node)
  import scala.concurrent.duration._
  counts
    .writeStream
    .format("console")
    .outputMode(OutputMode.Update)  // FIXME Use Complete
    .queryName(appName)
    .trigger(Trigger.ProcessingTime(5.seconds))
    .start
    .awaitTermination()
}
```

## Credits

* The example with customer orders and postal codes is borrowed from Apache Beam's [Using GroupByKey](https://beam.apache.org/documentation/programming-guide/#transforms-gbk) Programming Guide.
