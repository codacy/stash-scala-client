package com.codacy.client.stash

import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class Group(name: String, permission: String)

object Group {
  implicit val reader: Reads[Group] = {
    ((__ \ "group" \ "name").read[String] and
      (__ \ "permission").read[String])(Group.apply _)
  }
}
