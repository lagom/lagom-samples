organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

lazy val `fileupload` = (project in file("."))
  .aggregate(`fileupload-api`, `fileupload-impl`)

lazy val `fileupload-api` = (project in file("fileupload-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `fileupload-impl` = (project in file("fileupload-impl"))
  .enablePlugins(LagomScala)
  .disablePlugins(PlayLayoutPlugin)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`fileupload-api`)


lagomKafkaEnabled in ThisBuild := false
lagomCassandraEnabled in ThisBuild := false