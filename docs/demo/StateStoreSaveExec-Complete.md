== Demo: StateStoreSaveExec with Complete Output Mode

The following example code shows the behaviour of link:spark-sql-streaming-StateStoreSaveExec.md#doExecute-Complete[StateStoreSaveExec in Complete output mode].

[source, scala]
----
// START: Only for easier debugging
// The state is then only for one partition
// which should make monitoring it easier
import org.apache.spark.sql.internal.SQLConf.SHUFFLE_PARTITIONS
spark.sessionState.conf.setConf(SHUFFLE_PARTITIONS, 1)
scala> spark.sessionState.conf.numShufflePartitions
res1: Int = 1
// END: Only for easier debugging

// Read datasets from a Kafka topic
// ./bin/spark-shell --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0-SNAPSHOT
// Streaming aggregation using groupBy operator is required to have StateStoreSaveExec operator
val valuesPerGroup = spark.
  readStream.
  format("kafka").
  option("subscribe", "topic1").
  option("kafka.bootstrap.servers", "localhost:9092").
  load.
  withColumn("tokens", split('value, ",")).
  withColumn("group", 'tokens(0)).
  withColumn("value", 'tokens(1) cast "int").
  select("group", "value").
  groupBy($"group").
  agg(collect_list("value") as "values").
  orderBy($"group".asc)

// valuesPerGroup is a streaming Dataset with just one source
// so it knows nothing about output mode or watermark yet
// That's why StatefulOperatorStateInfo is generic
// and no batch-specific values are printed out
// That will be available after the first streaming batch
// Use sq.explain to know the runtime-specific values
scala> valuesPerGroup.explain
== Physical Plan ==
*Sort [group#25 ASC NULLS FIRST], true, 0
+- Exchange rangepartitioning(group#25 ASC NULLS FIRST, 1)
   +- ObjectHashAggregate(keys=[group#25], functions=[collect_list(value#36, 0, 0)])
      +- Exchange hashpartitioning(group#25, 1)
         +- StateStoreSave [group#25], StatefulOperatorStateInfo(<unknown>,899f0fd1-b202-45cd-9ebd-09101ca90fa8,0,0), Append, 0
            +- ObjectHashAggregate(keys=[group#25], functions=[merge_collect_list(value#36, 0, 0)])
               +- Exchange hashpartitioning(group#25, 1)
                  +- StateStoreRestore [group#25], StatefulOperatorStateInfo(<unknown>,899f0fd1-b202-45cd-9ebd-09101ca90fa8,0,0)
                     +- ObjectHashAggregate(keys=[group#25], functions=[merge_collect_list(value#36, 0, 0)])
                        +- Exchange hashpartitioning(group#25, 1)
                           +- ObjectHashAggregate(keys=[group#25], functions=[partial_collect_list(value#36, 0, 0)])
                              +- *Project [split(cast(value#1 as string), ,)[0] AS group#25, cast(split(cast(value#1 as string), ,)[1] as int) AS value#36]
                                 +- StreamingRelation kafka, [key#0, value#1, topic#2, partition#3, offset#4L, timestamp#5, timestampType#6]

// Start the query and hence StateStoreSaveExec
// Use Complete output mode
import scala.concurrent.duration._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
val sq = valuesPerGroup.
  writeStream.
  format("console").
  option("truncate", false).
  trigger(Trigger.ProcessingTime(10.seconds)).
  outputMode(OutputMode.Complete).
  start

-------------------------------------------
Batch: 0
-------------------------------------------
+-----+------+
|group|values|
+-----+------+
+-----+------+

// there's only 1 stateful operator and hence 0 for the index in stateOperators
scala> println(sq.lastProgress.stateOperators(0).prettyJson)
{
  "numRowsTotal" : 0,
  "numRowsUpdated" : 0,
  "memoryUsedBytes" : 60
}

// publish 1 new key-value pair in a single streaming batch
// 0,1

-------------------------------------------
Batch: 1
-------------------------------------------
+-----+------+
|group|values|
+-----+------+
|0    |[1]   |
+-----+------+

// it's Complete output mode so numRowsTotal is the number of keys in the state store
// no keys were available earlier (it's just started!) and so numRowsUpdated is 0
scala> println(sq.lastProgress.stateOperators(0).prettyJson)
{
  "numRowsTotal" : 1,
  "numRowsUpdated" : 0,
  "memoryUsedBytes" : 324
}

// publish new key and old key in a single streaming batch
// new keys
// 1,1
// updates to already-stored keys
// 0,2

-------------------------------------------
Batch: 2
-------------------------------------------
+-----+------+
|group|values|
+-----+------+
|0    |[2, 1]|
|1    |[1]   |
+-----+------+

// it's Complete output mode so numRowsTotal is the number of keys in the state store
// no keys were available earlier and so numRowsUpdated is...0?!
// Think it's a BUG as it should've been 1 (for the row 0,2)
// 8/30 Sent out a question to the Spark user mailing list
scala> println(sq.lastProgress.stateOperators(0).prettyJson)
{
  "numRowsTotal" : 2,
  "numRowsUpdated" : 0,
  "memoryUsedBytes" : 572
}

// In the end...
sq.stop
----
