sbt-play-artifact
=================

SBT plugin to build a [magenta / Riff-Raff](https://github.com/guardian/deploy) deployable artifact from Play 2.2. Note
that after v2.10 there are `play` tags which correspond to an upstream version of play. i.e. For Play 2.2.0 - use
tag play2.2.0_1. This plugin pulls in the version of Play.

Note also that many of the names have changed in the move to Play 2.2.

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

The most convenient way of using this plugin is to add a source dependency in a scala file under `project/project`:

```scala
val playArtifactPluginVersion = "" // tag id you want to use - e.g. play2.2.0_1
lazy val plugins = Project("plugins", file("."))
    .dependsOn(uri("git://github.com/guardian/sbt-play-artifact.git#" + playArtifactPluginVersion))
```

Then add `com.gu.deploy.MagentaArtifact.magentaArtifactSettings` in your project.

```scala
  import com.gu.deploy.MagentaArtifact._

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA)
    .settings(magentaArtifactSettings: _*)
```

If the `deploy.json` file exists in the `conf/` folder in your play project, then it will be automatically included in
the artifact.zip.

Once this has been done you can use the sbt command `magenta` to spit out an `artifact.zip` file under `target/`.

Release
-------
*Note:* This is currently not being released.

To release a new version, you can use the sbt-release command, which will test, tag, and update version numbers.
It could publish artifacts too, once we're set up with the TypeSafe SBT Community Repository.

```
sbt release
```
