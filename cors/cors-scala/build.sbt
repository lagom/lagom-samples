organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.4"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"

lazy val `cors-scala` = (project in file("."))
  .aggregate(`cors-scala-api`, `cors-scala-impl`)

lazy val `cors-scala-api` = (project in file("cors-scala-api"))
  .settings(
    libraryDependencies += lagomScaladslApi
  )

lazy val `cors-scala-impl` = (project in file("cors-scala-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      macwire,
      filters
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`cors-scala-api`)


// Disable Cassandra and Kafka un DevMode since we don't need either of them in this demo.
lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false
