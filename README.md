sbt-play-artifact
=================

SBT plugin to build a [magenta](https://github.com/guardian/deploy) deployable artifact from Play 2.1. Note
that Play 2.0 has been dropped as of version 2.5 of this plugin.

You might also be interested in the related [sbt-play-assethash](https://github.com/guardian/sbt-play-assethash)

Why use this?
-------------

If you are writing a new Play 2 application and want to use magenta to deploy it then you will find that
this plugin takes care of some basic tasks to create a suitable artifacts.zip file, this includes:
* Creating one super deluxe executable JAR file using [sbt-assembly](https://github.com/sbt/sbt-assembly)
* Bundling the JAR file and your deploy.json into an artifacts.zip
* Ensuring that common settings are provided and common conflicts are resolved
* Printing the magical TeamCity line to inform the CI server where the artifact is


How to use
----------

The most convenient way of using this plugin is to add a source dependency in a scala file under project/project:

```scala
val playArtifactPluginVersion = "" // tag id you want to use
lazy val plugins = Project("plugins", file("."))
    .dependsOn(uri("git://github.com/guardian/sbt-play-artifact.git#" + playArtifactPluginVersion))
```

Then add artifact.Artifact.distSettings or artifact.Artifact.compileSettings in your project.  distSettings includes
compileSettings - the only time you might want to use only the latter is when making a common library (such as
[frontend-common](https://github.com/guardian/frontend-common).

```scala
  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA)
    .settings(compileSettings: _*)
```


Release
-------
To release a new version, you can use the sbt-release command, which will test, tag, and update version numbers.
It could publish artifacts too, once we're set up with the TypeSafe SBT Community Repository.

```
sbt release
```
