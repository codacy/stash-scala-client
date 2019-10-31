package com.codacy.client.stash

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class PullRequest(
    id: Long,
    title: String,
    description: Option[String],
    state: String,
    createdDate: DateTime,
    updatedDate: DateTime,
    sourceRepository: String,
    sourceBranch: String,
    destRepository: String,
    destBranch: String,
    author: User
) {

  def url(baseUrl: String, projectKey: String) =
    s"$baseUrl/projects/$projectKey/repos/$destRepository/pull-requests/$id"
}

object PullRequest {
  implicit val reader: Reads[PullRequest] = (
    (__ \ "id").read[Long] and
      (__ \ "title").read[String] and
      (__ \ "description").readNullable[String] and
      (__ \ "state").read[String] and
      (__ \ "createdDate").read[Long].map(new DateTime(_)) and
      (__ \ "updatedDate").read[Long].map(new DateTime(_)) and
      (__ \ "fromRef" \ "repository" \ "slug").read[String] and
      (__ \ "fromRef" \ "id").read[String].map(_.stripPrefix("refs/heads/")) and
      (__ \ "toRef" \ "repository" \ "slug").read[String] and
      (__ \ "toRef" \ "id").read[String].map(_.stripPrefix("refs/heads/")) and
      (__ \ "author" \ "user").read[User]
  )(PullRequest.apply _)
}
