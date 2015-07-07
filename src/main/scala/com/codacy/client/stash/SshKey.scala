package com.codacy.client.stash

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class SshKey(id: Long, key: String, label: String)

object SshKey {
  implicit val reader: Reads[SshKey] = (
    (__ \ "key" \ "id").read[Long] and
      (__ \ "key" \ "text").read[String] and
      (__ \ "key" \ "label").read[String]
    )(SshKey.apply _)
}

case class SshKeySimple(key: String)

object SshKeySimple {
  implicit val reader: Reads[SshKeySimple] = Reads { jsValue =>
    (jsValue \ "key" \ "text").asOpt[String]
      .map(t => JsSuccess(SshKeySimple(t)))
      .getOrElse(JsError("could not retrive ssh key"))
  }
}
