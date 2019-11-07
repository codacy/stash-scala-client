import sbt._

object Dependencies {

  def playJson(scalaVersion: String) = {
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 11)) => "com.typesafe.play" %% "play-json" % "2.5.19"
      case _ => "com.typesafe.play" %% "play-json" % "2.7.4"
    }
  }

}
