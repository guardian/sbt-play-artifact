import sbt._
import Keys._

object PluginBuild extends Build {
  
  // Plugins included here are exported to projects that use this plugin because
  // they are dependencies of the plugin itself and not associated with the 
  // build definition as plugins usually are.

  lazy val main = Project("sbt-play-artifact", file("."))
    .settings(com.typesafe.sbtscalariform.ScalariformPlugin.scalariformSettings: _*)
    .settings(
      resolvers ++= Seq(
        "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
      ),

      addSbtPlugin("play" % "sbt-plugin" % "2.1.1"),
      addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.7")
    ).dependsOn(uri("git://github.com/guardian/sbt-version-info-plugin.git#sh-json-version-info"))

}