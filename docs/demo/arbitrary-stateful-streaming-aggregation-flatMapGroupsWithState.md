# Demo: Arbitrary Stateful Streaming Aggregation with KeyValueGroupedDataset.flatMapGroupsWithState Operator

The following demo shows an example of <<spark-sql-arbitrary-stateful-streaming-aggregation.md#, Arbitrary Stateful Streaming Aggregation>> with <<spark-sql-streaming-Dataset-operators.md#flatMapGroupsWithState, KeyValueGroupedDataset.flatMapGroupsWithState>> operator.

[source, scala]
----
import java.sql.Timestamp
type DeviceId = Long
case class Signal(timestamp: Timestamp, deviceId: DeviceId, value: Long)

// input stream
import org.apache.spark.sql.functions._
val signals = spark
  .readStream
  .format("rate")
  .option("rowsPerSecond", 1)
  .load
  .withColumn("deviceId", rint(rand() * 10) cast "int") // 10 devices randomly assigned to values
  .withColumn("value", $"value" % 10)  // randomize the values (just for fun)
  .as[Signal] // convert to our type (from "unpleasant" Row)

import org.apache.spark.sql.streaming.GroupState
type Key = Int
type Count = Long
type State = Map[Key, Count]
case class EventsCounted(deviceId: DeviceId, count: Long)
def countValuesPerDevice(
    deviceId: Int,
    signals: Iterator[Signal],
    state: GroupState[State]): Iterator[EventsCounted] = {
  val values = signals.toSeq
  println(s"Device: $deviceId")
  println(s"Signals (${values.size}):")
  values.zipWithIndex.foreach { case (v, idx) => println(s"$idx. $v") }
  println(s"State: $state")

  // update the state with the count of elements for the key
  val initialState: State = Map(deviceId -> 0)
  val oldState = state.getOption.getOrElse(initialState)
  // the name to highlight that the state is for the key only
  val newValue = oldState(deviceId) + values.size
  val newState = Map(deviceId -> newValue)
  state.update(newState)

  // you must not return as it's already consumed
  // that leads to a very subtle error where no elements are in an iterator
  // iterators are one-pass data structures
  Iterator(EventsCounted(deviceId, newValue))
}

// stream processing using flatMapGroupsWithState operator
val deviceId: Signal => DeviceId = { case Signal(_, deviceId, _) => deviceId }
val signalsByDevice = signals.groupByKey(deviceId)

import org.apache.spark.sql.streaming.{GroupStateTimeout, OutputMode}
val signalCounter = signalsByDevice.flatMapGroupsWithState(
  outputMode = OutputMode.Append,
  timeoutConf = GroupStateTimeout.NoTimeout)(countValuesPerDevice)

import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import scala.concurrent.duration._
val sq = signalCounter.
  writeStream.
  format("console").
  option("truncate", false).
  trigger(Trigger.ProcessingTime(10.seconds)).
  outputMode(OutputMode.Append).
  start
----
