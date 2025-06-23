addSbtPlugin("com.codacy" % "codacy-sbt-plugin" % "25.2.4")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.3.0")

// Override scala-xml version to resolve conflict
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
