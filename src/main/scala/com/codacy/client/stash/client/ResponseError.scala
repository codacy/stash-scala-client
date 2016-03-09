package com.codacy.client.stash.client

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class ResponseError(context: String, message: String, exceptionName: String)

object ResponseError {
  implicit val reader: Reads[ResponseError] = (
    (__ \ "context").read[String] and
      (__ \ "message").read[String] and
      (__ \ "exceptionName").read[String]
    ) (ResponseError.apply _)
}
