package com.codacy.client.stash

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Repository(slug: String, name: String, scm: String, public: Boolean, cloneUrls: Seq[RepositoryUrl])

object Repository {
  implicit val reader: Reads[Repository] = {
    ((__ \ "slug").read[String] and
      (__ \ "name").read[String] and
      (__ \ "scmId").read[String] and
      (__ \ "public").read[Boolean] and
      (__ \ "links" \ "clone").read[Seq[JsValue]].map(parseLinks))(Repository.apply _)
  }

  private def parseLinks(links: Seq[JsValue]): Seq[RepositoryUrl] = {
    for {
      link <- links
      url <- (link \ "href").asOpt[String]
      name <- (link \ "name").asOpt[String]
      urlType <- RepositoryUrlType.find(name)
    } yield RepositoryUrl(urlType, url)
  }
}

object RepositoryUrlType extends Enumeration {
  val Https = Value("http")
  val Ssh = Value("ssh")

  def find(urlType: String): Option[Value] = {
    values.find(_.toString == urlType)
  }
}

case class RepositoryUrl(urlType: RepositoryUrlType.Value, link: String)
