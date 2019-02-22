organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

// Disable Cassandra and Kafka
lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false

lazy val `auth-jwt-java-sbt` = (project in file("."))
  .aggregate(`auth-jwt-java-sbt-api`, `auth-jwt-java-sbt-impl`)

lazy val `auth-jwt-java-sbt-api` = (project in file("auth-jwt-java-sbt-api"))
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
    )
  )

lazy val `auth-jwt-java-sbt-impl` = (project in file("auth-jwt-java-sbt-impl"))
  .enablePlugins(LagomJava)
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomPac4j,
      pac4jHttp,
      pac4jJwt,
      lagomLogback,
      lagomJavadslTestKit,
      assertj,
      nimbusJoseJwt
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`auth-jwt-java-sbt-api`)

val pac4jVersion = "3.4.0"
val lagomPac4j = "org.pac4j" %% "lagom-pac4j" % "1.0.0"
val pac4jHttp = "org.pac4j" % "pac4j-http" % pac4jVersion
val pac4jJwt = "org.pac4j" % "pac4j-jwt" % pac4jVersion
val assertj = "org.assertj" % "assertj-core" % "3.11.0" % Test
val nimbusJoseJwt = "com.nimbusds" % "nimbus-jose-jwt" % "6.0" % Test

def common = Seq(
  javacOptions in compile += "-parameters"
)
