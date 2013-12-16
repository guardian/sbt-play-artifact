package com.gu.deploy

import sbt._
import sbt.Keys._
import sbtassembly.Plugin._
import sbtassembly.Plugin.AssemblyKeys._

object MagentaArtifact extends Plugin {

  val magenta = taskKey[File]("Builds a deployable zip file for magenta")
  val magentaResources = taskKey[Seq[(File, String)]]("Files that will be collected by the deployment-artifact task")
  val magentaFile = settingKey[String]("Filename of the artifact built by deployment-artifact")

  val executableName = settingKey[String]("Name of the executable jar file (without .jar)")

  lazy val magentaArtifactSettings = assemblySettings ++ Seq(
    mainClass in assembly := Some("play.core.server.NettyServer"),

    magentaResources <<= (assembly, executableName, baseDirectory) map {
      (assembly, name, baseDirectory) =>
        Seq(
          assembly -> "packages/%s/%s.jar".format(name, name),
          baseDirectory / "conf" / "deploy.json" -> "deploy.json"
        )
    },

    magentaFile := "artifacts.zip",
    executableName <<= name,
    magenta <<= buildDeployArtifact,

    mergeStrategy in assembly <<= (mergeStrategy in assembly) { current =>
      {
        // seems to collide between sbt and play
        case "play/core/server/ServerWithStop.class" => MergeStrategy.first

        // Take ours, i.e. MergeStrategy.last...
        case "logger.xml" => MergeStrategy.last
        case "version.txt" => MergeStrategy.last

        // Merge play.plugins because we need them all
        case "play.plugins" => MergeStrategy.filterDistinctLines

        // Try to be helpful...
        case "overview.html" => MergeStrategy.first
        case "NOTICE" => MergeStrategy.first
        case "LICENSE" => MergeStrategy.first

        case other => current(other)
      }
    },

    excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
      cp filter {jar => "commons-logging-1.1.1.jar" == jar.data.getName}
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

  private def buildDeployArtifact = (streams, assembly, target, magentaResources, magentaFile) map {
    (s, assembly, target, resources, artifactFileName) =>
      val distFile = target / artifactFileName
      s.log.info("Disting " + distFile)

      if (distFile.exists()) {
        distFile.delete()
      }
      IO.zip(resources, distFile)

      // Tells TeamCity to publish the artifact => leave this println in here
      println("##teamcity[publishArtifacts '%s => .']" format distFile)

      s.log.info("Done disting.")
      assembly
  }
}
