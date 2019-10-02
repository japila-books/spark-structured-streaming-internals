package demo

import java.util.concurrent.TimeUnit

import org.apache.spark.TaskContext
import org.apache.spark.sql.execution.datasources.DataSource
import org.apache.spark.sql.execution.streaming.{LongOffset, Offset, Source, StreamingRelation}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SQLContext, SparkSession, UsePrivateSqlHack}

import scala.util.Random

/**
 * Accept the SQLContext so getBatch can create new DataFrames
 */
class DemoSource(sqlContext: SQLContext) extends Source {

  /**
   * Used in getOffset to mimic pulling records from an external data storage
   */
  var _offset: Option[Long] = None

  // Used in getOffset execution phase
  // Could be None at the start of a streaming query
  // For all other micro-batches should really be non-None
  // IF None THEN
  //  no new offsets
  //  Every trigger gets no new offsets
  //  Observe MicroBatchExecution logs:
  //    14:32:23 DEBUG MicroBatchExecution: Starting Trigger Calculation
  //    >>> DemoSource.getOffset
  //    14:32:23 DEBUG MicroBatchExecution: getOffset took 0 ms
  //    14:32:23 TRACE MicroBatchExecution: noDataBatchesEnabled = true, lastExecutionRequiresAnotherBatch = false, isNewDataAvailable = false, shouldConstructNextBatch = false
  //    14:32:23 DEBUG MicroBatchExecution: triggerExecution took 0 ms
  //    14:32:23 DEBUG MicroBatchExecution: Execution stats: ExecutionStats(Map(),List(),Map())
  // Some = this is the latest offset available
  // IF Some has advanced since the last time asked THEN
  //  new data is available for processing
  override def getOffset: Option[Offset] = {
    println(">>> DemoSource.getOffset")
    println(">>> >>> getting offsets may take a while (e.g. a remote call)")
    println(">>> >>> Impacts getOffset and triggerExecution execution phases (see the logs of MicroBatchExecution)")
    val timeout = new Random().nextInt(5) + 1
    println(s">>> >>> sleeping for $timeout secs for the demo")
    println(s">>> >>> Execution phases get longer (at least ${timeout}000 ms)")
    TimeUnit.SECONDS.sleep(timeout)
    val offset = _offset.map(LongOffset.apply)
    println(s">>> >>> DemoSource.getOffset returns $offset")
    advanceOffset(timeout)
    offset
  }

  /**
   * Once getOffset returns non-None
   * Executed in getBatch execution phase
   *
   * @return streaming DataFrame (isStreaming must be `true`)
   *         and hence all the fun with BaseRelation (Range or a custom one)
   */
  override def getBatch(start: Option[Offset], end: Offset): DataFrame = {
    println(s">>> DemoSource.getBatch($start, $end)")
    val from = start.map { case o: LongOffset => o.offset }.getOrElse(0L)
    val to = end.asInstanceOf[LongOffset].offset

    // A Spark job for demonstration purposes
    // MicroBatchExecution engine sets StreamExecution.getBatchDescriptionString
    // So any Spark job from any source gets the same description, e.g in web UI
    val sc = sqlContext.sparkContext
    val numPartitions = (to - from).toInt
    val rdd = sc.textFile("build.sbt", numPartitions)
    val f: (TaskContext, Iterator[String]) => Int = { (ctx, ss) =>
      val r = ss.size
      val pid = ctx.partitionId()
      println(s">>> >>> >>> (SparkContext.runJob) f($pid) => $r")
      r
    }
    val rs = sc.runJob(rdd, f)
    rs.zipWithIndex.foreach { case (r, idx) =>
        println(s">>> >>> Partition $idx => result: $r")
    }

    // Version 1
    // Use the following to show the different interfaces to implement
    // For a custom BaseRelation that you'd then instantiate directly (not rely on this resolution)
//    val relation =
//      DataSource(
//        sparkSession,
//        className = this.getClass.getName).resolveRelation(checkFilesExist = false)
//    val logicalPlan = LogicalRelation(relation, isStreaming = true)

    // Version 2
    // FIXME Implement a custom BaseRelation?
    import org.apache.spark.sql.catalyst.plans.logical.Range
    val logicalPlan = Range(start = from, end = to, step = 1, numSlices = None, isStreaming = true)
    UsePrivateSqlHack.ofRows(sqlContext.sparkSession, logicalPlan)
  }

  override def stop(): Unit = {
    println(">>> DemoSource.stop")
  }

  // Used in logs
  // log4j.logger.org.apache.spark.sql.execution.streaming.MicroBatchExecution=ALL
  override def toString: String = {
    // toString will be used to report stream progress even if there is no progress
    // Use the following trick to know the exact call site
    // println(">>> DemoSource.toString")
    // new Throwable().printStackTrace()
    s"DemoSource.toString(${super.toString})"
  }

  /**
   * It seems of no use (and only used in tests)
   * Focus on [[DemoSourceProvider.sourceSchema]] instead
   */
  override def schema: StructType = {
    println(">>> DemoSource.schema")
    println(">>> >>> simply throwing an exception (= it is of no use)")
    ???
  }

  private def advanceOffset(progress: Int): Unit = {
    _offset.map { off =>
      val newOff = off + progress
      println(s">>> >>> Advancing offset from $off to $newOff")
      _offset = Some(newOff)
    }.getOrElse {
      println(s">>> >>> Advancing offset from None to $progress")
      _offset = Some(progress)
    }
  }
}
