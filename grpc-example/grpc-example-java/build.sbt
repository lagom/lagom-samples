import play.grpc.gen.javadsl.{ PlayJavaClientCodeGenerator, PlayJavaServerCodeGenerator }
import lagom.java.grpc.sample.BuildInfo
import sbt.Def
import sbt.Keys.dependencyOverrides

organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Java version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.13"

val `hello-impl-HTTP-port` = 11000
val playGrpcRuntime = "com.lightbend.play"  %% "play-grpc-runtime"          % BuildInfo.playGrpcVersion
val lagomGrpcTestkit = "com.lightbend.play" %% "lagom-javadsl-grpc-testkit" % BuildInfo.playGrpcVersion % Test

val akkaVersion = "2.6.14"
val akkaHttpVersion = "10.2.4"
val playVersion = "2.8.8"

val akkaOverrides = Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
  "com.typesafe.akka" %% "akka-protobuf-v3" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-coordination" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-pki" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
)

val akkaHttpOverrides = Seq(
  "akka-http2-support",
  "akka-http-core",
  "akka-http",
  "akka-parsing",
  "akka-http-spray-json"
).map(
artifactId => "com.typesafe.akka" %% artifactId % akkaHttpVersion
)

val playOverrides = Seq(
 "play",
 "play-akka-http-server",
 "play-server",
 "play-ws",
 "play-ahc-ws",
)
  .map(
    artifactId => "com.typesafe.play" %% artifactId % playVersion
  )

lazy val `lagom-java-grpc-example` = (project in file("."))
  .aggregate(`hello-api`, `hello-impl`, `hello-proxy-api`, `hello-proxy-impl`)

lazy val `hello-api` = (project in file("hello-api"))
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi
    )
  )

lazy val `hello-impl` = (project in file("hello-impl"))
  .enablePlugins(LagomJava)
  .enablePlugins(AkkaGrpcPlugin) // enables source generation for gRPC
  .enablePlugins(PlayAkkaHttp2Support) // enables serving HTTP/2 and gRPC
  .settings(common)
  .settings(
  akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Java),
  akkaGrpcGeneratedSources :=
    Seq(
      AkkaGrpc.Server,
      AkkaGrpc.Client // the client is only used in tests. See https://github.com/akka/akka-grpc/issues/410
    ),
  akkaGrpcExtraGenerators in Compile += PlayJavaServerCodeGenerator,

  // WORKAROUND: Lagom still can't register a service under the gRPC name so we hard-code
  // the port and the use the value to add the entry on the Service Registry
  lagomServiceHttpPort := `hello-impl-HTTP-port`,

  libraryDependencies ++= Seq(
    lagomJavadslTestKit,
    lagomLogback,
    playGrpcRuntime,
    lagomGrpcTestkit
  ) ++ akkaOverrides ++ akkaHttpOverrides ++ playOverrides
).settings(lagomForkedTestSettings: _*)
  .dependsOn(`hello-api`)

lazy val `hello-proxy-api` = (project in file("hello-proxy-api"))
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi
    )
  )

lazy val `hello-proxy-impl` = (project in file("hello-proxy-impl"))
  .enablePlugins(LagomJava)
  .enablePlugins(AkkaGrpcPlugin) // enables source generation for gRPC
  .settings(common)
  .settings(
  akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Java),
  akkaGrpcExtraGenerators += PlayJavaClientCodeGenerator,
  ).settings(
    libraryDependencies ++= Seq(
      lagomJavadslTestKit,
      lagomLogback,
      playGrpcRuntime
    )++ akkaOverrides ++ akkaHttpOverrides ++ playOverrides
  )
  .dependsOn(`hello-proxy-api`, `hello-api`)

// Documentation for this project:
//    sbt "project docs" "~ paradox"
//    open docs/target/paradox/site/main/index.html
lazy val docs = (project in file("docs")).enablePlugins(ParadoxPlugin)

lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false


// This adds an entry on the LagomDevMode Service Registry. With this information on
// the Service Registry a client using Service Discovery to Lookup("helloworld.GreeterService")
// will get "http://localhost:11000" and then be able to send a request.
lagomUnmanagedServices in ThisBuild := Map("helloworld.GreeterService" -> s"http://127.0.0.1:${`hello-impl-HTTP-port`}")


def common = Seq(
  // We don't care about doc artifacts here.
  sources in (Compile, doc) := Seq.empty,
  publishArtifact in (Compile, packageDoc) := false,
  javacOptions in Compile := Seq("-g", "-encoding", "UTF-8", "-parameters", "-Xlint:unchecked", "-Xlint:deprecation", "-parameters")
)
