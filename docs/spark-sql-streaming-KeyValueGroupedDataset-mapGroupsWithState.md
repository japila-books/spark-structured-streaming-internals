== [[mapGroupsWithState]] mapGroupsWithState Operator -- Stateful Streaming Aggregation (with Explicit State Logic)

[source, scala]
----
mapGroupsWithState[S: Encoder, U: Encoder](
  func: (K, Iterator[V], GroupState[S]) => U): Dataset[U] // <1>
mapGroupsWithState[S: Encoder, U: Encoder](
  timeoutConf: GroupStateTimeout)(
  func: (K, Iterator[V], GroupState[S]) => U): Dataset[U]
----
<1> Uses `GroupStateTimeout.NoTimeout` for `timeoutConf`

`mapGroupsWithState` operator...FIXME

!!! note
    `mapGroupsWithState` is a special case of [flatMapGroupsWithState](operators/flatMapGroupsWithState.md) operator with the following:

    * `func` being transformed to return a single-element `Iterator`

    * [Update](OutputMode.md#Update) output mode

    `mapGroupsWithState` also creates a `FlatMapGroupsWithState` with [isMapGroupsWithState](logical-operators/FlatMapGroupsWithState.md#isMapGroupsWithState) internal flag enabled.

```text
// numGroups defined at the beginning
scala> :type numGroups
org.apache.spark.sql.KeyValueGroupedDataset[Long,(java.sql.Timestamp, Long)]

import org.apache.spark.sql.streaming.GroupState
def mappingFunc(key: Long, values: Iterator[(java.sql.Timestamp, Long)], state: GroupState[Long]): Long = {
  println(s">>> key: $key => state: $state")
  val newState = state.getOption.map(_ + values.size).getOrElse(0L)
  state.update(newState)
  key
}

import org.apache.spark.sql.streaming.GroupStateTimeout
val longs = numGroups.mapGroupsWithState(
    timeoutConf = GroupStateTimeout.ProcessingTimeTimeout)(
    func = mappingFunc)

import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import scala.concurrent.duration._
val q = longs.
  writeStream.
  format("console").
  trigger(Trigger.ProcessingTime(10.seconds)).
  outputMode(OutputMode.Update). // <-- required for mapGroupsWithState
  start

// Note GroupState

-------------------------------------------
Batch: 1
-------------------------------------------
>>> key: 0 => state: GroupState(<undefined>)
>>> key: 1 => state: GroupState(<undefined>)
+-----+
|value|
+-----+
|    0|
|    1|
+-----+

-------------------------------------------
Batch: 2
-------------------------------------------
>>> key: 0 => state: GroupState(0)
>>> key: 1 => state: GroupState(0)
+-----+
|value|
+-----+
|    0|
|    1|
+-----+

-------------------------------------------
Batch: 3
-------------------------------------------
>>> key: 0 => state: GroupState(4)
>>> key: 1 => state: GroupState(4)
+-----+
|value|
+-----+
|    0|
|    1|
+-----+

// in the end
spark.streams.active.foreach(_.stop)
```
