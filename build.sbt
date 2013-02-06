import sbtrelease._
import ReleaseKeys._
import ReleaseStateTransformations._

name := "sbt-play-artifact"

organization := "com.gu"

scalaVersion := "2.10.0"

releaseSettings

releaseProcess ~= { releaseSteps => releaseSteps filterNot (_ == publishArtifacts) }
