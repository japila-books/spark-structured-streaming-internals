# FlatMapGroupsWithStateStrategy Execution Planning Strategy

`FlatMapGroupsWithStateStrategy` is an execution planning strategy ([Spark SQL]({{ book.spark_sql }}/execution-planning-strategies/SparkStrategy)) that plans streaming queries with [FlatMapGroupsWithState](../logical-operators/FlatMapGroupsWithState.md) logical operators to [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md) physical operator (with undefined `StatefulOperatorStateInfo`, `batchTimestampMs`, and `eventTimeWatermark`).

`FlatMapGroupsWithStateStrategy` is part of `extraPlanningStrategies` of the [SparkPlanner](../IncrementalExecution.md#planner) (of [IncrementalExecution](../IncrementalExecution.md)).

## <span id="spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion"> spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion

`FlatMapGroupsWithStateStrategy` uses [spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion](../configuration-properties.md#spark.sql.streaming.flatMapGroupsWithState.stateFormatVersion) for the state format version of [FlatMapGroupsWithStateExec](../physical-operators/FlatMapGroupsWithStateExec.md#stateFormatVersion).

## Demo

```scala
import org.apache.spark.sql.streaming.GroupState
val stateFunc = (key: Long, values: Iterator[(Timestamp, Long)], state: GroupState[Long]) => {
  Iterator((key, values.size))
}
import java.sql.Timestamp
import org.apache.spark.sql.streaming.{GroupStateTimeout, OutputMode}
val numGroups = spark.
  readStream.
  format("rate").
  load.
  as[(Timestamp, Long)].
  groupByKey { case (time, value) => value % 2 }.
  flatMapGroupsWithState(OutputMode.Update, GroupStateTimeout.NoTimeout)(stateFunc)
```

```text
scala> numGroups.explain(true)
== Parsed Logical Plan ==
'SerializeFromObject [assertnotnull(assertnotnull(input[0, scala.Tuple2, true]))._1 AS _1#267L, assertnotnull(assertnotnull(input[0, scala.Tuple2, true]))._2 AS _2#268]
+- 'FlatMapGroupsWithState <function3>, unresolveddeserializer(upcast(getcolumnbyordinal(0, LongType), LongType, - root class: "scala.Long"), value#262L), unresolveddeserializer(newInstance(class scala.Tuple2), timestamp#253, value#254L), [value#262L], [timestamp#253, value#254L], obj#266: scala.Tuple2, class[value[0]: bigint], Update, false, NoTimeout
   +- AppendColumns <function1>, class scala.Tuple2, [StructField(_1,TimestampType,true), StructField(_2,LongType,false)], newInstance(class scala.Tuple2), [input[0, bigint, false] AS value#262L]
      +- StreamingRelation DataSource(org.apache.spark.sql.SparkSession@38bcac50,rate,List(),None,List(),None,Map(),None), rate, [timestamp#253, value#254L]

...

== Physical Plan ==
*SerializeFromObject [assertnotnull(input[0, scala.Tuple2, true])._1 AS _1#267L, assertnotnull(input[0, scala.Tuple2, true])._2 AS _2#268]
+- FlatMapGroupsWithState <function3>, value#262: bigint, newInstance(class scala.Tuple2), [value#262L], [timestamp#253, value#254L], obj#266: scala.Tuple2, StatefulOperatorStateInfo(<unknown>,84b5dccb-3fa6-4343-a99c-6fa5490c9b33,0,0), class[value[0]: bigint], Update, NoTimeout, 0, 0
   +- *Sort [value#262L ASC NULLS FIRST], false, 0
      +- Exchange hashpartitioning(value#262L, 200)
         +- AppendColumns <function1>, newInstance(class scala.Tuple2), [input[0, bigint, false] AS value#262L]
            +- StreamingRelation rate, [timestamp#253, value#254L]
```
