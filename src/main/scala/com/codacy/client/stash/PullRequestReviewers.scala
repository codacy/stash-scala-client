package com.codacy.client.stash

import play.api.libs.json._

case class PullRequestReviewers(reviewers: Seq[String])

object PullRequestReviewers {

  implicit val reader: Reads[PullRequestReviewers] =
    (__ \ "reviewers" \ "user" \ "id").read[PullRequestReviewers]

}
