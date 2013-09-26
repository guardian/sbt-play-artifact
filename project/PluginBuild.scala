import sbt._
import Keys._

object PluginBuild extends Build {
  
  // Plugins included here are exported to projects that use this plugin because
  // they are dependencies of the plugin itself and not associated with the 
  // build definition as plugins usually are.

  lazy val main = Project("sbt-play-artifact", file("."))
    .settings(com.typesafe.sbt.SbtScalariform.scalariformSettings: _*)
    .settings(
      resolvers ++= Seq(
        "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
      ),

      addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.0"),
      addSbtPlugin("com.gu" % "sbt-version-info-plugin" % "2.8"),
      addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.9.2")
    )

}