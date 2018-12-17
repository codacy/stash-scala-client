package com.codacy.client.stash

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class User(username: String, email: Option[String], displayName: String, authorId: Long)

object User {
  implicit val reader: Reads[User] = (
    (__ \ "slug").read[String] and
      (__ \ "emailAddress").readNullable[String] and
      (__ \ "displayName").read[String] and
      (__ \ "id").read[Long]
    ) (User.apply _)
}
