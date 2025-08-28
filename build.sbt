name := """ToDo-List-Scala"""
organization := "ToDo-List-Scala"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.16"

libraryDependencies ++= Seq(
  guice,
  jdbc,
  "org.firebirdsql.jdbc" % "jaybird" % "6.0.3",
  "org.playframework.anorm" %% "anorm" % "2.8.0",
  "com.typesafe.play" %% "play-json" % "2.10.3",
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test,
  "org.scalactic" %% "scalactic" % "3.2.19",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "com.typesafe.akka" %% "akka-stream" % "2.6.20",
  "io.sentry" % "sentry-logback" % "7.17.0"
)

javaOptions += "-Dlogback.configurationFile=./conf/logback.xml"