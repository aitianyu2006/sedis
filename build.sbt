organization := "io.github.junheng"

name := "sedis"

version := "0.1"

scalaVersion := "2.11.4"

libraryDependencies ++= {
  Seq(
    "org.json4s" % "json4s-jackson_2.11" % "3.2.11" withSources(),
    "org.json4s" % "json4s-ext_2.11" % "3.2.11" withSources(),
    "redis.clients" % "jedis" % "2.6.2" withSources() withJavadoc(),
    "org.specs2" % "specs2_2.11" % "2.3.13"
  )
}