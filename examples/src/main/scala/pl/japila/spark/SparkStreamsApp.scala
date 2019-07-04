package pl.japila.spark

/**
 * Base of Spark Structured Streaming (aka Spark Streams) applications
 */
trait SparkStreamsApp extends App {
  val appName = this.getClass.getSimpleName.replace("$", "")
  val queryName = appName
  val rootDir = "target"
  val checkpointLocation = s"$rootDir/checkpoint-$queryName"
  val numPartitions = 1
  val master = "local[*]"
  val warehouseDir = s"$rootDir/$queryName-warehouse"

  import org.apache.spark.sql.SparkSession
  val spark = SparkSession
    .builder
    .master(master)
    .appName(appName)
    .config("spark.sql.shuffle.partitions", numPartitions)
    .config("spark.sql.warehouse.dir", warehouseDir)
    .getOrCreate

  /**
   * Deletes the checkpoint location from previous executions
   */
  def deleteCheckpointLocation(): Unit = {
    println(s">>> Deleting checkpoint location: $checkpointLocation")
    import java.nio.file.{Files, FileSystems}
    import java.util.Comparator
    import scala.collection.JavaConverters._
    val path = FileSystems.getDefault.getPath(checkpointLocation)
    if (Files.exists(path)) {
      Files.walk(path)
        .sorted(Comparator.reverseOrder())
        .iterator
        .asScala
        .foreach(p => p.toFile.delete)
    }
  }

  def pause() = {
    println("Pause processing")
    println("It is so you have time exploring logs and web UI @ http://localhost:4040 ")
    println("Press ENTER to continue...")
    System.in.read()
  }
}
