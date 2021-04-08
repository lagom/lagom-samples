enablePlugins(BuildInfoPlugin)
val akkaGrpcVersion = "1.1.1"
val playGrpcVersion = "0.9.1+23-e0dc2030+20210322-1151"
val lagomVersion = "1.6.4"

buildInfoKeys := Seq[BuildInfoKey]("playGrpcVersion" -> playGrpcVersion)
buildInfoPackage := "lagom.scala.grpc.sample"

// The Lagom plugin
addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % lagomVersion)

// Akka GRPC
addSbtPlugin("com.lightbend.akka.grpc" %% "sbt-akka-grpc" % akkaGrpcVersion)
libraryDependencies += "com.lightbend.play" %% "play-grpc-generators" % playGrpcVersion
libraryDependencies += "io.grpc" % "grpc-core" % "1.36.0"
libraryDependencies += "io.grpc" % "grpc-stub" % "1.36.0"
libraryDependencies += "io.grpc" % "grpc-netty-shaded" % "1.36.0"
