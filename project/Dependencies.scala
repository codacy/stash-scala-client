import sbt._

object Dependencies {

  def playJson(scalaVersion: String): Seq[ModuleID] = {
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 11)) => Seq("com.typesafe.play" %% "play-json" % sys.props.getOrElse("playVersion", "2.5.19"))
      case _ =>
        val playVersion = sys.props.getOrElse("playVersion", "2.7.4")
        Seq("com.typesafe.play" %% "play-json" % playVersion, "com.typesafe.play" %% "play-json-joda" % playVersion)
    }
  }

}
