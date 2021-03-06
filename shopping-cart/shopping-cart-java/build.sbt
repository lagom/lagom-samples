import com.lightbend.lagom.core.LagomVersion

organization in ThisBuild := "com.example"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.13.5"

val lombok                 = "org.projectlombok"               % "lombok"                  % "1.18.18"
val postgresDriver         = "org.postgresql"                  % "postgresql"              % "42.2.18"
val hamcrestLibrary        = "org.hamcrest"                    % "hamcrest-library"        % "2.1" % Test
val hibernateEntityManager = "org.hibernate"                   % "hibernate-entitymanager" % "5.4.2.Final"
val jpaApi                 = "org.hibernate.javax.persistence" % "hibernate-jpa-2.1-api"   % "1.0.0.Final"
val validationApi          = "javax.validation"                % "validation-api"          % "1.1.0.Final"

val akkaPersistenceQuery = "com.typesafe.akka" %% "akka-persistence-query" % LagomVersion.akka
val akkaStreamTestkit    = "com.typesafe.akka" %% "akka-stream-testkit"    % LagomVersion.akka

val akkaDiscoveryKubernetesApi = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api"                % "1.0.10"
val lagomJavadslAkkaDiscovery  = "com.lightbend.lagom"          %% "lagom-javadsl-akka-discovery-service-locator" % LagomVersion.current

val playJavaClusterSharding = "com.typesafe.play" %% "play-java-cluster-sharding" % LagomVersion.play

def dockerSettings = Seq(
  dockerUpdateLatest := true,
  dockerBaseImage := getDockerBaseImage(),
  dockerUsername := sys.props.get("docker.username"),
  dockerRepository := sys.props.get("docker.registry")
)

def getDockerBaseImage(): String = sys.props.get("java.version") match {
  case Some(v) if v.startsWith("11") => "adoptopenjdk/openjdk11"
  case _                             => "adoptopenjdk/openjdk8"
}

// Update the version generated by sbt-dynver to remove any + characters, since these are illegal in docker tags
version in ThisBuild ~= (_.replace('+', '-'))
dynver in ThisBuild ~= (_.replace('+', '-'))

lazy val `shopping-cart-java` = (project in file("."))
  .aggregate(`shopping-cart-api`, `shopping-cart`, `inventory-api`, inventory)

lazy val `shopping-cart-api` = (project in file("shopping-cart-api"))
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )

lazy val `shopping-cart` = (project in file("shopping-cart"))
  .enablePlugins(LagomJava)
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceJdbc,
      lagomJavadslPersistenceJpa,
      lagomJavadslKafkaBroker,
      lagomLogback,
      lagomJavadslTestKit,
      lombok,
      postgresDriver,
      hamcrestLibrary,
      lagomJavadslAkkaDiscovery,
      akkaDiscoveryKubernetesApi,
      akkaPersistenceQuery,
      hibernateEntityManager,
      jpaApi,
      validationApi
    )
  )
  .settings(dockerSettings)
  .settings(lagomForkedTestSettings)
  .dependsOn(`shopping-cart-api`)

lazy val `inventory-api` = (project in file("inventory-api"))
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi
    )
  )

lazy val inventory = (project in file("inventory"))
  .enablePlugins(LagomJava)
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslKafkaClient,
      lagomLogback,
      lagomJavadslTestKit,
      lagomJavadslAkkaDiscovery
    )
  )
  .settings(dockerSettings)
  .dependsOn(`inventory-api`, `shopping-cart-api`)

def common = Seq(
  // We don't care about doc artifacts here.
  sources in (Compile, doc) := Seq.empty,
  publishArtifact in (Compile, packageDoc) := false,
  javacOptions in Compile := Seq("-g", "-encoding", "UTF-8", "-Xlint:unchecked", "-Xlint:deprecation", "-parameters", "-Werror")
)

// The project uses PostgreSQL
lagomCassandraEnabled in ThisBuild := false

// Use Kafka server running in a docker container
lagomKafkaEnabled in ThisBuild := false
lagomKafkaPort in ThisBuild := 9092
