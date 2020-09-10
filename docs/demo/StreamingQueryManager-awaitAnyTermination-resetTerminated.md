== Demo: Using StreamingQueryManager for Query Termination Management

The demo shows how to use link:spark-sql-streaming-StreamingQueryManager.md[StreamingQueryManager] (and specifically link:spark-sql-streaming-StreamingQueryManager.md#awaitAnyTermination[awaitAnyTermination] and link:spark-sql-streaming-StreamingQueryManager.md#resetTerminated[resetTerminated]) for query termination management.

.demo-StreamingQueryManager.scala
[source, scala]
----
// Save the code as demo-StreamingQueryManager.scala
// Start it using spark-shell
// $ ./bin/spark-shell -i demo-StreamingQueryManager.scala

// Register a StreamingQueryListener to receive notifications about state changes of streaming queries
import org.apache.spark.sql.streaming.StreamingQueryListener
val myQueryListener = new StreamingQueryListener {
  import org.apache.spark.sql.streaming.StreamingQueryListener._
  def onQueryTerminated(event: QueryTerminatedEvent): Unit = {
    println(s"Query ${event.id} terminated")
  }

  def onQueryStarted(event: QueryStartedEvent): Unit = {}
  def onQueryProgress(event: QueryProgressEvent): Unit = {}
}
spark.streams.addListener(myQueryListener)

import org.apache.spark.sql.streaming._
import scala.concurrent.duration._

// Start streaming queries

// Start the first query
val q4s = spark.readStream.
  format("rate").
  load.
  writeStream.
  format("console").
  trigger(Trigger.ProcessingTime(4.seconds)).
  option("truncate", false).
  start

// Start another query that is slightly slower
val q10s = spark.readStream.
  format("rate").
  load.
  writeStream.
  format("console").
  trigger(Trigger.ProcessingTime(10.seconds)).
  option("truncate", false).
  start

// Both queries run concurrently
// You should see different outputs in the console
// q4s prints out 4 rows every batch and twice as often as q10s
// q10s prints out 10 rows every batch

/*
-------------------------------------------
Batch: 7
-------------------------------------------
+-----------------------+-----+
|timestamp              |value|
+-----------------------+-----+
|2017-10-27 13:44:07.462|21   |
|2017-10-27 13:44:08.462|22   |
|2017-10-27 13:44:09.462|23   |
|2017-10-27 13:44:10.462|24   |
+-----------------------+-----+

-------------------------------------------
Batch: 8
-------------------------------------------
+-----------------------+-----+
|timestamp              |value|
+-----------------------+-----+
|2017-10-27 13:44:11.462|25   |
|2017-10-27 13:44:12.462|26   |
|2017-10-27 13:44:13.462|27   |
|2017-10-27 13:44:14.462|28   |
+-----------------------+-----+

-------------------------------------------
Batch: 2
-------------------------------------------
+-----------------------+-----+
|timestamp              |value|
+-----------------------+-----+
|2017-10-27 13:44:09.847|6    |
|2017-10-27 13:44:10.847|7    |
|2017-10-27 13:44:11.847|8    |
|2017-10-27 13:44:12.847|9    |
|2017-10-27 13:44:13.847|10   |
|2017-10-27 13:44:14.847|11   |
|2017-10-27 13:44:15.847|12   |
|2017-10-27 13:44:16.847|13   |
|2017-10-27 13:44:17.847|14   |
|2017-10-27 13:44:18.847|15   |
+-----------------------+-----+
*/

// Stop q4s on a separate thread
// as we're about to block the current thread awaiting query termination
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS
def queryTerminator(query: StreamingQuery) = new Runnable {
  def run = {
    println(s"Stopping streaming query: ${query.id}")
    query.stop
  }
}
import java.util.concurrent.TimeUnit.SECONDS
// Stop the first query after 10 seconds
Executors.newSingleThreadScheduledExecutor.
  scheduleWithFixedDelay(queryTerminator(q4s), 10, 60 * 5, SECONDS)
// Stop the other query after 20 seconds
Executors.newSingleThreadScheduledExecutor.
  scheduleWithFixedDelay(queryTerminator(q10s), 20, 60 * 5, SECONDS)

// Use StreamingQueryManager to wait for any query termination (either q1 or q2)
// the current thread will block indefinitely until either streaming query has finished
spark.streams.awaitAnyTermination

// You are here only after either streaming query has finished
// Executing spark.streams.awaitAnyTermination again would return immediately

// You should have received the QueryTerminatedEvent for the query termination

// reset the last terminated streaming query
spark.streams.resetTerminated

// You know at least one query has terminated

// Wait for the other query to terminate
spark.streams.awaitAnyTermination

assert(spark.streams.active.isEmpty)

println("The demo went all fine. Exiting...")

// leave spark-shell
System.exit(0)
----
