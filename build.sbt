name := "sbt-play-artifact"

organization := "com.gu"

scalaVersion := "2.10.0"

releaseSettings

publishTo := Some(Resolver.url("scala-sbt-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns))

publishMavenStyle := false

