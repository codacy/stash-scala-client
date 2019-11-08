package com.codacy.client.stash

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class WebHook(
    id: Long,
    name: String,
    url: String,
    events: List[String],
    created: DateTime,
    updated: DateTime,
    active: Boolean
)

object WebHook {
  import DateTimeImplicits.datetimeReads
  implicit val reader: Reads[WebHook] = (
    (__ \ "id").read[Long] and
      (__ \ "name").read[String] and
      (__ \ "url").read[String] and
      (__ \ "events").read[List[String]] and
      (__ \ "createdDate").read[DateTime] and
      (__ \ "updatedDate").read[DateTime] and
      (__ \ "active").read[Boolean]
  )(WebHook.apply _)
}
