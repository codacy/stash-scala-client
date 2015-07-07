package com.codacy.client.stash

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class User(username: String, email: String, displayName: String)

object User {
  implicit val reader: Reads[User] = (
    (__ \ "slug").read[String] and
      (__ \ "emailAddress").read[String] and
      (__ \ "displayName").read[String]
    )(User.apply _)
}
