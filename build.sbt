organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.8"

val postgresDriver = "org.postgresql" % "postgresql" % "42.2.5"
val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

lazy val `shopping-cart` = (project in file("."))
  .aggregate(`shopping-cart-api`, `shopping-cart-impl`, `inventory-api`, `inventory-impl`)

lazy val `shopping-cart-api` = (project in file("shopping-cart-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `shopping-cart-impl` = (project in file("shopping-cart-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceJdbc,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest,
      postgresDriver
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`shopping-cart-api`)

lazy val `inventory-api` = (project in file("inventory-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `inventory-impl` = (project in file("inventory-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslKafkaClient,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .dependsOn(`inventory-api`, `shopping-cart-api`)

lagomCassandraEnabled in ThisBuild := false
