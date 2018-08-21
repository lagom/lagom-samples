organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % "2.5.14" % Test

lazy val `file-download` = (project in file("."))
  .aggregate(`file-download-api`, `file-download-impl`)

lazy val `file-download-api` = (project in file("file-download-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      scalaTest,
      akkaTestKit
    )
  )

lazy val `file-download-impl` = (project in file("file-download-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`file-download-api`)


lagomKafkaEnabled in ThisBuild := false
lagomCassandraEnabled in ThisBuild := false

scalacOptions in ThisBuild += "-deprecation"
