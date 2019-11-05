package com.codacy.client.stash

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class PullRequestComment(id: Long, author: User, content: String, createdDate: DateTime)

object PullRequestComment {
  implicit val reader: Reads[PullRequestComment] = (
    (__ \ "id").read[Long] and
      (__ \ "author").read[User] and
      (__ \ "text").read[String] and
      (__ \ "createdDate").read[Long].map(millis => new DateTime(millis))
  )(PullRequestComment.apply _)
}
