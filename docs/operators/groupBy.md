# groupBy Operator &mdash; Streaming Aggregation

```scala
groupBy(
  cols: Column*): RelationalGroupedDataset
groupBy(
  col1: String,
  cols: String*): RelationalGroupedDataset
```

`groupBy` operator aggregates rows by zero, one or more columns.

## Demo

```text
val fromTopic1 = spark.
  readStream.
  format("kafka").
  option("subscribe", "topic1").
  option("kafka.bootstrap.servers", "localhost:9092").
  load

// extract event time et al
// time,key,value
/*
2017-08-23T00:00:00.002Z,1,now
2017-08-23T00:05:00.002Z,1,5 mins later
2017-08-23T00:09:00.002Z,1,9 mins later
2017-08-23T00:11:00.002Z,1,11 mins later
2017-08-23T01:00:00.002Z,1,1 hour later
// late event = watermark should be (1 hour - 10 minutes) already
2017-08-23T00:49:59.002Z,1,==> SHOULD NOT BE INCLUDED in aggregation as too late <==

CAUTION: FIXME SHOULD NOT BE INCLUDED is included contrary to my understanding?!
*/
val timedValues = fromTopic1.
  select('value cast "string").
  withColumn("tokens", split('value, ",")).
  withColumn("time", to_timestamp('tokens(0))).
  withColumn("key", 'tokens(1) cast "int").
  withColumn("value", 'tokens(2)).
  select("time", "key", "value")

// aggregation with watermark
val counts = timedValues.
  withWatermark("time", "10 minutes").
  groupBy("key").
  agg(collect_list('value) as "values", collect_list('time) as "times")

// Note that StatefulOperatorStateInfo is mostly generic
// since no batch-specific values are currently available
// only after the first streaming batch
scala> counts.explain
== Physical Plan ==
ObjectHashAggregate(keys=[key#27], functions=[collect_list(value#33, 0, 0), collect_list(time#22-T600000ms, 0, 0)])
+- Exchange hashpartitioning(key#27, 200)
   +- StateStoreSave [key#27], StatefulOperatorStateInfo(<unknown>,25149816-1f14-4901-af13-896286a26d42,0,0), Append, 0
      +- ObjectHashAggregate(keys=[key#27], functions=[merge_collect_list(value#33, 0, 0), merge_collect_list(time#22-T600000ms, 0, 0)])
         +- Exchange hashpartitioning(key#27, 200)
            +- StateStoreRestore [key#27], StatefulOperatorStateInfo(<unknown>,25149816-1f14-4901-af13-896286a26d42,0,0)
               +- ObjectHashAggregate(keys=[key#27], functions=[merge_collect_list(value#33, 0, 0), merge_collect_list(time#22-T600000ms, 0, 0)])
                  +- Exchange hashpartitioning(key#27, 200)
                     +- ObjectHashAggregate(keys=[key#27], functions=[partial_collect_list(value#33, 0, 0), partial_collect_list(time#22-T600000ms, 0, 0)])
                        +- EventTimeWatermark time#22: timestamp, interval 10 minutes
                           +- *Project [cast(split(cast(value#1 as string), ,)[0] as timestamp) AS time#22, cast(split(cast(value#1 as string), ,)[1] as int) AS key#27, split(cast(value#1 as string), ,)[2] AS value#33]
                              +- StreamingRelation kafka, [key#0, value#1, topic#2, partition#3, offset#4L, timestamp#5, timestampType#6]

import org.apache.spark.sql.streaming._
import scala.concurrent.duration._
val sq = counts.writeStream.
  format("console").
  option("truncate", false).
  trigger(Trigger.ProcessingTime(30.seconds)).
  outputMode(OutputMode.Update).  // <-- only Update or Complete acceptable because of groupBy aggregation
  start

// After StreamingQuery was started,
// the physical plan is complete (with batch-specific values)
scala> sq.explain
== Physical Plan ==
ObjectHashAggregate(keys=[key#27], functions=[collect_list(value#33, 0, 0), collect_list(time#22-T600000ms, 0, 0)])
+- Exchange hashpartitioning(key#27, 200)
   +- StateStoreSave [key#27], StatefulOperatorStateInfo(file:/private/var/folders/0w/kb0d3rqn4zb9fcc91pxhgn8w0000gn/T/temporary-635d6519-b6ca-4686-9b6b-5db0e83cfd51/state,855cec1c-25dc-4a86-ae54-c6cdd4ed02ec,0,0), Update, 0
      +- ObjectHashAggregate(keys=[key#27], functions=[merge_collect_list(value#33, 0, 0), merge_collect_list(time#22-T600000ms, 0, 0)])
         +- Exchange hashpartitioning(key#27, 200)
            +- StateStoreRestore [key#27], StatefulOperatorStateInfo(file:/private/var/folders/0w/kb0d3rqn4zb9fcc91pxhgn8w0000gn/T/temporary-635d6519-b6ca-4686-9b6b-5db0e83cfd51/state,855cec1c-25dc-4a86-ae54-c6cdd4ed02ec,0,0)
               +- ObjectHashAggregate(keys=[key#27], functions=[merge_collect_list(value#33, 0, 0), merge_collect_list(time#22-T600000ms, 0, 0)])
                  +- Exchange hashpartitioning(key#27, 200)
                     +- ObjectHashAggregate(keys=[key#27], functions=[partial_collect_list(value#33, 0, 0), partial_collect_list(time#22-T600000ms, 0, 0)])
                        +- EventTimeWatermark time#22: timestamp, interval 10 minutes
                           +- *Project [cast(split(cast(value#76 as string), ,)[0] as timestamp) AS time#22, cast(split(cast(value#76 as string), ,)[1] as int) AS key#27, split(cast(value#76 as string), ,)[2] AS value#33]
                              +- Scan ExistingRDD[key#75,value#76,topic#77,partition#78,offset#79L,timestamp#80,timestampType#81]
```
