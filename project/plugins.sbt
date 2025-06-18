addSbtPlugin("com.codacy" % "codacy-sbt-plugin" % "25.2.4")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.3.0")

// Override scala-xml version to resolve conflict
dependencyOverrides += "org.scala-lang.modules" %% "scala-xml" % "2.3.0"
