organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

// Disable Cassandra and Kafka
lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false

val pac4jVersion = "3.4.0"
val lagomPac4j = "org.pac4j" %% "lagom-pac4j" % "1.0.0"
val pac4jHttp = "org.pac4j" % "pac4j-http" % pac4jVersion
val pac4jJwt = "org.pac4j" % "pac4j-jwt" % pac4jVersion
val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val nimbusJoseJwt = "com.nimbusds" % "nimbus-jose-jwt" % "6.0" % Test

lazy val `auth-jwt-scala-sbt` = (project in file("."))
  .aggregate(`auth-jwt-scala-sbt-api`, `auth-jwt-scala-sbt-impl`)

lazy val `auth-jwt-scala-sbt-api` = (project in file("auth-jwt-scala-sbt-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `auth-jwt-scala-sbt-impl` = (project in file("auth-jwt-scala-sbt-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      pac4jHttp,
      pac4jJwt,
      lagomPac4j,
      lagomScaladslTestKit,
      macwire,
      scalaTest,
      nimbusJoseJwt
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`auth-jwt-scala-sbt-api`)

