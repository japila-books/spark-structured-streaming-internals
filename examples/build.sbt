name := "spark-structured-streaming-examples"

ThisBuild / organization := "pl.japila"
ThisBuild / version      := "0.1"
ThisBuild / scalaVersion := "2.12.8"

val sparkVersion = "2.4.4"
lazy val commonSettings = Seq(
  libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion
)

lazy val p0 = (project in file("."))
  .aggregate(p1)
  .settings(commonSettings)

lazy val p1 = (project in file("streaming-source"))
  .settings(
    commonSettings
  )
