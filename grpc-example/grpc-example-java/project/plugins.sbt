enablePlugins(BuildInfoPlugin)
val playGrpcV = "0.8.1"
buildInfoKeys := Seq[BuildInfoKey]("playGrpcVersion" -> playGrpcV)
buildInfoPackage := "lagom.java.grpc.sample"

// The Lagom plugin
addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.6.1")

// Akka GRPC
addSbtPlugin("com.lightbend.akka.grpc" %% "sbt-akka-grpc" % "0.7.3")
libraryDependencies += "com.lightbend.play" %% "play-grpc-generators" % playGrpcV
