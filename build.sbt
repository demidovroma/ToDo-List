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
  "io.sentry" % "sentry-logback" % "6.20.0",
  "io.sentry" % "sentry" % "6.20.0"
)