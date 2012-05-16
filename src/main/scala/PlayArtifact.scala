package com.gu.deploy

import com.typesafe.sbtscalariform.ScalariformPlugin
import sbt._
import sbt.Keys._
import sbt.PlayProject._
import sbtassembly.Plugin._
import sbtassembly.Plugin.AssemblyKeys._

object PlayArtifact extends Plugin {

  val playArtifact = TaskKey[File]("play-artifact", "Builds a deployable zip file for magenta")
  val playArtifactResources = TaskKey[Seq[(File, String)]]("play-artifact-resources", "Files that will be collected by the deployment-artifact task")
  val playArtifactFile = SettingKey[String]("play-artifact-file", "Filename of the artifact built by deployment-artifact")

  lazy val playArtifactCompileSettings = ScalariformPlugin.scalariformSettings ++ Seq(
    scalaVersion := "2.9.1",

    maxErrors := 20,
    javacOptions := Seq("-g", "-source", "1.6", "-target", "1.6", "-encoding", "utf8"),
    scalacOptions := Seq("-unchecked", "-optimise", "-deprecation", "-Xcheckinit", "-encoding", "utf8"),

    ivyXML :=
      <dependencies>
        <exclude org="commons-logging"><!-- Conflicts with jcl-over-slf4j in Play. --></exclude>
        <exclude org="org.springframework"><!-- Because I don't like it. --></exclude>
      </dependencies>
  )

  lazy val playArtifactDistSettings = playArtifactCompileSettings ++ assemblySettings ++ Seq(
    mainClass in assembly := Some("play.core.server.NettyServer"),

    playArtifactResources <<= (assembly, name, baseDirectory) map {
      (assembly, name, baseDirectory) =>
        Seq(
          assembly -> "packages/%s/%s".format(name, assembly.getName),
          baseDirectory / "conf" / "deploy.json" -> "deploy.json"
        )
    },

    playArtifactFile := "artifacts.zip",

    playArtifact <<= buildDeployArtifact,
    dist <<= buildDeployArtifact,

    mergeStrategy in assembly <<= (mergeStrategy in assembly) { current =>
      {
        // Previous default MergeStrategy was first

        // Take ours, i.e. MergeStrategy.last...
        case "logger.xml" => MergeStrategy.last
        case "version.txt" => MergeStrategy.last

        // Merge play.plugins because we need them all
        case "play.plugins" => MergeStrategy.filterDistinctLines

        // Try to be helpful...
        case "overview.html" => MergeStrategy.first
        case "NOTICE" => MergeStrategy.first
        case "LICENSE" => MergeStrategy.first
        case meta if meta.startsWith("META-INF/") => MergeStrategy.first

        case other => current(other)
      }
    },

    excludedFiles in assembly := { (bases: Seq[File]) =>
      bases flatMap { base => (base / "META-INF" * "*").get } collect {
        case f if f.getName.toLowerCase == "license" => f
        case f if f.getName.toLowerCase == "manifest.mf" => f
        case f if f.getName.endsWith(".SF") => f
        case f if f.getName.endsWith(".DSA") => f
        case f if f.getName.endsWith(".RSA") => f
      }
    }
  )

  private def buildDeployArtifact = (streams, assembly, target, playArtifactResources, playArtifactFile) map {
    (s, assembly, target, resources, artifactFileName) =>
      val distFile = target / artifactFileName
      s.log.info("Disting " + distFile)

      if (distFile exists) {
        distFile.delete
      }
      IO.zip(resources, distFile)

      // Tells TeamCity to publish the artifact => leave this println in here
      println("##teamcity[publishArtifacts '%s => .']" format distFile)

      s.log.info("Done disting.")
      assembly
  }
}
