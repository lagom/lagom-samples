organization in ThisBuild := "com.lightbend.lagom.samples"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.8"

val h2 = "com.h2database" % "h2" % "1.4.199"
val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.2" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test

lazy val `mixed-persistence-scala-sbt` = (project in file("."))
  .aggregate(`hello-api`, `hello-impl`)

lazy val `hello-api` = (project in file("hello-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `hello-impl` = (project in file("hello-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslPersistenceJdbc,
      lagomScaladslTestKit,
      h2,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`hello-api`)

lagomKafkaEnabled in ThisBuild := false
