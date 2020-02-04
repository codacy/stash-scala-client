package com.codacy.client.stash

import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class Permission(user: User, permission: String)

object Permission {
  implicit val reader: Reads[Permission] = {
    ((__ \ "user").read[User] and
      (__ \ "permission").read[String])(Permission.apply _)
  }
}
