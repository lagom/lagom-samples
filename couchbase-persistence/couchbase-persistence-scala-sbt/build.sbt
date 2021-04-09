import com.lightbend.lagom.core.LagomVersion.{ akka => akkaVersion }
organization in ThisBuild := "com.lightbend.lagom.sample.couchbase"
name in ThisBuild := "couchbase-persistence-scala-sbt"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.13"

lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test

lazy val `couchbase-persistence-scala-sbt` = (project in file("."))
  .aggregate(`hello-api`, `hello-impl`)

lazy val `hello-api` = (project in file("hello-api"))
  .settings(libraryDependencies ++= Seq(
    lagomScaladslApi,
  ))

lazy val `hello-impl` = (project in file("hello-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      //#couchbase-begin
      "com.lightbend.akka" %% "lagom-scaladsl-persistence-couchbase" % "1.0",
      //#couchbase-end

      // lagom-scaladsl-persistence-couchbase doesn't depend on the latest versions for Lagom and Akka
      // so explicit dependencies must be added to ensure versions align.
      // Also, akka-persistence-typed is not a transitive dependency in lagom-scaladsl-persistence-couchbase
      // so it must be added explicitly.
      lagomScaladslPersistence, // align versions
      "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion, // align versions and add it in scope

      lagomScaladslTestKit,
      macwire,
      scalaTest,
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`hello-api`)
