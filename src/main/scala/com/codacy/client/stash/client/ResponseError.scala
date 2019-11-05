package com.codacy.client.stash.client

import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class ResponseError(context: Option[String], message: String, exceptionName: Option[String])

object ResponseError {
  implicit val reader: Reads[ResponseError] = (
    (__ \ "context").readNullable[String] and
      (__ \ "message").read[String] and
      (__ \ "exceptionName").readNullable[String]
  )(ResponseError.apply _)
}
