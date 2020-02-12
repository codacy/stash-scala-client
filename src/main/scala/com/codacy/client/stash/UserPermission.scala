package com.codacy.client.stash

import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class UserPermission(user: User, permission: String)

object UserPermission {
  implicit val reader: Reads[UserPermission] = {
    ((__ \ "user").read[User] and
      (__ \ "permission").read[String])(UserPermission.apply _)
  }
}
