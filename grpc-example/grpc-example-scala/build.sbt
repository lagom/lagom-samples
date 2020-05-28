import play.grpc.gen.scaladsl.{ PlayScalaClientCodeGenerator, PlayScalaServerCodeGenerator }
import lagom.scala.grpc.sample.BuildInfo
organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.10"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test

val playGrpcRuntime = "com.lightbend.play"      %% "play-grpc-runtime"   % BuildInfo.playGrpcVersion
val lagomGrpcTestkit = "com.lightbend.play" %% "lagom-scaladsl-grpc-testkit" % BuildInfo.playGrpcVersion % Test
// TODO remove after upgrade Akka gRPC
val akkaHttp = "com.typesafe.akka" %% "akka-http2-support" % "10.1.12"

val akkaDiscoveryKubernetesApi = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % "1.0.7"

lagomServiceEnableSsl in ThisBuild := true
val `hello-impl-HTTPS-port` = 11000

def dockerSettings = Seq(
  dockerUpdateLatest := true,
  dockerBaseImage := "adoptopenjdk/openjdk8",
  dockerUsername := sys.props.get("docker.username"),
  dockerRepository := sys.props.get("docker.registry")
)

// ALL SETTINGS HERE ARE TEMPORARY WORKAROUNDS FOR KNOWN ISSUES OR WIP
def workaroundSettings: Seq[sbt.Setting[_]] = Seq(
  // Lagom still can't register a service under the gRPC name so we hard-code 
  // the port and use the value to add the entry on the Service Registry
  lagomServiceHttpsPort := `hello-impl-HTTPS-port`
)

lazy val `lagom-scala-grpc-example` = (project in file("."))
  .aggregate(`hello-api`, `hello-impl`, `hello-proxy-api`, `hello-proxy-impl`)

lazy val `hello-api` = (project in file("hello-api"))
  .settings(
    libraryDependencies += lagomScaladslApi
  )

lazy val `hello-impl` = (project in file("hello-impl"))
  .enablePlugins(LagomScala)
  .enablePlugins(AkkaGrpcPlugin) // enables source generation for gRPC
  .enablePlugins(PlayAkkaHttp2Support) // enables serving HTTP/2 and gRPC
  .settings(
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcGeneratedSources :=
      Seq(
        AkkaGrpc.Server,
        AkkaGrpc.Client // the client is only used in tests. See https://github.com/akka/akka-grpc/issues/410
      ),
    akkaGrpcExtraGenerators in Compile += PlayScalaServerCodeGenerator,
  ).settings(
    workaroundSettings:_*
  ).settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      akkaHttp,
      playGrpcRuntime,
      scalaTest,
      lagomGrpcTestkit
    )
  ).settings(lagomForkedTestSettings: _*)
  .settings(dockerSettings)
  .dependsOn(`hello-api`)

lazy val `hello-proxy-api` = (project in file("hello-proxy-api"))
  .settings(
    libraryDependencies +=lagomScaladslApi
  )

lazy val `hello-proxy-impl` = (project in file("hello-proxy-impl"))
  .enablePlugins(LagomScala)
  .enablePlugins(AkkaGrpcPlugin) // enables source generation for gRPC
  .settings(
  akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcExtraGenerators += PlayScalaClientCodeGenerator,
  ).settings(
    libraryDependencies ++= Seq(
      akkaDiscoveryKubernetesApi,
      lagomScaladslTestKit,
      akkaHttp,
      macwire,
      scalaTest
  ),
 
  // workaround for akka discovery method lookup in dev-mode
  lagomDevSettings := Seq("akka.discovery.method" -> "lagom-dev-mode")
)
  .settings(dockerSettings)
  .dependsOn(`hello-proxy-api`, `hello-api`)


// This sample application doesn't need either Kafka or Cassandra so we disable them
// to make the devMode startup faster.
lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false


// This adds an entry on the LagomDevMode Service Registry. With this information on
// the Service Registry a client using Service Discovery to Lookup("helloworld.GreeterService")
// will get "https://localhost:11000" and then be able to send a request.
// See declaration and usages of `hello-impl-HTTPS-port`.
lagomUnmanagedServices in ThisBuild := Map("helloworld.GreeterService" -> s"https://localhost:${`hello-impl-HTTPS-port`}")

//----------------------------------


// Documentation for this project:
//    sbt "project docs" "~ paradox"
//    open docs/target/paradox/site/main/index.html
lazy val docs = (project in file("docs"))
  .enablePlugins(ParadoxPlugin)


//----------------------------------

ThisBuild / scalacOptions ++= List("-encoding", "utf8", "-deprecation", "-feature", "-unchecked")
