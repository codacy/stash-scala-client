package com.codacy.client.stash

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Commit(hash: String, authorName: String, parents: Option[Seq[String]], authorTimestamp: DateTime, message: String)

object Commit {
  implicit val reader: Reads[Commit] = (
    (__ \ "id").read[String] and
    (__ \ "author" \ "name").read[String] and
    (__ \ "parents" \\ "id").read[Seq[String]].map(Option(_).filter(_.nonEmpty)) and
    (__ \ "authorTimestamp").read[Long].map(new DateTime(_)) and
    (__ \ "message").read[String]
  )(Commit.apply _)
}
