organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.8"

lazy val `shopping-cart-java` = (project in file("."))
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
      hamcrestLibrary,
      akkaDiscoveryServiceLocator,
      clusterBootstrap,
      clusterHttp,
      akkaDiscoveryKubernetesApi
    ),
    dockerBaseImage := "adoptopenjdk/openjdk8"
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
      lagomJavadslTestKit,
      akkaDiscoveryServiceLocator
    ),
    dockerBaseImage := "adoptopenjdk/openjdk8"
  )
  .dependsOn(`shopping-cart-api`, `inventory-api`)

val lombok = "org.projectlombok" % "lombok" % "1.16.18"
val postgresDriver = "org.postgresql" % "postgresql" % "42.2.5"
val hamcrestLibrary = "org.hamcrest" % "hamcrest-library" % "2.1" % Test
val akkaDiscoveryServiceLocator = "com.lightbend.lagom" %% "lagom-javadsl-akka-discovery-service-locator" % "0.0.12"

val akkaManagementVersion = "1.0.0-RC2"
val clusterBootstrap = "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion
val clusterHttp = "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion
val akkaDiscoveryKubernetesApi = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion

def common = Seq(
  javacOptions in (Compile,compile) ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-parameters", "-Werror")
)

lagomCassandraEnabled in ThisBuild := false
