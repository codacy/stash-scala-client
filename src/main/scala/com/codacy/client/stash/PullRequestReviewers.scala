package com.codacy.client.stash

import play.api.libs.json._

case class PullRequestReviewers(reviewers: Seq[String])

object PullRequestReviewers {

  implicit val reader: Reads[PullRequestReviewers] =
    (__ \ "reviewers").read(Reads.list((__ \\ "id").read[Long])).map(ids => PullRequestReviewers(ids.map(_.toString)))

}
