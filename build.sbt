import codacy.libs._

val stashScalaClient = project
  .in(file("."))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(
    organization := "com.codacy",
    name := "stash-scala-client",
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.11.6",
    crossScalaVersions := Seq("2.11.12", "2.12.10"),
    scalacOptions := Seq("-deprecation", "-feature", "-unchecked", "-Ywarn-adapted-args", "-Xlint"),
    libraryDependencies ++= Seq(jodaTime, scalajHttp, "com.typesafe.play" %% "play-json" % "2.5.19"),
    libraryDependencies ++= Seq(scalatest, mockitoScalaScalatest, jsch).map(_ % "test,it"),
    organizationName := "Codacy",
    organizationHomepage := Some(new URL("https://www.codacy.com")),
    startYear := Some(2015),
    description := "Stash Scala Client",
    licenses := Seq(
      "The Apache Software License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")
    ),
    homepage := Some(url("https://github.com/codacy/stash-scala-client.git")),
    pomExtra :=
      <scm>
    <url>https://github.com/codacy/stash-scala-client.git</url>
    <connection>scm:git:git@github.com:codacy/stash-scala-client.git</connection>
    <developerConnection>scm:git:https://github.com/codacy/stash-scala-client.git</developerConnection>
  </scm>
    <developers>
      <developer>
        <id>rtfpessoa</id>
        <name>Rodrigo</name>
        <email>rodrigo [at] codacy.com</email>
        <url>https://github.com/rtfpessoa</url>
      </developer>
    </developers>
  )
