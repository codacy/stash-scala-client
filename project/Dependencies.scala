import sbt._

object Dependencies {

  val playJson24: String = "2.4.3"
  val playJson25: String = "2.5.19"
  val playJson27: String = "2.7.4"

  def playJson(scalaVersion: String): Seq[ModuleID] = {
    val playJsonVersion = CrossVersion.partialVersion(scalaVersion) match {
      case _ if sys.props.contains("playVersion") => sys.props.get("playVersion").get
      case Some((2, 11)) => playJson25
      case Some((2, 12)) => playJson27
    }

    playJsonVersion match {
      case `playJson24` | `playJson25` => Seq("com.typesafe.play" %% "play-json" % playJsonVersion)
      case jsonVersion =>
        Seq("com.typesafe.play" %% "play-json" % jsonVersion, "com.typesafe.play" %% "play-json-joda" % jsonVersion)
    }
  }

}
