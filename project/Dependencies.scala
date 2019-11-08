import sbt._

object Dependencies {

  def playJson(scalaVersion: String): Seq[ModuleID] = {
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 11)) => Seq("com.typesafe.play" %% "play-json" % "2.5.19")
      case _ => Seq("com.typesafe.play" %% "play-json" % "2.7.4", "com.typesafe.play" %% "play-json-joda" % "2.7.4")
    }
  }

}
