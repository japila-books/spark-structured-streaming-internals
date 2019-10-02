package demo

import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.execution.streaming.Source
import org.apache.spark.sql.sources.{DataSourceRegister, StreamSourceProvider}
import org.apache.spark.sql.types.{LongType, StructField, StructType}

class DemoSourceProvider
    extends StreamSourceProvider
    with DataSourceRegister {

  import DemoSourceProvider._

  override def shortName(): String = SHORT_NAME

  override def sourceSchema(
      sqlContext: SQLContext,
      schema: Option[StructType],
      providerName: String,
      parameters: Map[String, String]): (String, StructType) = {
    println(s">>> DemoSourceProvider.sourceSchema(" +
      s"schema = $schema, " +
      s"providerName = $providerName, " +
      s"parameters = $parameters)")
    val s = schema.getOrElse(DEFAULT_SCHEMA)
    val r = (shortName(), s)
    println(s"<<< <<< DemoSourceProvider.sourceSchema returns $r")
    r
  }

  override def createSource(
    sqlContext: SQLContext,
    metadataPath: String,
    schema: Option[StructType],
    providerName: String,
    parameters: Map[String, String]): Source = new DemoSource(sqlContext)
}

object DemoSourceProvider {
  val SHORT_NAME = "demo"
  val DEFAULT_SCHEMA = StructType(StructField("id", LongType) :: Nil)
}
