name := """tsa-gbif-tool"""
organization := "animalsoundarchive.org"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.10"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "animalsoundarchive.org.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "animalsoundarchive.org.binders._"

// DB connectivity
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "mysql" % "mysql-connector-java" % "8.0.15"
)

// HttpClient
//libraryDependencies += ws
libraryDependencies += "com.lihaoyi" %% "requests" % "0.8.0"