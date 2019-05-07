// The Lagom plugin
addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.5.1")

// Akka GRPC
addSbtPlugin("com.lightbend.akka.grpc" %% "sbt-akka-grpc" % "0.6.0")
resolvers ++= Seq(          // for the snapshot ^
  Resolver.bintrayIvyRepo("akka", "sbt-plugin-releases"),
  Resolver.bintrayRepo("akka", "maven"),
)

