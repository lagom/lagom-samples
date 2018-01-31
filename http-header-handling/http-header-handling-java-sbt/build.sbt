organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

lazy val `http-header-handling` = (project in file("."))
  .aggregate(`hello-api`, `hello-impl`)

lazy val `hello-api` = (project in file("hello-api"))
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )

lazy val `hello-impl` = (project in file("hello-impl"))
  .enablePlugins(LagomJava)
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslTestKit,
      lombok
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`hello-api`)

val lombok = "org.projectlombok" % "lombok" % "1.16.10"

def common = Seq(
  javacOptions in compile += "-parameters"
)

lagomKafkaEnabled in ThisBuild := false
lagomCassandraEnabled in ThisBuild := false