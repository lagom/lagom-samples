// The Lagom plugin
addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.5.0-RC2")
// Needed for importing the project into Eclipse
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4")
// Set the version dynamically to the git hash
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "3.3.0")
// Not needed once upgraded to Play 2.7.1
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.19")

