package pl.japila.spark

import java.sql.Timestamp

case class Session(
  userId: Long,
  beginTimestamp: Long,
  var clicks: Long = 0,
  var endTimestamp: Long = 0) {
  def duration: Long = endTimestamp - beginTimestamp
  def recordClick(time: Timestamp): Unit = {
    clicks = clicks + 1
    val ms = time.getTime
    if (ms > endTimestamp) {
      endTimestamp = ms
    }
  }
  def expire(time: Long): Unit = {
    endTimestamp = time
  }
}
object Session {
  type UserId = Long
  import org.apache.spark.sql.streaming.GroupState
  def countClicksPerSession(
    userId: UserId,
    values: Iterator[(Timestamp, UserId)],
    state: GroupState[Session]): Iterator[Session] = {
    val s = state.getOption.getOrElse("<undefined>")
    val currentProcessingTimeMs = state.getCurrentProcessingTimeMs
    val currentWatermarkMs = state.getCurrentWatermarkMs
    val hasTimedOut = state.hasTimedOut
    println(
      s"""
         |... countClicksPerSession(userId=$userId, state=$s)
         |... ... Batch Processing Time: $currentProcessingTimeMs ms
         |... ... Event-Time Watermark:  $currentWatermarkMs ms
         |... ... hasTimedOut:           $hasTimedOut
       """.stripMargin)
    val result = if (hasTimedOut) {
      val userSession = state.get
      // FIXME What's the expiration time?
      userSession.expire(currentWatermarkMs)
      println(
        s"""
           |... countClicksPerSession: State expired for userId=$userId
           |... ... userSession: $userSession
           |... ... Goes to the output
           |... ... Removing state (to keep memory usage low)
         """.stripMargin)
      state.remove()
      Iterator(userSession)
    } else {
      val session = if (state.exists) {
        val session = state.get
        for {
          (time, _) <- values
        } session.recordClick(time)
        println(
          s"""
             |... countClicksPerSession: State exists for userId=$userId
             |... ... userSession: $session
           """.stripMargin)
        session
      } else {
        // FIXME How to record events for a new session?
        // Avoid two passes over values
        // Iterator is a one-off data structure
        // Once an element is accessed, it is gone forever
        val vs = values.toSeq
        val beginTimestamp = vs.maxBy { case (_, time) => time }._1
        val session = Session(userId, beginTimestamp.getTime)
        for {
          (time, _) <- vs
        } session.recordClick(time)
        println(
          s"""
             |... countClicksPerSession: State does not exist for userId=$userId
             |... ... userSession: $session
         """.stripMargin)
        session
      }
      state.update(session)

      // timeout makes a session expired when no data received for stateDurationMs
      // FIXME Decouple it from FlatMapGroupsWithStateApp
      val stateDurationMs = FlatMapGroupsWithStateApp.stateDurationMs
      val timeoutMs = currentWatermarkMs + stateDurationMs
      println(
        s"""
           |... ... Setting state timeout to $timeoutMs ms
           |... ... $stateDurationMs ms later after event-time watermark: $currentWatermarkMs ms
         """.stripMargin)
      state.setTimeoutTimestamp(timeoutMs)

      Iterator.empty
    }
    println("")
    result
  }
}
