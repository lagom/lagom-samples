organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

lazy val `fileupload` = (project in file("."))
  .aggregate(`fileupload-api`, `fileupload-impl`)

lazy val `fileupload-api` = (project in file("fileupload-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi
    )
  )

lazy val `fileupload-impl` = (project in file("fileupload-impl"))
  .enablePlugins(LagomJava, PlayJava)
  .disablePlugins(PlayLayoutPlugin)
  .settings(
    routesGenerator := InjectedRoutesGenerator
  )
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslTestKit
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`fileupload-api`)


lagomKafkaEnabled in ThisBuild := false
lagomCassandraEnabled in ThisBuild := false