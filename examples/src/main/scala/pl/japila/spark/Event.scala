package pl.japila.spark

import java.sql.Timestamp

// Define event "format"
// Event time must be defined on a window or a timestamp
case class Event(time: Timestamp, value: Long, batch: Long)

import scala.concurrent.duration._
object Event {
  def apply(secs: Long, value: Long, batch: Long): Event = {
    Event(new Timestamp(secs.seconds.toMillis), value, batch)
  }

  def apply(value: Long, batch: Long): Event = {
    Event(batch * 10, value, batch)
  }
}
