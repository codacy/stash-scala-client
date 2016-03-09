package com.codacy.client.stash

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class CommitComment(id: Long, author: User, content: String, createdDate: DateTime)

object CommitComment {
  implicit val reader: Reads[CommitComment] = (
    (__ \ "id").read[Long] and
      (__ \ "author").read[User] and
      (__ \ "text").read[String] and
      (__ \ "createdDate").read[Long].map(millis => new DateTime(millis))
    ) (CommitComment.apply _)
}
