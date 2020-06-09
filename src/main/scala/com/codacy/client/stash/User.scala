package com.codacy.client.stash

import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * @param username This field is actually the slug on bitbucket server side which is the one that should be used for doing requests
  */
final case class User(
    username: String,
    email: Option[String],
    displayName: String,
    authorId: Long,
    avatarUrl: Option[String],
    name: String
)

object User {
  implicit val reader: Reads[User] = (
    (__ \ "slug").read[String] and
      (__ \ "emailAddress").readNullable[String] and
      (__ \ "displayName").read[String] and
      (__ \ "id").read[Long] and
      (__ \ "avatarUrl").readNullable[String] and
      (__ \ "name").read[String]
  )(User.apply _)
}
