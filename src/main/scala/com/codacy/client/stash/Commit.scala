package com.codacy.client.stash

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class Commit(
    hash: String,
    authorName: String,
    parents: Option[List[Parent]],
    authorTimestamp: DateTime,
    message: String
)

object Commit {
  implicit val reader: Reads[Commit] = (
    (__ \ "id").read[String] and
      (__ \ "author" \ "name").read[String] and
      (__ \ "parents").readNullable[List[Parent]] and
      (__ \ "authorTimestamp").read[Long].map(new DateTime(_)) and
      (__ \ "message").read[String]
  )(Commit.apply _)
}

final case class Parent(id: String, displayId: String)

object Parent {
  implicit val fmt: Format[Parent] = Json.format[Parent]
}
