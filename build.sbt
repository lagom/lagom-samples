organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

lazy val `shopping-cart` = (project in file("."))
  .aggregate(`shopping-cart-api`, `shopping-cart-impl`, `inventory-api`, `inventory-impl`)

lazy val `shopping-cart-api` = (project in file("shopping-cart-api"))
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )

lazy val `shopping-cart-impl` = (project in file("shopping-cart-impl"))
  .enablePlugins(LagomJava)
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceJdbc,
      lagomJavadslKafkaBroker,
      lagomLogback,
      lagomJavadslTestKit,
      lombok,
      postgresDriver,
      hamcrestLibrary
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`shopping-cart-api`)

lazy val `inventory-api` = (project in file("inventory-api"))
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi
    )
  )

lazy val `inventory-impl` = (project in file("inventory-impl"))
  .enablePlugins(LagomJava)
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslKafkaClient,
      lagomLogback,
      lagomJavadslTestKit
    )
  )
  .dependsOn(`shopping-cart-api`, `inventory-api`)

val lombok = "org.projectlombok" % "lombok" % "1.16.18"
val postgresDriver = "org.postgresql" % "postgresql" % "42.2.5"
val hamcrestLibrary = "org.hamcrest" % "hamcrest-library" % "2.1" % Test

def common = Seq(
  javacOptions in compile += "-parameters"
)

lagomCassandraEnabled in ThisBuild := false
