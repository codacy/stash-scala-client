import codacy.libs._

val stashScalaClient = project
  .in(file("."))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(
    name := "stash-scala-client",
    scalaVersion := crossScalaVersions.value(1),
    crossScalaVersions := Seq("2.11.12", "2.12.10"),
    scalacOptions := Seq("-deprecation", "-feature", "-unchecked", "-Ywarn-adapted-args", "-Xlint"),
    libraryDependencies ++= Seq(jodaTime, scalajHttp) ++ Dependencies.playJson(scalaVersion.value),
    libraryDependencies ++= Seq(scalatest, mockitoScalaScalatest, jsch).map(_ % "test,it"),
    libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    organizationName := "Codacy",
    organizationHomepage := Some(new URL("https://www.codacy.com")),
    startYear := Some(2015),
    description := "Stash Scala Client",
    licenses := Seq(
      "The Apache Software License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")
    ),
    homepage := Some(url("https://github.com/codacy/stash-scala-client.git")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/codacy/stash-scala-client"),
        "scm:git:git@github.com:codacy/stash-scala-client.git"
      )
    ),
    pgpPassphrase := Option(System.getenv("SONATYPE_GPG_PASSPHRASE")).map(_.toCharArray),
    publicMvnPublish
  )
  .settings(Compile / unmanagedSourceDirectories += {
    Dependencies.playJson(scalaVersion.value).head.revision match {
      case (Dependencies.playJson24 | Dependencies.playJson25) =>
        (Compile / baseDirectory).value / "src" / "main" / "play_json_2.5-"
      case Dependencies.playJson27 => (Compile / baseDirectory).value / "src" / "main" / "play_json_2.7"
      case _ => throw new Exception("Unsupported Play JSON version")
    }
  })
  .settings(name := (Dependencies.playJson(scalaVersion.value).head.revision match {
    case Dependencies.playJson24 => s"${name.value}_playjson24"
    case Dependencies.playJson25 => s"${name.value}_playjson25"
    case Dependencies.playJson27 => s"${name.value}_playjson27"
    case _ => throw new Exception("Unsupported Play JSON version")
  }))
