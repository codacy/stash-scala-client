package com.codacy.client.stash

import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class Project(
    key: String,
    id: Long,
    name: String,
    public: Boolean,
    avatarUrl: Option[String],
    links: Seq[String]
)

object Project {
  implicit val reader: Reads[Project] = {
    ((__ \ "key").read[String] and
      (__ \ "id").read[Long] and
      (__ \ "name").read[String] and
      (__ \ "public").read[Boolean] and
      (__ \ "avatarUrl").readNullable[String] and
      (__ \ "links" \ "self").read[Seq[JsValue]].map(parseLinks))(Project.apply _)
  }

  private def parseLinks(links: Seq[JsValue]): Seq[String] = {
    for {
      link <- links
      url <- (link \ "href").asOpt[String]
    } yield url
  }
}
