val stashScalaClient = project
  .in(file("."))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(
    name := "stash-scala-client",
    scalaVersion := crossScalaVersions.value.head,
    crossScalaVersions := Seq("2.12.10"),
    scalacOptions := Seq("-deprecation", "-feature", "-unchecked", "-Ywarn-adapted-args", "-Xlint"),
    libraryDependencies ++= Seq(Dependencies.jodaTime, Dependencies.scalajHttp) ++ Dependencies.playJson,
    libraryDependencies ++= Seq(Dependencies.scalatest, Dependencies.mockitoScalaScalatest, Dependencies.jsch)
      .map(_ % "test,it"),
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
  .settings(name := (Dependencies.playJson.head.revision.split('.').take(2) match {
    case Array("2", "7") => s"${name.value}_playjson27"
    case _ => throw new Exception("Unsupported Play JSON version")
  }))
