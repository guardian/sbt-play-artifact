package com.gu

import com.typesafe.sbtscalariform.ScalariformPlugin
import java.io.{File}
import sbt._
import sbtassembly.Plugin._
import AssemblyKeys._
import PlayProject._
import Keys._

object PlayArtifact extends Plugin {

  val playArtifact = TaskKey[File]("play-artifact", "Builds a deployable zip file for magenta")
  val playArtifactResources = TaskKey[Seq[(File, String)]]("play-artifact-resources", "Files that will be collected by the deployment-artifact task")
  val playArtifactFile = SettingKey[String]("play-artifact-file", "Filename of the artifact built by deployment-artifact")

  lazy val playArtifactCompileSettings: Seq[Setting[_]] = ScalariformPlugin.scalariformSettings ++ Seq(
    organization := "com.gu",
    scalaVersion := "2.9.1",

    maxErrors := 20,
    javacOptions := Seq("-g", "-source", "1.6", "-target", "1.6", "-encoding", "utf8"),
    scalacOptions := Seq("-unchecked", "-optimise", "-deprecation", "-Xcheckinit", "-encoding", "utf8"),

    externalResolvers <<= resolvers map {
      rs =>
        Resolver.withDefaultResolvers(rs, scalaTools = false)
    },

    ivyXML :=
      <dependencies>
          <exclude org="commons-logging"/>
        // conflicts with jcl-over-slf4j
          <exclude org="org.springframework"/>
        // because I don't like it
      </dependencies>
  )

  lazy val playArtifactDistSettings: Seq[Setting[_]] = playArtifactCompileSettings ++ assemblySettings ++ Seq(
    test in assembly := {},

    mainClass in assembly := Some("play.core.server.NettyServer"),

    playArtifactResources <<= (assembly, name, baseDirectory) map {
      (assembly, name, baseDirectory) =>
        Seq(
          (assembly, "packages/%s/%s".format(name, assembly.getName)),
          (baseDirectory / "conf" / "deploy.json", "deploy.json")
        )
    },

    playArtifactFile := "artifacts.zip",

    playArtifact <<= buildDeployArtifact,
    dist <<= buildDeployArtifact,

    mergeStrategy in assembly <<= (mergeStrategy in assembly) {
      current => {
        // Previous default MergeStrategy was first

        // Take ours, i.e. MergeStrategy.last...
        case "logger.xml" => MergeStrategy.last
        case "version.txt" => MergeStrategy.last

        case "overview.html" => MergeStrategy.first
        case "NOTICE" => MergeStrategy.first
        case "LICENSE" => MergeStrategy.first
        case meta if meta.startsWith("META-INF/") => MergeStrategy.first

        case other => current(other)
      }
    },

    excludedFiles in assembly := {
      (bases: Seq[File]) =>
        bases flatMap {
          base =>
            (base / "META-INF" * "*").get collect {
              case f if f.getName.toLowerCase == "license" => f
              case f if f.getName.toLowerCase == "manifest.mf" => f
              case f if f.getName.endsWith(".SF") => f
              case f if f.getName.endsWith(".DSA") => f
              case f if f.getName.endsWith(".RSA") => f
            }
        }
    }
  )

  private def buildDeployArtifact =
    (streams, assembly, target, playArtifactResources, playArtifactFile) map {
      (s, assembly, target, resources, artifactFileName) => {
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
}