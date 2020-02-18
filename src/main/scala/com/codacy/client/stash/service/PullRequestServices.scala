package com.codacy.client.stash.service

import com.codacy.client.stash.client.{Request, RequestResponse, StashClient}
import com.codacy.client.stash.{Commit, PullRequest, PullRequestComment, PullRequestReviewers}
import play.api.libs.json._

class PullRequestServices(client: StashClient) {

  /*
   * Gets the list of a repository pull requests
   *
   * Direction: INCOMING | OUTGOING
   * States: ALL | OPEN | MERGED | DECLINED
   * Order: NEWEST | OLDEST
   *
   */
  def getPullRequests(
      projectKey: String,
      repository: String,
      direction: String = "INCOMING",
      state: String = "OPEN",
      order: String = "NEWEST"
  ): RequestResponse[Seq[PullRequest]] = {
    val directionParam = "direction"
    val stateParam = "state"
    val orderParam = "order"
    val url =
      s"/rest/api/1.0/projects/$projectKey/repos/$repository/pull-requests?$directionParam=$direction&$stateParam=$state&$orderParam=$order"

    client.executePaginated(Request(url, classOf[Seq[PullRequest]]))(
      Map(directionParam -> direction, stateParam -> state, orderParam -> order)
    )
  }

  /*
   * Gets the list of commits of a pull request
   *
   */
  def getPullRequestCommits(projectKey: String, repository: String, prId: Long): RequestResponse[Seq[Commit]] = {
    val url = s"/rest/api/1.0/projects/$projectKey/repos/$repository/pull-requests/$prId/commits"

    client.executePaginated(Request(url, classOf[Seq[Commit]]))()
  }

  /*
   * Gets the list of comments of a pull request
   *
   */
  def getPullRequestComments(
      projectKey: String,
      repository: String,
      prId: Long
  ): RequestResponse[Seq[PullRequestComment]] = {
    val url = s"/rest/api/1.0/projects/$projectKey/repos/$repository/pull-requests/$prId/comments"

    client.executePaginated(Request(url, classOf[Seq[PullRequestComment]]))()
  }

  /*
   * Comment a file in a pull request
   */
  def createComment(
      projectKey: String,
      repo: String,
      prId: Long,
      body: String,
      file: Option[String],
      line: Option[Int],
      lineType: String = "ADDED"
  ): RequestResponse[PullRequestComment] = {
    val url = s"/rest/api/1.0/projects/$projectKey/repos/$repo/pull-requests/$prId/comments"

    val params = JsObject(
      file.map(filename => "path" -> JsString(filename)).toSeq ++
        line.map(lineTo => "line" -> JsNumber(lineTo)) ++
        Some("lineType" -> JsString(lineType))
    )

    val values = Json.obj("text" -> body, "anchor" -> params)

    client.postJson(Request(url, classOf[PullRequestComment]), values)
  }

  /*
   * Delete a comment in a pull request
   */
  def deleteComment(projectKey: String, repo: String, prId: Long, commentId: Long): RequestResponse[Boolean] = {
    val url = s"/rest/api/1.0/projects/$projectKey/repos/$repo/pull-requests/$prId/comments/$commentId?version=0"

    client.delete(url)
  }

  def getPullRequestsReviewers(
      projectKey: String,
      repository: String,
      prId: Long
  ): RequestResponse[PullRequestReviewers] = {
    val url = s"/rest/api/1.0/projects/$projectKey/repos/$repository/pull-requests/$prId"

    client.execute(Request(url, classOf[PullRequestReviewers]))
  }
}
