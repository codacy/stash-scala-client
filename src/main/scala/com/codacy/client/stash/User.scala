package com.codacy.client.stash

import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class User(username: String, email: Option[String], displayName: String, authorId: Long, avatarUrl: String)

object User {
  implicit val reader: Reads[User] = (
    (__ \ "slug").read[String] and
      (__ \ "emailAddress").readNullable[String] and
      (__ \ "displayName").read[String] and
      (__ \ "id").read[Long] and
      (__ \ "avatarUrl").read[String]
  )(User.apply _)
}
