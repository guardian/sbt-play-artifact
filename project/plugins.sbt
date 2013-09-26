resolvers ++= Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

// The following plugins are not exported to projects that use this plugin
// because by being in project/plugins.sbt they are associated instead with the
// SBT project used to build the plugin and not with the plugin itself.

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8")
