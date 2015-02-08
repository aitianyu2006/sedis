organization := "io.github.junheng"

name := "sedis"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.4"

libraryDependencies ++= {
  Seq(
    "org.json4s" % "json4s-jackson_2.11" % "3.2.11" withSources(),
    "org.json4s" % "json4s-ext_2.11" % "3.2.11" withSources(),
    "redis.clients" % "jedis" % "2.6.2" withSources() withJavadoc(),
    "org.specs2" % "specs2_2.11" % "2.3.13"
  )
}

publishMavenStyle := true

publishTo := {
  val nexus = "http://maven.nearfor.me:8081/nexus/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "content/repositories/releases")
}

credentials += Credentials("Sonatype Nexus Repository Manager", "58.220.7.39", "deployment", "nearfor.me")