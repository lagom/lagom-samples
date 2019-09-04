organization in ThisBuild := "com.lightbend.lagom.sample.couchbase"
name in ThisBuild := "couchbase-persistence-scala-sbt"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.8"

lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8" % Test

lazy val `couchbase-persistence-scala-sbt` = (project in file("."))
  .aggregate(`hello-api`, `hello-impl`)

lazy val `hello-api` = (project in file("hello-api"))
  .settings(libraryDependencies ++= Seq(lagomScaladslApi))

lazy val `hello-impl` = (project in file("hello-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      //#couchbase-begin
      "com.lightbend.akka" %% "lagom-scaladsl-persistence-couchbase" % "1.0",
      //#couchbase-end
      // An explicit dependency to lagom-scaladsl-projection is required to
      // workaround https://github.com/lagom/lagom/issues/2192
      "com.lightbend.lagom" %% "lagom-scaladsl-projection" % "1.6.0-M5",
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`hello-api`)
