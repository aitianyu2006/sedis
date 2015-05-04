organization := "io.github.junheng"

name := "sedis"

version := "0.1.1-SNAPSHOT"

scalaVersion := "2.11.4"

libraryDependencies ++= {
  Seq(
    "org.json4s"    %% "json4s-jackson" % "3.2.11" withSources(),
    "org.json4s"    %% "json4s-ext"     % "3.2.11" withSources(),
    "redis.clients"  % "jedis"          % "2.6.2"  withSources() withJavadoc(),
    "org.specs2"    %% "specs2"         % "2.3.13",
    "org.scalatest" %% "scalatest"      % "2.2.1" % "test",
    "junit"          % "junit"          % "4.11"  % "test"
  )
}