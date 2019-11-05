import codacy.libs._

val stashScalaClient = project
  .in(file("."))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(
    name := "stash-scala-client",
    scalaVersion := "2.11.12",
    crossScalaVersions := Seq(scalaVersion.value, "2.12.10"),
    scalacOptions := Seq("-deprecation", "-feature", "-unchecked", "-Ywarn-adapted-args", "-Xlint"),
    libraryDependencies ++= Seq(jodaTime, scalajHttp, Dependencies.playJson(scalaVersion.value)),
    libraryDependencies ++= Seq(scalatest, mockitoScalaScalatest, jsch).map(_ % "test,it"),
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
