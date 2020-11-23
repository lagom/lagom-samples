enablePlugins(BuildInfoPlugin)
val akkaGrpcVersion = "1.0.2"
val playGrpcVersion = "0.9.1"
val lagomVersion = "1.6.4"

buildInfoKeys := Seq[BuildInfoKey]("playGrpcVersion" -> playGrpcVersion)
buildInfoPackage := "lagom.java.grpc.sample"

// The Lagom plugin
addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % lagomVersion)

// Akka GRPC
addSbtPlugin("com.lightbend.akka.grpc" %% "sbt-akka-grpc" % akkaGrpcVersion)
libraryDependencies += "com.lightbend.play" %% "play-grpc-generators" % playGrpcVersion
