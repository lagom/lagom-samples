organization in ThisBuild := "com.lightbend.lagom.recipes"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

lazy val `cors-java` = (project in file("."))
  .aggregate(`cors-java-api`, `cors-java-impl`)

lazy val `cors-java-api` = (project in file("cors-java-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi
    )
  )

lazy val `cors-java-impl` = (project in file("cors-java-impl"))
  .enablePlugins(LagomJava)
  .settings(
    libraryDependencies ++= Seq(
    )
  )
  .dependsOn(`cors-java-api`)

lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false
