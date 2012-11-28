resolvers ++= Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"
)

// The following plugins are not exported to projects that use this plugin
// because by being in project/plugins.sbt they are associated instead with the
// SBT project used to build the plugin and not with the plugin itself.

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.0.0")

addSbtPlugin("com.typesafe.sbtscalariform" % "sbtscalariform" % "0.4.0")
