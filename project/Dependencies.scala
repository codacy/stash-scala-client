import sbt._

object Dependencies {

  val playJson =
    Seq("play-json", "play-json-joda").map("com.typesafe.play" %% _ % "2.7.4")
  
  val scalatest = "org.scalatest" %% "scalatest" % "3.0.8"
  val jodaTime = "joda-time" % "joda-time" % "2.9.9"
  val scalajHttp = "org.scalaj" %% "scalaj-http" % "2.4.2"
  val jsch = "com.jcraft" % "jsch" % "0.1.55"
  val mockitoScalaScalatest = "org.mockito" %% "mockito-scala-scalatest" % "1.4.6"
}
