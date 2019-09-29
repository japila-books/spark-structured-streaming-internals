package demo

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.execution.streaming.{Offset, Source}
import org.apache.spark.sql.types.StructType

class DemoSource extends Source {
  override def schema: StructType = ???

  override def getOffset: Option[Offset] = ???

  override def getBatch(start: Option[Offset], end: Offset): DataFrame = ???

  override def stop(): Unit = ???
}
