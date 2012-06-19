import sbt._
import Keys._
import com.typesafe.sbtscalariform.ScalariformPlugin

object PluginBuild extends Build {
  
  // Plugins included here are exported to projects that use this plugin because
  // they are dependencies of the plugin itself and not associated with the 
  // build definition as plugins usually are.

  lazy val artifactory = Resolver.url("sbt-plugin-releases",
    url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

  lazy val main = Project("sbt-play-artifact", file("."))
    .settings(ScalariformPlugin.scalariformSettings: _*)
    .settings(
      name := "sbt-play-artifact",
      organization := "com.gu",
      sbtPlugin := true,

      resolvers ++= Seq(
        artifactory,
        "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
      ),

      addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.1"),
      addSbtPlugin("play" % "sbt-plugin" % "2.1-06142012")
    )
    .dependsOn(uri("git://github.com/guardian/sbt-version-info-plugin.git#2.1"))
}