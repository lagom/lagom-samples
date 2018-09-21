// The Lagom plugin
addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.5.0-SNAPSHOT")
// Needed for importing the project into Eclipse
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4")

// Akka GRPC
addSbtPlugin("com.lightbend.akka.grpc" %% "sbt-akka-grpc" % "0.2")
