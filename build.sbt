name := "crawler"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies += "org.jsoup" % "jsoup" % "1.7.2"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.16",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.16" % Test
)

mainClass in assembly := Some("Main")
