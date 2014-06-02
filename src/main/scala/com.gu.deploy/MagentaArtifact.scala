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

        case meta@PathList("META-INF", xs @ _*) =>
          (xs map {_.toLowerCase}) match {
            case ("manifest.mf" :: Nil) | ("license" :: Nil) =>
              MergeStrategy.discard
            case ps @ (x :: xs) if ps.last.endsWith(".rsa") =>
              MergeStrategy.discard
            case _ => current(meta)
          }

        case other => current(other)
      }
    },

    excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
      cp filter {jar => "commons-logging-1.1.1.jar" == jar.data.getName}
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
