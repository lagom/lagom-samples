// The Lagom plugin
addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.5.0-RC2")

// Akka GRPC
addSbtPlugin("com.lightbend.akka.grpc" %% "sbt-akka-grpc" % "0.5.0+14-c97a24a0")
resolvers ++= Seq(          // for the snapshot ^
  Resolver.bintrayIvyRepo("akka", "sbt-plugin-releases"),
  Resolver.bintrayRepo("akka", "maven"),
)

// Needed for importing the project into Eclipse
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4")
