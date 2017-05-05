name := "ProjectPhase4"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  // Change this to another test framework if you prefer
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.13",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.12",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.1",
  "com.typesafe.akka" %% "akka-stream" % "2.5.1",
  "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.23",
  "com.typesafe.akka" %% "akka-persistence" % "2.5.1",
  "com.typesafe.akka" %% "akka-http-core" % "10.0.5",
  "com.typesafe.akka" %% "akka-http" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-jackson" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-xml" % "10.0.5",
  "com.typesafe.akka" %% "akka-cluster" % "2.5.1",
  "com.typesafe.akka" %% "akka-cluster-sharding" % "2.5.1",
  "io.spray" %% "spray-json" % "1.3.1",
  "org.json4s" %% "json4s-ext" % "3.5.0",
  "org.json4s" %% "json4s-native" % "3.5.0",
  "com.google.protobuf" % "protobuf-java"  % "2.5.0"
)
cancelable in Global := true