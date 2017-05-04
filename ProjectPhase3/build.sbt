name := "ProjectPhase3"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq (
  "com.typesafe.akka" %% "akka-actor" % "2.5.0",
  "com.typesafe.akka" %% "akka-stream" % "2.5.0",
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.13",
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.0",
  "com.typesafe.akka" %% "akka-http" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-jackson" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-core" % "10.0.5",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.12",
  "org.scalatest"     %% "scalatest" % "3.0.1" % "test",
  "org.elasticsearch" % "elasticsearch" % "2.3.5"
    exclude("log4j", "log4j")
)
        