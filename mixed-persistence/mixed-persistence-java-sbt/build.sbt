organization in ThisBuild := "com.lightbend.lagom.samples"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.8"

lazy val `hello` = (project in file("."))
  .aggregate(`hello-api`, `hello-impl`)

lazy val `hello-api` = (project in file("hello-api"))
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )

val lombok = "org.projectlombok" % "lombok" % "1.18.8"
val h2 = "com.h2database" % "h2" % "1.4.196"
val hibernate = "org.hibernate" % "hibernate-core" % "5.2.12.Final"

lazy val `hello-impl` = (project in file("hello-impl"))
  .enablePlugins(LagomJava)
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslPersistenceJpa,
      lagomJavadslTestKit,
      lombok,
      h2,
      hibernate
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`hello-api`)


def common = Seq(
  javacOptions in Compile := Seq("-g", "-encoding", "UTF-8", "-parameters", "-Xlint:unchecked", "-Xlint:deprecation")
)

lagomKafkaEnabled in ThisBuild := false
