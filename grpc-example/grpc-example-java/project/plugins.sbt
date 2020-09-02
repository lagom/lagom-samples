enablePlugins(BuildInfoPlugin)
val playGrpcV = "0.8.2"
buildInfoKeys := Seq[BuildInfoKey]("playGrpcVersion" -> playGrpcV)
buildInfoPackage := "lagom.java.grpc.sample"

// The Lagom plugin
addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.6.4")

// Akka GRPC
addSbtPlugin("com.lightbend.akka.grpc" %% "sbt-akka-grpc" % "0.8.4")
libraryDependencies += "com.lightbend.play" %% "play-grpc-generators" % playGrpcV
