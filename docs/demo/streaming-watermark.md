# Demo: Streaming Watermark

!!! note "FIXME"
    The title may be misleading as it's currently more a demo of a streaming query to load CSV files (and no watermark yet).

```text title="/tmp/csv-input/1.csv"
id,time
0,0
1,1
```

```scala
val schema = spark
  .read
  .option("inferSchema", true)
  .option("header", true)
  .csv("/tmp/csv-input")
  .schema
```

```shell
rm -rf /tmp/csv-checkpoint /tmp/csv-output
```

```scala
import org.apache.spark.sql.streaming.Trigger
import scala.concurrent.duration._
spark
  .readStream
  .schema(schema)
  .csv("/tmp/csv-input")
  .select("id", "time")
  .writeStream
  .format("csv")
  .queryName("Demo: Streaming Watermark")
  .trigger(Trigger.ProcessingTime(5.seconds))
  .option("path", "/tmp/csv-output")
  .option("checkpointLocation", "/tmp/csv-checkpoint")
  .start
```

```shell
$ cat > /tmp/csv-input/2.csv << EOF
id,name
2,two
EOF
```
