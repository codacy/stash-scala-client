package com.codacy.client.stash.service

import com.codacy.client.stash.client.{Request, RequestResponse, StashClient}
import com.codacy.client.stash.{Commit, PullRequest}
import play.api.libs.json.{JsNull, JsObject, Json}

class PullRequestServices(client: StashClient) {

  /*
   * Gets the list of a repository pull requests
   *
   * Direction: INCOMING | OUTGOING
   * States: ALL | OPEN | MERGED | DECLINED
   * Order: NEWEST | OLDEST
   *
   */
  def getPullRequests(projectKey: String, repository: String, direction: String = "INCOMING",
                      state: String = "OPEN", order: String = "NEWEST"): RequestResponse[Seq[PullRequest]] = {
    val url = s"/rest/api/1.0/projects/$projectKey/repos/$repository/pull-requests?direction=$direction&state=$state&order=$order"

    client.executePaginated(Request(url, classOf[Seq[PullRequest]]))
  }

  /*
   * Gets the list of commits of a pull request
   *
   */
  def getPullRequestCommits(projectKey: String, repository: String, prId: Long): RequestResponse[Seq[Commit]] = {
    val url = s"/rest/api/1.0/projects/$projectKey/repos/$repository/pull-requests/$prId/commits"

    client.executePaginated(Request(url, classOf[Seq[Commit]]))
  }

}
